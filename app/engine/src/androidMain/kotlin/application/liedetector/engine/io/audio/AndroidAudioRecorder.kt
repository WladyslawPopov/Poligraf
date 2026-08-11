package application.liedetector.engine.io.audio

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

class AndroidAudioRecorder(
    private val context: Context,
    private val scope: CoroutineScope
) : AudioRecorder {

    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var audioFile: File? = null // This will always hold the "current complete" file
    private var tempPartFile: File? = null // Current recording segment
    private var timerJob: Job? = null
    private var playbackJob: Job? = null

    private val _isRecording = MutableStateFlow(false)
    override val isRecording = _isRecording.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    override val isPaused = _isPaused.asStateFlow()

    private val _durationMillis = MutableStateFlow(0L)
    override val durationMillis = _durationMillis.asStateFlow()

    private val _amplitudes = MutableStateFlow<List<Float>>(emptyList())
    override val amplitudes = _amplitudes.asStateFlow()

    private val _playbackPositionMillis = MutableStateFlow(0L)
    override val playbackPositionMillis = _playbackPositionMillis.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying = _isPlaying.asStateFlow()

    private var startTime = 0L

    private var replacePosition = 0L
    private var isReplacingMode = false

    override fun start() {
        if (_isRecording.value) return
        isReplacingMode = false
        replacePosition = 0L
        
        // If we are starting from scratch (no loaded file), reset everything.
        // Otherwise, keep audioFile to allow appending.
        if (audioFile == null) {
            _durationMillis.value = 0L
            _amplitudes.value = emptyList()
            _playbackPositionMillis.value = 0L
        }
        
        tempPartFile = null
        startRecordingSession()
    }

    private fun startRecordingSession() {
        scope.launch {
            try {
                releaseRecorder() // Fully release before starting new session
                stopPlayback()
                
                // Crucial: Small delay for hardware to breathe, especially on emulators
                delay(150.milliseconds)
                
                val file = File(context.cacheDir, "part_${UUID.randomUUID()}.m4a")
                tempPartFile = file

                val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }
                
                recorder = newRecorder.apply {
                    // VOICE_RECOGNITION is cleaner on many devices/emulators
                    setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioSamplingRate(44100)
                    setAudioEncodingBitRate(128000)
                    setOutputFile(file.absolutePath)
                    prepare()
                }

                recorder?.start()

                _isRecording.value = true
                _isPaused.value = false
                
                startTime = System.currentTimeMillis() - _durationMillis.value
                startTimer()
                Napier.d { "Recording session started: ${file.name}" }
            } catch (e: Exception) {
                Napier.e(e) { "Failed to start recording session" }
                _isRecording.value = false
                releaseRecorder()
            }
        }
    }

    override fun pause() {
        if (!_isRecording.value || _isPaused.value) return
        try {
            recorder?.pause()
            _isPaused.value = true
            timerJob?.cancel()
        } catch (e: Exception) {
            Napier.e(e) { "Failed to pause recording" }
        }
    }

    override fun resume() {
        if (!_isRecording.value || !_isPaused.value) return
        try {
            recorder?.resume()
            _isPaused.value = false
            // Reset startTime to account for the pause duration
            startTime = System.currentTimeMillis() - _durationMillis.value
            startTimer()
        } catch (e: Exception) {
            Napier.e(e) { "Failed to resume recording" }
        }
    }

    private fun releaseRecorder() {
        try {
            timerJob?.cancel()
            recorder?.apply {
                if (_isRecording.value) {
                    try { stop() } catch (_: Exception) {}
                }
                release()
            }
            recorder = null
        } catch (e: Exception) {
            Napier.e(e) { "Error releasing recorder" }
        }
    }

    override suspend fun stop(): String? {
        if (!_isRecording.value) return audioFile?.absolutePath
        return withContext(Dispatchers.IO) {
            try {
                timerJob?.cancel()
                
                // Capture last sample
                val maxAmp = try { recorder?.maxAmplitude ?: 0 } catch (_: Exception) { 0 }
                val normalized = kotlin.math.sqrt(maxAmp.toFloat() / 32767f).coerceIn(0f, 1f)
                _amplitudes.update { it + normalized }

                recorder?.stop()
                recorder?.release()
                recorder = null

                val currentPart = tempPartFile
                val baseFile = audioFile

                if (currentPart != null && currentPart.exists()) {
                    if (baseFile == null || !baseFile.exists()) {
                        audioFile = currentPart
                        Napier.d { "Recording stopped, using single part: ${currentPart.name}" }
                    } else {
                        Napier.d { "Recording stopped, merging ${baseFile.name} and ${currentPart.name}" }
                        val merged = mergeAudioFiles(baseFile, currentPart)
                        if (merged != null) {
                            // Cleanup temporary parts but DON'T delete history files if they were base
                            if (baseFile.absolutePath.contains("cache")) baseFile.delete()
                            currentPart.delete()
                            audioFile = merged
                        } else {
                            Napier.e { "Merge failed, falling back to new part only" }
                            audioFile = currentPart
                        }
                    }
                }
                tempPartFile = null

                // Refresh total duration from the final file
                audioFile?.let { file ->
                    val mp = MediaPlayer()
                    try {
                        mp.setDataSource(file.absolutePath)
                        mp.prepare()
                        _durationMillis.value = mp.duration.toLong()
                    } finally {
                        mp.release()
                    }
                }

                _isRecording.value = false
                _isPaused.value = false
                _playbackPositionMillis.value = _durationMillis.value
                
                audioFile?.absolutePath
            } catch (e: Exception) {
                Napier.e(e) { "Failed to stop recording" }
                _isRecording.value = false
                null
            }
        }
    }

    override fun cancel() {
        scope.launch {
            stop()
            // Safety: Only delete if it's a temporary segment in cache, never a loaded history file
            audioFile?.let { 
                if (it.absolutePath.contains("cache")) {
                    it.delete()
                }
            }
            audioFile = null
            tempPartFile?.delete()
            tempPartFile = null
        }
    }

    override fun play() {
        if (_isRecording.value) {
            Napier.w { "AudioRecorder: Play ignored. Currently recording" }
            return
        }
        
        val currentFile = audioFile
        if (currentFile == null || !currentFile.exists()) {
            Napier.e { "AudioRecorder: Play failed. audioFile is ${if (currentFile == null) "NULL" else "MISSING"} at ${currentFile?.absolutePath}" }
            return
        }
        
        val path = currentFile.absolutePath
        try {
            stopPlayback() 
            
            val newPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepare()
            }
            
            player = newPlayer
            val duration = newPlayer.duration.toLong()
            val startPos = _playbackPositionMillis.value

            if (startPos > 0 && startPos < duration - 200) {
                newPlayer.seekTo(startPos.toInt())
            } else if (newPlayer.currentPosition >= duration - 200) {
                newPlayer.seekTo(0)
                _playbackPositionMillis.value = 0
            }
            
            newPlayer.setOnCompletionListener {
                _isPlaying.value = false
                playbackJob?.cancel()
                _playbackPositionMillis.value = duration
                Napier.i { "AudioRecorder: Playback finished" }
            }
            
            newPlayer.start()
            _isPlaying.value = true
            startPlaybackTimer()
            Napier.i { "AudioRecorder: Playback started from ${startPos}ms" }
        } catch (e: Exception) {
            Napier.e(e) { "AudioRecorder: Playback failed for $path" }
            _isPlaying.value = false
        }
    }

    override fun pausePlayback() {
        player?.pause()
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    override fun seekTo(positionMillis: Long) {
        player?.seekTo(positionMillis.toInt())
        _playbackPositionMillis.value = positionMillis
    }

    override suspend fun trim(startMillis: Long, endMillis: Long): String? {
        val sourcePath = audioFile?.absolutePath ?: return null
        val outPath = File(context.cacheDir, "trimmed_${UUID.randomUUID()}.m4a").absolutePath
        
        Napier.d { "Trimming: $sourcePath from $startMillis to $endMillis" }
        
        return withContext(Dispatchers.IO) {
            try {
                val extractor = MediaExtractor()
                extractor.setDataSource(sourcePath)
                
                var audioTrackIndex = -1
                var format: MediaFormat? = null
                
                for (i in 0 until extractor.trackCount) {
                    val f = extractor.getTrackFormat(i)
                    if (f.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                        audioTrackIndex = i
                        format = f
                        break
                    }
                }
                
                if (audioTrackIndex == -1 || format == null) {
                    extractor.release()
                    return@withContext null
                }
                
                extractor.selectTrack(audioTrackIndex)
                
                // IMPORTANT: Use closest sync to ensure the muxer can start cleanly
                extractor.seekTo(startMillis * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                // Real start time after seek might be slightly different from startMillis
                val actualStartUs = extractor.sampleTime

                val muxer = MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                val newTrackIndex = muxer.addTrack(format)
                muxer.start()
                
                val bufferSize = 1024 * 1024
                val buffer = java.nio.ByteBuffer.allocate(bufferSize)
                val bufferInfo = MediaCodec.BufferInfo()
                
                var totalWritten = 0
                while (true) {
                    val sampleSize = extractor.readSampleData(buffer, 0)
                    if (sampleSize < 0) break
                    
                    val presentationTimeUs = extractor.sampleTime
                    if (presentationTimeUs > endMillis * 1000) break

                    bufferInfo.offset = 0
                    bufferInfo.size = sampleSize
                    // Ensure monotonic and starting from 0
                    bufferInfo.presentationTimeUs = (presentationTimeUs - actualStartUs).coerceAtLeast(0)
                    @Suppress("WrongConstant")
                    bufferInfo.flags = extractor.sampleFlags
                    
                    muxer.writeSampleData(newTrackIndex, buffer, bufferInfo)
                    extractor.advance()
                    totalWritten++
                }
                
                muxer.stop()
                muxer.release()
                extractor.release()
                
                if (totalWritten == 0) {
                    Napier.e { "Trim failed: No samples written" }
                    return@withContext null
                }

                // Sync UI amplitudes - create a fresh list to avoid subList side effects
                val startIdx = (startMillis / 33).toInt().coerceIn(0, _amplitudes.value.size)
                val endIdx = (endMillis / 33).toInt().coerceIn(startIdx, _amplitudes.value.size)
                val newAmplitudes = _amplitudes.value.subList(startIdx, endIdx).toList()
                _amplitudes.value = newAmplitudes

                // Update internal state
                audioFile = File(outPath)
                _durationMillis.value = endMillis - startMillis
                _playbackPositionMillis.value = 0
                
                stopPlayback()
                
                Napier.d { "Trim successful: $outPath, duration: ${_durationMillis.value}ms" }
                outPath
            } catch (e: Exception) {
                Napier.e(e) { "Failed to trim audio: ${e.message}" }
                null
            }
        }
    }

    override fun replace(positionMillis: Long) {
        if (_isRecording.value) return
        
        scope.launch {
            val currentDuration = _durationMillis.value
            
            if (positionMillis <= 0L) {
                Napier.d { "Replacing: Resetting and starting from scratch" }
                _durationMillis.value = 0
                _amplitudes.value = emptyList()
                audioFile = null
            } else if (positionMillis < currentDuration) {
                Napier.d { "Replacing: Truncating file to $positionMillis ms" }
                trim(0, positionMillis)
            } else {
                Napier.d { "Replacing: Appending to the end ($positionMillis >= $currentDuration)" }
                _playbackPositionMillis.value = currentDuration
            }
            
            // 2. Sync UI state amplitudes (truncate list to match position)
            val keepIdx = (positionMillis / 33).toInt().coerceIn(0, _amplitudes.value.size)
            _amplitudes.value = _amplitudes.value.take(keepIdx)
            _durationMillis.value = positionMillis.coerceAtLeast(0)
            
            startRecordingSession()
        }
    }

    override suspend fun loadFile(path: String, amplitudes: List<Float>?) {
        Napier.i { "AudioRecorder: Loading file from $path" }
        withContext(Dispatchers.Main) {
            stopPlayback()
            releaseRecorder()
        }
        
        withContext(Dispatchers.IO) {
            val file = File(path)
            if (!file.exists() || file.length() <= 0L) {
                Napier.e { "AudioRecorder: Load failed. File missing or empty. Path: $path" }
                audioFile = null
                _durationMillis.value = 0
                _amplitudes.value = emptyList()
                return@withContext
            }

            audioFile = file
            _playbackPositionMillis.value = 0
            
            try {
                val mp = MediaPlayer()
                mp.setDataSource(file.absolutePath)
                mp.prepare()
                val totalDuration = mp.duration.toLong()
                mp.release()
                
                _durationMillis.value = totalDuration

                if (amplitudes != null && amplitudes.isNotEmpty()) {
                    _amplitudes.value = amplitudes
                } else {
                    val sampleCount = (totalDuration / 33).toInt().coerceAtLeast(1)
                    _amplitudes.value = List(sampleCount) { index ->
                        val base = 0.15f
                        val vari = kotlin.math.sin(index.toDouble() / 10.0).toFloat() * 0.1f
                        (base + vari + (java.util.Random().nextFloat() * 0.05f)).coerceIn(0.05f, 0.8f)
                    }
                }
                Napier.i { "AudioRecorder: Load SUCCESS. Duration: $totalDuration ms" }
            } catch (e: Exception) {
                Napier.e(e) { "AudioRecorder: MediaPlayer failed to prepare for $path" }
                _durationMillis.value = 0
                _amplitudes.value = emptyList()
                audioFile = null
            }
        }
    }

    private fun mergeAudioFiles(file1: File, file2: File): File? {
        val outPath = File(context.cacheDir, "merged_${UUID.randomUUID()}.m4a")
        Napier.d { "Merging files: ${file1.absolutePath} and ${file2.absolutePath}" }
        
        return try {
            val extractor1 = MediaExtractor()
            extractor1.setDataSource(file1.absolutePath)
            val trackIndex1 = (0 until extractor1.trackCount).firstOrNull { 
                extractor1.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true 
            } ?: run {
                extractor1.release()
                return null
            }
            extractor1.selectTrack(trackIndex1)
            val format1 = extractor1.getTrackFormat(trackIndex1)

            val extractor2 = MediaExtractor()
            extractor2.setDataSource(file2.absolutePath)
            val trackIndex2 = (0 until extractor2.trackCount).firstOrNull { 
                extractor2.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true 
            } ?: run {
                extractor1.release()
                extractor2.release()
                return null
            }
            extractor2.selectTrack(trackIndex2)

            val muxer = MediaMuxer(outPath.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val newTrackIndex = muxer.addTrack(format1)
            muxer.start()
            
            val buffer = java.nio.ByteBuffer.allocate(1024 * 1024)
            val bufferInfo = MediaCodec.BufferInfo()
            
            var lastPresentationTimeUs = 0L
            
            // Write first file
            while (true) {
                val sampleSize = extractor1.readSampleData(buffer, 0)
                if (sampleSize < 0) break
                bufferInfo.offset = 0
                bufferInfo.size = sampleSize
                bufferInfo.presentationTimeUs = extractor1.sampleTime
                @Suppress("WrongConstant")
                bufferInfo.flags = extractor1.sampleFlags
                muxer.writeSampleData(newTrackIndex, buffer, bufferInfo)
                lastPresentationTimeUs = bufferInfo.presentationTimeUs
                extractor1.advance()
            }
            
            // Calculate real duration of file 1. 
            // We use the format duration if available, but ensure it's at least lastPresentationTimeUs + small buffer
            val formatDurationUs = if (format1.containsKey(MediaFormat.KEY_DURATION)) format1.getLong(MediaFormat.KEY_DURATION) else 0L
            val startTimeOffsetUs = maxOf(lastPresentationTimeUs + 20000L, formatDurationUs + 1000L)

            Napier.d { "File 1 written. lastSampleTime: $lastPresentationTimeUs. Using offset: $startTimeOffsetUs" }

            // Write second file
            while (true) {
                val sampleSize = extractor2.readSampleData(buffer, 0)
                if (sampleSize < 0) break
                bufferInfo.offset = 0
                bufferInfo.size = sampleSize
                bufferInfo.presentationTimeUs = extractor2.sampleTime + startTimeOffsetUs
                @Suppress("WrongConstant")
                bufferInfo.flags = extractor2.sampleFlags
                muxer.writeSampleData(newTrackIndex, buffer, bufferInfo)
                extractor2.advance()
            }
            
            extractor1.release()
            extractor2.release()
            muxer.stop()
            muxer.release()
            
            Napier.d { "Merge complete: ${outPath.absolutePath}, size: ${outPath.length()} bytes" }
            outPath
        } catch (e: Exception) {
            Napier.e(e) { "Failed to merge audio files: ${e.message}" }
            null
        }
    }

    private fun stopPlayback() {
        player?.stop()
        player?.release()
        player = null
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            var lastSampleCount = (_durationMillis.value / 33).toInt()
            while (isActive) {
                val now = System.currentTimeMillis()
                val currentDuration = now - startTime
                _durationMillis.value = currentDuration
                
                // Keep amplitudes in sync with duration (1 sample per 33ms)
                val expectedSamples = (currentDuration / 33).toInt()
                val samplesToAdd = expectedSamples - lastSampleCount
                
                if (samplesToAdd > 0) {
                    val maxAmp = try { recorder?.maxAmplitude ?: 0 } catch (_: Exception) { 0 }
                    val normalized = kotlin.math.sqrt(maxAmp.toFloat() / 32767f).coerceIn(0f, 1f)
                    
                    _amplitudes.update { current ->
                        current + List(samplesToAdd) { normalized }
                    }
                    lastSampleCount = expectedSamples
                }
                
                delay(33.milliseconds) 
            }
        }
    }

    private fun startPlaybackTimer() {
        playbackJob?.cancel()
        playbackJob = scope.launch {
            while (isActive) {
                player?.let {
                    _playbackPositionMillis.value = it.currentPosition.toLong()
                }
                delay(33.milliseconds) 
            }
        }
    }
}

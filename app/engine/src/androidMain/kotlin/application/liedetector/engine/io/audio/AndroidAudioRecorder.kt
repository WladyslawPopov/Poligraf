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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private var pausedTime = 0L

    private var replacePosition = 0L
    private var isReplacingMode = false

    override fun start() {
        if (_isRecording.value) return
        isReplacingMode = false
        replacePosition = 0L
        startRecordingSession()
    }

    private fun startRecordingSession() {
        try {
            stopPlayback()
            val file = File(context.cacheDir, "part_${UUID.randomUUID()}.m4a")
            tempPartFile = file

            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }

            _isRecording.value = true
            _isPaused.value = false
            
            // startTime should account for already recorded duration
            startTime = System.currentTimeMillis() - _durationMillis.value
            startTimer()
        } catch (e: Exception) {
            Napier.e(e) { "Failed to start recording session" }
        }
    }

    override fun pause() {
        if (!_isRecording.value || _isPaused.value) return
        try {
            recorder?.pause()
            _isPaused.value = true
            pausedTime = System.currentTimeMillis()
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
            startTime += (System.currentTimeMillis() - pausedTime)
            startTimer()
        } catch (e: Exception) {
            Napier.e(e) { "Failed to resume recording" }
        }
    }

    override fun stop(): String? {
        if (!_isRecording.value) return audioFile?.absolutePath
        return try {
            timerJob?.cancel()
            
            // Capture one last amplitude sample
            val maxAmp = try { recorder?.maxAmplitude ?: 0 } catch (_: Exception) { 0 }
            val normalized = kotlin.math.sqrt(maxAmp.toFloat() / 32767f).coerceIn(0f, 1f)
            val finalAmplitudes = _amplitudes.value.toMutableList()
            finalAmplitudes.add(normalized)
            _amplitudes.value = finalAmplitudes

            recorder?.stop()
            recorder?.release()
            recorder = null

            val currentPart = tempPartFile
            val baseFile = audioFile

            if (currentPart != null && currentPart.exists()) {
                if (baseFile == null || !baseFile.exists()) {
                    // First part
                    audioFile = currentPart
                } else {
                    // Append part to base
                    val merged = mergeAudioFiles(baseFile, currentPart)
                    if (merged != null) {
                        baseFile.delete()
                        currentPart.delete()
                        audioFile = merged
                    } else {
                        // Fallback if merge fails (keep part at least?)
                        audioFile = currentPart
                    }
                }
            }
            tempPartFile = null

            // Finalize duration
            val finalFile = audioFile
            if (finalFile != null && finalFile.exists()) {
                val mp = MediaPlayer()
                mp.setDataSource(finalFile.absolutePath)
                mp.prepare()
                val duration = mp.duration.toLong()
                _durationMillis.value = duration
                _playbackPositionMillis.value = duration 
                mp.release()
            }

            _isRecording.value = false
            _isPaused.value = false
            
            audioFile?.absolutePath
        } catch (e: Exception) {
            Napier.e(e) { "Failed to stop recording" }
            null
        }
    }

    override fun cancel() {
        stop()
        audioFile?.delete()
        audioFile = null
    }

    override fun play() {
        if (_isRecording.value) return
        val path = audioFile?.absolutePath ?: return
        
        try {
            if (player == null) {
                player = MediaPlayer().apply {
                    setDataSource(path)
                    prepare()
                    setOnCompletionListener {
                        _isPlaying.value = false
                        playbackJob?.cancel()
                        _playbackPositionMillis.value = duration.toLong()
                    }
                }
            }
            
            val currentPlayer = player ?: return
            
            // If we are at the end, seek to start before playing
            if (!currentPlayer.isPlaying && currentPlayer.currentPosition >= currentPlayer.duration - 200) {
                currentPlayer.seekTo(0)
                _playbackPositionMillis.value = 0
                Napier.d { "Auto-restarting playback from 0" }
            }
            
            currentPlayer.start()
            _isPlaying.value = true
            startPlaybackTimer()
            Napier.d { "Playback started: $path" }
        } catch (e: Exception) {
            Napier.e(e) { "Failed to play recording" }
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

    override fun trim(startMillis: Long, endMillis: Long): String? {
        val sourcePath = audioFile?.absolutePath ?: return null
        val outPath = File(context.cacheDir, "trimmed_${UUID.randomUUID()}.m4a").absolutePath
        
        return try {
            val extractor = MediaExtractor()
            extractor.setDataSource(sourcePath)
            
            val muxer = MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            
            val trackIndex = (0 until extractor.trackCount).firstOrNull { 
                extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true 
            } ?: return null
            
            extractor.selectTrack(trackIndex)
            val format = extractor.getTrackFormat(trackIndex)
            val newTrackIndex = muxer.addTrack(format)
            
            muxer.start()
            
            val bufferSize = 1024 * 1024
            val buffer = java.nio.ByteBuffer.allocate(bufferSize)
            val bufferInfo = MediaCodec.BufferInfo()
            
            extractor.seekTo(startMillis * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            
            while (true) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break
                
                val presentationTimeUs = extractor.sampleTime
                if (presentationTimeUs > endMillis * 1000) break
                
                bufferInfo.offset = 0
                bufferInfo.size = sampleSize
                bufferInfo.presentationTimeUs = presentationTimeUs - startMillis * 1000
                @Suppress("WrongConstant")
                bufferInfo.flags = extractor.sampleFlags
                
                muxer.writeSampleData(newTrackIndex, buffer, bufferInfo)
                extractor.advance()
            }
            
            muxer.stop()
            muxer.release()
            extractor.release()
            
            // Update audioFile to the trimmed version
            audioFile = File(outPath)
            _durationMillis.value = endMillis - startMillis
            
            // Reset player
            player?.release()
            player = null
            
            outPath
        } catch (e: Exception) {
            Napier.e(e) { "Failed to trim audio" }
            null
        }
    }

    override fun replace(positionMillis: Long) {
        if (_isRecording.value) return
        
        // "Recording from end always" means we don't actually replace middle parts for now
        // But we prepare for an append session. 
        // We set durationMillis to positionMillis so the timer continues correctly.
        _durationMillis.value = positionMillis
        
        startRecordingSession()
    }

    override fun loadFile(path: String) {
        audioFile = File(path)
        stopPlayback()
        if (audioFile?.exists() == true) {
            try {
                val mp = MediaPlayer()
                mp.setDataSource(path)
                mp.prepare()
                _durationMillis.value = mp.duration.toLong()
                _playbackPositionMillis.value = 0
                // For simplicity, reset amplitudes as we don't have a full waveform generator
                _amplitudes.value = List((_durationMillis.value / 33).toInt()) { 0.1f }
                mp.release()
            } catch (e: Exception) {
                Napier.e(e) { "Failed to load file for playback" }
            }
        }
    }

    private fun mergeAudioFiles(file1: File, file2: File): File? {
        val outPath = File(context.cacheDir, "merged_${UUID.randomUUID()}.m4a")
        return try {
            val muxer = MediaMuxer(outPath.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            
            val extractor1 = MediaExtractor()
            extractor1.setDataSource(file1.absolutePath)
            val trackIndex1 = (0 until extractor1.trackCount).firstOrNull { 
                extractor1.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true 
            } ?: return null
            extractor1.selectTrack(trackIndex1)
            val format1 = extractor1.getTrackFormat(trackIndex1)
            
            val newTrackIndex = muxer.addTrack(format1)
            muxer.start()
            
            val buffer = java.nio.ByteBuffer.allocate(1024 * 1024)
            val bufferInfo = MediaCodec.BufferInfo()
            
            // Write first file
            var lastPresentationTimeUs = 0L
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
            extractor1.release()

            // Write second file
            val extractor2 = MediaExtractor()
            extractor2.setDataSource(file2.absolutePath)
            val trackIndex2 = (0 until extractor2.trackCount).firstOrNull { 
                extractor2.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true 
            } ?: return null
            extractor2.selectTrack(trackIndex2)
            
            // Small gap to prevent overlapping? Or just start immediately after
            val startTimeOffsetUs = lastPresentationTimeUs + 1000L // 1ms gap

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
            extractor2.release()
            
            muxer.stop()
            muxer.release()
            outPath
        } catch (e: Exception) {
            Napier.e(e) { "Failed to merge audio files" }
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
                    
                    val currentList = _amplitudes.value.toMutableList()
                    repeat(samplesToAdd) {
                        currentList.add(normalized)
                    }
                    _amplitudes.value = currentList
                    lastSampleCount = expectedSamples
                }
                
                delay(16.milliseconds) // Higher frequency check for better sync
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
                delay(16.milliseconds) // ~60fps for smooth seek bar
            }
        }
    }
}

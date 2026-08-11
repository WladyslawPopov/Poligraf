package application.liedetector.engine.io.audio

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import io.github.aakira.napier.Napier
import java.io.File
import java.nio.ByteBuffer
import java.util.*

/**
 * Internal helper for low-level audio manipulations using MediaMuxer and MediaExtractor.
 */
internal object AndroidAudioProcessor {
    
    fun merge(file1: File, file2: File, cacheDir: File): File? {
        val outPath = File(cacheDir, "merged_${UUID.randomUUID()}.m4a")
        Napier.d { "Processor: Merging ${file1.name} and ${file2.name}" }
        
        return try {
            val extractor1 = MediaExtractor().apply { setDataSource(file1.absolutePath) }
            val trackIndex1 = (0 until extractor1.trackCount).firstOrNull { 
                extractor1.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true 
            } ?: return null
            extractor1.selectTrack(trackIndex1)
            val format1 = extractor1.getTrackFormat(trackIndex1)

            val extractor2 = MediaExtractor().apply { setDataSource(file2.absolutePath) }
            val trackIndex2 = (0 until extractor2.trackCount).firstOrNull { 
                extractor2.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true 
            } ?: return null
            extractor2.selectTrack(trackIndex2)

            val muxer = MediaMuxer(outPath.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val newTrackIndex = muxer.addTrack(format1)
            muxer.start()
            
            val buffer = ByteBuffer.allocate(1024 * 1024)
            val bufferInfo = MediaCodec.BufferInfo()
            var lastPresentationTimeUs = 0L
            
            // File 1
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
            
            val formatDurationUs = if (format1.containsKey(MediaFormat.KEY_DURATION)) format1.getLong(MediaFormat.KEY_DURATION) else 0L
            val startTimeOffsetUs = maxOf(lastPresentationTimeUs + 20000L, formatDurationUs + 1000L)

            // File 2
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
            
            outPath
        } catch (e: Exception) {
            Napier.e(e) { "Processor: Merge failed" }
            null
        }
    }

    fun trim(sourcePath: String, outPath: String, startMillis: Long, endMillis: Long): Boolean {
        return try {
            val extractor = MediaExtractor().apply { setDataSource(sourcePath) }
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
            
            if ((audioTrackIndex == -1) || (format == null)) return false
            extractor.selectTrack(audioTrackIndex)
            extractor.seekTo(startMillis * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            val actualStartUs = extractor.sampleTime

            val muxer = MediaMuxer(outPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val newTrackIndex = muxer.addTrack(format)
            muxer.start()
            
            val buffer = ByteBuffer.allocate(1024 * 1024)
            val bufferInfo = MediaCodec.BufferInfo()
            var totalWritten = 0

            while (true) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break
                val presentationTimeUs = extractor.sampleTime
                if (presentationTimeUs > endMillis * 1000) break

                bufferInfo.offset = 0
                bufferInfo.size = sampleSize
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
            totalWritten > 0
        } catch (e: Exception) {
            Napier.e(e) { "Processor: Trim failed" }
            false
        }
    }
}

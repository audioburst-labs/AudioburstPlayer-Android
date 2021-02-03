package com.audioburst.sdkdemo

import android.content.Context
import android.media.*
import android.os.Process
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import kotlin.coroutines.resume

class AudioRecorder(context: Context) {

    private val localStoragePath: String = context.filesDir.absolutePath
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)

    var isRecording = false
        private set

    suspend fun record(): Result =
        suspendCancellableCoroutine { continuation ->
            startRecording(continuation::resume)
        }

    private fun startRecording(result: (Result) -> Unit) {
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
        isRecording = true
        val rawFilePath = "$localStoragePath/speech-recording.raw"
        val waveFilePath = "$localStoragePath/speech-recording.pcm"
        Thread {
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            val data = ByteArray(BUFFER_SIZE)
            val audioRecord = AudioRecord(AUDIO_SOURCE, SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE)
            val outputStream: BufferedOutputStream
            audioRecord.startRecording()

            try {
                outputStream = BufferedOutputStream(FileOutputStream(rawFilePath))
            } catch (e: FileNotFoundException) {
                result(Result.Failure(e))
                isRecording = false
                return@Thread
            }

            while (isRecording) {
                val status = audioRecord.read(data, 0, data.size)
                if (status == AudioRecord.ERROR_INVALID_OPERATION || status == AudioRecord.ERROR_BAD_VALUE) {
                    result(Result.Failure(IOException()))
                }
                try {
                    outputStream.write(data, 0, data.size)
                } catch (e: IOException) {
                    result(Result.Failure(e))
                }
            }

            try {
                outputStream.close()
                audioRecord.stop()
                audioRecord.release()
                val rawFile = File(rawFilePath)
                val wavFile = File(waveFilePath)
                saveAsWave(rawFile, wavFile)
                result(Result.Success(wavFile))
                rawFile.deleteOnExit()
            } catch (e: IOException) {
                result(Result.Failure(e))
            }
        }.start()
    }

    fun stopRecording() {
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
        isRecording = false
    }

    @Throws(IOException::class)
    private fun saveAsWave(rawFile: File, waveFile: File) {
        val rawData = ByteArray(rawFile.length().toInt())
        DataInputStream(FileInputStream(rawFile)).use { input ->
            var readBytes: Int
            do {
                readBytes = input.read(rawData)
            } while (readBytes != -1)
        }
        DataOutputStream(FileOutputStream(waveFile)).use { output ->
            with(output) {
                val asciiCharset = Charset.forName("US-ASCII")
                write("RIFF".toByteArray(asciiCharset))
                write(convertToLittleEndian(ConvertInput.Int(36 + rawData.size)))
                write("WAVE".toByteArray(asciiCharset))
                write("fmt ".toByteArray(asciiCharset))
                write(convertToLittleEndian(ConvertInput.Int(16)))
                write(convertToLittleEndian(ConvertInput.Short(1.toShort())))
                write(convertToLittleEndian(ConvertInput.Short(1.toShort())))
                write(convertToLittleEndian(ConvertInput.Int(SAMPLE_RATE_IN_HZ)))
                write(convertToLittleEndian(ConvertInput.Int(SAMPLE_RATE_IN_HZ * 2)))
                write(convertToLittleEndian(ConvertInput.Short(2.toShort())))
                write(convertToLittleEndian(ConvertInput.Short(16.toShort())))
                write("data".toByteArray(asciiCharset))
                write(convertToLittleEndian(ConvertInput.Int(rawData.size)))

                val rawShorts = ShortArray(rawData.size / 2)
                ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()[rawShorts]
                val bytes = ByteBuffer.allocate(rawData.size)
                for (s in rawShorts) {
                    bytes.putShort(s)
                }

                write(readFile(rawFile))
            }
        }
    }

    @Throws(IOException::class)
    private fun readFile(f: File): ByteArray {
        val size = f.length().toInt()
        val bytes = ByteArray(size)
        val tmpBuff = ByteArray(size)
        FileInputStream(f).use { fis ->
            var read = fis.read(bytes, 0, size)
            if (read < size) {
                var remain = size - read
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain)
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read)
                    remain -= read
                }
            }
        }
        return bytes
    }

    private fun convertToLittleEndian(convertInput: ConvertInput): ByteArray {
        val size = convertInput.size
        val littleEndianBytes = ByteArray(size)
        ByteBuffer.allocate(size).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            when (convertInput) {
                is ConvertInput.Int -> putInt(convertInput.value)
                is ConvertInput.Short -> putShort(convertInput.value)
            }
            flip()
            this[littleEndianBytes]
        }
        return littleEndianBytes
    }

    private sealed class ConvertInput {
        abstract val size: kotlin.Int
        class Int(val value: kotlin.Int): ConvertInput() {
            override val size: kotlin.Int = 4
        }

        class Short(val value: kotlin.Short): ConvertInput() {
            override val size: kotlin.Int = 2
        }
    }

    sealed class Result {
        data class Success(val output: File): Result()
        data class Failure(val exception: Exception): Result()
    }

    companion object {
        private const val AUDIO_SOURCE = MediaRecorder.AudioSource.UNPROCESSED
        private const val SAMPLE_RATE_IN_HZ = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT)
    }
}
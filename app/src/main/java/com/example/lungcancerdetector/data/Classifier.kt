package com.example.lungcancerdetector.data

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class Classifier(context: Context) {

    private var tflite: Interpreter? = null
    private val bufferSize = 4 * 200 * 200 * 3

    init {
        tflite = Interpreter(loadModel(context))
    }

    private fun loadModel(context: Context): MappedByteBuffer {
        val fd = context.assets.openFd("lung_cancer.tflite")
        val stream = FileInputStream(fd.fileDescriptor)
        val channel = stream.channel
        return channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
    }

    fun analyze(bitmap: Bitmap): FloatArray {
        val scaled = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
        val input = ByteBuffer.allocateDirect(bufferSize)
        input.order(ByteOrder.nativeOrder())

        val pixels = IntArray(200 * 200)
        scaled.getPixels(pixels, 0, 200, 0, 0, 200, 200)

        for (pixel in pixels) {
            val r = ((pixel shr 16 and 0xFF) - 127.5f) / 127.5f
            val g = ((pixel shr 8 and 0xFF) - 127.5f) / 127.5f
            val b = ((pixel and 0xFF) - 127.5f) / 127.5f
            input.putFloat(r)
            input.putFloat(g)
            input.putFloat(b)
        }

        val output = Array(1) { FloatArray(3) }
        tflite?.run(input, output)

        return output[0]
    }
}

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
    private val bufferSize = 4 * 224 * 224 * 3

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
        val scaled = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val input = ByteBuffer.allocateDirect(bufferSize)
        input.order(ByteOrder.nativeOrder())

        val pixels = IntArray(224 * 224)
        scaled.getPixels(pixels, 0, 224, 0, 224, 224)

        for (pixel in pixels) {
            val r = (pixel shr 16 and 0xFF) / 255.0f
            val g = (pixel shr 8 and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f
            input.putFloat(r)
            input.putFloat(g)
            input.putFloat(b)
        }

        val output = Array(1) { FloatArray(2) }
        tflite?.run(input, output)

        return output[0]
    }
}

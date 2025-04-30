package com.example.pclink.streaming

import android.graphics.BitmapFactory
import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket

class ScreenReceiver(private val surface: Surface) {
    private lateinit var codec: MediaCodec
    private var running = true

    fun start() {
        val socket = DatagramSocket(5000)
        val buffer = ByteArray(65536)

        codec = MediaCodec.createDecoderByType("video/avc") // H.264 = AVC
        val format = MediaFormat.createVideoFormat("video/avc", 1280, 720)
        codec.configure(format, surface, null, 0)
        codec.start()

        Thread {
            while (running) {
                val packet = DatagramPacket(buffer, buffer.size)
                socket.receive(packet)

                val inputBufferIndex = codec.dequeueInputBuffer(10000)
                if (inputBufferIndex >= 0) {
                    val inputBuffer = codec.getInputBuffer(inputBufferIndex)
                    inputBuffer?.clear()
                    inputBuffer?.put(packet.data, 0, packet.length)

                    codec.queueInputBuffer(inputBufferIndex, 0, packet.length, System.nanoTime() / 1000, 0)
                }
            }
        }.start()
    }

    fun stop() {
        running = false
        codec.stop()
        codec.release()
    }
}
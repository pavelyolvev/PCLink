package com.example.pclink;

import android.R
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.wifi.WifiManager
import android.util.Log
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.PrintWriter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket
import java.net.NetworkInterface
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class NetworkLink {
    private var socket: DatagramSocket? = null
    private var serverAddress: InetAddress? = null
    private var serverPort: Int = 0


    var onConnected: (() -> Unit)? = null // Колбэк для уведомления о готовности соединения

    fun connect(ip: String, port: Int) {
        Thread {
            try {
                socket = DatagramSocket()
                serverAddress = InetAddress.getByName(ip)
                serverPort = port
                println("UDP подключение установлено с $ip:$port")
                onConnected?.invoke() // Вызываем колбэк после успешного подключения
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun sendCommand(id: Int, command: String, parameters: Array<String> = emptyArray(), responseRequired: Boolean = false) {
        Thread {
            try {
                val json = JSONObject().apply {
                    put("id", id)
                    put("message", command)
                    put("parameters", JSONArray(parameters))
                    put("response", responseRequired)
                }
                val buffer = json.toString().toByteArray()
                val packet = DatagramPacket(buffer, buffer.size, serverAddress, serverPort)
                socket?.send(packet)

            } catch (e: SocketTimeoutException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
    suspend fun requestAccess(ip: String, port: Int, request: String, parameters: Array<String> = emptyArray(), timeout: Int = 2000): JSONObject = withContext(Dispatchers.IO) {
        val id = 0
        val responseRequired = true
        var socket: DatagramSocket? = null

        try {
            socket = DatagramSocket()
            socket.soTimeout = timeout
            val serverAddress = InetAddress.getByName(ip)

            val json = JSONObject().apply {
                put("id", id)
                put("message", request)
                put("parameters", JSONArray(parameters))
                put("response", responseRequired)
            }

            val buffer = json.toString().toByteArray()
            val packet = DatagramPacket(buffer, buffer.size, serverAddress, port)
            socket.send(packet)

            val receiveBuffer = ByteArray(1024)
            val responsePacket = DatagramPacket(receiveBuffer, receiveBuffer.size)
            socket.receive(responsePacket)

            val responseJson = String(responsePacket.data, 0, responsePacket.length)
            val response = JSONObject(responseJson)
            response
        } catch (e: SocketTimeoutException) {
            JSONObject().apply {
                put("message", "TIMEOUT")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            JSONObject().apply {
                put("message", "SOCKET_ERROR")
            }
        } finally {
            socket?.close()
        }
    }

    fun sendWakeOnLan(mac: String, ip: String, port: Int = 9): Boolean {
        return try {
            val macBytes = getMacBytes(mac)
            val bytes = ByteArray(6 + 16 * macBytes.size)

            // Префикс — 6 байт 0xFF
            for (i in 0 until 6) {
                bytes[i] = 0xFF.toByte()
            }

            // Повторяем MAC 16 раз
            for (i in 6 until bytes.size step macBytes.size) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.size)
            }

            val address = InetAddress.getByName(ip)
            val packet = DatagramPacket(bytes, bytes.size, address, port)
            DatagramSocket().use { socket ->
                socket.send(packet)
            }

            println("WoL пакет отправлен на $mac по адресу $ip:$port")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    fun getBroadcastAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            for (intf in interfaces) {
                if (!intf.isUp || intf.isLoopback) continue

                for (addr in intf.interfaceAddresses) {
                    val inetAddr = addr.address
                    if (inetAddr.isLoopbackAddress || inetAddr !is Inet4Address) continue

                    val prefixLength = addr.networkPrefixLength.toInt()
                    val mask = (0xffffffff).toInt() shl (32 - prefixLength)

                    val ip = ByteBuffer.wrap(inetAddr.address).int
                    val broadcast = ip or mask.inv()

                    val bytes = ByteBuffer.allocate(4).putInt(broadcast).array()
                    return InetAddress.getByAddress(bytes).hostAddress
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun getMacBytes(macStr: String): ByteArray {
        val bytes = ByteArray(6)
        val hex = macStr.split(":")
        if (hex.size != 6) {
            throw IllegalArgumentException("Неверный MAC-адрес: $macStr")
        }
        for (i in hex.indices) {
            bytes[i] = hex[i].toInt(16).toByte()
        }
        return bytes
    }
    fun disconnect() {
        socket?.close()
    }
    fun getLocalIpAddress(): String? {
        try {
            val interfaces: List<NetworkInterface> = NetworkInterface.getNetworkInterfaces().toList()
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue // Пропускаем loopback и неактивные интерфейсы
                val addresses = intf.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (addr is Inet4Address && !addr.isLoopbackAddress && addr.isSiteLocalAddress) {
                        // Нашли локальный IPv4 адрес (192.168.x.x, 10.x.x.x, 172.16-31.x.x)
                        Log.d("getLocalIpAddress", "Found IP: ${addr.hostAddress} on interface ${intf.displayName}")
                        return addr.hostAddress
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("getLocalIpAddress", "Error getting IP: ${ex.message}", ex)
        }
        Log.w("getLocalIpAddress", "No suitable local IP address found.")
        return null
    }
    fun isHostReachable(ip: String, timeout: Int = 1000): Boolean {
        return try {
            InetAddress.getByName(ip).isReachable(timeout)
        } catch (e: Exception) {
            false
        }
    }

    private val videoPort = 12313
    private var udpSocket: DatagramSocket? = null
    private val receiveBuffer = ByteArray(65536)
    private var receiveJob: Job? = null
    private val uiScope = CoroutineScope(Dispatchers.Main)

    fun startReceiving(imageView: ImageView) {
        receiveJob?.cancel()
        receiveJob = CoroutineScope(Dispatchers.IO).launch {
            val fragmentMap = mutableMapOf<Int, FrameBuffer>()
            udpSocket = DatagramSocket(null).apply {
                reuseAddress = true
                bind(InetSocketAddress(videoPort))
            }
            Log.d("DesktopViewer", "UDP Socket bound to port $videoPort")

            while (isActive) {
                val packet = DatagramPacket(receiveBuffer, receiveBuffer.size)
                udpSocket!!.receive(packet)
                handlePacket(packet, imageView, fragmentMap);
//                try {
//                    udpSocket?.receive(packet)
//
//                    if (packet.length < 8) continue // слишком короткий пакет
//
//                    val data = packet.data
//                    val offset = packet.offset
//
//                    // Чтение заголовка (4 байта frameId, 2 байта totalFragments, 2 байта fragmentIndex)
//                    val frameId = ByteBuffer.wrap(data, offset, 4).order(ByteOrder.BIG_ENDIAN).int
//                    val totalFragments = ByteBuffer.wrap(data, offset + 4, 2).order(ByteOrder.BIG_ENDIAN).short.toInt()
//                    val fragmentIndex = ByteBuffer.wrap(data, offset + 6, 2).order(ByteOrder.BIG_ENDIAN).short.toInt()
//
//                    val fragmentData = data.copyOfRange(offset + 8, offset + packet.length)
//
//                    val frame = fragmentMap.getOrPut(frameId) {
//                        FrameFragment(System.currentTimeMillis(), totalFragments, Array(totalFragments) { null })
//                    }
//
//                    frame.fragments[fragmentIndex] = fragmentData
////                    Log.d("DesktopViewer", "fragment get with index $fragmentIndex")
//
//                    // Проверяем, собраны ли все фрагменты
//                    if (frame.fragments.take(frame.totalFragments).all { it != null }) {
//                        val imageBytes = frame.fragments
//                            .take(frame.totalFragments)
//                            .flatMap { it!!.asIterable() }
//                            .toByteArray()
//                        fragmentMap.remove(frameId)
//                        Log.d("DesktopViewer", "image created from fragments")
//                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
//                        if (bitmap != null) {
//                            uiScope.launch {
//                                imageView.setImageBitmap(bitmap)
//                            }
//                        } else {
//                            Log.w("DesktopViewer", "Failed to decode JPEG frame (frameId $frameId)")
//                        }
//                    }
//
//                    // Очистка старых фреймов (старше 1 секунды)
//                    val now = System.currentTimeMillis()
//                    fragmentMap.entries.removeIf { now - it.value.timestamp > 1000 }
//
//                } catch (e: Exception) {
//                    if (isActive) {
//                        Log.e("DesktopViewer", "Receiving error: ${e.message}", e)
//                    }
//                    break
//                }
            }

            udpSocket?.close()
            udpSocket = null
            Log.d("DesktopViewer", "UDP receiving loop finished.")
        }
    }

    fun stopReceiving(imageView: ImageView) {
        receiveJob?.cancel()
        receiveJob = null
        Log.d("DesktopViewer", "Stop receiving requested.")
        uiScope.launch {
            imageView.setImageDrawable(null)
        }
    }
    data class FrameBuffer(val totalFragments: Int) {
        val fragments = Array<ByteArray?>(totalFragments) { null }
    }

    val fragmentMap = mutableMapOf<Int, FrameBuffer>()

    fun handlePacket(packet: DatagramPacket, imageView: ImageView, fragmentMap: MutableMap<Int, FrameBuffer>) {
        val data = packet.data
        val buffer = ByteBuffer.wrap(data, 0, packet.length).order(ByteOrder.LITTLE_ENDIAN)

        val frameId = buffer.int
        val totalFragments = buffer.short.toInt() and 0xFFFF
        val fragmentIndex = buffer.short.toInt() and 0xFFFF

        val fragmentData = ByteArray(packet.length - 8)
        System.arraycopy(data, 8, fragmentData, 0, fragmentData.size)

        val frame = fragmentMap.getOrPut(frameId) { FrameBuffer(totalFragments) }

        if (fragmentIndex >= totalFragments) {
            Log.w("UDP", "Skipping invalid fragment index $fragmentIndex >= $totalFragments")
            return
        }

        frame.fragments[fragmentIndex] = fragmentData
        Log.d("UDP", "Fragment $fragmentIndex / $totalFragments for frame $frameId received")

        if (frame.fragments.take(totalFragments).all { it != null }) {
            val imageBytes = frame.fragments.take(totalFragments)
                .flatMap { it!!.asIterable() }
                .toByteArray()

            fragmentMap.remove(frameId)
            Log.d("DesktopViewer", "image created from fragments (frameId=$frameId)")

            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            if (bitmap != null) {
                uiScope.launch {
                    imageView.setImageBitmap(bitmap)
                }
            } else {
                Log.w("DesktopViewer", "Failed to decode JPEG frame (frameId $frameId)")
            }
        }

    }


}

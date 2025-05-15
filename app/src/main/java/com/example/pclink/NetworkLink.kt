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

    private var udpSocket: DatagramSocket? = null
    private val receiveBuffer = ByteArray(65536) // Буфер для приема UDP пакетов (максимальный размер)
    private val videoPort = 9050 // Должен совпадать с C# сервером
    private var receiveJob: Job? = null
    private val uiScope = CoroutineScope(Dispatchers.Main) // Для обновления UI
    fun startReceiving(imageView: ImageView) {
        // Отменяем предыдущую задачу, если она была
        receiveJob?.cancel()
        receiveJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                // Создаем и привязываем сокет
                // Важно: Используем 0.0.0.0 чтобы слушать на всех интерфейсах
                udpSocket = DatagramSocket(null)
                udpSocket?.reuseAddress = true // Позволяет переиспользовать адрес немедленно
                udpSocket?.bind(InetSocketAddress(videoPort))
                Log.d("DesktopViewer", "UDP Socket bound to port $videoPort")

                while (isActive) { // Пока корутина активна
                    val packet = DatagramPacket(receiveBuffer, receiveBuffer.size)
                    try {
                        udpSocket?.receive(packet) // Блокирующая операция - ждем пакет

                        if (packet.length > 0) {
                            //Log.d("DesktopViewer", "Received packet: ${packet.length} bytes")

                            // Декодируем полученные байты (JPEG) в Bitmap
                            val bitmap = BitmapFactory.decodeByteArray(packet.data, packet.offset, packet.length)

                            if (bitmap != null) {
                                //Log.d("DesktopViewer", "Successfully decoded bitmap")
                                uiScope.launch {
                                    imageView.setImageBitmap(bitmap)
                                }
                            } else {
                                Log.w("DesktopViewer", "Failed to decode received packet into Bitmap")
                            }
                        } else {
                            Log.w("DesktopViewer", "Received empty packet (length 0)")
                        }
                    } catch (e: SocketException) {
                        // SocketException часто возникает при закрытии сокета из другого потока
                        if (isActive) { // Логируем ошибку только если корутина не отменена
                            Log.e("DesktopViewer", "SocketException receiving data: ${e.message}", e)
                        } else {
                            Log.d("DesktopViewer", "Socket closed.")
                        }
                        break // Выходим из цикла при ошибке сокета
                    } catch (e: Exception) {
                        Log.e("DesktopViewer", "Error receiving or processing packet: ${e.message}", e)
                        // Можно добавить небольшую паузу перед следующей попыткой
                        delay(100)
                    }
                }
            } catch (e: Exception) {
                Log.e("DesktopViewer", "Error setting up UDP socket: ${e.message}", e)
            } finally {
                udpSocket?.close() // Убедимся, что сокет закрыт при выходе из корутины
                udpSocket = null
                Log.d("DesktopViewer", "UDP receiving loop finished, socket closed.")
            }
        }
    }

    fun stopReceiving(imageView: ImageView) {
        receiveJob?.cancel() // Отменяем корутину
        receiveJob = null
        // Закрытие сокета произойдет в finally блоке корутины
        // Важно: не вызывать close() здесь напрямую из UI потока, если receive() блокирует IO поток
        // Корутина сама закроет сокет при отмене или ошибке
        Log.d("DesktopViewer", "Stop receiving requested.")
        // Опционально - очистить ImageView
        uiScope.launch{
            imageView.setImageDrawable(null)
        }

    }
}

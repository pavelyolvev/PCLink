package com.example.pclink;

import java.io.PrintWriter
import java.net.Socket


public class NetworkLink {
    private var socket: Socket? = null
    private var writer: PrintWriter? = null


    fun connect(ip: String, port: Int) {
        Thread {
            try {
                socket = Socket(ip, port)
                writer = PrintWriter(socket!!.getOutputStream(), true)
                println("Подключено к серверу: $ip:$port")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun sendCommand(command: String) {
        Thread {
            try {
                writer?.println(command)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun disconnect() {
        try {
            writer?.close()
            socket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}

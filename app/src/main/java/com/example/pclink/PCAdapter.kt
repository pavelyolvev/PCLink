package com.example.pclink

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pclink.PreferencesFuncs.PC
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class PCListAdapter(
    private val pcList: List<PC>,
    private val onConnectClick: (Int) -> Unit,
    private val onSettingsClick: (Int) -> Unit,
    private val onExtraClick: (Int) -> Unit
) : RecyclerView.Adapter<PCListAdapter.PCViewHolder>() {

    inner class PCViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.pcName)
        val statusText: TextView = itemView.findViewById(R.id.pcStatus)
        val btnConnect: ImageButton = itemView.findViewById(R.id.btnConnect)
        val btnSettings: ImageButton = itemView.findViewById(R.id.btnSettings)
        val btnExtra: ImageButton = itemView.findViewById(R.id.btnExtraFunctions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PCViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pc_item_layout, parent, false)
        return PCViewHolder(view)
    }

    override fun onBindViewHolder(holder: PCViewHolder, position: Int) {
        val pc = pcList[position]
        holder.nameText.text = pc.name
        setStatus(pc, holder)
        //holder.statusText.text = "Онлайн" // или ваша логика

        holder.btnConnect.setOnClickListener {
            onConnectClick(position)
        }

        holder.btnSettings.setOnClickListener {
            onSettingsClick(position)
        }
        holder.btnExtra.setOnClickListener {
            onExtraClick(position)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun setStatus(pc: PC, holder: PCViewHolder) {
        Log.d("NETWORKTEST", "setStatus called for IP: ${pc.ip}")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("NETWORKTEST", "Coroutine launched")
                val isReachable = isHostAlive(pc.ip)
                Log.d("NETWORKTEST", "isReachable: $isReachable")
                val isAppRunning = isPortOpen(pc.ip, pc.port)
                Log.d("NETWORKTEST", "isAppRunning: $isAppRunning")

                withContext(Dispatchers.Main) {
                    if (!isReachable) {
                        holder.statusText.text = "Не в сети"
                        holder.statusText.setTextColor(Color.RED)
                    } else if (!isAppRunning) {
                        holder.statusText.text = "PCLink не запущен"
                        holder.statusText.setTextColor(Color.YELLOW)
                    } else {
                        holder.statusText.text = "Онлайн"
                        holder.statusText.setTextColor(Color.GREEN)
                    }
                }
            } catch (e: Exception) {
                Log.e("NETWORKTEST", "Ошибка: ${e.message}", e)
            }
        }
    }
    fun isPortOpen(ip: String, port: Int, timeout: Int = 500): Boolean {
        return try {
            val socket = DatagramSocket()
            val serverAddress = InetAddress.getByName(ip)
            val serverPort = port
            socket.connect(serverAddress, serverPort)
            println("UDP подключение установлено с $ip:$port")
            socket.close()
            true
        } catch (e: IOException) {
            Log.e("NETWORKTEST", "Ошибка: ${e.message}", e)
            false
        }
    }
    fun isHostAlive(ip: String): Boolean {
        return try {
            val addr = InetAddress.getByName(ip);
            addr.isReachable(5000);
        } catch (e: Exception) {
            Log.e("NETWORKTEST", "Ping error: ${e.message}")
            false
        }
    }



    override fun getItemCount() = pcList.size
}

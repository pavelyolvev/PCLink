package com.example.pclink

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.pclink.databinding.PcsettingsBinding
import kotlin.math.abs

class PCStreamFragment : Fragment() {

    private var _binding: PcsettingsBinding? = null
    private val binding get() = _binding!!
    private var pcId: Int = -1;
    private lateinit var imageView: ImageView

    val net = NetworkLink()
    val prefs = PreferencesFuncs()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        pcId = arguments?.getInt("pcId") ?: -1

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.pcstream, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val pc = prefs.loadPCPref(requireContext(), pcId)
        Log.d("PCPREFDEBUG", "pc IP: ${pc?.ip}")
        Log.d("PCPREFDEBUG", "pc port: ${pc?.port}")

        val serverIpForCommands = pc?.ip
        val serverPortForCommands = pc?.port // Должен быть 12312
        imageView = view.findViewById<ImageView>(R.id.streamView)
        if (serverIpForCommands != null && serverPortForCommands != null) {
            net.onConnected = { // Устанавливаем колбэк
                activity?.runOnUiThread { // Возвращаемся в UI поток (хотя отправка команды может быть и в фоне)
                    // 1. Получаем свой IP
                    val myIp = net.getLocalIpAddress() //

                    // 2. Отправляем свой IP серверу
                    if (myIp != null) {
                        net.sendCommand("CLIENT_IP:$myIp")
                        Log.d("PCStreamFragment", "Sent client IP: $myIp to server")
                    } else {
                        Log.w("PCStreamFragment", "Failed to get client IP to send to server.")
                    }

                    // 3. Отправляем команду режима (как было)
                    net.sendCommand("${pc.mode}")
                    Log.d("PCStreamFragment", "Sent mode command: ${pc.mode}")

                }
            }
            // net.connect должен подключаться к серверу команд на 12312
            net.connect(serverIpForCommands, serverPortForCommands)

        } else {
            Log.e("PCPREFDEBUG", "Server IP or command port is null. Connection failed.")
            // Может быть, показать пользователю сообщение об ошибке
        }

        touchPad()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        net.disconnect()
    }
    private var lastX = 0f
    private var lastY = 0f
    private var downX = 0f
    private var downY = 0f
    private val threshold = 1f // фильтрация дрожания
    private val thresholdClick = 10f // фильтрация движения при клике

    @SuppressLint("ClickableViewAccessibility")
    fun touchPad() {
        imageView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastX = event.x
                    lastY = event.y
                    downX = event.x
                    downY = event.y
                }

                MotionEvent.ACTION_MOVE -> {
                    for (i in 0 until event.historySize) {
                        val hx = event.getHistoricalX(i)
                        val hy = event.getHistoricalY(i)
                        val dx = hx - lastX
                        val dy = hy - lastY

                        if (abs(dx) > threshold || abs(dy) > threshold) {
                            net.sendCommand("MOVE:${dx.toInt()},${dy.toInt()}")
                            lastX = hx
                            lastY = hy
                        }
                    }

                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    if (abs(dx) > threshold || abs(dy) > threshold) {
                        net.sendCommand("MOVE:${dx.toInt()},${dy.toInt()}")
                        lastX = event.x
                        lastY = event.y
                    }
                }

                MotionEvent.ACTION_UP -> {
                    val dx = event.x - downX
                    val dy = event.y - downY
                    if (abs(dx) < thresholdClick || abs(dy) < thresholdClick) {
                        net.sendCommand("CLICK")
                    }
                }
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        net.startReceiving(imageView)
        Log.d("DesktopViewer", "Resumed, starting receiving")
    }

    override fun onPause() {
        super.onPause()
        net.stopReceiving(imageView)
        Log.d("DesktopViewer", "Paused, stopping receiving")
    }
}

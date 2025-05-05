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
import java.util.Timer
import java.util.TimerTask
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
            net.onConnected = {
                activity?.runOnUiThread {
                    val myIp = net.getLocalIpAddress()

                    if (myIp != null) {
                        // Сначала отправляем IP
                        net.sendCommand("CLIENT_IP:$myIp")
                        Log.d("PCStreamFragment", "Sent client IP: $myIp to server")

                        // Показываем диалог авторизации
                        val dialog = PcAuthDialog.newInstance(pcId, net) {
                            // Этот колбэк вызывается после успешной авторизации
                            net.sendCommand("${pc.mode}")
                            Log.d("PCStreamFragment", "Sent mode command: ${pc.mode}")
                        }
                        dialog.show(parentFragmentManager, "pc auth")
                    } else {
                        Log.w("PCStreamFragment", "Failed to get client IP to send to server.")
                    }
                }
            }
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
    private var lastAvgX = 0f
    private var lastAvgY = 0f
    private var threshold = 2f
    private var thresholdClick = 10f
    private var lastScrollTime = 0L
    private val scrollIntervalMs = 50L

    private var scrollVelocityX = 0f
    private var scrollVelocityY = 0f
    private var scrollTimer: Timer? = null
    private var wasTwoFingerScroll = false

    @SuppressLint("ClickableViewAccessibility")
    fun touchPad() {
        imageView.setOnTouchListener { _, event ->
            val pointerCount = event.pointerCount
            val currentTime = System.currentTimeMillis()

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    if (pointerCount == 1) {
                        lastX = event.getX(0)
                        lastY = event.getY(0)
                        downX = lastX
                        downY = lastY

                        if (wasTwoFingerScroll) {
                            // сброс после скролла — чтобы не было скачка мыши
                            wasTwoFingerScroll = false
                            return@setOnTouchListener true
                        }

                        stopScrollInertia()
                    } else if (pointerCount == 2) {
                        val x0 = event.getX(0)
                        val x1 = event.getX(1)
                        val y0 = event.getY(0)
                        val y1 = event.getY(1)
                        lastAvgX = (x0 + x1) / 2
                        lastAvgY = (y0 + y1) / 2

                        stopScrollInertia()
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (pointerCount == 1 && !wasTwoFingerScroll) {
                        val dx = event.getX(0) - lastX
                        val dy = event.getY(0) - lastY

                        if (abs(dx) > threshold || abs(dy) > threshold) {
                            net.sendCommand("MOVE:${dx.toInt()},${dy.toInt()}")
                            lastX = event.getX(0)
                            lastY = event.getY(0)
                        }
                    } else if (pointerCount == 2) {
                        wasTwoFingerScroll = true

                        val x0 = event.getX(0)
                        val x1 = event.getX(1)
                        val y0 = event.getY(0)
                        val y1 = event.getY(1)

                        val avgX = (x0 + x1) / 2
                        val avgY = (y0 + y1) / 2
                        val deltaX = avgX - lastAvgX
                        val deltaY = avgY - lastAvgY

                        if (currentTime - lastScrollTime > scrollIntervalMs) {
                            if (abs(deltaX) > threshold) {
                                net.sendCommand("HSCROLL:${(-deltaX).toInt()}")
                            }
                            if (abs(deltaY) > threshold) {
                                net.sendCommand("SCROLL:${(-deltaY).toInt()}")
                            }
                            // Запоминаем дельты для инерции
                            scrollVelocityX = deltaX
                            scrollVelocityY = deltaY

                            lastAvgX = avgX
                            lastAvgY = avgY
                            lastScrollTime = currentTime
                        }
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    if (wasTwoFingerScroll && pointerCount <= 2) {
                        startScrollInertia()
                    }

                    val dx = event.x - downX
                    val dy = event.y - downY
                    if (!wasTwoFingerScroll && abs(dx) < thresholdClick && abs(dy) < thresholdClick) {
                        net.sendCommand("CLICK")
                    }
                }
            }
            true
        }
    }
    private fun startScrollInertia() {
        scrollTimer?.cancel()
        scrollTimer = Timer()
        scrollTimer?.schedule(object : TimerTask() {
            override fun run() {
                scrollVelocityX *= 0.9f
                scrollVelocityY *= 0.9f

                if (abs(scrollVelocityX) < 1f && abs(scrollVelocityY) < 1f) {
                    stopScrollInertia()
                    return
                }

                if (abs(scrollVelocityX) >= 1f) {
                    net.sendCommand("HSCROLL:${(-scrollVelocityX).toInt()}")
                }
                if (abs(scrollVelocityY) >= 1f) {
                    net.sendCommand("SCROLL:${(-scrollVelocityY).toInt()}")
                }
            }
        }, 0, scrollIntervalMs)
    }

    private fun stopScrollInertia() {
        scrollTimer?.cancel()
        scrollTimer = null
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

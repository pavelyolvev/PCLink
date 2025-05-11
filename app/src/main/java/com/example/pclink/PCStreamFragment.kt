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
import androidx.navigation.fragment.findNavController
import com.example.pclink.databinding.PcsettingsBinding
import com.example.pclink.touchpad.Touchpad
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
//                        net.sendCommand(1, "CLIENT_IP:$myIp")
                        Log.d("PCStreamFragment", "Sent client IP: $myIp to server")

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

        val tp = Touchpad(imageView, net)
        tp.touchPad()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        net.disconnect()
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

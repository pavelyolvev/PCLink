package com.example.pclink

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.example.pclink.databinding.PcsettingsBinding
import com.example.pclink.streaming.ScreenReceiver
import com.example.pclink.touchpad.Keyboard
import com.example.pclink.touchpad.Touchpad
import kotlinx.coroutines.launch

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
//        val sr = ScreenReceiver(imageView);
        tp.touchPad()

        net.startReceiving(imageView)


        val buttonPc = view.findViewById<AppCompatImageButton>(R.id.draggableButtonPC)
        val buttonKeyboard = view.findViewById<AppCompatImageButton>(R.id.draggableButtonKeyboard)
        val hiddenInput = view.findViewById<EditText>(R.id.hiddenInput)
        val kb = Keyboard(net, view, buttonKeyboard, hiddenInput, requireContext())
        kb.setListener()

        setDraggableButton(view, buttonPc, ::onDraggableButtonPcClick, kb)
        setDraggableButton(view, buttonKeyboard, ::onDraggableButtonKeyBoardClick, kb)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        val pc = prefs.loadPCPref(requireContext(), pcId)
        val serverIpForCommands = pc?.ip
        val serverPortForCommands = pc?.port // Должен быть 12312
        if (serverIpForCommands != null && serverPortForCommands != null) {
            lifecycleScope.launch {
                net.requestAccess(serverIpForCommands, serverPortForCommands, "END")
            }
            net.stopReceiving(imageView)
            net.disconnect()
        }
    }



    @SuppressLint("ClickableViewAccessibility")
    fun setDraggableButton(view: View, appCompatImageButton: AppCompatImageButton, onDraggableButtonClick: () -> Unit, kb:Keyboard){
        val layout = view.findViewById<ConstraintLayout>(R.id.btnRootView)

        layout.post {
            val screenWidth = layout.width
            val screenHeight = layout.height

            var dX = 0f
            var dY = 0f
            var isDragging = false

            appCompatImageButton.setOnTouchListener { view, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        dX = view.x - event.rawX
                        dY = view.y - event.rawY
                        isDragging = false
                    }

                    MotionEvent.ACTION_MOVE -> {
                        val newX = event.rawX + dX
                        val newY = event.rawY + dY

                        val maxX = screenWidth - view.width
                        val maxY = screenHeight - view.height

                        if (maxX < 0 || maxY < 0) return@setOnTouchListener true

                        val clampedX = newX.coerceIn(0f, maxX.toFloat())
                        val clampedY = newY.coerceIn(0f, maxY.toFloat())

                        view.animate().x(clampedX).y(clampedY).setDuration(0).start()

                        isDragging = true
                    }

                    MotionEvent.ACTION_UP -> {
                        if (!isDragging) {
                            onDraggableButtonClick()
                            kb.openKeyboard()
                        }
                    }
                }
                true
            }
        }
    }
    private fun onDraggableButtonPcClick() {
        Toast.makeText(requireContext(), "Кнопка пк нажата", Toast.LENGTH_SHORT).show()
        // Здесь можно вызвать любую вашу логику
    }
    private fun onDraggableButtonKeyBoardClick() {
        Toast.makeText(requireContext(), "Кнопка клавиатуры нажата", Toast.LENGTH_SHORT).show()
        // Здесь можно вызвать любую вашу логику
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

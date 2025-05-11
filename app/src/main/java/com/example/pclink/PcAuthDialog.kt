package com.example.pclink

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch

class PcAuthDialog(
    private val onAuthSuccess: (authCode: Int, mac: String) -> Unit
) : DialogFragment() {

    private val net = NetworkLink()
    private lateinit var pc: PreferencesFuncs.PC
    val prefs = PreferencesFuncs()

    companion object {
        fun newInstance(pc: PreferencesFuncs.PC, onAuthSuccess: (authCode: Int, mac: String) -> Unit): PcAuthDialog {
            val fragment = PcAuthDialog(onAuthSuccess)
            fragment.pc = pc
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.pc_auth_dialog, null)

        val serverIpForCommands = pc.ip
        val serverPortForCommands = pc.port

        lifecycleScope.launch {
            net.requestAccess(serverIpForCommands, serverPortForCommands, "AUTH_START", emptyArray())
        }

        view.findViewById<Button>(R.id.btnConnectPC).setOnClickListener {
            val code = view.findViewById<EditText>(R.id.codeEditText).text.toString()
            lifecycleScope.launch {
                val serverResponse = net.requestAccess(serverIpForCommands, serverPortForCommands, "AUTH", arrayOf(code, pc.mode.toString()))
                if(serverResponse.getString("message") == "AUTH_SUCCEED") {
                    Log.d("DEGUG================", serverResponse.getJSONArray("parameters").getString(0))
                    onAuthSuccess(code.toInt(), serverResponse.getJSONArray("parameters").getString(0))
                    dismiss()
                }
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }
}

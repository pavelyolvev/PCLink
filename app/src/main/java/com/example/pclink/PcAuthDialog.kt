package com.example.pclink

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.DialogFragment

class PcAuthDialog(
    private val onAuthSuccess: () -> Unit
) : DialogFragment() {

    val prefs = PreferencesFuncs()
    private lateinit var net: NetworkLink

    companion object {
        fun newInstance(pcId: Int, net: NetworkLink, onAuthSuccess: () -> Unit): PcAuthDialog {
            val fragment = PcAuthDialog(onAuthSuccess)
            fragment.arguments = Bundle().apply {
                putInt("pcId", pcId)
            }
            fragment.net = net
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val pcId = arguments?.getInt("pcId") ?: -1
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.pc_auth_dialog, null)

        view.findViewById<Button>(R.id.btnConnectPC).setOnClickListener {
            val code = view.findViewById<EditText>(R.id.codeEditText).text.toString()
            net.sendCommand("AUTH:$code")

            // Немного подождём и потом вызовем onAuthSuccess
            Handler(Looper.getMainLooper()).postDelayed({
                onAuthSuccess()
            }, 300)

            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }
}

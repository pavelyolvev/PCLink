package com.example.pclink

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class ExtraDialogFragment : DialogFragment() {
    val prefs = PreferencesFuncs()
    val net = NetworkLink()

    companion object {
        fun newInstance(pcId: Int): ExtraDialogFragment {
            val args = Bundle().apply {
                putInt("pcId", pcId)
            }
            val fragment = ExtraDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val pcId = arguments?.getInt("pcId") ?: -1
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.extra_dialog, null)

        val pc = prefs.loadPCPref(requireContext(), pcId)

        // Пример установки текста
        val pcName = pc?.name
        val pcIp = pc?.ip
        val pcPort = pc?.port

        view.findViewById<TextView>(R.id.tvPcInfoName).text = pcName
        view.findViewById<TextView>(R.id.tvPcInfoIP).text = pcIp

        // Пример действий
        view.findViewById<Button>(R.id.btnActionOn).setOnClickListener {
            if (pcIp != null && pcPort != null) {
                net.onConnected = {
                    net.sendCommand("AUTH")
                }
                net.connect(pcIp, pcPort)
                net.sendCommand("SHUTDOWN")
            }
            Toast.makeText(requireContext(), "Выполнено действие для ПК $pcId", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        view.findViewById<Button>(R.id.btnClose).setOnClickListener {
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }
}


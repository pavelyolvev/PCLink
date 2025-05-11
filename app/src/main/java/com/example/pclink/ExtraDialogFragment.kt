package com.example.pclink

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

        val pcName = pc?.name
        val pcIp = pc?.ip
        val pcPort = pc?.port
        val pcMac = pc?.macAdress

        // установка текста
        view.findViewById<TextView>(R.id.tvPcInfoName).text = pcName
        view.findViewById<TextView>(R.id.tvPcInfoIP).text = pcIp

        view.findViewById<Button>(R.id.btnActionOn).setOnClickListener {
            if (pcMac != null) {
                val broadcastIp = net.getBroadcastAddress(requireContext())
                if (broadcastIp != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val success = net.sendWakeOnLan(pcMac, broadcastIp)
                        if (success) {
                            Toast.makeText(requireContext(), "ПК пробуждён", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Ошибка при отправке WoL", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Не удалось определить broadcast-адрес", Toast.LENGTH_SHORT).show()
                }
            }
            dismiss()
        }

        view.findViewById<Button>(R.id.btnActionOff).setOnClickListener {
            lifecycleScope.launch {
                if (pcIp != null && pcPort != null) {
                    net.requestAccess(pcIp, pcPort, "PC_SHUTDOWN")
                }
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


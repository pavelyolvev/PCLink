package com.example.pclink

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment

class ExtraDialogFragment : DialogFragment() {

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

        // Пример установки текста
        view.findViewById<TextView>(R.id.tvPcInfo).text = "ПК ID: $pcId"

        // Пример действий
        view.findViewById<Button>(R.id.btnAction1).setOnClickListener {
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


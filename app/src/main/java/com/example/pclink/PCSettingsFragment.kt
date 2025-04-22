package com.example.pclink

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.navigation.fragment.findNavController
import com.example.pclink.databinding.PcsettingsBinding

class PCSettingsFragment : Fragment() {

    private var _binding: PcsettingsBinding? = null
    private val binding get() = _binding!!
    private var isNew: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isNew = arguments?.getBoolean("isNew") ?: false

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = PcsettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
//        val backBtn: ImageButton = view.findViewById(R.id.btnBack)
        val saveBtn: Button = view.findViewById(R.id.btnSave)
        val sharedPref = requireContext().getSharedPreferences("PC_PREFS", Context.MODE_PRIVATE)
        binding.spinnerMode.setSelection(
            resources.getStringArray(R.array.mode_options).indexOf(sharedPref.getString("mode", "Тачпад"))
        )
        binding.spinnerMouseMode.setSelection(
            resources.getStringArray(R.array.mouse_mode_options).indexOf(sharedPref.getString("mouse_mode", "Тачпад"))
        )
        binding.editIP.setText(sharedPref.getString("ip", ""))
        binding.editPort.setText(sharedPref.getString("port", ""))

        if (isNew) saveBtn.visibility = View.VISIBLE

//        backBtn.setOnClickListener {
//            findNavController().popBackStack()
//        }

        saveBtn.setOnClickListener {
            // Сохраняем настройки, потом возвращаемся
            val mode = binding.spinnerMode.selectedItem.toString()
            val mouseMode = binding.spinnerMouseMode.selectedItem.toString()
            val ip = binding.editIP.text.toString()
            val port = binding.editPort.text.toString()

            val sharedPref = requireContext().getSharedPreferences("PC_PREFS", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("mode", mode)
                putString("mouse_mode", mouseMode)
                putString("ip", ip)
                putString("port", port)
                apply()
            }
            findNavController().popBackStack()
        }
    }
}

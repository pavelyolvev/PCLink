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
        if(arguments?.getInt("pcId") == -1)
            isNew = true

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
        val saveBtn: Button = view.findViewById(R.id.btnSave)
        val delBtn: Button = view.findViewById(R.id.btnDelete)
        if (!isNew) {
            loadSaved()

        } else{
            delBtn.visibility = View.GONE
        }

        saveBtn.setOnClickListener {
            val modeIndex = binding.spinnerMode.selectedItemPosition
            val mouseModeIndex = binding.spinnerMouseMode.selectedItemPosition
            val ip = binding.editIP.text.toString()
            val port = binding.editPort.text.toString().toIntOrNull() ?: 0
            val pcName = binding.editName.text.toString()
            val mac = binding.editMac.text.toString()

            val updatedPC = PreferencesFuncs.PC(
                name = pcName,
                ip = ip,
                port = port,
                mode = modeIndex,
                mouseMode = mouseModeIndex,
                macAdress = mac
            )

            val pcId = arguments?.getInt("pcId") ?: -1
            if (!isNew) {
                PreferencesFuncs().updatePC(requireContext(), pcId, updatedPC)
            } else PreferencesFuncs().saveNewPC(requireContext(), updatedPC)

            findNavController().popBackStack()
        }
        delBtn.setOnClickListener {
            val pcId = arguments?.getInt("pcId") ?: -1
            if (!isNew && pcId != -1) {
                PreferencesFuncs().removePC(requireContext(), pcId)
            }
            findNavController().popBackStack()
        }
    }
    fun loadSaved(){
        val sharedPref = requireContext().getSharedPreferences("PC_PREFS", Context.MODE_PRIVATE)
        binding.spinnerMode.setSelection(
            resources.getStringArray(R.array.mode_options).indexOf(sharedPref.getString("mode", "Тачпад"))
        )
        binding.spinnerMouseMode.setSelection(
            resources.getStringArray(R.array.mouse_mode_options).indexOf(sharedPref.getString("mouse_mode", "Тачпад"))
        )
        binding.editIP.setText(sharedPref.getString("ip", ""))
        binding.editPort.setText(sharedPref.getString("port", ""))
    }
}

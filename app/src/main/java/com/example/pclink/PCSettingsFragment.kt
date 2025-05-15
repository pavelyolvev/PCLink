package com.example.pclink

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.navigation.fragment.findNavController
import com.example.pclink.databinding.PcsettingsBinding

class PCSettingsFragment : Fragment() {

    private var _binding: PcsettingsBinding? = null
    private val binding get() = _binding!!
    private var isNew: Boolean = false
    private var pcId: Int = -1
    private var loadedCode: Int = -1;
    val prefs = PreferencesFuncs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(arguments?.getInt("pcId") == -1)
            isNew = true
        else pcId = arguments?.getInt("pcId")!!

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = PcsettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun setSpinnerAdapter(spinner: Spinner, itemArrayR: Int){
        val adapterMode = ArrayAdapter.createFromResource(
            requireContext(),
            itemArrayR,
            R.layout.spinner_item
        )

// Задаём стиль выпадающего списка
        adapterMode.setDropDownViewResource(R.layout.spinner_item)
        spinner.adapter = adapterMode
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
                authCode = -1,
                mode = modeIndex,
                mouseMode = mouseModeIndex,
                macAdress = mac
            )

            val pcId = arguments?.getInt("pcId") ?: -1
            if (!isNew) {
                Log.d("DEBUG============", "PC UPDATE")
                updatedPC.authCode = loadedCode
                PreferencesFuncs().updatePC(requireContext(), pcId, updatedPC)
                findNavController().popBackStack()
            } else {
                // Показываем диалог авторизации

                Log.d("DEBUG============", "AUTHSTART SEND")
                val dialog = PcAuthDialog.newInstance(updatedPC) { authCode, mac ->
                    // Этот колбэк вызывается после успешной авторизации
                    updatedPC.authCode = authCode // Сохраняем полученный authCode в updatedPC
                    Log.d("DEBUG============", mac)
                    updatedPC.macAdress = mac
                    PreferencesFuncs().saveNewPC(requireContext(), updatedPC) // Сохраняем объект PC
                    findNavController().popBackStack() // Закрываем текущий экран
                }

                dialog.show(parentFragmentManager, "pc auth")
            }

        }
        delBtn.setOnClickListener {
            val pcId = arguments?.getInt("pcId") ?: -1
            if (!isNew && pcId != -1) {
                PreferencesFuncs().removePC(requireContext(), pcId)
            }
            findNavController().popBackStack()
        }
    }
    fun loadSaved() {
        val pc = prefs.loadPCPref(requireContext(), pcId)
        if (pc != null) {
            loadedCode = pc.authCode
            val modeOptions = resources.getStringArray(R.array.mode_options)
            val mouseModeOptions = resources.getStringArray(R.array.mouse_mode_options)

            // Проверяем и устанавливаем значение для первого spinner (mode)
            if (pc.mode in modeOptions.indices) {
                binding.spinnerMode.setSelection(pc.mode)
            } else {
                Log.e("PCSettingsFragment", "Invalid mode index: ${pc.mode}")
                binding.spinnerMode.setSelection(0) // Устанавливаем дефолтное значение
            }

            // Проверяем и устанавливаем значение для второго spinner (mouseMode)
            if (pc.mouseMode in mouseModeOptions.indices) {
                binding.spinnerMouseMode.setSelection(pc.mouseMode)
            } else {
                Log.e("PCSettingsFragment", "Invalid mouseMode index: ${pc.mouseMode}")
                binding.spinnerMouseMode.setSelection(0) // Устанавливаем дефолтное значение
            }
            binding.editIP.setText(pc.ip)
            binding.editPort.setText(pc.port.toString())
            binding.editName.setText(pc.name)
            binding.editMac.setText(pc.macAdress)
        }
    }
}

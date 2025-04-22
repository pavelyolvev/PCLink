package com.example.pclink

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pclink.databinding.PcNewBinding

class PCNewFragment : Fragment()  {
    private var _binding: PcNewBinding? = null
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
        _binding = PcNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
//        val backBtn: ImageButton = view.findViewById(R.id.btnBack)
        val saveBtn: Button = view.findViewById(R.id.btnSave)

        if (isNew) saveBtn.visibility = View.VISIBLE

//        backBtn.setOnClickListener {
//            findNavController().popBackStack()
//        }

        saveBtn.setOnClickListener {
            // Сохраняем настройки, потом возвращаемся
            val pc = PreferencesFuncs.PC(
                name = binding.editName.text.toString(),
                ip = binding.editIP.text.toString(),
                port = binding.editPort.text.toString().toIntOrNull() ?: 12312,
                mode = binding.spinnerMode.selectedItemPosition,
                mouseMode = binding.spinnerMouseMode.selectedItemPosition
            )

            val prefs = PreferencesFuncs()
            prefs.saveNewPC(requireContext(), pc)
            findNavController().popBackStack()
        }
    }
}
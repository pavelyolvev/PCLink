package com.example.pclink

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

        if (isNew) saveBtn.visibility = View.VISIBLE

//        backBtn.setOnClickListener {
//            findNavController().popBackStack()
//        }

        saveBtn.setOnClickListener {
            // Сохраняем настройки, потом возвращаемся
            findNavController().popBackStack()
        }
    }
}

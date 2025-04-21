package com.example.pclink;

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.navigation.fragment.findNavController
import com.example.pclink.databinding.PcselectBinding

class PCSelectFragment : Fragment() {

    private var _binding: PcselectBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = PcselectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val connectBtn: Button = view.findViewById(R.id.btnConnect)
        val settingsBtn: Button = view.findViewById(R.id.btnSettings)
        val addBtn: ImageButton = view.findViewById(R.id.btnAddPC)

        connectBtn.setOnClickListener {
            val bundle = Bundle().apply {
                putBoolean("isNew", true)
            }
            findNavController().navigate(R.id.action_PCSelectFragment_to_PCStreamFragment, bundle)
        }

        settingsBtn.setOnClickListener {
            val bundle = Bundle().apply {
                putBoolean("isNew", true)
            }
            findNavController().navigate(R.id.action_PCSelectFragment_to_PCSettingsFragment, bundle)
        }
    }
}


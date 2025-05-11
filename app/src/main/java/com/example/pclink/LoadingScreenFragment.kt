package com.example.pclink

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.pclink.databinding.LoadingScreenBinding
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoadingScreenFragment : Fragment() {
    private var _binding: LoadingScreenBinding? = null
    private val binding get() = _binding!!
    private var pcId: Int = -1;
    val net = NetworkLink()
    val prefs = PreferencesFuncs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pcId = arguments?.getInt("pcId") ?: -1

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = LoadingScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val pc = prefs.loadPCPref(requireContext(), pcId)
        val serverIpForCommands = pc?.ip
        val serverPortForCommands = pc?.port
        val authCode = pc?.authCode
        var serverResponse: JSONObject
        super.onViewCreated(view, savedInstanceState)
        if (serverIpForCommands != null && serverPortForCommands != null && authCode != null) {
            lifecycleScope.launch {
                serverResponse = net.requestAccess(serverIpForCommands, serverPortForCommands, "REQUEST_ACCESS", arrayOf(authCode.toString()))
                if(serverResponse.getString("message") == "ACCESS_GRANTED") {
                    val bundle = Bundle().apply {
                        putInt("pcId", pcId)
                    }
                    findNavController().navigate(R.id.action_loadingScreenFragment_to_PCStreamFragment, bundle, navOptions {
                        popUpTo(R.id.loadingScreenFragment) {
                            inclusive = true // удалит LoadingScreenFragment из стека
                        }
                    })
                } else if (serverResponse.getString("message") == "ACCESS_DENIED"){
                    findNavController().popBackStack()
                    Toast.makeText(context, "Код доступа неверный. Пожалуйста, добавьте пк заново", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
}
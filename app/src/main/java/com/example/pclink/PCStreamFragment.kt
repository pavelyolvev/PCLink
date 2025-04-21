package com.example.pclink

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.navigation.fragment.findNavController

class PCStreamFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.pcstream, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        val backBtn: ImageButton = view.findViewById(R.id.btnBackStream)
//
//        backBtn.setOnClickListener {
//            findNavController().popBackStack()
//        }
    }
}

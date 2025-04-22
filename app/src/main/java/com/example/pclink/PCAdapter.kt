package com.example.pclink

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pclink.PreferencesFuncs.PC

class PCListAdapter(
    private val pcList: List<PC>,
    private val onConnectClick: (Int) -> Unit,
    private val onSettingsClick: (Int) -> Unit
) : RecyclerView.Adapter<PCListAdapter.PCViewHolder>() {

    inner class PCViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.pcName)
        val statusText: TextView = itemView.findViewById(R.id.pcStatus)
        val btnConnect: Button = itemView.findViewById(R.id.btnConnect)
        val btnSettings: Button = itemView.findViewById(R.id.btnSettings)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PCViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pc_item_layout, parent, false)
        return PCViewHolder(view)
    }

    override fun onBindViewHolder(holder: PCViewHolder, position: Int) {
        val pc = pcList[position]
        holder.nameText.text = pc.name
        holder.statusText.text = "Онлайн" // или ваша логика

        holder.btnConnect.setOnClickListener {
            onConnectClick(position)
        }

        holder.btnSettings.setOnClickListener {
            onSettingsClick(position)
        }
    }

    override fun getItemCount() = pcList.size
}

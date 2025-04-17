package com.example.pclink;

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.google.gson.Gson
import androidx.core.content.edit
import com.google.gson.reflect.TypeToken


public class PreferencesFuncs {
    data class PC(val name: String, val ip: String, val mode: Int)


    fun savePCPref(context: Context, pcs: List<PC>){
        val sharedPreferences = context.getSharedPreferences("PCs", MODE_PRIVATE)
        sharedPreferences.edit() {
            val gson = Gson()
            val json = gson.toJson(pcs)
            putString("pc_list", json)
        }
    }
    fun loadPCPref(context: Context, id: Int): PC? {
        val sharedPreferences = context.getSharedPreferences("PCs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("pc_list", null)

        return if (json != null) {
            val type = object : TypeToken<List<PC>>() {}.type
            val list: List<PC> = gson.fromJson(json, type)
            list.getOrNull(id) // безопасно получить элемент по индексу
        } else {
            null
        }
    }

}

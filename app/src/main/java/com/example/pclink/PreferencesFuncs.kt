package com.example.pclink

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesFuncs {

    data class PC(
        val name: String,
        val ip: String,
        val port: Int,
        val mode: Int,
        val mouseMode: Int
    )

    // Сохраняет список ПК в SharedPreferences
    private fun savePCList(context: Context, pcs: List<PC>) {
        val sharedPreferences = context.getSharedPreferences("PCs", MODE_PRIVATE)
        val gson = Gson()
        val json = gson.toJson(pcs)
        sharedPreferences.edit {
            putString("pc_list", json)
        }
    }
    // Обновляет ПК по индексу в списке
    fun updatePC(context: Context, id: Int, updatedPC: PC) {
        val list = loadAllPCsPrefs(context).toMutableList()
        if (id in list.indices) {
            list[id] = updatedPC
            savePCList(context, list)
        }
    }
    // Добавляет новый ПК в список и сохраняет
    fun saveNewPC(context: Context, newPC: PC) {
        val currentList = loadAllPCsPrefs(context).toMutableList()
        currentList.add(newPC)
        savePCList(context, currentList)
    }

    // Загружает ПК по индексу
    fun loadPCPref(context: Context, id: Int): PC? {
        val list = loadAllPCsPrefs(context)
        return list.getOrNull(id)
    }

    // Загружает весь список ПК
    fun loadAllPCsPrefs(context: Context): List<PC> {
        val sharedPreferences = context.getSharedPreferences("PCs", MODE_PRIVATE)
        val json = sharedPreferences.getString("pc_list", null) ?: return emptyList()
        val type = object : TypeToken<List<PC>>() {}.type
        return Gson().fromJson(json, type)
    }
}

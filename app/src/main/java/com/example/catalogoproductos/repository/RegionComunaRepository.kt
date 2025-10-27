package com.example.catalogoproductos.repository

import android.content.Context
import com.example.catalogoproductos.model.RegionComuna
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RegionComunaRepository {
    fun cargarDesdeAssets(context: Context, filename: String = "ComunaRegion.json"): List<RegionComuna> {
        return try {
            val json = context.assets.open(filename).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<RegionComuna>>() {}.type
            Gson().fromJson<List<RegionComuna>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

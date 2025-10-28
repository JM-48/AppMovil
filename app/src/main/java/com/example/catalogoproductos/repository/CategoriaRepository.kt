package com.example.catalogoproductos.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CategoriaRepository {
    fun obtenerCategoriasDesdeAssets(context: Context, filename: String = "categorias.json"): List<String> {
        return try {
            val json = context.assets.open(filename).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson<List<String>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

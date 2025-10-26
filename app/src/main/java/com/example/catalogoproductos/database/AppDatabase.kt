package com.example.catalogoproductos.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.catalogoproductos.model.Direccion
import com.example.catalogoproductos.model.ItemCarrito
import com.example.catalogoproductos.model.PerfilUsuario

@Database(
    entities = [PerfilUsuario::class, ItemCarrito::class, Direccion::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun perfilUsuarioDao(): PerfilUsuarioDao
    abstract fun itemCarritoDao(): ItemCarritoDao
    abstract fun direccionDao(): DireccionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "catalogo_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
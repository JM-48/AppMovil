package com.example.catalogoproductos.database

import androidx.room.*
import com.example.catalogoproductos.model.ItemCarrito
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemCarritoDao {
    @Query("SELECT * FROM items_carrito WHERE emailUsuario = :email")
    fun getItemsByUsuario(email: String): Flow<List<ItemCarrito>>

    @Query("SELECT * FROM items_carrito WHERE id = :id")
    suspend fun getItemById(id: Int): ItemCarrito?

    @Query("SELECT * FROM items_carrito WHERE productoId = :productoId AND emailUsuario = :email")
    suspend fun getItemByProductoId(productoId: Int, email: String): ItemCarrito?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemCarrito): Long

    @Update
    suspend fun updateItem(item: ItemCarrito)

    @Delete
    suspend fun deleteItem(item: ItemCarrito)

    @Query("DELETE FROM items_carrito WHERE emailUsuario = :email")
    suspend fun clearCarrito(email: String)
}
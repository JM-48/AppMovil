package com.example.catalogoproductos.repository

import com.example.catalogoproductos.database.ItemCarritoDao
import com.example.catalogoproductos.model.ItemCarrito
import kotlinx.coroutines.flow.Flow

class CarritoRepository(private val itemCarritoDao: ItemCarritoDao) {
    
    fun getItemsCarrito(email: String): Flow<List<ItemCarrito>> {
        return itemCarritoDao.getItemsByUsuario(email)
    }
    
    suspend fun agregarAlCarrito(item: ItemCarrito): Long {
        val existingItem = itemCarritoDao.getItemByProductoId(item.productoId, item.emailUsuario)
        return if (existingItem != null) {
            val updatedItem = existingItem.copy(cantidad = existingItem.cantidad + item.cantidad)
            itemCarritoDao.updateItem(updatedItem)
            existingItem.id.toLong()
        } else {
            itemCarritoDao.insertItem(item)
        }
    }
    
    suspend fun actualizarCantidad(id: Int, cantidad: Int) {
        val item = itemCarritoDao.getItemById(id)
        item?.let {
            val updatedItem = it.copy(cantidad = cantidad)
            itemCarritoDao.updateItem(updatedItem)
        }
    }
    
    suspend fun eliminarItem(id: Int) {
        val item = itemCarritoDao.getItemById(id)
        item?.let {
            itemCarritoDao.deleteItem(it)
        }
    }
    
    suspend fun vaciarCarrito(email: String) {
        itemCarritoDao.clearCarrito(email)
    }
}
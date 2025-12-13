package com.example.catalogoproductos

import com.example.catalogoproductos.model.Producto
import com.example.catalogoproductos.viewmodel.CatalogoViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CatalogoViewModelTest {
    @Test
    fun `buscarProductoPorId retorna el producto correcto`() {
        val vm = CatalogoViewModel()
        val field = CatalogoViewModel::class.java.getDeclaredField("_productos")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val flow = field.get(vm) as kotlinx.coroutines.flow.MutableStateFlow<List<Producto>>
        val list = listOf(
            Producto(
                id = 1,
                nombre = "A",
                descripcion = "desc",
                precio = 1000,
                precioOriginal = null,
                imagen = null,
                stock = 1,
                tipo = "Cat"
            ),
            Producto(
                id = 2,
                nombre = "B",
                descripcion = "desc",
                precio = 2000,
                precioOriginal = null,
                imagen = null,
                stock = 2,
                tipo = "Cat"
            )
        )
        flow.value = list

        assertEquals("A", vm.buscarProductoPorId(1)?.nombre)
        assertEquals("B", vm.buscarProductoPorId(2)?.nombre)
        assertNull(vm.buscarProductoPorId(999))
    }
}

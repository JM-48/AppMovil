package com.example.catalogoproductos

import com.example.catalogoproductos.database.ItemCarritoDao
import com.example.catalogoproductos.model.ItemCarrito
import com.example.catalogoproductos.model.Producto
import com.example.catalogoproductos.repository.CarritoRepository
import com.example.catalogoproductos.viewmodel.CarritoViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeItemCarritoDao : ItemCarritoDao {
    private var autoId = 1
    private val items = mutableListOf<ItemCarrito>()
    private val flow = MutableStateFlow<List<ItemCarrito>>(emptyList())

    override fun getItemsByUsuario(email: String): Flow<List<ItemCarrito>> = flow
    override suspend fun getItemById(id: Int): ItemCarrito? = items.find { it.id == id }
    override suspend fun getItemByProductoId(productoId: Int, email: String): ItemCarrito? = items.find { it.productoId == productoId && it.emailUsuario == email }
    override suspend fun insertItem(item: ItemCarrito): Long {
        val newItem = item.copy(id = autoId++)
        items.add(newItem)
        flow.value = items.toList()
        return newItem.id.toLong()
    }
    override suspend fun updateItem(item: ItemCarrito) {
        val idx = items.indexOfFirst { it.id == item.id }
        if (idx >= 0) items[idx] = item
        flow.value = items.toList()
    }
    override suspend fun deleteItem(item: ItemCarrito) { items.removeIf { it.id == item.id }; flow.value = items.toList() }
    override suspend fun clearCarrito(email: String) { items.removeIf { it.emailUsuario == email }; flow.value = items.toList() }
}

class CarritoViewModelTest {
    @org.junit.Rule
    @JvmField
    val mainDispatcherRule = MainDispatcherRule()
    @Test
    fun `agregar producto actualiza cantidad y total`() = runTest {
        val repo = CarritoRepository(FakeItemCarritoDao())
        val vm = CarritoViewModel(repo, "user@example.com")
        val p = Producto(
            id = 1,
            nombre = "Juego",
            descripcion = "Desc",
            precio = 10000,
            precioOriginal = null,
            imagen = null,
            stock = 10,
            tipo = "Gaming"
        )
        vm.agregarProductoAlCarrito(p, cantidad = 2)
        Thread.sleep(50)
        assertEquals(2, vm.cantidadItems.value)
        // Total en CLP sin decimales; valida que contiene "20.000"
        assert(vm.totalCarrito.value.contains("20.000"))
    }

    @Test
    fun `actualizar cantidad a cero elimina`() = runTest {
        val repo = CarritoRepository(FakeItemCarritoDao())
        val vm = CarritoViewModel(repo, "user@example.com")
        val p = Producto(
            id = 2,
            nombre = "Mouse",
            descripcion = "Desc",
            precio = 5000,
            precioOriginal = null,
            imagen = null,
            stock = 10,
            tipo = "Accesorios"
        )
        vm.agregarProductoAlCarrito(p, cantidad = 1)
        Thread.sleep(10)
        val itemId = vm.itemsCarrito.value.first().id
        vm.actualizarCantidad(itemId, 0)
        Thread.sleep(10)
        assertEquals(0, vm.itemsCarrito.value.size)
    }

    @Test
    fun `vaciar carrito limpia items`() = runTest {
        val repo = CarritoRepository(FakeItemCarritoDao())
        val vm = CarritoViewModel(repo, "user@example.com")
        vm.agregarAlCarrito(
            ItemCarrito(
                id = 0,
                productoId = 3,
                nombre = "Teclado",
                precio = 20000,
                imagen = "",
                cantidad = 1,
                emailUsuario = "user@example.com"
            )
        )
        Thread.sleep(10)
        vm.vaciarCarrito()
        kotlinx.coroutines.delay(10)
        assertEquals(0, vm.cantidadItems.value)
    }

    @Test
    fun `calculo de total para multiples items`() = runTest {
        val repo = CarritoRepository(FakeItemCarritoDao())
        val vm = CarritoViewModel(repo, "user@example.com")
        vm.agregarAlCarrito(
            ItemCarrito(
                id = 0,
                productoId = 5,
                nombre = "Pad",
                precio = 5000,
                imagen = "",
                cantidad = 2,
                emailUsuario = "user@example.com"
            )
        )
        vm.agregarAlCarrito(
            ItemCarrito(
                id = 0,
                productoId = 6,
                nombre = "Cable",
                precio = 3000,
                imagen = "",
                cantidad = 3,
                emailUsuario = "user@example.com"
            )
        )
        kotlinx.coroutines.delay(10)
        // Total debería contener 19.000 (2*5000 + 3*3000)
        assert(vm.totalCarrito.value.contains("19.000"))
    }
}

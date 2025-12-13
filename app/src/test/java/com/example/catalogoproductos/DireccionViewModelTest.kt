package com.example.catalogoproductos

import com.example.catalogoproductos.database.DireccionDao
import com.example.catalogoproductos.model.Direccion
import com.example.catalogoproductos.repository.DireccionRepository
import com.example.catalogoproductos.viewmodel.DireccionViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeDireccionDao : DireccionDao {
    private val data = mutableListOf<Direccion>()
    private val flow = MutableStateFlow<List<Direccion>>(emptyList())
    private val defaultFlow = MutableStateFlow<Direccion?>(null)

    override fun getDireccionesByUsuario(email: String): Flow<List<Direccion>> = flow
    override fun getDireccionDefaultByUsuario(email: String): Flow<Direccion?> = defaultFlow
    override suspend fun insertDireccion(direccion: Direccion): Long {
        data.add(direccion)
        flow.value = data.toList()
        if (direccion.esDefault) defaultFlow.value = direccion
        return data.size.toLong()
    }
    override suspend fun updateDireccion(direccion: Direccion) { /* no-op for test */ }
    override suspend fun deleteDireccion(direccion: Direccion) { data.remove(direccion); flow.value = data.toList() }
    override suspend fun clearDefaultByUsuario(email: String) { defaultFlow.value = null }
    override suspend fun getDireccionDefaultOnceByUsuario(email: String): Direccion? = defaultFlow.value
    override suspend fun insertDireccionAsDefault(email: String, direccion: Direccion): Long {
        clearDefaultByUsuario(email)
        return insertDireccion(direccion.copy(esDefault = true))
    }
}

class DireccionViewModelTest {
    private fun vm(): DireccionViewModel = DireccionViewModel(DireccionRepository(FakeDireccionDao()))

    @Test
    fun `calle obligatoria`() {
        val v = vm()
        v.updateCalle("")
        assertEquals("La calle es obligatoria", v.calleError)
        v.updateCalle("Av")
        assertEquals("La calle debe tener al menos 3 caracteres", v.calleError)
        v.updateCalle("Avenida")
        assertNull(v.calleError)
    }

    @Test
    fun `numero obligatorio`() {
        val v = vm()
        v.updateNumero("")
        assertEquals("El número es obligatorio", v.numeroError)
        v.updateNumero("123")
        assertNull(v.numeroError)
    }

    @Test
    fun `ciudad valida`() {
        val v = vm()
        v.updateCiudad("")
        assertEquals("La ciudad es obligatoria", v.ciudadError)
        v.updateCiudad("Vi")
        assertEquals("La ciudad debe tener al menos 3 caracteres", v.ciudadError)
        v.updateCiudad("Viña del Mar")
        assertNull(v.ciudadError)
        v.updateCiudad("Valp0")
        assertEquals("La ciudad solo debe contener letras", v.ciudadError)
    }

    @Test
    fun `provincia valida`() {
        val v = vm()
        v.updateProvincia("")
        assertEquals("La provincia es obligatoria", v.provinciaError)
        v.updateProvincia("Me")
        assertEquals("La provincia debe tener al menos 3 caracteres", v.provinciaError)
        v.updateProvincia("Metropolitana")
        assertNull(v.provinciaError)
    }

    @Test
    fun `codigo postal valido`() {
        val v = vm()
        v.updateCodigoPostal("")
        assertEquals("El código postal es obligatorio", v.codigoPostalError)
        v.updateCodigoPostal("1234")
        assertEquals("El código postal debe tener 5 dígitos", v.codigoPostalError)
        v.updateCodigoPostal("12345")
        assertNull(v.codigoPostalError)
    }

    @Test
    fun `telefono valido`() {
        val v = vm()
        v.updateTelefono("")
        assertEquals("El teléfono es obligatorio", v.telefonoError)
        v.updateTelefono("1234567")
        assertEquals("El teléfono debe tener al menos 9 dígitos", v.telefonoError)
        v.updateTelefono("123456789")
        assertNull(v.telefonoError)
        v.updateTelefono("12345abcd")
        assertEquals("El teléfono solo debe contener números", v.telefonoError)
    }

    @Test
    fun `guardar direccion invalida muestra errorMessage`() {
        val v = vm()
        v.updateCalle("")
        v.guardarDireccion("user@example.com")
        assertEquals("Por favor, corrija los errores en el formulario", v.errorMessage)
    }
}

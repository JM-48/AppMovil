package com.example.catalogoproductos

import com.example.catalogoproductos.viewmodel.UsuariosViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UsuariosViewModelTest {
    @Test
    fun `validaciones muestran errores apropiados`() {
        val vm = UsuariosViewModel()
        vm.updateEmail("")
        vm.updateNombre("A")
        vm.updatePassword("123")
        vm.updateRole("")

        // Email obligatorio
        assertEquals("El email es obligatorio", vm.emailError)
        // Nombre muy corto
        assertEquals("El nombre debe tener al menos 2 caracteres", vm.nombreError)
        // Password demasiado corta
        assertEquals("La contraseña debe tener al menos 6 caracteres", vm.passwordError)
        // Rol obligatorio
        assertEquals("El rol es obligatorio", vm.roleError)

        // Corregir
        vm.updateEmail("user@example.com")
        vm.updateNombre("Juan")
        vm.updatePassword("123456")
        vm.updateRole("USER")

        assertNull(vm.emailError)
        assertNull(vm.nombreError)
        assertNull(vm.passwordError)
        assertNull(vm.roleError)
    }

    @Test
    fun `errores en vivo tras updates`() {
        val vm = UsuariosViewModel()
        vm.updateEmail("")
        vm.updateNombre("")
        vm.updatePassword("1")
        vm.updateRole("")
        assertEquals("El email es obligatorio", vm.emailError)
        assertEquals("El nombre es obligatorio", vm.nombreError)
        assertEquals("La contraseña debe tener al menos 6 caracteres", vm.passwordError)
        assertEquals("El rol es obligatorio", vm.roleError)
    }
}

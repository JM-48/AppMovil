package com.example.catalogoproductos

import com.example.catalogoproductos.viewmodel.AuthViewModel
import org.junit.Assert.assertNull
import org.junit.Test

class AuthViewModelLogicTest {
    @Test
    fun `logout limpia el estado de sesión`() {
        val vm = AuthViewModel()
        vm.token.value = "tok"
        vm.usuarioActual.value = "user@example.com"
        vm.role.value = "ADMIN"
        vm.esAdministrador.value = true

        vm.logout()

        assertNull(vm.token.value)
        assertNull(vm.usuarioActual.value)
        org.junit.Assert.assertEquals("CLIENT", vm.role.value)
    }
}

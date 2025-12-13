package com.example.catalogoproductos

import com.example.catalogoproductos.viewmodel.NoticiasViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Test

class NoticiasViewModelTest {
    @org.junit.Rule
    @JvmField
    val mainDispatcherRule = MainDispatcherRule()
    @Test
    fun `api key vacia produce error`() = runTest {
        val vm = NoticiasViewModel(apiKeyProvider = { "" })
        vm.buscarNoticias()
        Thread.sleep(10)
        assertNotNull(vm.error.value)
    }

    @Test
    fun `updateQuery cambia el término de búsqueda`() {
        val vm = NoticiasViewModel(apiKeyProvider = { "" })
        vm.updateQuery("tecnologia")
        assert(vm.query == "tecnologia")
    }
}

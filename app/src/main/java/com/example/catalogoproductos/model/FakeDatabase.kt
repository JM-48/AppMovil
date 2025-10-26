package com.example.catalogoproductos.model

object FakeDatabase {
    private val usuarios = mutableListOf<Usuario>()
    private val administradores = mutableListOf<Administrador>()
    private val productos = mutableListOf<Producto>()

    init {
        // Productos iniciales (alineados con el modelo Producto)
        productos.add(Producto(id = 1, nombre = "Smartphone XYZ", descripcion = "Smartphone de última generación", precio = 599, imagen = "https://picsum.photos/id/1/200", stock = 50))
        productos.add(Producto(id = 2, nombre = "Laptop Pro", descripcion = "Laptop para profesionales", precio = 1299, imagen = "https://picsum.photos/id/2/200", stock = 25))
        productos.add(Producto(id = 3, nombre = "Auriculares Bluetooth", descripcion = "Auriculares inalámbricos de alta calidad", precio = 99, imagen = "https://picsum.photos/id/3/200", stock = 100))
    }

    fun registrar(usuario: Usuario): Boolean {
        if (usuarios.any { it.email == usuario.email }) return false
        usuarios.add(usuario)
        return true
    }

    fun login(email: String, password: String): Boolean {
        return usuarios.any { it.email == email && it.password == password }
    }
    
    fun registrarAdministrador(admin: Administrador): Boolean {
        if (administradores.any { it.email == admin.email }) return false
        administradores.add(admin)
        return true
    }
    
    fun loginAdministrador(email: String, password: String): Boolean {
        return administradores.any { it.email == email && it.password == password }
    }
    
    fun existeAdministrador(email: String): Boolean {
        return administradores.any { it.email == email }
    }
    
    fun getProductos(): List<Producto> = productos.toList()
    
    fun getProducto(id: Int): Producto? = productos.find { it.id == id }
    
    fun agregarProducto(producto: Producto) {
        val nuevoId = if (productos.isEmpty()) 1 else (productos.maxOf { it.id } + 1)
        productos.add(producto.copy(id = nuevoId))
    }
    
    fun actualizarProducto(producto: Producto) {
        val index = productos.indexOfFirst { it.id == producto.id }
        if (index != -1) {
            productos[index] = producto
        }
    }
    
    fun eliminarProducto(id: Int) {
        productos.removeIf { it.id == id }
    }
}
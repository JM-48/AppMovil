## Driftx

## Jean Morales

## Descripción Breve
Aplicación móvil Android (Jetpack Compose + Kotlin) para explorar un catálogo de productos, agregar items al carrito y realizar una compra simulada. Mantiene arquitectura MVVM, persistencia con Room/DataStore y consumo de API con Retrofit/OkHttp y JWT para rutas protegidas. Incluye control de acceso por roles para pantallas administrativas.

## Cómo Funciona
- Inicio de sesión y registro con validaciones. Persiste sesión (token y rol) en DataStore.
- Catálogo y detalle de productos consumidos desde API pública; imágenes y precios formateados en CLP.
- Carrito de compras con total acumulado y flujo de checkout: dirección de envío, confirmación y boleta con desglose de IVA solo después de la compra.
- Perfil de usuario: sincroniza datos desde `GET /users/me` cuando hay token, editable localmente.
- Administración basada en rol:
  - `ADMIN`: acceso a productos y usuarios.
  - `USER_AD`: solo usuarios.
  - `PROD_AD`: solo productos.
  - `CLIENT`: uso general, sin acceso a backoffice.

## Arquitectura
- MVVM con `ViewModel` + `StateFlow` para estado reactivo.
- Persistencia local: Room (`PerfilUsuario`, `ItemCarrito`, `Direccion`).
- Sesión: DataStore para `token` y `role`.
- Red: Retrofit + OkHttp con timeouts 60s, `retryOnConnectionFailure`, forzado a HTTP/1.1 y `ConnectionSpec.MODERN_TLS`.
- Autenticación: JWT en rutas protegidas.
- UI: Jetpack Compose, Coil para imágenes, formateo CLP sin decimales.

## Credenciales de Admin (demo)
- Email: `admin@catalogo.com`
- Password: `admin123`

## Ejecución
- Ejecutar en Android Studio (Run). Requiere JDK 11+.
- Ejecutar en celular por medio de apk

## Endpoints
- Base: `https://apitest-1-95ny.onrender.com/`

- Públicos:
  - `GET /productos` → lista de productos.
  - `GET /productos/{id}/precio` → desglose de precio (`precioOriginal`, `precioConIva`, `ivaPorcentaje`, `ivaMonto`).

- Autenticación y Perfil:
  - `POST /auth/register` → registro (sin token).
  - `POST /auth/login` → login, devuelve `token`, `role`, `email` (sin token).
  - `GET /users/me` → perfil del usuario autenticado (Bearer).
  - `PATCH /users/me` → actualiza campos del perfil que cambiaron (Bearer).

- Usuarios (administración; `ADMIN`, `USER_AD`):
  - `GET /users` → listar usuarios (Bearer).
  - `POST /users` → crear usuario (Bearer).
  - `PUT /users/{id}` → actualizar usuario (Bearer).
  - `DELETE /users/{id}` → eliminar usuario (Bearer).

- Productos (administración; `ADMIN`, `PROD_AD`):
  - `POST /productos` → crear producto (Bearer).
  - `PUT /productos/{id}` → actualizar producto (Bearer).
  - `DELETE /productos/{id}` → eliminar producto (Bearer).

- Subir imágenes:
  - `POST /imagenes` (multipart/form-data) → devuelve `{"url": "https://..."}`.

- Noticias (tercero):
  - `GET https://gnews.io/api/v4/search?q={q}&lang=es&max=10&apikey={API_KEY}`

## Ejemplos de Uso (HTTP)
- Login:
  ```
  POST /auth/login
  Content-Type: application/json
  {
    "email": "user@example.com",
    "password": "123456"
  }
  // Respuesta:
  // { "token": "...", "role": "CLIENT", "email": "user@example.com" }
  ```

- Autorización:
  ```
  Authorization: Bearer <TOKEN>
  ```

- Crear Usuario (ADMIN/USER_AD):
  ```
  POST /users
  Authorization: Bearer <TOKEN>
  Content-Type: application/json
  {
    "email": "nuevo@example.com",
    "password": "123456",
    "nombre": "Juan",
    "apellido": "Pérez",
    "telefono": "987654321",
    "direccion": "Calle 123, Dpto 45",
    "ciudad": "Valparaíso",
    "region": "Valparaíso",
    "codigoPostal": "12345",
    "role": "CLIENT"
  }
  ```

- Crear Producto (ADMIN/PROD_AD):
  ```
  POST /productos
  Authorization: Bearer <TOKEN>
  Content-Type: application/json
  {
    "nombre": "Mouse Gamer",
    "descripcion": "Sensor óptico",
    "precio": 19990.0,
    "tipo": "Accesorios",
    "imagenUrl": "https://...",
    "stock": 10
  }
  ```

- Desglose de Precio:
  ```
  GET /productos/123/precio
  // Respuesta:
  // { "precioOriginal": 10000.0, "precioConIva": 11900.0, "ivaPorcentaje": 0.19, "ivaMonto": 1900.0 }
  ```

## Pantallas
- Login/Registro:
  - Validaciones en vivo; contraseña oculta”; Enter confirma.
  - Persistencia de token y rol en DataStore. Manejo de errores: `401` → “Token requerido”, `403` → “Acceso denegado”.

- Catálogo:
  - Búsqueda por nombre y categoría. Lista y detalle con imágenes y precio en CLP.
  - Botón “Agregar al carrito” funcional.

- Carrito:
  - Items agregados con cantidades; total acumulado (`CurrencyUtils`); vaciar carrito.
  - Actualización de cantidades y eliminación de items.

- Dirección:
  - Formulario de envío con validaciones (calle, número, ciudad, provincia, código postal, teléfono).
  - Si el usuario no tiene dirección predeterminada, la primera guardada se marca como default.
  - Se removió el checkbox “Establecer como dirección predeterminada” en la compra.

- Confirmación de Compra:
  - Boleta con desglose: `precio sin IVA`, `IVA`, `precio final con IVA` solo después de la compra.

- Perfil:
  - Carga `GET /users/me` y prellena datos; `PATCH /users/me` envía solo campos cambiados.

- BackOffice:
  - Control por rol. `ADMIN` ve y gestiona usuarios y productos. `USER_AD` solo usuarios. `PROD_AD` solo productos. `CLIENT` no accede.
  - CRUD de productos: crear/editar/eliminar con JWT.

## Lógica de Negocio
- Roles válidos: `ADMIN`, `USER_AD`, `PROD_AD`, `CLIENT`. Si llega `USER`, se normaliza a `CLIENT`.
- Rutas protegidas requieren JWT. UI oculta acciones no permitidas según rol.
- Precios:
  - `ProductoDTO.precio` se interpreta con IVA incluido (19%).
  - Desglose oficial vía `GET /productos/{id}/precio`.
  - Formateo en CLP sin decimales y validaciones por substring en pruebas.

## Pruebas
- Comando: `gradlew test`
- Cobertura: 24 pruebas, 0 fallos (validaciones de formularios, carrito, catálogo, autenticación, markdown y formato de moneda).

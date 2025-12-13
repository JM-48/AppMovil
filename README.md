# Catálogo de Productos

## Jean Morales

## Descripción
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

## Credenciales de Admin (demo)
- Email: `admin@catalogo.com`
- Password: `admin123`

## Ejecución y Pruebas
- Ejecutar en Android Studio (Run). Requiere JDK 11+.
- Pruebas unitarias: `gradlew test`. 

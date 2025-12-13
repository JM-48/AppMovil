# Instructivo de Pruebas (Unitarias)

## Cómo Ejecutarlas
- Requisitos: JDK 11+, Gradle y (opcional) Android SDK para el proyecto; las pruebas unitarias no requieren SDK.
- Comando:
  - Windows: `gradlew test`
  - Mac/Linux: `./gradlew test`

## Qué Esperar
- Resultado: todas las pruebas deben pasar.
- Resumen esperado: 24 pruebas, 0 fallos.

## Paquetes y Archivos de Prueba
- `app/src/test/java/com/example/catalogoproductos/UsuariosViewModelTest.kt`
  - Valida errores en vivo: email, nombre, password y rol.
  - Verifica corrección al ingresar datos válidos.
  - Resultado: debe pasar (errores presentes cuando corresponde y nulos cuando se corrigen).

- `app/src/test/java/com/example/catalogoproductos/DireccionViewModelTest.kt`
  - `calle obligatoria`, `numero obligatorio`, `ciudad valida`, `provincia valida`, `codigo postal valido`, `telefono valido`.
  - `guardar direccion invalida muestra errorMessage` (sin tocar DB real).
  - Resultado: todas pasan, errores correctos y mensaje de formulario inválido.

- `app/src/test/java/com/example/catalogoproductos/CarritoViewModelTest.kt`
  - `agregar producto actualiza cantidad y total`.
  - `actualizar cantidad a cero elimina`.
  - `vaciar carrito limpia items`.
  - `calculo de total para multiples items`.
  - Resultado: deben pasar; el total contiene montos formateados (`20.000`, `19.000`).

- `app/src/test/java/com/example/catalogoproductos/NoticiasViewModelTest.kt`
  - `api key vacia produce error` (sin consumir red real).
  - `updateQuery cambia el término de búsqueda`.
  - Resultado: error no nulo con API key vacía; `query` actualizado.

- `app/src/test/java/com/example/catalogoproductos/AuthViewModelLogicTest.kt`
  - `logout limpia el estado de sesión`.
  - Resultado: token y usuario quedan en `null`; rol queda en `CLIENT`.

- `app/src/test/java/com/example/catalogoproductos/MarkdownRegexTest.kt`
  - `negrita con regex triple comillas`: usa `"""\*\*(.*?)\*\*"""`.
  - `cursiva`: `_texto_` → `<i>texto</i>`.
  - `encabezados y lista`: `#` y `-`.
  - Resultado: todas pasan; conversiones correctas.

## Notas Técnicas
- Se usan fakes in-memory para `ItemCarritoDao` y `DireccionDao` evitando Room/DB.
- Se emplea `kotlinx-coroutines-test` y regla `MainDispatcherRule` con `UnconfinedTestDispatcher` para evitar dependencias de `Looper` y ejecutar corrutinas en pruebas unitarias.
- No hay acceso a UI ni Android framework en estas pruebas.

## Troubleshooting
- Formato de moneda: las pruebas validan substrings (`20.000`, `19.000`, `10.000`); evitan dependencia del locale.
- Corrutinas/Looper: asegure que la regla `MainDispatcherRule` esté presente; de lo contrario, aparecerán errores de `Looper` en `viewModelScope`.
- Ejecución de Gradle: verifique permisos de `gradlew` (Mac/Linux) y que JDK 11 esté activo.

## Resultado Esperado
- Al ejecutar `gradlew test`, el resultado esperado es: `BUILD SUCCESSFUL` y reporte con 24 pruebas, 0 fallos.

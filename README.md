# Motor de Base de Datos — Interfaz web (UI) y Motor Simulado

Este proyecto es una pequeña simulación de un motor de base de datos escrita en Java y empacada con Spring Boot, junto con una interfaz web estática (HTML/JS) que actúa como fachada para realizar operaciones sobre tablas, índices y registros.

El propósito es ofrecer una UI simple para crear tablas, insertar/editar/eliminar registros y crear índices (basados en una estructura AVL implementada en el paquete `service.engine`).

---

## Contenido del repositorio

- `pom.xml` — configuración Maven / Spring Boot.
- `src/main/java/com/example/demo` — código Java del backend.
  - `controller/RestController.java` — API REST que expone operaciones del motor bajo `/api/*`.
  - `service/service/BDService.java` — envoltorio (`wrapper`) que usa `DataBaseEngine`.
  - `service/engine` — implementación del motor: `DataBaseEngine`, `Table`, `Record`, `AVLTree`, `AVLNode`.
  - `config/CorsConfig.java` — (opcional) configuración global CORS para desarrollo.
- `src/main/resources/static` — UI estática servida por Spring Boot:
  - `index.html`, `app.js`, `style.css` — frontend (Bootstrap + JS vanilla).
- `src/main/resources/application.properties` — propiedades de Spring Boot (puerto, etc.).

---

## Requisitos

- JDK 17+ (el proyecto usa Java 21 en el entorno de desarrollo, pero Java 17 o superior debe funcionar dependiendo del `pom.xml`).
- Maven (el proyecto incluye `mvnw`, así que puedes usar el wrapper incluido).
- Navegador web moderno (Chrome/Firefox/Edge) para usar la UI.

---

## Descargar y ejecutar

1. Clona o descarga el repositorio:

```bash
git clone <URL-del-repositorio>
cd final
```

2. Compilar y ejecutar con Maven (usa el wrapper incluido):

```bash
./mvnw -DskipTests package
./mvnw spring-boot:run
```

O bien ejecutar el JAR generado:

```bash
./mvnw -DskipTests package
java -jar target/final-0.0.1-SNAPSHOT.jar
```

Por defecto la aplicación arranca en el puerto configurado en `src/main/resources/application.properties` (puede variar según su copia local). En el código del frontend hay una heurística para development que apunta al puerto `3000` cuando detecta que la página se sirve desde Live Server (127.0.0.1:5500). Si tu backend usa otro puerto, edita `src/main/resources/static/app.js` y ajusta `API_ROOT`.

---

## Endpoints principales (API REST)

La API está expuesta bajo `/api`. Ejemplos:

- Listar tablas

  GET /api/tables

- Crear tabla

  POST /api/tables
  Body JSON: { "name": "miTabla" }

- Eliminar tabla

  DELETE /api/tables/{tableName}

- Obtener registros

  GET /api/tables/{tableName}/records

- Insertar registro

  POST /api/tables/{tableName}/records
  Body JSON: { "campo1": "valor", "edad": 30 }

- Actualizar registro

  PUT /api/tables/{tableName}/records/{id}
  Body JSON: { "campo1": "nuevo" }

- Eliminar registro

  DELETE /api/tables/{tableName}/records/{id}

- Crear índice en un campo

  POST /api/tables/{tableName}/indexes
  Body JSON: { "field": "nombreCampo" }

- Consultar por índice

  GET /api/indexes/{field}?value=...  (según implementación del backend puede variar)

Usa `curl` para probar manualmente. Ejemplo para insertar:

```bash
curl -X POST 'http://localhost:3000/api/tables/usuarios/records' \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"Juan","edad":25}'
```

---

## Interfaz web (cómo usarla)

1. Abre `http://localhost:<puerto>/` en tu navegador (ej: `http://localhost:3000/`).
2. En la columna izquierda puedes crear tablas, seleccionar una ya existente o usar `Poblar ejemplo` para cargar una tabla `usuarios` con filas de ejemplo.
3. Al seleccionar una tabla, verás la lista de registros. Puedes:
   - Insertar: en la sección inferior selecciona un campo del desplegable o pulsa `Nuevo` para crear un campo y añade su valor; pulsa `Agregar campo` para construir el registro, y luego `Insertar`.
   - Editar: pulsa `Editar` sobre un registro. La UI cargará los campos en inputs pre-llenados; modifica los valores y pulsa `Guardar`.
   - Eliminar registro: botón `Eliminar` en la fila del registro.

Notas UX:
- No necesitas volver a escribir el nombre del campo al editar; la interfaz lista los campos detectados y muestra inputs por campo.
- Si deseas agregar campos nuevos, usa el botón `Nuevo` para mostrar un input de nombre.

---

## Consideraciones de desarrollo y CORS

- Si editas los archivos estáticos con Live Server (o sirves `index.html` desde otro origen), el navegador hará preflight (OPTIONS) para peticiones `fetch`. Existe una configuración CORS en `CorsConfig.java` para desarrollo; si tu frontend está fuera del mismo origen asegúrate que el backend permita el Origin o ejecuta la UI desde el mismo servidor Spring Boot (más sencillo).
- Si el puerto del backend ya está ocupado (por ejemplo docker), cambia el puerto en `application.properties` o ajusta `API_ROOT` en `app.js` para apuntar al puerto correcto.

---

## Persistencia

El motor serializa su estado en un archivo (por ejemplo `mibasedatos.db`) usando serialización Java (ver `DataBaseEngine`). Asegúrate de tener permisos de escritura en el directorio donde arrancas la app.

---

## Errores conocidos / debugging

- Si al arrancar observas errores al guardar en el shutdown, revisa permisos de escritura y el log completo. El motor intenta serializar su estado al cerrar.
- Problemas CORS: añade temporalmente orígenes permitidos o sirve la UI desde el mismo backend.
- Si el frontend no muestra campos en el select, prueba a `Poblar ejemplo` o crear registros para que el motor detecte campos existentes.

---

## Siguientes mejoras (opcional)

- Reemplazar prompts/alerts por modales y toasts (UX más moderno).
- Implementar búsqueda por índice directamente en la UI.
- Añadir tests unitarios/integración para el `BDService` y el `DataBaseEngine`.
- Mejorar la persistencia (usar JSON o una base de datos embebida en lugar de serialización binaria).

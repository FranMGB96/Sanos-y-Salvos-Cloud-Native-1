# report-service

Microservicio encargado de los reportes de mascotas perdidas y encontradas en la plataforma **Sanos y Salvos**.

---

## 📋 Responsabilidades

- CRUD de reportes (crear, consultar, actualizar, cambiar estado, eliminar)
- Filtrado de reportes por tipo, estado, usuario y mascota
- Soft-delete: los reportes "eliminados" pasan a estado `CERRADO`
- Control de acceso: solo el creador del reporte o un ADMIN pueden modificarlo

---

## ⚙️ Configuración

### `application.yml` (local)

```yaml
server:
  port: 8083

spring:
  application:
    name: report-service
  config:
    import: "optional:configserver:${CONFIG_SERVER_URI:http://localhost:8888}"
```

### `config-server/config/report-service.yml` (centralizado)

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:3309/report_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
```

### Variables de entorno

| Variable           | Default                       | Descripción           |
|--------------------|-------------------------------|-----------------------|
| `CONFIG_SERVER_URI`| `http://localhost:8888`       | URL del config-server |
| `EUREKA_URI`       | `http://localhost:8761/eureka`| URL de Eureka         |
| `DB_HOST`          | `localhost`                   | Host de MySQL         |

---

## 🗄️ Base de datos

- **Nombre:** `report_db`
- **Puerto MySQL:** `3309` (por defecto)
- **Tabla principal:** `reports`

### Modelo `Report`

| Campo                  | Tipo      | Restricciones                                 |
|------------------------|-----------|-----------------------------------------------|
| `id`                   | BIGINT    | PK, autoincrement                             |
| `tipo`                 | ENUM      | `PERDIDO`, `ENCONTRADO` — NOT NULL            |
| `descripcion`          | VARCHAR   | NOT NULL, máximo 1000 caracteres              |
| `latitud`              | DOUBLE    | Opcional                                      |
| `longitud`             | DOUBLE    | Opcional                                      |
| `ubicacion_descripcion`| VARCHAR   | Opcional (texto libre de la ubicación)        |
| `pet_id`               | BIGINT    | Opcional, referencia a la mascota             |
| `reporter_user_id`     | BIGINT    | NOT NULL, usuario que creó el reporte         |
| `estado`               | ENUM      | `ACTIVO`, `RESUELTO`, `CERRADO` — Default: `ACTIVO` |
| `created_at`           | DATETIME  | Inmutable                                     |
| `updated_at`           | DATETIME  | Se actualiza en cada cambio                   |

---

## 🌐 Endpoints

Base path en el gateway: `/api/reports`  
Todos requieren **JWT** válido.

| Método  | Ruta                        | Descripción                                  |
|---------|-----------------------------|----------------------------------------------|
| GET     | `/api/reports`              | Listar todos los reportes                    |
| GET     | `/api/reports/{id}`         | Obtener reporte por ID                       |
| GET     | `/api/reports/tipo/{tipo}`  | Filtrar por `PERDIDO` o `ENCONTRADO`         |
| GET     | `/api/reports/estado/{estado}` | Filtrar por `ACTIVO`, `RESUELTO` o `CERRADO` |
| GET     | `/api/reports/usuario/{id}` | Reportes creados por un usuario              |
| GET     | `/api/reports/mascota/{id}` | Reportes asociados a una mascota             |
| POST    | `/api/reports`              | Crear nuevo reporte                          |
| PUT     | `/api/reports/{id}`         | Actualizar reporte completo                  |
| PATCH   | `/api/reports/{id}/estado`  | Cambiar solo el estado del reporte           |
| DELETE  | `/api/reports/{id}`         | Soft-delete (pasa a `CERRADO`)               |

### Body `POST /api/reports`

```json
{
  "tipo": "PERDIDO",
  "descripcion": "Se perdió en el parque el sábado en la mañana.",
  "latitud": -33.4569,
  "longitud": -70.6483,
  "ubicacionDescripcion": "Parque Forestal, Santiago",
  "petId": 3,
  "reporterUserId": 1
}
```

### `PATCH /api/reports/{id}/estado?estado=RESUELTO`

Parámetro de query `estado` con uno de los valores: `ACTIVO`, `RESUELTO`, `CERRADO`.

---

## 🔒 Control de acceso

Las operaciones de modificación y eliminación verifican que:
- El usuario que hace la solicitud sea el **creador del reporte** (`X-User-Id`), **o**
- Tenga el rol **ADMIN** (`X-User-Role`)

De lo contrario, se retorna `403 Unauthorized`.

---

## ⚠️ Nota CORS importante

El endpoint `PATCH /{id}/estado` puede verse bloqueado por un error de CORS si el API Gateway no tiene `PATCH` en su lista `allowedMethods`. Ver README del Backend raíz para la solución.

---

## 📖 Swagger UI

Disponible en: `http://localhost:8083/swagger-ui.html`

---

## 🚀 Iniciar el servicio

```bash
cd businessdomain/report-service
mvn spring-boot:run
```

Requiere que `config-server` y `eureka-server` estén corriendo primero.

---

## 🧪 Pruebas

### Pruebas unitarias — `ReportServiceTest`

Ubicación: `src/test/java/com/sanosysalvos/reportservice/ReportServiceTest.java`

Usa **JUnit 5 + Mockito**. No requiere base de datos ni servidor.

| Test | Descripción |
|---|---|
| `createReport_datosValidos_retornaReporte` | Crear reporte con datos válidos |
| `updateEstado_cambiaCorrecto` | Cambiar estado de reporte correctamente |
| `getByTipo_retornaFiltrados` | Filtrar reportes por tipo PERDIDO/ENCONTRADO |
| `getReportById_noExiste_lanzaExcepcion` | Lanza `ResourceNotFoundException` si no existe |

### Pruebas de integración — `ReportControllerIntegrationTest`

Ubicación: `src/test/java/com/sanosysalvos/reportservice/ReportControllerIntegrationTest.java`

Usa `@SpringBootTest` + **H2 en memoria** + `MockMvc`. No requiere MySQL ni ningún servicio externo.

| Test | Descripción |
|---|---|
| `crearReporte_perdido_retorna201` | Crear reporte PERDIDO retorna 201 con estado ACTIVO |
| `crearReporte_encontrado_retorna201` | Crear reporte ENCONTRADO retorna 201 |
| `crearReporte_sinTipo_retornaError400` | Validación de campo tipo requerido |
| `crearReporte_sinDescripcion_retornaError400` | Validación de campo descripción requerido |
| `listarReportes_retornaLista` | Lista no vacía después de crear un reporte |
| `filtrarPorTipo_perdido_soloRetornaPerdidos` | Filtro por tipo funciona correctamente |
| `cambiarEstado_aResuelto_exitoso` | Cambio de estado a RESUELTO funciona |
| `getReporteById_noExiste_retorna404` | ID inexistente retorna 404 |

**Correr desde IntelliJ:** clic derecho en la clase → **Run**

**Correr desde terminal:**
```bash
cd businessdomain/report-service
mvn test
```

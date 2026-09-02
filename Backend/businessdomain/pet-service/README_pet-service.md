# pet-service

Microservicio encargado de la gestión de mascotas en la plataforma **Sanos y Salvos**. Permite registrar mascotas con foto, consultarlas y administrarlas con control de acceso por dueño o rol ADMIN.

---

## 📋 Responsabilidades

- CRUD de mascotas (crear, listar, actualizar, eliminar)
- Subida y almacenamiento de fotos de mascotas en disco
- Soft-delete (las mascotas eliminadas se marcan como inactivas)
- Control de acceso: solo el dueño de la mascota o un ADMIN pueden modificarla o eliminarla

---

## ⚙️ Configuración

### `application.yml` (local)

```yaml
server:
  port: 8082

spring:
  application:
    name: pet-service
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 10MB
```

### `config-server/config/pet-service.yml` (centralizado)

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:3308/pet_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
```

### Variables de entorno

| Variable              | Default                  | Descripción                         |
|-----------------------|--------------------------|-------------------------------------|
| `CONFIG_SERVER_URI`   | `http://localhost:8888`  | URL del config-server               |
| `EUREKA_URI`          | `http://localhost:8761/eureka` | URL de Eureka                 |
| `DB_HOST`             | `localhost`              | Host de MySQL                       |
| `PET_SERVICE_BASE_URL`| `http://localhost:8082`  | URL pública para construir la URL de las fotos |

---

## 🗄️ Base de datos

- **Nombre:** `pet_db`
- **Puerto MySQL:** `3308` (por defecto)
- **Tabla principal:** `pets`

### Modelo `Pet`

| Campo         | Tipo      | Restricciones                         |
|---------------|-----------|---------------------------------------|
| `id`          | BIGINT    | PK, autoincrement                     |
| `nombre`      | VARCHAR   | NOT NULL                              |
| `especie`     | VARCHAR   | NOT NULL                              |
| `raza`        | VARCHAR   | Opcional                              |
| `color`       | VARCHAR   | Opcional                              |
| `tamanio`     | ENUM      | `PEQUENIO`, `MEDIANO`, `GRANDE`       |
| `foto_url`    | VARCHAR   | URL pública de la foto                |
| `descripcion` | VARCHAR   | Opcional                              |
| `owner_id`    | BIGINT    | NOT NULL, FK al usuario               |
| `active`      | BOOLEAN   | Default: true (soft-delete)           |
| `created_at`  | DATETIME  | Inmutable                             |

---

## 🌐 Endpoints

Base path en el gateway: `/api/pets`  
Todos requieren **JWT** válido.

| Método | Ruta                      | Descripción                                   | Body/Params                      |
|--------|---------------------------|-----------------------------------------------|----------------------------------|
| GET    | `/api/pets`               | Listar mascotas activas                        | —                                |
| GET    | `/api/pets/{id}`          | Obtener mascota por ID                         | —                                |
| GET    | `/api/pets/owner/{id}`    | Mascotas de un usuario                         | —                                |
| GET    | `/api/pets/especie/{e}`   | Filtrar por especie (ej: perro, gato)          | —                                |
| POST   | `/api/pets`               | Crear mascota                                  | `multipart/form-data` (ver abajo)|
| PUT    | `/api/pets/{id}`          | Actualizar mascota                             | `multipart/form-data`            |
| DELETE | `/api/pets/{id}`          | Soft-delete de mascota                         | —                                |

### Campos `multipart/form-data` para crear/actualizar

| Campo         | Requerido | Descripción                      |
|---------------|-----------|----------------------------------|
| `nombre`      | ✅        | Nombre de la mascota             |
| `especie`     | ✅        | Especie (ej: perro, gato, ave)   |
| `ownerId`     | ✅        | ID del dueño (solo en creación)  |
| `raza`        | ❌        | Raza de la mascota               |
| `color`       | ❌        | Color del pelaje                 |
| `tamanio`     | ❌        | `PEQUENIO`, `MEDIANO` o `GRANDE` |
| `descripcion` | ❌        | Descripción adicional            |
| `foto`        | ❌        | Archivo de imagen (JPG, PNG...)  |

---

## 🖼️ Manejo de fotos

Las fotos se guardan en el disco local del servicio bajo `uploads/` en el directorio de trabajo. La URL resultante sigue el formato:

```
http://localhost:8082/uploads/<timestamp>_<nombre_archivo>
```

Para cambiar la URL base (ej. en producción), configura la variable de entorno `PET_SERVICE_BASE_URL`.

---

## 🔒 Control de acceso

Las operaciones de modificación y eliminación verifican que:
- El usuario que hace la solicitud sea el **dueño** de la mascota (`X-User-Id`), **o**
- Tenga el rol **ADMIN** (`X-User-Role`)

De lo contrario, se retorna `403 Unauthorized`.

---

## 📖 Swagger UI

Disponible en: `http://localhost:8082/swagger-ui.html`

---

## 🚀 Iniciar el servicio

```bash
cd businessdomain/pet-service
mvn spring-boot:run
```

Requiere que `config-server` y `eureka-server` estén corriendo primero.

---

## 🧪 Pruebas

### Pruebas unitarias — `PetServiceTest`

Ubicación: `src/test/java/com/sanosysalvos/petservice/PetServiceTest.java`

Usa **JUnit 5 + Mockito**. No requiere base de datos ni servidor.

| Test | Descripción |
|---|---|
| `createPet_datosValidos_retornaPet` | Crear mascota con datos válidos |
| `getPetById_noExiste_lanzaExcepcion` | Lanza `ResourceNotFoundException` si no existe |
| `getAllPets_retornaActivos` | Solo retorna mascotas con `active = true` |
| `deletePet_desactiva` | Soft-delete marca la mascota como inactiva |

**Correr desde IntelliJ:** clic derecho en `PetServiceTest` → **Run**

**Correr desde terminal:**
```bash
cd businessdomain/pet-service
mvn test
```

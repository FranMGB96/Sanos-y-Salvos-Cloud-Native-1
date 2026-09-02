# user-service

Microservicio encargado de la gestión de usuarios y la autenticación JWT para toda la plataforma **Sanos y Salvos**.

---

## 📋 Responsabilidades

- Registro e inicio de sesión de usuarios
- Generación y validación de tokens JWT
- CRUD de usuarios (listar, obtener, actualizar, eliminar)
- Creación automática del usuario administrador al arrancar (`DataInitializer`)

---

## ⚙️ Configuración

### `application.yml` (local)

```yaml
server:
  port: 8081

spring:
  application:
    name: user-service
  config:
    import: "optional:configserver:${CONFIG_SERVER_URI:http://localhost:8888}"
```

### `config-server/config/user-service.yml` (centralizado)

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:3307/user_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update

jwt:
  secret: "sanosysalvos-secret-key-2024-duocuc-proyecto-semestral"
  expiration: 86400000
```

### Variables de entorno

| Variable           | Default              | Descripción                  |
|--------------------|----------------------|------------------------------|
| `CONFIG_SERVER_URI`| `http://localhost:8888` | URL del config-server     |
| `EUREKA_URI`       | `http://localhost:8761/eureka` | URL de Eureka        |
| `DB_HOST`          | `localhost`          | Host de MySQL                |

---

## 🗄️ Base de datos

- **Nombre:** `user_db`
- **Puerto MySQL:** `3307` (por defecto)
- **Tabla principal:** `users`

### Modelo `User`

| Campo       | Tipo         | Restricciones               |
|-------------|--------------|-----------------------------|
| `id`        | BIGINT       | PK, autoincrement           |
| `nombre`    | VARCHAR      | NOT NULL                    |
| `email`     | VARCHAR      | NOT NULL, UNIQUE            |
| `password`  | VARCHAR      | NOT NULL (BCrypt)           |
| `telefono`  | VARCHAR      | NOT NULL                    |
| `rol`       | ENUM         | `OWNER`, `CITIZEN`, `ORG`, `ADMIN` |
| `active`    | BOOLEAN      | Default: true (soft-delete) |
| `created_at`| DATETIME     | Inmutable                   |

---

## 🌐 Endpoints

Base path en el gateway: `/api/auth` y `/api/users`

### Autenticación (sin JWT)

| Método | Ruta             | Descripción                        |
|--------|------------------|------------------------------------|
| POST   | `/api/auth/register` | Registrar nuevo usuario. Retorna JWT |
| POST   | `/api/auth/login`    | Login con email y contraseña. Retorna JWT |

#### Body `POST /register`
```json
{
  "nombre": "Juan Pérez",
  "email": "juan@mail.com",
  "password": "miClave123",
  "telefono": "+56912345678"
}
```

#### Body `POST /login`
```json
{
  "email": "juan@mail.com",
  "password": "miClave123"
}
```

#### Respuesta exitosa (ambos)
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "id": 1,
  "nombre": "Juan Pérez",
  "email": "juan@mail.com",
  "telefono": "+56912345678",
  "rol": "OWNER"
}
```

### Usuarios (requieren JWT)

| Método | Ruta               | Descripción                      |
|--------|--------------------|----------------------------------|
| GET    | `/api/users`       | Listar todos los usuarios        |
| GET    | `/api/users/{id}`  | Obtener usuario por ID           |
| PUT    | `/api/users/{id}`  | Actualizar datos del usuario     |
| DELETE | `/api/users/{id}`  | Soft-delete (activa = false)     |

---

## 👤 Usuario admin por defecto

Al iniciar el servicio, `DataInitializer` crea automáticamente:

| Campo  | Valor                      |
|--------|----------------------------|
| Email  | `admin@sanosysalvos.cl`    |
| Password | `admin1`               |
| Rol    | `ADMIN`                    |

---

## 🔐 JWT

El token incluye:
- **Subject:** `userId` (como string)
- **Claim `role`:** rol del usuario (`OWNER`, `ADMIN`, etc.)
- **Expiración:** 24 horas (86400000 ms)

El API Gateway extrae estos valores y los reenvía como `X-User-Id` y `X-User-Role`.

---

## 📖 Swagger UI

Disponible en: `http://localhost:8081/swagger-ui.html`

---

## 🚀 Iniciar el servicio

```bash
cd businessdomain/user-service
mvn spring-boot:run
```

Requiere que `config-server` y `eureka-server` estén corriendo primero.

---

## 🧪 Pruebas

### Pruebas de integración — `AuthControllerIntegrationTest`

Ubicación: `src/test/java/com/sanosysalvos/userservice/AuthControllerIntegrationTest.java`

Usa `@SpringBootTest` + **H2 en memoria** + `MockMvc`. No requiere MySQL ni ningún servicio externo.

| Test | Descripción |
|---|---|
| `registro_datosValidos_retornaTokenYDatos` | Registro exitoso retorna 201 con token JWT |
| `registro_emailDuplicado_retornaError400` | Email ya registrado retorna 400 |
| `registro_sinNombre_retornaError400` | Validación de campo nombre vacío |
| `login_credencialesCorrectas_retornaToken` | Login exitoso retorna token JWT válido |
| `login_passwordIncorrecta_retornaError401` | Contraseña incorrecta retorna 401 |
| `login_usuarioNoExiste_retornaError` | Usuario inexistente retorna 401 |

**Correr desde IntelliJ:** clic derecho en `AuthControllerIntegrationTest` → **Run**

**Correr desde terminal:**
```bash
cd businessdomain/user-service
mvn test
```

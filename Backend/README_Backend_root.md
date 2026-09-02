# 🐾 Sanos y Salvos — Backend

Plataforma de recuperación de mascotas perdidas y encontradas. El backend está construido sobre una **arquitectura de microservicios** con Spring Boot 3, Spring Cloud y MySQL.

---

## 📁 Estructura del proyecto

```
Backend/
├── businessdomain/
│   ├── user-service/       # Usuarios y autenticación JWT
│   ├── pet-service/        # Gestión de mascotas
│   └── report-service/     # Reportes de mascotas perdidas/encontradas
└── infrastructure/
    ├── config-server/      # Configuración centralizada
    ├── eureka-server/      # Service discovery
    ├── api-gateway/        # Punto de entrada único (puerto 8080)
    ├── bff-service/        # Backend For Frontend
    └── springboot-admin/   # Panel de monitoreo
```

---

## 🗺️ Mapa de servicios

| Servicio           | Puerto | Descripción                                          |
|--------------------|--------|------------------------------------------------------|
| `config-server`    | 8888   | Provee configuración centralizada a todos los servicios |
| `eureka-server`    | 8761   | Registro y descubrimiento de servicios               |
| `api-gateway`      | 8080   | Punto único de entrada; ruteo, JWT y CORS            |
| `user-service`     | 8081   | Gestión de usuarios, registro y login                |
| `pet-service`      | 8082   | Gestión de mascotas y subida de fotos                |
| `report-service`   | 8083   | Reportes de mascotas perdidas o encontradas          |
| `bff-service`      | 8084   | Agrega datos de servicios para el frontend           |
| `springboot-admin` | 9090   | Monitoreo visual de todos los microservicios         |

---

## ⚙️ Requisitos previos

- **Java 21**
- **Maven 3.9+**
- **MySQL 8+**

---

## 🗄️ Bases de datos

Cada servicio de negocio usa su propia base de datos. Créalas antes de iniciar:

```sql
CREATE DATABASE user_db;
CREATE DATABASE pet_db;
CREATE DATABASE report_db;
```

> Las bases de datos se crean automáticamente si usas Docker con la variable `createDatabaseIfNotExist=true`, pero en local debes crearlas manualmente.

### Puertos MySQL por servicio (configuración por defecto)

| Base de datos | Puerto MySQL |
|---------------|-------------|
| `user_db`     | 3307        |
| `pet_db`      | 3308        |
| `report_db`   | 3309        |

> Si usas una sola instancia MySQL en el puerto 3306, ajusta las URLs en `config-server/src/main/resources/config/`.

---

## 🔑 Credenciales por defecto

| Campo    | Valor                                                        |
|----------|--------------------------------------------------------------|
| DB user  | `root`                                                       |
| DB pass  | `root`                                                       |
| JWT secret | `sanosysalvos-secret-key-2024-duocuc-proyecto-semestral`  |
| JWT exp  | 86400000 ms (24 horas)                                       |
| Admin email | `admin@sanosysalvos.cl`                                   |
| Admin pass | `admin1`                                                   |

> El usuario admin se crea automáticamente al iniciar `user-service` gracias al `DataInitializer`.

---

## 🚀 Orden de arranque

Deben iniciarse **en este orden** para que el descubrimiento y la configuración funcionen:

```
1. config-server     → provee configuración al resto
2. eureka-server     → registro de servicios
3. api-gateway       → necesita Eureka activo
4. user-service      → independiente
5. pet-service       → independiente
6. report-service    → independiente
7. bff-service       → consume user, pet y report
8. springboot-admin  → opcional, monitoreo
```

Para iniciar cada servicio desde su carpeta:

```bash
mvn spring-boot:run
```

---

## 🔐 Seguridad

- **Autenticación:** JWT firmado con HMAC-SHA256
- **Autorización:** el gateway inyecta `X-User-Id` y `X-User-Role` en cada petición; los servicios internos los usan para controlar acceso
- **CORS:** configurado globalmente en el API Gateway para `http://localhost:4200`
- **Contraseñas:** almacenadas con BCrypt

---

## 🌐 CORS — Nota importante

El archivo `infrastructure/config-server/src/main/resources/config/api-gateway.yml` debe incluir **`PATCH`** en `allowedMethods`, ya que el endpoint de cambio de estado de reportes usa ese método:

```yaml
allowedMethods: [GET, POST, PUT, PATCH, DELETE, OPTIONS]
```

Además, `AuthenticationFilter` debe dejar pasar las solicitudes `OPTIONS` (preflight) sin validar JWT.

---

## 📦 Stack tecnológico

| Tecnología               | Versión  | Uso                            |
|--------------------------|----------|--------------------------------|
| Spring Boot              | 3.2.5    | Framework base                 |
| Spring Cloud             | 2023.0.1 | Microservicios y configuración |
| Spring Cloud Gateway     | —        | API Gateway reactivo           |
| Netflix Eureka           | —        | Service discovery              |
| Spring Security          | —        | Autenticación                  |
| JJWT                     | 0.11.5   | Generación y validación de JWT |
| Spring Data JPA          | —        | Acceso a base de datos         |
| MySQL Connector          | 8.0.33   | Driver MySQL                   |
| Lombok                   | 1.18.36  | Reducción de boilerplate       |
| Springdoc OpenAPI        | 2.3.0    | Documentación Swagger          |
| Resilience4j             | —        | Circuit Breaker en BFF         |
| Spring Boot Admin        | 3.2.3    | Monitoreo                      |
| Java                     | 21       | Lenguaje                       |

---

## 🐳 Docker

Cada servicio incluye un `Dockerfile`. Para construir la imagen de un servicio:

```bash
docker build -t sns-<nombre-servicio> .
```

---

## 📖 READMEs por servicio

Cada microservicio tiene su propio README con detalles específicos:

- [`businessdomain/user-service/README.md`](businessdomain/user-service/README.md)
- [`businessdomain/pet-service/README.md`](businessdomain/pet-service/README.md)
- [`businessdomain/report-service/README.md`](businessdomain/report-service/README.md)
- [`infrastructure/config-server/README.md`](infrastructure/config-server/README.md)
- [`infrastructure/eureka-server/README.md`](infrastructure/eureka-server/README.md)
- [`infrastructure/api-gateway/README.md`](infrastructure/api-gateway/README.md)
- [`infrastructure/bff-service/README.md`](infrastructure/bff-service/README.md)
- [`infrastructure/springboot-admin/README.md`](infrastructure/springboot-admin/README.md)

---

## 🧪 Pruebas del backend

### Pruebas unitarias

No requieren nada levantado. Correr desde IntelliJ con clic derecho → **Run** sobre cada clase:

| Clase | Servicio | Tests | Qué prueba |
|---|---|---|---|
| `PetServiceTest` | pet-service | 4 | Crear mascota, obtener por ID, listar activas, eliminar |
| `ReportServiceTest` | report-service | 4 | Crear reporte, cambiar estado, filtrar por tipo, obtener por ID |

### Pruebas de integración

Usan **H2 en memoria** — no requieren MySQL ni Config Server. Correr desde IntelliJ con clic derecho → **Run**:

| Clase | Servicio | Tests | Qué prueba |
|---|---|---|---|
| `AuthControllerIntegrationTest` | user-service | 6 | Registro exitoso, email duplicado, validaciones, login correcto, contraseña incorrecta, usuario inexistente |
| `ReportControllerIntegrationTest` | report-service | 8 | Crear PERDIDO/ENCONTRADO, validaciones, listar, filtrar, cambiar estado, ID inexistente |

### Correr todos los tests desde terminal

```bash
# user-service
cd businessdomain/user-service
mvn test

# pet-service
cd businessdomain/pet-service
mvn test

# report-service
cd businessdomain/report-service
mvn test
```

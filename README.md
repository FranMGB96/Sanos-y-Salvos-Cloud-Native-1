[README.md](https://github.com/user-attachments/files/28027615/README.md)
# 🐾 Sanos y Salvos

Plataforma web de reporte y búsqueda de mascotas perdidas. Permite a los usuarios registrarse, registrar sus mascotas y publicar reportes geolocalizados cuando una mascota se pierde o es encontrada.

---

## 🛠️ Stack tecnológico

| Capa | Tecnología |
|---|---|
| Frontend | Angular 17 · TypeScript · Nginx |
| Backend | Java 21 · Spring Boot 3.2 · Spring Cloud |
| Seguridad | JWT · Spring Security · API Gateway Filter |
| Base de datos | MySQL 8.0 (3 instancias independientes) |
| Infraestructura | Docker · Docker Compose |
| Service Discovery | Netflix Eureka |
| Configuración | Spring Cloud Config Server |
| Monitoreo | Spring Boot Admin |
| Documentación | Swagger / OpenAPI 3 |

---

## 🏗️ Arquitectura

El proyecto sigue una arquitectura de **microservicios** con las siguientes capas:

### Infraestructura
| Servicio | Puerto | Descripción |
|---|---|---|
| Eureka Server | 8761 | Service Discovery — registro de microservicios |
| Config Server | 8888 | Configuración centralizada para todos los servicios |
| API Gateway | 8080 | Punto de entrada único, validación JWT, enrutamiento |
| BFF Service | 8084 | Backend for Frontend — agrega datos de múltiples servicios |
| Spring Boot Admin | — | Monitoreo y métricas de los microservicios |

### Microservicios de negocio
| Servicio | Puerto | Descripción |
|---|---|---|
| User Service | 8081 | Registro, login, gestión de usuarios y JWT |
| Pet Service | 8082 | CRUD de mascotas con subida de fotos |
| Report Service | 8083 | Reportes de mascotas perdidas/encontradas con geolocalización |

### Bases de datos
| Base de datos | Puerto | Servicio |
|---|---|---|
| user_db | 3307 | User Service |
| pet_db | 3308 | Pet Service |
| report_db | 3309 | Report Service |

---

## 🔐 Seguridad

- Autenticación con **JWT** generado por el `user-service`
- El **API Gateway** valida el token en cada request e inyecta los headers `X-User-Id` y `X-User-Role`
- Cada usuario solo puede editar o eliminar sus propios registros
- El rol **ADMIN** puede gestionar todos los registros sin restricción
- El registro público siempre asigna el rol `OWNER`

### Roles disponibles
| Rol | Descripción |
|---|---|
| `OWNER` | Dueño de mascota — rol por defecto al registrarse |
| `CITIZEN` | Ciudadano colaborador |
| `ORG` | Organización o refugio |
| `ADMIN` | Administrador del sistema |

### Usuario administrador
Al iniciar el `user-service`, se crea automáticamente si no existe:

| Campo | Valor |
|---|---|
| Email | `admin@sanosysalvos.cl` |
| Password | `admin1` |
| Rol | `ADMIN` |

---

## 📁 Estructura del repositorio

```
Sanos-y-Salvos/
├── Backend/
│   ├── businessdomain/
│   │   ├── user-service/          # Gestión de usuarios y autenticación
│   │   ├── pet-service/           # Gestión de mascotas
│   │   └── report-service/        # Gestión de reportes
│   ├── infrastructure/
│   │   ├── api-gateway/           # Punto de entrada y validación JWT
│   │   ├── bff-service/           # Backend for Frontend
│   │   ├── config-server/         # Configuración centralizada
│   │   ├── eureka-server/         # Service Discovery
│   │   └── springboot-admin/      # Monitoreo
│   ├── docker-compose.yml
│   └── pom.xml
└── Frontend/
    ├── src/
    │   ├── app/
    │   │   ├── core/              # Guards, interceptors, servicios, modelos
    │   │   ├── features/
    │   │   │   ├── admin/         # Panel de administración
    │   │   │   ├── auth/          # Login y registro
    │   │   │   ├── dashboard/     # Página de inicio
    │   │   │   ├── pets/          # Lista y formulario de mascotas
    │   │   │   └── reports/       # Lista y formulario de reportes
    │   │   └── shared/            # Navbar, footer
    │   └── environments/
    ├── Dockerfile
    └── nginx.conf
```

---

## 🚀 Cómo ejecutar el proyecto

### Prerrequisitos
- Java 21
- Maven 3.9+
- Node.js 18+ y npm
- Docker Desktop

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/Sanos-y-Salvos.git
cd Sanos-y-Salvos
```

### 2. Levantar las bases de datos con Docker

```bash
cd Backend
docker compose up -d user-db pet-db report-db
```

### 3. Levantar los microservicios desde IntelliJ

En este orden, esperando que cada uno muestre `Started` antes de lanzar el siguiente:

```
1. EurekaServerApplication     → http://localhost:8761
2. ConfigServerApplication     → http://localhost:8888
3. UserServiceApplication      → http://localhost:8081
4. PetServiceApplication       → http://localhost:8082
5. ReportServiceApplication    → http://localhost:8083
6. ApiGatewayApplication       → http://localhost:8080
7. BffServiceApplication       → http://localhost:8084
8. SpringBootAdminApplication  → (opcional)
```

### 4. Levantar el Frontend

```bash
cd Frontend
npm install
ng serve
```

La aplicación estará disponible en **http://localhost:4200**

---

## 🐳 Ejecución completa con Docker

```bash
cd Backend
docker compose up --build
```

Solo bases de datos (recomendado para desarrollo local):

```bash
docker compose up -d user-db pet-db report-db
```

---

## 🌐 Endpoints principales

Todos los endpoints pasan por el API Gateway en `http://localhost:8080`

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| POST | `/api/auth/register` | ❌ | Registrar usuario |
| POST | `/api/auth/login` | ❌ | Iniciar sesión |
| GET | `/api/users` | ✅ | Listar usuarios |
| GET | `/api/pets` | ✅ | Listar mascotas |
| POST | `/api/pets` | ✅ | Registrar mascota |
| PUT | `/api/pets/{id}` | ✅ dueño/admin | Actualizar mascota |
| DELETE | `/api/pets/{id}` | ✅ dueño/admin | Eliminar mascota |
| GET | `/api/reports` | ✅ | Listar reportes |
| POST | `/api/reports` | ✅ | Crear reporte |
| PUT | `/api/reports/{id}` | ✅ dueño/admin | Actualizar reporte |
| PATCH | `/api/reports/{id}/estado` | ✅ dueño/admin | Cambiar estado |
| DELETE | `/api/reports/{id}` | ✅ dueño/admin | Eliminar reporte |
| GET | `/api/bff/dashboard` | ✅ | Dashboard con estadísticas |

---

## 📖 Documentación Swagger

Con los servicios corriendo:

| Servicio | URL |
|---|---|
| User Service | http://localhost:8081/swagger-ui.html |
| Pet Service | http://localhost:8082/swagger-ui.html |
| Report Service | http://localhost:8083/swagger-ui.html |

---

## 📚 READMEs por servicio

- [`Backend/businessdomain/user-service/README_user-service.md`](Backend/businessdomain/user-service/README_user-service.md)
- [`Backend/businessdomain/pet-service/README_pet-service.md`](Backend/businessdomain/pet-service/README_pet-service.md)
- [`Backend/businessdomain/report-service/README_report-service.md`](Backend/businessdomain/report-service/README_report-service.md)
- [`Backend/infrastructure/api-gateway/README_api-gateway.md`](Backend/infrastructure/api-gateway/README_api-gateway.md)
- [`Backend/infrastructure/bff-service/README_bff-service.md`](Backend/infrastructure/bff-service/README_bff-service.md)

---

## 🧪 Pruebas

El proyecto implementa tres niveles de pruebas cubriendo los procesos de negocio más críticos.

### Resumen

| Nivel | Herramienta | Tests | Archivos |
|---|---|---|---|
| Unitarias | JUnit 5 + Mockito | 8 | `PetServiceTest`, `ReportServiceTest` |
| Integración | JUnit 5 + Spring Boot Test + H2 | 14 | `AuthControllerIntegrationTest`, `ReportControllerIntegrationTest` |
| E2E | Cypress 15 + Chrome | 15 | `login.cy.js`, `reporte.cy.js` |
| **Total** | | **37** | |

### Pruebas unitarias
Prueban clases individuales de forma aislada usando **Mockito**. No requieren base de datos ni servidor.

### Pruebas de integración
Levantan el contexto completo de Spring Boot con **H2 en memoria**. Prueban el flujo Controller → Service → Base de datos sin necesitar MySQL ni ningún servicio externo.

### Pruebas E2E
Simulan un usuario real navegando en el navegador con **Cypress**. Requieren frontend en `localhost:4200` y backend en `localhost:8080`.

---

## 👥 Equipo

Proyecto desarrollado como parte del ramo de **Fullstack III** en DuocUC.

| Integrante | GitHub |
|---|---|
| Lucas Ribeiro | [@LucasVeloster](https://github.com/LucasVeloster) |
| Francisco García | [@FranMGB96](https://github.com/FranMGB96) |

---

```bash
# Correr tests E2E
cd Frontend
npx cypress open
```

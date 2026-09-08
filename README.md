# 🐾 Sanos y Salvos — Cloud Native I (DSY1107)

Plataforma web de reporte y búsqueda de mascotas perdidas. Permite a los usuarios registrar sus mascotas y publicar reportes geolocalizados cuando una mascota se pierde o es encontrada.

Este repositorio corresponde a la evaluación de **Desarrollo Cloud Native I**, que retoma el proyecto de un ramo anterior (Fullstack III) y lo migra hacia una arquitectura de nube real: autenticación con **Microsoft Entra ID (Azure AD)** vía **MSAL** y **OAuth 2.0 / OpenID Connect**, backend protegido como **Resource Server**, y despliegue en **AWS** (API Gateway + EC2).

---

## 🛠️ Stack tecnológico

| Capa | Tecnología |
|---|---|
| Frontend | Angular 17 · TypeScript · MSAL (`@azure/msal-angular`) |
| Backend | Java 21 · Spring Boot 3.2 · Spring Cloud |
| Identidad (IDaaS) | Microsoft Entra ID (Azure AD) — OAuth 2.0 / OIDC |
| Seguridad backend | Spring Security · OAuth2 Resource Server (`spring-cloud-azure-starter-active-directory`) |
| Base de datos | MySQL 8.0 (una instancia por microservicio) |
| Infraestructura | Docker · Docker Compose · AWS (API Gateway + EC2) |
| Service Discovery | Netflix Eureka |
| Configuración | Spring Cloud Config Server |
| Documentación | Swagger / OpenAPI 3 |

---

## 🏗️ Arquitectura

### Infraestructura
| Servicio | Puerto | Descripción |
|---|---|---|
| Eureka Server | 8761 | Service Discovery — registro de microservicios |
| Config Server | 8888 | Configuración centralizada para todos los servicios |
| API Gateway | 8080 | Punto de entrada único y enrutamiento interno |
| BFF Service | 8084 | Backend for Frontend — agrega datos y valida el JWT de Azure AD |

### Microservicios de negocio
| Servicio | Puerto | Descripción |
|---|---|---|
| User Service | 8081 | Gestión de usuarios |
| Pet Service | 8082 | CRUD de mascotas con subida de fotos |
| Report Service | 8083 | Reportes de mascotas perdidas/encontradas con geolocalización |
| Category Service | 8085 | Catálogo de especies (Perro, Gato, Ave, Otros) para filtrar mascotas — microservicio agregado en esta evaluación |

### Bases de datos
| Base de datos | Puerto | Servicio |
|---|---|---|
| user_db | 3307 | User Service |
| pet_db | 3308 | Pet Service |
| report_db | 3309 | Report Service |
| category_db | 3310 | Category Service |

---

## 🔐 Autenticación y seguridad (Azure AD)

La autenticación **ya no usa un login propio con JWT interno**. El flujo real es:

1. El frontend usa **MSAL** (flujo *Authorization Code + Redirect*) para autenticar al usuario contra el tenant de **Microsoft Entra ID**.
2. MSAL obtiene un **Access Token** con el scope `Pets.Access`, y lo adjunta automáticamente a cada llamada a la API (`MsalInterceptor`).
3. El **BFF** actúa como *Resource Server*: valida la firma, el `issuer`, la `audience` y el scope del token con Spring Security antes de responder cualquier endpoint.
4. En la nube, el objetivo es que **AWS API Gateway** también valide el mismo token (vía JWT Authorizer) antes de reenviar la petición al backend en EC2.

### Apps registradas en Microsoft Entra ID

| App | Rol |
|---|---|
| `SanosYSalvos-Backend` | Expone la API (`api://<tenant>/sanosysalvos`) y el scope `Pets.Access` |
| `SanosYSalvos-Frontend` | Aplicación SPA que inicia sesión y solicita el scope del backend |

### Roles
Por simplicidad para esta evaluación, el rol de administrador se controla en el frontend por email (no hay App Roles configurados en Azure AD todavía). El resto de los usuarios autenticados entra como usuario normal.

> ⚠️ **Nota:** el login propio (usuario/contraseña, registro público) del proyecto original fue reemplazado por completo. Funcionalidades que dependían del `userId` interno (dueño de una mascota, perfil editable) quedan simplificadas, ya que Azure AD no conoce ese identificador — no era parte de lo evaluado en esta entrega.

---

## 📁 Estructura del repositorio

```
Sanos-y-Salvos-Cloud-Native-1/
├── Backend/
│   ├── businessdomain/
│   │   ├── user-service/          # Gestión de usuarios
│   │   ├── pet-service/           # Gestión de mascotas
│   │   ├── report-service/        # Gestión de reportes
│   │   └── category-service/      # Catálogo de especies (nuevo en esta evaluación)
│   ├── infrastructure/
│   │   ├── api-gateway/           # Enrutamiento interno
│   │   ├── bff-service/           # Backend for Frontend + validación JWT (Azure AD)
│   │   ├── config-server/         # Configuración centralizada
│   │   └── eureka-server/         # Service Discovery
│   ├── docker-compose.yml
│   └── pom.xml
└── Frontend/
    ├── src/
    │   ├── app/
    │   │   ├── msal-config.ts     # Configuración de MSAL (instancia, guard, interceptor)
    │   │   ├── core/               # Guards, servicios, modelos
    │   │   ├── features/
    │   │   │   ├── admin/          # Panel de administración
    │   │   │   ├── auth/           # Login (MSAL) y página de auth-redirect
    │   │   │   ├── dashboard/      # Página de inicio
    │   │   │   ├── pets/           # Lista y formulario de mascotas
    │   │   │   └── reports/        # Lista y formulario de reportes
    │   │   └── shared/             # Navbar, footer
    │   └── environments/           # Config de Azure AD (clientId, authority, scopes)
    └── Dockerfile
```

---

## 🚀 Cómo ejecutar el proyecto localmente

### Prerrequisitos
- Java 21, Maven 3.9+
- Node.js y npm
- Docker Desktop
- Un tenant de Microsoft Entra ID con las dos apps registradas (ver sección de Seguridad)

### 1. Base de datos y microservicios de negocio (Docker)

```bash
cd Backend
docker compose up --build -d user-service pet-service report-service category-service
```

### 2. Infraestructura (desde el IDE, en este orden)

```
1. EurekaServerApplication     → http://localhost:8761
2. ConfigServerApplication     → http://localhost:8888
3. ApiGatewayApplication       → http://localhost:8080
4. BffServiceApplication       → http://localhost:8084
```

> El `config-server` debe estar arriba **antes** que el `api-gateway` y el `bff-service`, ya que ambos leen su configuración (rutas, credenciales de Azure AD) desde ahí al arrancar.

### 3. Frontend

```bash
cd Frontend
npm install
npm start
```

La aplicación queda disponible en **http://localhost:4200**. Al abrirla, el login redirige a Microsoft para autenticar contra el tenant configurado en `environment.ts`.

---

## 🌐 Endpoints principales

Todos pasan por el API Gateway en `http://localhost:8080`, salvo el BFF que además exige el JWT de Azure AD:

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/users` | Listar usuarios |
| GET | `/api/pets` | Listar mascotas |
| POST | `/api/pets` | Registrar mascota |
| GET | `/api/categories` | Listar especies (Perro, Gato, Ave, Otros) |
| GET | `/api/reports` | Listar reportes |
| POST | `/api/reports` | Crear reporte |
| GET | `/api/bff/dashboard` | Dashboard con estadísticas (requiere JWT de Azure AD) |
| GET | `/api/bff/usuarios/{id}/mascotas` | Requiere JWT + scope `Pets.Access` |

---

## ☁️ Estado del despliegue en la nube

| Componente | Estado |
|---|---|
| Tenant de Microsoft Entra ID + apps registradas | ✅ Listo |
| Frontend con MSAL (login/logout, guards, interceptor) | ✅ Listo |
| BFF como Resource Server (valida JWT de Azure AD) | ✅ Listo |
| AWS API Gateway con JWT Authorizer | ✅ Listo |
| Microservicios desplegados en EC2 | ✅ Listo |

---

## 👥 Equipo

Proyecto desarrollado como parte del ramo de **Desarrollo Cloud Native I** en DuocUC.

| Integrante | GitHub |
|---|---|
| Aaron Ojeda | [@aaron16-code](https://github.com/aaron16-code) |
| Francisco García | [@FranMGB96](https://github.com/FranMGB96) |

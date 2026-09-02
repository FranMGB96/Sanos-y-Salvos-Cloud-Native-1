# 🐾 Sanos y Salvos — Frontend

Aplicación **Angular 17** de página única (SPA) para la plataforma de recuperación de mascotas perdidas y encontradas. Se comunica con el backend a través del API Gateway en `http://localhost:8080`.

---

## 📁 Estructura del proyecto

```
Frontend/
└── src/
    ├── app/
    │   ├── core/
    │   │   ├── guards/
    │   │   │   ├── auth.guard.ts        # Redirige a /login si no hay sesión
    │   │   │   └── admin.guard.ts       # Redirige a /inicio si no es ADMIN
    │   │   ├── interceptors/
    │   │   │   └── auth.interceptor.ts  # Inyecta JWT en cada petición HTTP
    │   │   ├── models/
    │   │   │   ├── user.model.ts
    │   │   │   ├── pet.model.ts
    │   │   │   └── report.model.ts
    │   │   └── services/
    │   │       ├── auth.service.ts
    │   │       ├── pet.service.ts
    │   │       └── report.service.ts
    │   ├── features/
    │   │   ├── auth/
    │   │   │   ├── login/               # LoginComponent
    │   │   │   └── register/            # RegisterComponent
    │   │   ├── dashboard/               # DashboardComponent
    │   │   ├── pets/
    │   │   │   ├── pet-list/            # PetListComponent
    │   │   │   └── pet-form/            # PetFormComponent (crear y editar)
    │   │   ├── reports/
    │   │   │   ├── report-list/         # ReportListComponent
    │   │   │   └── report-form/         # ReportFormComponent
    │   │   ├── profile/                 # ProfileComponent
    │   │   ├── admin/                   # AdminPanelComponent (solo ADMIN)
    │   │   └── about/                   # AboutComponent
    │   └── shared/
    │       └── components/
    │           ├── navbar/              # NavbarComponent
    │           └── footer/             # FooterComponent
    └── environments/
        ├── environment.ts               # Desarrollo (localhost:8080)
        └── environment.prod.ts          # Producción
```

---

## ⚙️ Requisitos previos

- **Node.js 18+**
- **npm 9+**
- **Angular CLI 17**

```bash
npm install -g @angular/cli@17
```

---

## 🚀 Instalación y ejecución

```bash
# 1. Instalar dependencias
npm install

# 2. Iniciar en modo desarrollo
ng serve
```

La aplicación queda disponible en `http://localhost:4200`.

El backend debe estar corriendo en `http://localhost:8080` (API Gateway).

---

## 🏗️ Build para producción

```bash
npm run build
```

El output queda en `dist/sanos-y-salvos-frontend/`. Configura la URL del backend en `src/environments/environment.prod.ts` antes de compilar.

---

## 🌐 Rutas de la aplicación

| Ruta              | Componente             | Guard       | Descripción                              |
|-------------------|------------------------|-------------|------------------------------------------|
| `/login`          | LoginComponent         | —           | Formulario de inicio de sesión           |
| `/register`       | RegisterComponent      | —           | Registro de nuevo usuario                |
| `/inicio`         | DashboardComponent     | `authGuard` | Panel con estadísticas y últimos reportes|
| `/pets`           | PetListComponent       | `authGuard` | Listado de mascotas del usuario          |
| `/pets/new`       | PetFormComponent       | `authGuard` | Agregar nueva mascota                    |
| `/pets/edit/:id`  | PetFormComponent       | `authGuard` | Editar mascota existente                 |
| `/reports`        | ReportListComponent    | `authGuard` | Listado de reportes con filtros          |
| `/reports/new`    | ReportFormComponent    | `authGuard` | Crear nuevo reporte                      |
| `/perfil`         | ProfileComponent       | `authGuard` | Ver y editar perfil del usuario          |
| `/admin`          | AdminPanelComponent    | `adminGuard`| Panel de administración (solo ADMIN)     |
| `/nosotros`       | AboutComponent         | —           | Página informativa                       |

---

## 🔐 Autenticación

1. El usuario ingresa credenciales en `/login`
2. `AuthService` llama a `POST /api/auth/login`
3. El JWT y los datos del usuario se guardan en `localStorage` (`sns_token`, `sns_user`)
4. `authInterceptor` inyecta `Authorization: Bearer <token>` en cada petición
5. Al hacer logout se borran ambas claves y se redirige a `/login`

**Roles disponibles:** `OWNER`, `CITIZEN`, `ORG`, `ADMIN`

Solo los usuarios con rol `ADMIN` pueden acceder a `/admin`.

---

## 📡 Servicios HTTP

| Servicio        | Descripción                                              |
|-----------------|----------------------------------------------------------|
| `AuthService`   | Login, registro, logout, gestión de sesión en localStorage |
| `PetService`    | CRUD de mascotas usando `multipart/form-data`            |
| `ReportService` | CRUD de reportes y consultas al BFF (dashboard, enriquecidos) |

Todos los servicios usan `environment.apiUrl` (`http://localhost:8080/api`) como base.

---

## 🛠️ Stack tecnológico

| Tecnología       | Versión | Uso                                  |
|------------------|---------|--------------------------------------|
| Angular          | 17.3    | Framework SPA                        |
| TypeScript       | 5.4     | Lenguaje                             |
| RxJS             | 7.8     | Programación reactiva                |
| Angular Router   | 17.3    | Navegación y lazy loading            |
| Angular Forms    | 17.3    | Formularios reactivos                |
| HttpClient       | 17.3    | Comunicación con el backend          |
| Zone.js          | 0.14    | Change detection                     |

---

## 🔧 Configuración de entorno

Edita `src/environments/environment.ts` para apuntar a otro backend:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

---

## 🧪 Tests

```bash
npm test
```

Ejecuta los tests con Karma en modo headless (ChromeHeadless).

---

## 🧪 Pruebas E2E con Cypress

### Instalación

```bash
npm install cypress --save-dev
```

### Correr tests

```bash
# Abrir interfaz gráfica de Cypress
npx cypress open
```

Seleccionar **E2E Testing** → **Chrome** → elegir el archivo de test.

> Requiere que el frontend esté corriendo en `localhost:4200` y el backend en `localhost:8080`.

### Archivos de test

Ubicación: `cypress/e2e/`

#### `login.cy.js` — 7 tests

| Test | Descripción |
|---|---|
| Login exitoso con credenciales válidas | Redirige al dashboard |
| Login admin | Muestra el link al panel de administración |
| Login con contraseña incorrecta | Permanece en /login |
| Login con campos vacíos | No permite enviar |
| Logout | Redirige al login |
| Acceder a /inicio sin login | Redirige al login |
| Acceder a /admin sin login | Redirige al login |

#### `reporte.cy.js` — 8 tests

| Test | Descripción |
|---|---|
| Dashboard | Muestra estadísticas del sistema |
| Navegar a /reports | Muestra el listado de reportes |
| Formulario de nuevo reporte | Carga correctamente |
| Seleccionar tipo mascota perdida | Activa el botón |
| Panel admin — usuarios | Carga la lista de usuarios |
| Panel admin — mascotas | Tab de mascotas muestra contenido |
| Panel admin — reportes | Tab de reportes muestra contenido |
| Logout desde el dashboard | Redirige al login |

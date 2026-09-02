# bff-service

Servicio **Backend For Frontend** de la plataforma **Sanos y Salvos**. Agrega y combina datos de `user-service`, `pet-service` y `report-service` en respuestas optimizadas para el frontend, evitando que el cliente tenga que hacer múltiples llamadas.

---

## 📋 Responsabilidades

- Proveer un endpoint de dashboard con estadísticas globales
- Retornar reportes enriquecidos con datos del usuario reporter y la mascota asociada
- Retornar un usuario junto con todas sus mascotas en una sola llamada
- Implementar **Circuit Breaker** con Resilience4j: si un servicio falla, retorna datos vacíos en lugar de propagar el error

---

## ⚙️ Configuración

### `application.yml` (local)

```yaml
server:
  port: 8084

spring:
  application:
    name: bff-service
  config:
    import: "optional:configserver:${CONFIG_SERVER_URI:http://localhost:8888}"

resilience4j:
  circuitbreaker:
    instances:
      bff-dashboard:
        failureRateThreshold: 50
        minimumNumberOfCalls: 5
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 3
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 10
      bff-usuario:
        # misma config que bff-dashboard
      bff-reportes:
        # misma config que bff-dashboard
```

### `config-server/config/bff-service.yml` (centralizado)

```yaml
services:
  user-url:   ${USER_SERVICE_URL:http://user-service:8081}
  pet-url:    ${PET_SERVICE_URL:http://pet-service:8082}
  report-url: ${REPORT_SERVICE_URL:http://report-service:8083}
```

### Variables de entorno

| Variable              | Default                       | Descripción                     |
|-----------------------|-------------------------------|---------------------------------|
| `CONFIG_SERVER_URI`   | `http://localhost:8888`       | URL del config-server           |
| `EUREKA_URI`          | `http://localhost:8761/eureka`| URL de Eureka                   |
| `USER_SERVICE_URL`    | `http://user-service:8081`    | URL directa del user-service    |
| `PET_SERVICE_URL`     | `http://pet-service:8082`     | URL directa del pet-service     |
| `REPORT_SERVICE_URL`  | `http://report-service:8083`  | URL directa del report-service  |

---

## 🌐 Endpoints

Base path en el gateway: `/api/bff`  
Todos requieren **JWT** válido.

| Método | Ruta                              | Descripción                                              |
|--------|-----------------------------------|----------------------------------------------------------|
| GET    | `/api/bff/dashboard`              | Estadísticas globales + últimos 5 reportes               |
| GET    | `/api/bff/usuarios/{id}/mascotas` | Datos del usuario con su lista de mascotas               |
| GET    | `/api/bff/reportes`               | Todos los reportes enriquecidos                          |
| GET    | `/api/bff/reportes/tipo/{tipo}`   | Reportes enriquecidos filtrados por tipo                 |
| GET    | `/api/bff/reportes/usuario/{id}`  | Reportes enriquecidos de un usuario                      |

### Respuesta `GET /api/bff/dashboard`

```json
{
  "totalUsuarios": 15,
  "totalMascotas": 32,
  "totalReportes": 8,
  "reportesActivos": 5,
  "reportesPerdidos": 6,
  "reportesEncontrados": 2,
  "ultimosReportes": [ /* últimos 5 reportes enriquecidos */ ]
}
```

### Respuesta `GET /api/bff/reportes`

Cada reporte incluye:

```json
{
  "id": 1,
  "tipo": "PERDIDO",
  "descripcion": "Se perdió en el parque...",
  "estado": "ACTIVO",
  "latitud": -33.4569,
  "longitud": -70.6483,
  "ubicacionDescripcion": "Parque Forestal",
  "reporterUserId": 1,
  "nombreReporter": "Juan Pérez",
  "telefonoReporter": "+56912345678",
  "mascota": {
    "id": 3,
    "nombre": "Firulais",
    "especie": "perro",
    "fotoUrl": "http://localhost:8082/uploads/..."
  },
  "createdAt": "2026-05-16T10:30:00"
}
```

---

## ⚡ Circuit Breaker

El BFF implementa Resilience4j para protegerse de fallas en los servicios dependientes:

| Circuit Breaker   | Protege                       | Fallback                         |
|-------------------|-------------------------------|----------------------------------|
| `bff-dashboard`   | Llamadas al dashboard         | Retorna conteos en 0             |
| `bff-usuario`     | Consulta de usuario+mascotas  | Retorna datos "No disponible"    |
| `bff-reportes`    | Consulta de reportes          | Retorna lista vacía `[]`         |

El circuito se abre cuando el 50% de las últimas 10 llamadas fallan, y se intenta recuperar después de 10 segundos.

Puedes ver el estado de los circuit breakers en:
```
http://localhost:8084/actuator/circuitbreakers
```

---

## 🚀 Iniciar el servicio

```bash
cd infrastructure/bff-service
mvn spring-boot:run
```

Requiere que `config-server`, `eureka-server`, `user-service`, `pet-service` y `report-service` estén corriendo primero.

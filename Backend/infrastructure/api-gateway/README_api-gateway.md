# api-gateway

Punto único de entrada para todos los clientes de la plataforma **Sanos y Salvos**. Basado en **Spring Cloud Gateway** (reactivo). Gestiona autenticación JWT, CORS y enrutamiento hacia los microservicios internos.

---

## 📋 Responsabilidades

- Enrutar solicitudes del frontend a los microservicios correctos
- Validar el JWT en cada solicitud protegida
- Inyectar `X-User-Id` y `X-User-Role` como headers para los servicios internos
- Gestionar CORS para el origen `http://localhost:4200`
- Dejar pasar solicitudes `OPTIONS` (preflight CORS) sin validar JWT

---

## ⚙️ Configuración

### `application.yml` (local)

```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  config:
    import: "optional:configserver:${CONFIG_SERVER_URI:http://localhost:8888}"
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins:
              - "http://localhost:4200"
            allowedMethods:
              - GET
              - POST
              - PUT
              - PATCH        # ← Necesario para PATCH /reports/{id}/estado
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
      default-filters:
        - DedupeResponseHeader=Access-Control-Allow-Origin Access-Control-Allow-Credentials
```

### `config-server/config/api-gateway.yml` (centralizado — rutas)

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service-auth
          uri: lb://user-service
          predicates:
            - Path=/api/auth/**
          filters:
            - StripPrefix=1            # Sin AuthenticationFilter (login/register son públicos)

        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=1
            - AuthenticationFilter

        - id: pet-service
          uri: lb://pet-service
          predicates:
            - Path=/api/pets/**
          filters:
            - StripPrefix=1
            - AuthenticationFilter

        - id: report-service
          uri: lb://report-service
          predicates:
            - Path=/api/reports/**
          filters:
            - StripPrefix=1
            - AuthenticationFilter

        - id: bff-service
          uri: lb://bff-service
          predicates:
            - Path=/api/bff/**
          filters:
            - StripPrefix=1
            - AuthenticationFilter

      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins:
              - "http://localhost:4200"
            allowedMethods: [GET, POST, PUT, PATCH, DELETE, OPTIONS]
            allowedHeaders: "*"
            allowCredentials: true
```

> ⚠️ **Importante:** el `api-gateway.yml` del config-server sobreescribe el CORS del `application.yml` local. Asegúrate de que ambos incluyan `PATCH`.

---

## 🔐 AuthenticationFilter

Intercepta todas las rutas protegidas (las que llevan `- AuthenticationFilter`). Su lógica:

1. Si el método es `OPTIONS` → deja pasar sin validar (preflight CORS)
2. Si no hay header `Authorization: Bearer <token>` → retorna `401`
3. Si el token no es válido → retorna `401`
4. Si el token es válido → extrae `userId` y `role`, los inyecta como `X-User-Id` y `X-User-Role`, y reenvía la solicitud

### JWT Secret

```
sanosysalvos-secret-key-2024-duocuc-proyecto-semestral
```

Debe coincidir con el secret configurado en `user-service`.

---

## 🌐 Tabla de rutas

| Prefijo             | Servicio destino  | Auth JWT | Ruta interna              |
|---------------------|-------------------|----------|---------------------------|
| `/api/auth/**`      | user-service      | ❌       | `/auth/**`                |
| `/api/users/**`     | user-service      | ✅       | `/users/**`               |
| `/api/pets/**`      | pet-service       | ✅       | `/pets/**`                |
| `/api/reports/**`   | report-service    | ✅       | `/reports/**`             |
| `/api/bff/**`       | bff-service       | ✅       | `/bff/**`                 |

---

## 🐛 Problema conocido — CORS con PATCH

**Síntoma:** `No 'Access-Control-Allow-Origin' header is present` al cambiar estado de reporte.

**Causa:** `allowedMethods` en `api-gateway.yml` del config-server no incluía `PATCH`, y el `AuthenticationFilter` bloqueaba el preflight `OPTIONS` al no tener JWT.

**Solución:**
1. Agregar `PATCH` a `allowedMethods` en `config-server/src/main/resources/config/api-gateway.yml`
2. Agregar verificación `OPTIONS` en `AuthenticationFilter`:

```java
if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
    return chain.filter(exchange);
}
```

---

## 🚀 Iniciar el servicio

```bash
cd infrastructure/api-gateway
mvn spring-boot:run
```

Requiere que `config-server` y `eureka-server` estén corriendo primero.

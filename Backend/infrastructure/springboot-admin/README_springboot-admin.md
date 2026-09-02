# springboot-admin

Panel de monitoreo visual para todos los microservicios de la plataforma **Sanos y Salvos**. Basado en **Spring Boot Admin 3.2.3**. Descubre automáticamente los servicios registrados en Eureka y muestra su estado, métricas y logs en tiempo real.

---

## 📋 Responsabilidades

- Mostrar el estado (`UP` / `DOWN`) de todos los microservicios registrados
- Visualizar métricas (memoria, CPU, threads, solicitudes HTTP)
- Permitir ver y cambiar el nivel de logs en caliente (`/actuator/loggers`)
- Acceder a los health checks detallados de cada servicio
- Panel protegido con usuario y contraseña básicos

---

## ⚙️ Configuración

```yaml
server:
  port: 9090

spring:
  application:
    name: springboot-admin
  security:
    user:
      name: admin
      password: admin123

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
  instance:
    prefer-ip-address: true

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
```

---

## 🌐 Acceso al panel

URL: `http://localhost:9090`

| Campo    | Valor      |
|----------|------------|
| Usuario  | `admin`    |
| Contraseña | `admin123` |

---

## 🔍 Qué puedes ver en el panel

Para cada microservicio registrado en Eureka:

| Sección         | Descripción                                           |
|-----------------|-------------------------------------------------------|
| **Health**      | Estado general y detalle de cada indicador de salud   |
| **Metrics**     | JVM (heap, GC), HTTP requests, threads activos        |
| **Environment** | Variables de entorno y propiedades activas            |
| **Loggers**     | Ver y cambiar el nivel de log (DEBUG, INFO, WARN...) en caliente |
| **Mappings**    | Todos los endpoints expuestos por el servicio         |
| **Threads**     | Estado de los threads de la JVM                       |

---

## 📡 Requisito: Actuator en los servicios

Para que Spring Boot Admin pueda monitorear un servicio, este debe exponer los endpoints de Actuator. Todos los servicios del proyecto ya lo hacen:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, loggers
  endpoint:
    health:
      show-details: always
```

---

## ⚠️ Consideraciones

- Este servicio es **opcional** para el funcionamiento de la plataforma
- No interfiere con la lógica de negocio
- El puerto `9090` no pasa por el API Gateway; se accede directamente
- En producción, cambiar la contraseña por defecto `admin123`

---

## 🚀 Iniciar el servicio

```bash
cd infrastructure/springboot-admin
mvn spring-boot:run
```

Requiere que `eureka-server` esté corriendo. Los demás servicios aparecerán automáticamente cuando estén activos.

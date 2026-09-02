# eureka-server

Servidor de descubrimiento de servicios para la plataforma **Sanos y Salvos**. Basado en **Netflix Eureka**. Permite que el API Gateway y el BFF localicen dinámicamente las instancias de cada microservicio sin necesitar URLs hardcodeadas.

---

## 📋 Responsabilidades

- Registrar todas las instancias de microservicios que se conecten
- Permitir al API Gateway resolver `lb://nombre-servicio` a una IP y puerto real
- Proveer un dashboard visual con el estado de todos los servicios registrados

---

## ⚙️ Configuración

```yaml
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false   # No se registra a sí mismo
    fetch-registry: false         # No consulta su propio registro
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

---

## 🌐 Dashboard

Disponible en: `http://localhost:8761`

Desde aquí puedes ver:
- Todos los microservicios registrados y su estado (UP / DOWN)
- Número de instancias activas por servicio
- Información de la instancia (IP, puerto, metadata)

---

## 📡 Cómo se registran los otros servicios

Cada microservicio incluye en su configuración:

```yaml
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URI:http://localhost:8761/eureka}
  instance:
    prefer-ip-address: false
    hostname: localhost
```

Al arrancar, el microservicio envía un `heartbeat` periódico a Eureka. Si deja de enviarlo (servicio caído), Eureka lo marca como no disponible después del timeout de expiración.

---

## ⚠️ Consideraciones

- Debe arrancar **después** de `config-server` y **antes** que el resto de servicios
- En Docker, cambiar `hostname: localhost` por el nombre del contenedor correspondiente
- Si un servicio tarda en registrarse, el gateway puede retornar `503 Service Unavailable` durante los primeros segundos

---

## 🚀 Iniciar el servicio

```bash
cd infrastructure/eureka-server
mvn spring-boot:run
```

Requiere que `config-server` esté corriendo primero.

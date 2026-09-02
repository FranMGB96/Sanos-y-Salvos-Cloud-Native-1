# config-server

Servidor de configuración centralizada para la plataforma **Sanos y Salvos**. Basado en **Spring Cloud Config Server**. Todos los microservicios obtienen su configuración de base de datos, JWT y rutas desde este servicio al arrancar.

---

## 📋 Responsabilidades

- Servir la configuración de cada microservicio desde archivos YAML locales
- Permitir cambiar la configuración de todos los servicios desde un único lugar sin recompilar
- Registrarse en Eureka para que otros servicios lo encuentren automáticamente

---

## ⚙️ Configuración propia

```yaml
server:
  port: 8888

spring:
  application:
    name: config-server
  profiles:
    active: native
  cloud:
    config:
      server:
        native:
          search-locations: classpath:/config
```

El perfil `native` hace que lea los YAMLs directamente del filesystem/classpath, sin necesidad de un repositorio Git.

---

## 📁 Archivos de configuración que provee

Ubicados en `src/main/resources/config/`:

| Archivo               | Configura                                              |
|-----------------------|--------------------------------------------------------|
| `api-gateway.yml`     | Rutas, CORS, filtros de autenticación, JWT secret      |
| `user-service.yml`    | Datasource `user_db`, JWT secret y expiración, Swagger |
| `pet-service.yml`     | Datasource `pet_db`, Swagger                           |
| `report-service.yml`  | Datasource `report_db`, Swagger                        |
| `bff-service.yml`     | URLs internas de user, pet y report service            |

---

## 🌐 Cómo consumen la configuración los otros servicios

Cada microservicio declara en su `application.yml` local:

```yaml
spring:
  config:
    import: "optional:configserver:${CONFIG_SERVER_URI:http://localhost:8888}"
```

Al arrancar, el servicio llama a `http://localhost:8888/<nombre-servicio>/default` y recibe su configuración.

---

## 🔍 Verificar que funciona

Puedes consultar la configuración de cualquier servicio directamente:

```bash
# Ver configuración del user-service
curl http://localhost:8888/user-service/default

# Ver configuración del api-gateway
curl http://localhost:8888/api-gateway/default
```

---

## ⚠️ Consideraciones

- Debe ser el **primer servicio en arrancar**
- Si el config-server no está disponible, los demás servicios arrancan con su configuración local gracias a `optional:`
- Para cambiar una configuración, edita el YAML correspondiente en `src/main/resources/config/` y reinicia el servicio afectado (o usa `/actuator/refresh` si está habilitado)

---

## 🚀 Iniciar el servicio

```bash
cd infrastructure/config-server
mvn spring-boot:run
```

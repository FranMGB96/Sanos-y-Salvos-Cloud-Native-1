# 🐾 Sanos y Salvos — Cloud Native I (DSY1107)

Plataforma web de reporte y búsqueda de mascotas perdidas. Permite a los usuarios registrar sus mascotas y publicar reportes geolocalizados cuando una mascota se pierde o es encontrada.

Este repositorio corresponde al ramo **Desarrollo Cloud Native I**, que retoma el proyecto de un ramo anterior (Fullstack III) y lo fue llevando a una arquitectura de nube real en tres evaluaciones:

| Evaluación | Qué se agregó |
|---|---|
| **Eval 1** | Autenticación con **Microsoft Entra ID (Azure AD)** vía **MSAL** y **OAuth 2.0 / OpenID Connect**, backend como **Resource Server** y despliegue en **AWS** (API Gateway + EC2) |
| **Eval 2** | Mensajería asíncrona con **RabbitMQ**: clúster de 2 nodos, 3 colas con su DLQ, exchanges topic y direct, ACK manual y reintentos |
| **Eval 3** | Streaming de eventos con **Apache Kafka**: clúster de 3 ZooKeeper y 3 brokers, 2 topics con 3 particiones y 3 réplicas, **Kafka Streams**, administración de Kafka por API y panel de actividad en tiempo real |

---

## 🛠️ Stack tecnológico

| Capa | Tecnología |
|---|---|
| Frontend | Angular 17 · TypeScript · MSAL (`@azure/msal-angular`) |
| Backend | Java 21 · Spring Boot 3.2 · Spring Cloud |
| Identidad (IDaaS) | Microsoft Entra ID (Azure AD) — OAuth 2.0 / OIDC |
| Seguridad backend | Spring Security · OAuth2 Resource Server (`spring-cloud-azure-starter-active-directory`) |
| Mensajería | RabbitMQ 3 (clúster de 2 nodos) · Spring AMQP |
| Streaming | Apache Kafka 3.5 (Confluent 7.5) · Spring Kafka · Kafka Streams · Kafka UI |
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
| Config Server | 8888 | Configuración centralizada para todos los servicios (incluye los nombres de colas, exchanges y topics) |
| API Gateway | 8080 | Punto de entrada único y enrutamiento interno |
| BFF Service | 8084 | Backend for Frontend — agrega datos y valida el JWT de Azure AD |
| RabbitMQ Admin Service | 8086 | API REST para administrar colas, exchanges y bindings de RabbitMQ (Eval 2) |
| Kafka Admin Service | 8087 | API REST para administrar topics, configuraciones y consumer groups de Kafka (Eval 3) |

### Microservicios de negocio
| Servicio | Puerto | Descripción |
|---|---|---|
| User Service | 8081 | Gestión de usuarios · consume las alertas por zona desde Kafka |
| Pet Service | 8082 | CRUD de mascotas con subida de fotos · arma el historial de cada mascota desde Kafka |
| Report Service | 8083 | Reportes de mascotas perdidas/encontradas con geolocalización · publica en RabbitMQ y en Kafka |
| Category Service | 8085 | Catálogo de especies (Perro, Gato, Ave, Otros) para filtrar mascotas |
| Analytics Service | 8088 | Kafka Streams: contadores en vivo y detección de alertas por zona (Eval 3) |

### Mensajería y streaming
| Componente | Puerto | Descripción |
|---|---|---|
| rabbitmq-1 / rabbitmq-2 | 5672, 15672 / 5673, 15673 | Clúster de RabbitMQ y su dashboard de administración |
| zookeeper-1, 2 y 3 | 2181 (interno) | Ensemble de ZooKeeper que coordina el clúster de Kafka |
| kafka-1, 2 y 3 | 9092, 9093, 9094 (solo localhost) | Brokers de Kafka (dentro de Docker se usan en el puerto 29092) |
| Kafka UI | 8090 (solo localhost) | Panel web para ver brokers, topics, mensajes y consumer groups |

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
4. En la nube, **AWS API Gateway** valida el mismo token con un **JWT Authorizer** (`AzureAD-Authorizer`) antes de reenviar la petición al backend en EC2. Sin token, responde `401 Unauthorized` y la petición ni siquiera llega a la EC2.

### Apps registradas en Microsoft Entra ID

| App | Rol |
|---|---|
| `SanosYSalvos-Backend` | Expone la API (`api://<tenant>/sanosysalvos`) y el scope `Pets.Access` |
| `SanosYSalvos-Frontend` | Aplicación SPA que inicia sesión y solicita el scope del backend |

---

## 📨 Mensajería con RabbitMQ (Eval 2)

RabbitMQ se usa para **tareas puntuales** que se hacen una vez por mensaje. Corre como clúster de 2 nodos y todos los nombres están en el `application.yml` del Config Server (`sanosysalvos.messaging`). Las colas, exchanges y bindings se declaran como beans en `RabbitConfig` (report-service).

| Caso de uso | Exchange (tipo) | Routing key | Cola | Consumidor |
|---|---|---|---|---|
| Notificar reporte creado | `sys.reports.topic` (topic) | `report.created.#` | `notification.report-created.queue` | user-service |
| Generar ticket | `sys.tickets.direct` (direct) | `ticket.generate` | `ticket.generation.queue` | report-service |
| Auditar cambio de estado | `sys.reports.topic` (topic) | `report.status.*` | `audit.report-status.queue` | pet-service |

- Cada cola tiene su **DLQ** (`*.dlq`) a través del exchange `sys.dlx`.
- **ACK manual** y **3 reintentos** con backoff antes de mandar el mensaje a la DLQ. Los mensajes inválidos van directo a la DLQ.
- Para la demo, si la descripción de un reporte contiene `[FALLO-TEMPORAL]`, el consumidor simula un error y se ven los reintentos y la DLQ.

---

## 📊 Streaming con Kafka (Eval 3)

Kafka se suma a RabbitMQ, **no lo reemplaza**: RabbitMQ ejecuta tareas puntuales, y Kafka guarda el **flujo de eventos** para que varios servicios lo lean por su cuenta y se pueda analizar en tiempo real.

### Casos de uso
**CU1 · Seguimiento de reportes en tiempo real (`sys.reports.events`).** Cada vez que se crea un reporte o cambia su estado, report-service publica un `ReportEvent` (key = `reportId`, así los eventos de un reporte quedan en la misma partición y en orden). Lo leen dos grupos por separado:
- **analytics-service** (`analytics-group`): contadores en vivo para el panel de actividad.
- **pet-service** (`pet-history-group`): historial de eventos de cada mascota.

**CU2 · Alertas por zona (`sys.reports.alerts`).** analytics-service genera una alerta cuando hay **3 o más mascotas perdidas en la misma zona (~1 km) en una ventana de 60 minutos** (5 o más = ALTA) y la publica con key = zona. La lee **user-service** (`user-alerts-group`), que la guarda y envía un aviso simulado.

### Topics
| Topic | Particiones / réplicas | Retención | Para qué |
|---|---|---|---|
| `sys.reports.events` | 3 / 3 | 7 días | Historial de eventos de reportes; se puede volver a leer |
| `sys.reports.alerts` | 3 / 3 | 24 horas | Alertas por zona; una alerta vieja ya no sirve |
| `sys.reports.events.DLT` y `sys.reports.alerts.DLT` | 3 / 3 | 14 días | Mensajes que fallaron, para revisarlos |

Todos con `cleanup.policy=delete` y `min.insync.replicas=2`: con 3 brokers se puede caer uno y se sigue escribiendo sin perder datos. La creación automática de topics está apagada; los topics se declaran como beans `NewTopic` en `KafkaAdminConfig` y sus nombres solo existen en el `application.yml` del Config Server (`sanosysalvos.kafka.topics`).

### Detalles de implementación
- **Productor** (`KafkaReportEventProducer`): `acks=all`, idempotente, y si Kafka está caído el reporte se crea igual (el error queda en el log).
- **Kafka Streams** (`AnalyticsTopology`): procesamiento *exactly-once* (`exactly_once_v2`), 3 hilos, contadores y alertas guardados en state stores con respaldo en topics changelog. El panel consulta esos stores directamente (consultas interactivas).
- **Consumidores** (`@KafkaListener` en clases dedicadas, con la lógica en servicios aparte): 3 hilos por grupo (uno por partición) y lectura `read_committed`.
- **Manejo de errores**: validación con Bean Validation (`KafkaEventParser`), reintentos con backoff exponencial (1 s y 2 s), **Dead Letter Topic** con el mensaje original y headers que explican el error, idempotencia por `eventId`/`alertaId` y logs con el id del evento (correlationId).
- **Frontend**: página **📊 Actividad** que muestra los contadores y las alertas, y se actualiza sola cada 5 segundos.

---

## 📁 Estructura del repositorio

```
Sanos-y-Salvos-Cloud-Native-1/
├── Backend/
│   ├── businessdomain/
│   │   ├── user-service/          # Usuarios + consumidor de alertas (Kafka)
│   │   ├── pet-service/           # Mascotas + historial desde Kafka
│   │   ├── report-service/        # Reportes + productor de RabbitMQ y Kafka
│   │   ├── category-service/      # Catálogo de especies
│   │   └── analytics-service/     # Kafka Streams: contadores y alertas (Eval 3)
│   ├── infrastructure/
│   │   ├── api-gateway/           # Enrutamiento interno
│   │   ├── bff-service/           # Backend for Frontend + validación JWT (Azure AD)
│   │   ├── config-server/         # Configuración centralizada (colas, exchanges y topics)
│   │   ├── eureka-server/         # Service Discovery
│   │   ├── rabbitmq-admin-service/# Administración de RabbitMQ por API (Eval 2)
│   │   └── kafka-admin-service/   # Administración de Kafka por API (Eval 3)
│   ├── docker-compose.yml         # Todo el sistema: 25 contenedores
│   └── pom.xml
└── Frontend/
    ├── src/
    │   ├── app/
    │   │   ├── msal-config.ts     # Configuración de MSAL (instancia, guard, interceptor)
    │   │   ├── core/              # Guards, servicios, modelos, interceptores
    │   │   ├── features/
    │   │   │   ├── activity/      # Panel de actividad en tiempo real (Eval 3)
    │   │   │   ├── admin/         # Panel de administración
    │   │   │   ├── auth/          # Login (MSAL) y página de auth-redirect
    │   │   │   ├── dashboard/     # Página de inicio
    │   │   │   ├── pets/          # Lista y formulario de mascotas
    │   │   │   └── reports/       # Lista y formulario de reportes
    │   │   └── shared/            # Navbar, footer
    │   └── environments/          # Config de Azure AD (clientId, authority, scopes)
    └── Dockerfile
```

---

## 🚀 Cómo ejecutar el proyecto localmente

### Prerrequisitos
- Java 21 (con un JDK más nuevo, Lombok no compila en IntelliJ), Maven 3.9+
- Node.js y npm
- Docker Desktop (ideal con 8 GB de RAM o más asignados)
- Un tenant de Microsoft Entra ID con las dos apps registradas (ver sección de Seguridad)

### 1. Backend completo (Docker)
Todo el backend corre con Docker Compose. Primero ZooKeeper, y unos 30 segundos después el resto (así los brokers no chocan con sesiones antiguas de ZooKeeper al reiniciar):

```bash
cd Backend
docker compose up -d zookeeper-1 zookeeper-2 zookeeper-3
# esperar ~30 segundos
docker compose up -d --build
docker compose ps --format "table {{.Name}}\t{{.Status}}"
```

Deben quedar **25 contenedores** arriba (los 3 brokers, las bases de datos y RabbitMQ en *healthy*). Para detener todo sin perder datos: `docker compose stop`.

### 2. Frontend
El contenedor `frontend` del compose usa la configuración de producción (apunta a AWS). Para probar en local, se detiene ese contenedor y se usa `ng serve`:

```bash
docker stop frontend
cd Frontend
npm install
npm start
```

La aplicación queda disponible en **http://localhost:4200**. Al abrirla, el login redirige a Microsoft para autenticar contra el tenant configurado en `environment.ts`.

### 3. Paneles y herramientas
| Herramienta | URL |
|---|---|
| Kafka UI | http://localhost:8090 |
| Swagger del Kafka Admin | http://localhost:8087/swagger-ui.html |
| Swagger del RabbitMQ Admin | http://localhost:8086/swagger-ui.html |
| Dashboard de RabbitMQ | http://localhost:15672 |
| Eureka | http://localhost:8761 |

---

## 🌐 Endpoints principales

Todos pasan por el API Gateway en `http://localhost:8080` (en la nube, por AWS API Gateway con el token de Azure AD):

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/users` | Listar usuarios |
| GET | `/api/users/alertas` | Últimas alertas por zona recibidas desde Kafka |
| GET | `/api/pets` | Listar mascotas |
| POST | `/api/pets` | Registrar mascota |
| GET | `/api/pets/{id}/historial` | Historial de eventos de una mascota (Kafka) |
| GET | `/api/categories` | Listar especies (Perro, Gato, Ave, Otros) |
| GET | `/api/reports` | Listar reportes |
| POST | `/api/reports` | Crear reporte (publica en RabbitMQ y en Kafka) |
| PATCH | `/api/reports/{id}/estado` | Cambiar el estado de un reporte |
| GET | `/api/analytics/resumen` | Contadores en vivo (Kafka Streams) |
| GET | `/api/analytics/alertas` | Última alerta de cada zona |
| GET | `/api/bff/dashboard` | Dashboard con estadísticas (requiere JWT de Azure AD) |

Administración (solo por localhost o túnel SSH, no pasan por el gateway):

| Servicio | Endpoints |
|---|---|
| Kafka Admin (8087) | `GET/POST /kafka/topics` · `GET/DELETE /kafka/topics/{nombre}` · `PUT /kafka/topics/{nombre}/config` · `PUT /kafka/topics/{nombre}/partitions` · `GET /kafka/consumer-groups/{groupId}` · `GET /kafka/cluster` |
| RabbitMQ Admin (8086) | Colas, exchanges, bindings y publicación de mensajes de prueba |

---

## 🧪 Pruebas

| Tipo | Cantidad | Herramienta |
|---|---|---|
| Unitarias de Kafka (productor, Kafka Streams, consumidores, validación y admin) | 24 | JUnit 5 + Mockito + TopologyTestDriver |
| Unitarias de los servicios | 8 | JUnit 5 + Mockito |
| Integración | 14 | Spring Boot Test + H2 |
| E2E | 15 | Cypress |

La topología de Kafka Streams se prueba completa sin brokers con `TopologyTestDriver`. Para correr las pruebas de Kafka desde la carpeta `Backend`:

```bash
mvn -pl businessdomain/report-service,businessdomain/pet-service,businessdomain/user-service,businessdomain/analytics-service,infrastructure/kafka-admin-service -am test
```

---

## ☁️ Despliegue en la nube

| Componente | Estado |
|---|---|
| Tenant de Microsoft Entra ID + apps registradas | ✅ Listo |
| Frontend con MSAL (login/logout, guards, interceptor) | ✅ Listo |
| BFF como Resource Server (valida JWT de Azure AD) | ✅ Listo |
| AWS API Gateway con JWT Authorizer (incluida la ruta `/analytics`) | ✅ Listo |
| Microservicios desplegados en EC2 (r5.large, IP elástica) | ✅ Listo |
| Clúster de RabbitMQ (2 nodos) en EC2 | ✅ Listo |
| Clúster de Kafka (3 ZooKeeper + 3 brokers) y Kafka UI en EC2 | ✅ Listo |

En la EC2, los paneles de administración no se exponen a internet: se abren con un túnel SSH.

```bash
ssh -i "<ruta-a-la-llave>.pem" -L 15672:localhost:15672 -L 8086:localhost:8086 -L 8090:localhost:8090 -L 8087:localhost:8087 ec2-user@<ip-elastica>
```

Con el túnel abierto: Kafka UI en `http://localhost:8090`, Kafka Admin en `http://localhost:8087/swagger-ui.html` y RabbitMQ en `http://localhost:15672`.

---

## 👥 Equipo

Proyecto desarrollado como parte del ramo de **Desarrollo Cloud Native I** en DuocUC.

| Integrante | GitHub |
|---|---|
| Aaron Ojeda | [@aaron16-code](https://github.com/aaron16-code) |
| Ángel Pérez | — |
| Francisco García | [@FranMGB96](https://github.com/FranMGB96) |

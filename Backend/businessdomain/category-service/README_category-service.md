# category-service

Microservicio de negocio para Sanos y Salvos. Administra el catálogo de
categorías/especies (ej. Perro, Gato, Ave) usado para filtrar mascotas
perdidas.

## Cómo integrarlo en el monorepo

1. Copia esta carpeta completa a:
   `Sanos-y-Salvos-main/Backend/businessdomain/category-service`

2. En `Backend/businessdomain/pom.xml`, agrega el módulo:
   ```xml
   <modules>
       <module>user-service</module>
       <module>pet-service</module>
       <module>report-service</module>
       <module>category-service</module>
   </modules>
   ```

3. En `Backend/infrastructure/config-server/src/main/resources/config/`,
   crea `category-service.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://${DB_HOST:localhost}:3310/category_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
       username: root
       password: root
       driver-class-name: com.mysql.cj.jdbc.Driver
     jpa:
       hibernate:
         ddl-auto: update
       show-sql: false
       properties:
         hibernate:
           dialect: org.hibernate.dialect.MySQLDialect

   springdoc:
     api-docs:
       path: /api-docs
     swagger-ui:
       path: /swagger-ui.html
   ```

4. En `Backend/infrastructure/api-gateway/src/main/resources/config/api-gateway.yml`
   (o donde tengas las rutas), agrega:
   ```yaml
           - id: category-service
             uri: lb://category-service
             predicates:
               - Path=/api/categories/**
             filters:
               - StripPrefix=1
               - AuthenticationFilter
   ```

5. En `Backend/docker-compose.yml`, agrega el servicio de base de datos y el
   microservicio (puerto backend 8085, puerto MySQL 3310 — son los
   siguientes libres tras user/pet/report/bff):
   ```yaml
     category-db:
       image: mysql:8.0
       container_name: category-db
       environment:
         MYSQL_ROOT_PASSWORD: root
         MYSQL_DATABASE: category_db
       ports:
         - "3310:3306"
       networks:
         - petshop-net
       volumes:
         - category-db-data:/var/lib/mysql
       healthcheck:
         test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
         interval: 10s
         timeout: 5s
         retries: 5

     category-service:
       build:
         context: .
         dockerfile: businessdomain/category-service/Dockerfile
       container_name: category-service
       ports:
         - "8085:8085"
       environment:
         DB_HOST: category-db
         EUREKA_URI: http://eureka-server:8761/eureka
         CONFIG_SERVER_URI: http://config-server:8888
       depends_on:
         category-db:
           condition: service_healthy
         config-server:
           condition: service_healthy
       networks:
         - petshop-net
   ```
   Y agrega `category-db-data:` a la sección `volumes:` al final del
   archivo.

## Endpoints

| Método | Ruta                | Descripción            |
|--------|----------------------|-------------------------|
| GET    | `/categories`        | Lista todas las categorías |
| GET    | `/categories/{id}`   | Obtiene una categoría por id |
| POST   | `/categories`        | Crea una categoría (`{ "nombre": "Gato" }`) |
| PUT    | `/categories/{id}`   | Actualiza una categoría |
| DELETE | `/categories/{id}`   | Elimina una categoría |

Vía API Gateway, quedan expuestas como `/api/categories/**`.

Para filtrar mascotas por especie, usa el endpoint que ya existe en
`pet-service`: `GET /pets/especie/{especie}`, pasando como `{especie}` el
mismo texto que el `nombre` de la categoría elegida.

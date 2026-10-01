FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Copiar pom padre raíz
COPY pom.xml ./pom.xml
# Copiar poms agrupadores
COPY infrastructure/pom.xml ./infrastructure/pom.xml
COPY businessdomain/pom.xml ./businessdomain/pom.xml
# Copiar poms hijos (Maven necesita ver TODOS los módulos para resolver el proyecto)
COPY businessdomain/user-service/pom.xml     ./businessdomain/user-service/pom.xml
COPY businessdomain/pet-service/pom.xml      ./businessdomain/pet-service/pom.xml
COPY businessdomain/report-service/pom.xml   ./businessdomain/report-service/pom.xml
COPY businessdomain/category-service/pom.xml ./businessdomain/category-service/pom.xml
COPY infrastructure/eureka-server/pom.xml    ./infrastructure/eureka-server/pom.xml
COPY infrastructure/config-server/pom.xml    ./infrastructure/config-server/pom.xml
COPY infrastructure/api-gateway/pom.xml      ./infrastructure/api-gateway/pom.xml
COPY infrastructure/bff-service/pom.xml      ./infrastructure/bff-service/pom.xml
COPY infrastructure/springboot-admin/pom.xml ./infrastructure/springboot-admin/pom.xml
COPY infrastructure/rabbitmq-admin-service/pom.xml ./infrastructure/rabbitmq-admin-service/pom.xml
RUN mvn dependency:go-offline -pl infrastructure/rabbitmq-admin-service -am -B
# Copiar solo el código fuente del servicio que vamos a compilar
COPY infrastructure/rabbitmq-admin-service/src ./infrastructure/rabbitmq-admin-service/src
RUN mvn package -pl infrastructure/rabbitmq-admin-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/infrastructure/rabbitmq-admin-service/target/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]

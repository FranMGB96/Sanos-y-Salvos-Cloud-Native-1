FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Copiar pom padre raíz y poms agrupadores
COPY pom.xml ./pom.xml
COPY infrastructure/pom.xml ./infrastructure/pom.xml
COPY businessdomain/pom.xml ./businessdomain/pom.xml
# Copiar poms hijos (Maven necesita ver TODOS los módulos para resolver el proyecto)
COPY businessdomain/user-service/pom.xml      ./businessdomain/user-service/pom.xml
COPY businessdomain/pet-service/pom.xml       ./businessdomain/pet-service/pom.xml
COPY businessdomain/report-service/pom.xml    ./businessdomain/report-service/pom.xml
COPY businessdomain/category-service/pom.xml  ./businessdomain/category-service/pom.xml
COPY businessdomain/analytics-service/pom.xml ./businessdomain/analytics-service/pom.xml
COPY infrastructure/eureka-server/pom.xml     ./infrastructure/eureka-server/pom.xml
COPY infrastructure/config-server/pom.xml     ./infrastructure/config-server/pom.xml
COPY infrastructure/api-gateway/pom.xml       ./infrastructure/api-gateway/pom.xml
COPY infrastructure/bff-service/pom.xml       ./infrastructure/bff-service/pom.xml
COPY infrastructure/springboot-admin/pom.xml  ./infrastructure/springboot-admin/pom.xml
COPY infrastructure/rabbitmq-admin-service/pom.xml ./infrastructure/rabbitmq-admin-service/pom.xml
COPY infrastructure/kafka-admin-service/pom.xml    ./infrastructure/kafka-admin-service/pom.xml
RUN mvn dependency:go-offline -pl infrastructure/kafka-admin-service -am -B
# Copiar solo el código fuente del servicio que vamos a compilar
COPY infrastructure/kafka-admin-service/src ./infrastructure/kafka-admin-service/src
RUN mvn package -pl infrastructure/kafka-admin-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/infrastructure/kafka-admin-service/target/*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]

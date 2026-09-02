FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
# Copiar pom padre raíz
COPY pom.xml ./pom.xml
# Copiar pom agrupador infrastructure
COPY infrastructure/pom.xml ./infrastructure/pom.xml
# Copiar pom agrupador businessdomain (Maven lo necesita para resolver el proyecto)
COPY businessdomain/pom.xml ./businessdomain/pom.xml
# Copiar poms hijos solo para resolución de dependencias
COPY infrastructure/bff-service/pom.xml ./infrastructure/bff-service/pom.xml
COPY businessdomain/user-service/pom.xml   ./businessdomain/user-service/pom.xml
COPY businessdomain/pet-service/pom.xml    ./businessdomain/pet-service/pom.xml
COPY businessdomain/report-service/pom.xml ./businessdomain/report-service/pom.xml
COPY infrastructure/eureka-server/pom.xml  ./infrastructure/eureka-server/pom.xml
COPY infrastructure/config-server/pom.xml  ./infrastructure/config-server/pom.xml
COPY infrastructure/api-gateway/pom.xml    ./infrastructure/api-gateway/pom.xml
COPY infrastructure/bff-service/pom.xml    ./infrastructure/bff-service/pom.xml
RUN mvn dependency:go-offline -pl infrastructure/bff-service -am -B
# Copiar solo el código fuente del servicio que vamos a compilar
COPY infrastructure/bff-service/src ./infrastructure/bff-service/src
RUN mvn package -pl infrastructure/bff-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/infrastructure/bff-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]

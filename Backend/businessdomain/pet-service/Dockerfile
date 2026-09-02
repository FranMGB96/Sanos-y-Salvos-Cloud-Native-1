FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./pom.xml
COPY businessdomain/pom.xml ./businessdomain/pom.xml
COPY infrastructure/pom.xml ./infrastructure/pom.xml
COPY businessdomain/pet-service/pom.xml ./businessdomain/pet-service/pom.xml
COPY businessdomain/user-service/pom.xml   ./businessdomain/user-service/pom.xml
COPY businessdomain/pet-service/pom.xml    ./businessdomain/pet-service/pom.xml
COPY businessdomain/report-service/pom.xml ./businessdomain/report-service/pom.xml
COPY infrastructure/eureka-server/pom.xml  ./infrastructure/eureka-server/pom.xml
COPY infrastructure/config-server/pom.xml  ./infrastructure/config-server/pom.xml
COPY infrastructure/api-gateway/pom.xml    ./infrastructure/api-gateway/pom.xml
COPY infrastructure/bff-service/pom.xml    ./infrastructure/bff-service/pom.xml
RUN mvn dependency:go-offline -pl businessdomain/pet-service -am -B
COPY businessdomain/pet-service/src ./businessdomain/pet-service/src
RUN mvn package -pl businessdomain/pet-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/businessdomain/pet-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]

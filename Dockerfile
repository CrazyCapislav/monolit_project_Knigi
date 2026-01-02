FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

ARG SERVICE_NAME

COPY backend ./backend

WORKDIR /app/backend
RUN mvn clean package -DskipTests -pl ${SERVICE_NAME} -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

ARG SERVICE_NAME

COPY --from=build /app/backend/${SERVICE_NAME}/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
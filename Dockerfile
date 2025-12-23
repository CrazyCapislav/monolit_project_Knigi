FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

ARG SERVICE_NAME

# Настройка Maven для использования только Maven Central
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>' > /root/.m2/settings.xml && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.2.0">' >> /root/.m2/settings.xml && \
    echo '  <mirrors>' >> /root/.m2/settings.xml && \
    echo '    <mirror>' >> /root/.m2/settings.xml && \
    echo '      <id>central</id>' >> /root/.m2/settings.xml && \
    echo '      <name>Maven Central</name>' >> /root/.m2/settings.xml && \
    echo '      <url>https://repo1.maven.org/maven2</url>' >> /root/.m2/settings.xml && \
    echo '      <mirrorOf>*</mirrorOf>' >> /root/.m2/settings.xml && \
    echo '    </mirror>' >> /root/.m2/settings.xml && \
    echo '  </mirrors>' >> /root/.m2/settings.xml && \
    echo '</settings>' >> /root/.m2/settings.xml

# Шаг 1: Копируем parent POM
COPY backend/pom.xml ./backend/pom.xml

# Шаг 2: Устанавливаем parent POM в локальный репозиторий (кэшируется)
WORKDIR /app/backend
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn clean install -N -DskipTests -B

# Шаг 3: Копируем структуру директорий модулей с pom.xml файлами
# Создаем директории и копируем pom.xml для каждого модуля
COPY backend/eureka-server/pom.xml ./eureka-server/pom.xml
COPY backend/config-server/pom.xml ./config-server/pom.xml
COPY backend/gateway/pom.xml ./gateway/pom.xml
COPY backend/auth-service/pom.xml ./auth-service/pom.xml
COPY backend/book-service/pom.xml ./book-service/pom.xml
COPY backend/exchange-service/pom.xml ./exchange-service/pom.xml
COPY backend/publication-service/pom.xml ./publication-service/pom.xml
COPY backend/jacoco-report/pom.xml ./jacoco-report/pom.xml

# Шаг 4: Скачиваем зависимости для всех модулей (кэшируется)
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn dependency:go-offline -B || true

# Шаг 5: Копируем исходный код конкретного сервиса
COPY backend/${SERVICE_NAME}/src ./${SERVICE_NAME}/src

# Шаг 5: Компилируем только измененный сервис (с кэшированием Maven репозитория)
RUN --mount=type=cache,target=/root/.m2/repository \
    mvn clean package -DskipTests -pl ${SERVICE_NAME} -am -B

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

ARG SERVICE_NAME

COPY --from=build /app/backend/${SERVICE_NAME}/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
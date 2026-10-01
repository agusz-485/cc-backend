# Etapa 1: Compilacion con Temurin JDK 21
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copiar configuracion de Maven y dar permisos de ejecucion
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Compilar empaquetado omitiendo tests
COPY src/ ./src/
RUN ./mvnw clean package -DskipTests

# Etapa 2: Runtime liviano JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear directorio de uploads y dar permisos completos al usuario spring
RUN addgroup -S spring && adduser -S spring -G spring && \
    mkdir -p /app/uploads && \
    chown -R spring:spring /app

USER spring:spring

COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

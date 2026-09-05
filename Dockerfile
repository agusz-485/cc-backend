# Etapa 1: Compilación con Temurin JDK 21
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Cachear dependencias de Maven
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

# Compilar empaquetado omitiendo tests unitarios
COPY src/ ./src/
RUN ./mvnw clean package -DskipTests

# Etapa 2: Runtime liviano JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario sin privilegios por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
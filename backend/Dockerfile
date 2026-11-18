# Multi-stage build: compile inside Docker so `docker compose up --build` works without a prebuilt JAR
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# Use non-alpine JRE for broader platform support (incl. Apple Silicon)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/learnova-app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

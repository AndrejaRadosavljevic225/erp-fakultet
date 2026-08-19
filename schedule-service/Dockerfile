# syntax=docker/dockerfile:1
# --- Build (Maven + JDK 21); gradi i erp-common preko -am ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn -q -pl schedule-service -am package -DskipTests

# --- Run (JRE 21) ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/schedule-service/target/schedule-service-0.0.1-SNAPSHOT.war app.war
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.war"]

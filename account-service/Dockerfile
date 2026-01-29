# ===== STAGE 1: build (Java 25) =====
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -q -B -e dependency:go-offline

COPY src ./src
RUN mvn -q -B -e package -DskipTests

# ===== STAGE 2: runtime (Java 25) =====
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

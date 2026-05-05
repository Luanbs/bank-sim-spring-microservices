FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

COPY . .

RUN mvn -q -B -e -DskipTests package -pl auth-service -am

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /build/auth-service/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
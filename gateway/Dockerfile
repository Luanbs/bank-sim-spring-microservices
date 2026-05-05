FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build

# copia tudo (multi-module)
COPY . .

# builda gateway + dependências (common-lib)
RUN mvn -q -B -e -DskipTests package -pl gateway -am

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /build/gateway/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
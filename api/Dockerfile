FROM maven:3.9.11-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml ./
RUN mvn --batch-mode dependency:go-offline

COPY src ./src
RUN mvn --batch-mode -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S simpleguard && adduser -S simpleguard -G simpleguard
COPY --from=build /workspace/target/api-*.jar /app/api.jar

USER simpleguard
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/api.jar"]

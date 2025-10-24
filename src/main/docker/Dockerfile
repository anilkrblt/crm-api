FROM maven:3.9-eclipse-temurin-17 AS builder


WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests



FROM eclipse-temurin:17-jre-focal
WORKDIR /app
RUN addgroup --system appgroup && \
    adduser --system --ingroup appgroup appuser

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080


COPY --from=builder /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
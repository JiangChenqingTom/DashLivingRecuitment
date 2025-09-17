FROM openjdk:11-jdk-slim AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle/
COPY build.gradle .
COPY settings.gradle .

COPY dashliving-common/ dashliving-common/
COPY dashliving-gateway-svc/ dashliving-gateway-svc/

RUN chmod +x ./gradlew
RUN ./gradlew :dashliving-gateway-svc:clean :dashliving-gateway-svc:build -x test


FROM openjdk:11-jre-slim

WORKDIR /app

COPY --from=builder /app/dashliving-gateway-svc/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

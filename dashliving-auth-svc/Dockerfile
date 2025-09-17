FROM openjdk:11-jdk-slim AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle/
COPY build.gradle .
COPY settings.gradle .

COPY dashliving-common/ dashliving-common/
COPY dashliving-auth-svc/ dashliving-auth-svc/

RUN chmod +x ./gradlew
RUN ./gradlew :dashliving-auth-svc:clean :dashliving-auth-svc:build -x test


FROM openjdk:11-jre-slim

WORKDIR /app

COPY --from=builder /app/dashliving-auth-svc/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]

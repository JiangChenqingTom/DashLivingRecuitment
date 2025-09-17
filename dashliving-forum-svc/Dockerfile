FROM openjdk:11-jdk-slim AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle/
COPY build.gradle .
COPY settings.gradle .

COPY dashliving-common/ dashliving-common/
COPY dashliving-forum-svc/ dashliving-forum-svc/

RUN chmod +x ./gradlew
RUN ./gradlew :dashliving-forum-svc:clean :dashliving-forum-svc:build -x test


FROM openjdk:11-jre-slim

WORKDIR /app

COPY --from=builder /app/dashliving-forum-svc/build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]

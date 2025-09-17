FROM openjdk:11-jdk-slim AS builder

WORKDIR /app

# 复制gradle相关文件
COPY ../../gradlew .
COPY ../../gradle gradle
COPY build.gradle .
COPY ../../settings.gradle .

# 复制源代码
COPY src src

RUN chmod +x ./gradlew

# 构建gateway服务
RUN ./gradlew :dashliving-gateway-svc:clean :dashliving-gateway-svc:build -x test

# 运行时镜像
FROM openjdk:11-jre-slim

WORKDIR /app

# 从构建阶段复制jar包
COPY --from=builder /app/dashliving-gateway-svc/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
FROM openjdk:11-jdk-slim AS builder

WORKDIR /app

# 复制gradle相关文件
COPY ../gradlew .
COPY ../gradle gradle
COPY build.gradle .
COPY ../settings.gradle .

# 复制源代码
COPY src/ src/

RUN chmod +x ./gradlew

# 构建auth服务
RUN ./gradlew :dashliving-auth-svc:clean :dashliving-auth-svc:build -x test

# 运行时镜像
FROM openjdk:11-jre-slim

WORKDIR /app

# 从构建阶段复制jar包
COPY --from=builder /app/dashliving-auth-svc/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
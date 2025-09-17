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

# 构建forum服务
RUN ./gradlew :dashliving-forum-svc:clean :dashliving-forum-svc:build -x test

# 运行时镜像
FROM openjdk:11-jre-slim

WORKDIR /app

# 从构建阶段复制jar包
COPY --from=builder /app/dashliving-forum-svc/build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
# 构建阶段：使用Java 11作为基础镜像，用于编译打包应用
FROM openjdk:11-jdk-slim AS builder

# 设置工作目录
WORKDIR /app

# 复制Gradle相关文件和项目配置文件
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# 赋予gradlew执行权限
RUN chmod +x ./gradlew

# 构建项目，生成可执行Jar包
RUN ./gradlew clean build -x test

# 运行阶段：使用Java 11运行时镜像，减小最终镜像体积
FROM openjdk:11-jre-slim

# 设置工作目录
WORKDIR /app

# 从构建阶段复制打包好的Jar包到当前目录
COPY --from=builder /app/build/libs/*.jar app.jar

# 暴露应用端口（与docker-compose.yml中映射的端口一致）
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
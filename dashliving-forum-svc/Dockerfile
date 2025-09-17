FROM openjdk:11-jdk-slim AS builder

WORKDIR /app

# 1. 复制根项目的Gradle核心配置（必须）
COPY gradlew .
COPY gradle gradle/
COPY build.gradle .
COPY settings.gradle .

# 2. 复制所有子模块（包括依赖的common和目标forum-svc）
# 关键：保持子模块的目录结构，不破坏层级
COPY dashliving-common/ dashliving-common/  # 复制依赖的common模块
COPY dashliving-forum-svc/ dashliving-forum-svc/  # 复制目标子项目（包含其src和build.gradle）

# 3. 赋予执行权限并构建
RUN chmod +x ./gradlew
# 构建目标子项目（使用模块路径，确保Gradle能找到）
RUN ./gradlew :dashliving-forum-svc:clean :dashliving-forum-svc:build -x test


# 运行时镜像
FROM openjdk:11-jre-slim

WORKDIR /app

# 从构建阶段复制打包好的JAR（路径对应子项目的build/libs）
COPY --from=builder /app/dashliving-forum-svc/build/libs/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]

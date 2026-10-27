# ============================================================
# video-web 多阶段构建镜像
# 构建:  docker build -t video-web:latest .
# 说明:  构建阶段使用 JDK 21 打包，运行阶段仅保留 JRE 21
# ============================================================

# ---------- 构建阶段 ----------
# 注意：Spring Boot 插件 3.1.4 与 Gradle 9 不兼容（FileCopyDetails.getMode() 已被移除），
# 必须使用 Gradle 8.5 构建（与 gradle-wrapper.properties 保持一致）
FROM gradle:8.5-jdk21 AS builder
USER root
WORKDIR /build
# 先复制构建脚本（利用 Docker 层缓存，依赖不变时无需重复下载）
COPY build.gradle settings.gradle ./
RUN gradle --no-daemon dependencies
# 复制源码并打包（跳过测试）
COPY src ./src
RUN gradle --no-daemon clean bootJar -x test

# ---------- 运行阶段 ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# 时区与 JVM 参数
ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms256m -Xmx512m -Dfile.encoding=UTF-8" \
    # 文件上传目录（覆盖 application.yml 中 Windows 路径 D:/uploads，可挂载宿主机目录持久化）
    FILE_UPLOAD_PATH=/data/uploads

# 创建文件上传目录
RUN mkdir -p /data/uploads
# 复制可执行 jar
COPY --from=builder /build/build/libs/*.jar app.jar
# 服务端口（application.yml: server.port=8899，context-path=/api）
EXPOSE 8899
# 上传目录数据卷
VOLUME ["/data/uploads"]
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

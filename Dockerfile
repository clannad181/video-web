# ============================================================
# video-web 运行镜像（通用版：jar 需提前构建好放入 target 目录）
#
# 构建（在包含 target 目录的项目根执行）:
#   docker build -t video-web:latest .
# 如需指定 jar 路径:
#   docker build --build-arg JAR_FILE=target/video-web-0.0.1-SNAPSHOT.jar -t video-web:latest .
#
# 说明:
#   - Gradle 打包在镜像外完成（如 ./gradlew bootJar -x test），本镜像只包含 JRE 21 运行环境
#   - target 目录下只保留主 jar，勿放入 xxx-plain.jar（bootJar 可能同时生成），否则通配符匹配多个文件会报错
# ============================================================

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# 时区与 JVM 参数（运行时可被 docker run -e 覆盖）
ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms256m -Xmx512m -Dfile.encoding=UTF-8" \
    # 容器内文件上传目录（覆盖 application.yml 中 Windows 路径 D:/uploads，可挂载宿主机目录持久化）
    FILE_UPLOAD_PATH=/data/uploads

# 创建文件上传目录
RUN mkdir -p /data/uploads

# 复制预构建的 jar，默认取 target 目录下唯一 jar，可通过 --build-arg 覆盖
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# 服务端口（application.yml: server.port=8899，context-path=/api）
EXPOSE 8899

# 上传目录数据卷
VOLUME ["/data/uploads"]

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

# syntax=docker/dockerfile:1

ARG MAVEN_IMAGE=maven:3.10.0-eclipse-temurin-25-noble
ARG JAVA_IMAGE=eclipse-temurin:25-jre-noble

FROM ${MAVEN_IMAGE} AS build
WORKDIR /workspace

COPY pom.xml ./
COPY whim-framework ./whim-framework
COPY whim-modules ./whim-modules
COPY whim-start ./whim-start

# 只构建后端及其依赖模块，依赖缓存不进入运行镜像。
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    timeout 1200 mvn --batch-mode --no-transfer-progress \
    -Pprod -pl whim-start -am -Dmaven.test.skip=true package \
    && mkdir /output \
    && cp whim-start/target/whim-start-*.jar /output/app.jar

FROM ${JAVA_IMAGE} AS runtime
WORKDIR /app

# 验证码依赖字体；curl 用于容器内健康检查。
RUN timeout 180 apt-get -o Acquire::Retries=2 -o Acquire::http::Timeout=30 -o Acquire::https::Timeout=30 update \
    && timeout 180 apt-get -o Acquire::Retries=2 -o Acquire::http::Timeout=30 -o Acquire::https::Timeout=30 install -y --no-install-recommends curl fontconfig fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --gid 10001 whim \
    && useradd --uid 10001 --gid whim --no-create-home --shell /usr/sbin/nologin whim \
    && mkdir -p /app/config /app/data/files /app/logs \
    && chown -R whim:whim /app

COPY --from=build --chown=whim:whim /output/app.jar /app/app.jar

ENV TZ=Asia/Shanghai \
    SPRING_PROFILES_ACTIVE=prod \
    SPRING_CONFIG_ADDITIONAL_LOCATION=optional:file:/app/config/ \
    FILE_STORAGE_PATH=/app/data/files \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError -Djava.awt.headless=true -Dfile.encoding=UTF-8"

USER 10001:10001
EXPOSE 8089
STOPSIGNAL SIGTERM

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

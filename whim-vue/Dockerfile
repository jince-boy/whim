# syntax=docker/dockerfile:1

ARG NODE_IMAGE=node:26-bookworm-slim
ARG NGINX_IMAGE=nginx:1.30-alpine

FROM ${NODE_IMAGE} AS build
WORKDIR /app

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
RUN timeout 180 npm install --global pnpm@12.8.1 --fetch-retries=2 --fetch-timeout=30000
RUN --mount=type=cache,id=whim-pnpm,target=/pnpm/store,sharing=locked \
    timeout 600 pnpm install --frozen-lockfile --store-dir=/pnpm/store

COPY . .

# 前端只接收公开配置；不复制任何本地 .env 或后端私钥。
ARG VITE_API_BASE_URL=/api
ARG VITE_REQUEST_ENCRYPTION_PUBLIC_KEY
ARG VITE_APP_TITLE="Whim Vue"
ARG VITE_APP_DESCRIPTION="Whim 后台管理系统"
ARG VITE_APP_COPYRIGHT="Copyright © 2025–Present Whim. All Rights Reserved."
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL} \
    VITE_REQUEST_ENCRYPTION_PUBLIC_KEY=${VITE_REQUEST_ENCRYPTION_PUBLIC_KEY} \
    VITE_APP_TITLE=${VITE_APP_TITLE} \
    VITE_APP_DESCRIPTION=${VITE_APP_DESCRIPTION} \
    VITE_APP_COPYRIGHT=${VITE_APP_COPYRIGHT}

RUN test -n "$VITE_REQUEST_ENCRYPTION_PUBLIC_KEY" \
    && timeout 600 pnpm build

FROM ${NGINX_IMAGE} AS runtime
COPY --from=build /app/dist /usr/share/nginx/html
COPY deploy/nginx/templates/default.conf.template /etc/nginx/templates/default.conf.template

ENV BACKEND_ADDRESS=whim-api:8089 \
    VITE_API_BASE_URL=/api \
    NGINX_ENVSUBST_FILTER="^(BACKEND_ADDRESS|VITE_API_BASE_URL)$"

EXPOSE 80
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget -q -T 3 -O /dev/null http://127.0.0.1/healthz || exit 1

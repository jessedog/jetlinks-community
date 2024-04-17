#! /bin/bash

SERVER_PORT=8846
SERVER_DOCKER_PORT=8846
APP_NAME=jetlinks
APP_VERSION=latest
IMAGE_NAME=jetlinks
IMAGE_VERSION=latest
PACKAGE_NAME=jetlinks

# run container
containerId=$(docker ps -f name="${APP_NAME}-${APP_VERSION}" -aq)
if [ "${containerId}" != "" ]; then
    echo "删除旧容器：${containerId}"
    docker rm -f "${containerId}"
fi

echo "删除旧镜像"
docker rmi "${IMAGE_NAME}:${IMAGE_VERSION}"

echo "构建镜像"
#echo "docker build -t ${IMAGE_NAME}:${IMAGE_VERSION} --build-arg PACKAGE_NAME=${PACKAGE_NAME} ."
# 构建镜像
# docker build -t rn-server .
docker build --no-cache -t "${IMAGE_NAME}:${IMAGE_VERSION}" --build-arg PACKAGE_NAME="${PACKAGE_NAME}" .


# 运行新容器
#echo "docker run --restart=always -dp ${SERVER_DOCKER_PORT}:${SERVER_PORT} --name ${APP_NAME}-${APP_VERSION} ${IMAGE_NAME}:${IMAGE_VERSION}"
docker run --restart=always -dp "${SERVER_DOCKER_PORT}:${SERVER_PORT}" --name "${APP_NAME}-${APP_VERSION}" "${IMAGE_NAME}:${IMAGE_VERSION}"
echo "运行新容器：${APP_NAME}-${APP_VERSION}"

# 判断容器运行情况，未运行则抛出异常
docker ps -f name="${APP_NAME}-${APP_VERSION}"
newContainerId=$(docker ps -f name="${APP_NAME}-${APP_VERSION}" -q)

if [ "${newContainerId}" = "" ]; then
    echo "新容器运行失败"
    exit 42
else
    echo "新容器运行成功，id:：${newContainerId}"
fi
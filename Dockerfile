# Docker for java flow-service

# 基础镜像
FROM registry.cn-hangzhou.aliyuncs.com/brianchou/openjdk:8-jre-alpine

# 作者
LABEL maintainer="hua.feng@changhong.com"

## JAVA_OPTS：JAVA启动参数 ## APP_NAME：应用名称（各项目需要修改）
ENV JAVA_OPTS=""  APP_NAME="flow-service"

# 添加应用
ADD $APP_NAME/build/libs/$APP_NAME.jar $APP_NAME.jar

# 开放8080端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["sh","-c","java -server -XX:InitialRAMPercentage=75.0  -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC $JAVA_OPTS -jar $APP_NAME.jar --server.servlet.context-path=/$APP_NAME --server.port=8080"]

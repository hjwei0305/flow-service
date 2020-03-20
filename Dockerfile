# Docker for java flow-service
# Base image
FROM tomcat8-jre8-zh-lm:v1.0.3

# Maintainer
LABEL  maintainer="hua.feng@changhong.com"

# Add app
ADD build/war/flow-service.war /usr/local/tomcat/webapps

# Start app
ENTRYPOINT ["/usr/local/limit_memory.sh"]
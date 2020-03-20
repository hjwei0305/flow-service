# Docker for java flow-service
# Base image
FROM tomcat8-jre8-zh-lm:v1.0.3

# Maintainer
LABEL  maintainer="hua.feng@changhong.com"

# Add app
ADD build/war/flow-service.war /usr/local/tomcat/webapps

# USER
USER root

# Start app
ENTRYPOINT ["sh","/usr/local/tomcat/bin/catalina.sh","run"]
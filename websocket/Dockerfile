FROM openjdk:8-jdk-alpine

ENV APP_HOME /app
ENV JAVA_OPTS=""
 
#Create base app folder
RUN mkdir $APP_HOME
#Create folder to save image files
RUN mkdir $APP_HOME/images
#Create folder with application logs
RUN mkdir $APP_HOME/log
 
VOLUME $APP_HOME/images
VOLUME $APP_HOME/log
 
WORKDIR $APP_HOME
RUN ls
ARG JAR_FILE=websocket-0.0.1.jar
ADD target/${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar app.jar" ]
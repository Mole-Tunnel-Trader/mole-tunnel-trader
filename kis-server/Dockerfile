FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu

ARG JAR_FILE=build/libs/kis-server-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} kis-server.jar

ENTRYPOINT [ "java", "-jar", "/kis-server.jar", "--spring.profiles.active=prod" ]

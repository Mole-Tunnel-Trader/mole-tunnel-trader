FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu

ARG JAR_FILE=build/libs/data-go-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} data-go.jar

ENTRYPOINT [ "java", "-jar", "/data-go.jar", "--spring.profiles.active=prod" ]

FROM mcr.microsoft.com/openjdk/jdk:23-ubuntu

ARG JAR_FILE=build/libs/kis-server-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} kis-server.jar

ENTRYPOINT [ "java", "-jar", "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=/heapdump", "/kis-server.jar", "--spring.profiles.active=prod" ]


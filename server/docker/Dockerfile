FROM eclipse-temurin:21-jdk-jammy AS deps

VOLUME ["/orchestrator"]
WORKDIR /orchestrator

ADD server-2.0.0-all.jar /orchestrator/

EXPOSE 2323
EXPOSE 9191

CMD ["java", "-jar", "server-2.0.0-all.jar"]
FROM eclipse-temurin:21-jdk-jammy AS deps

VOLUME ["/orchestrator"]
WORKDIR /orchestrator

ADD orchestrator.jar /orchestrator/

EXPOSE 2323
EXPOSE 9191

CMD ["java", "-jar", "orchestrator.jar"]
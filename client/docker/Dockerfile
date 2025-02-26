FROM eclipse-temurin:21-jdk-jammy as deps

WORKDIR /orchestrator

ADD orchestrator-client-all.jar /orchestrator/

ENV orchestrator.hostaddress=orchestrator

EXPOSE 2323
EXPOSE 9191

CMD ["java", "-jar", "orchestrator-client-all.jar"]
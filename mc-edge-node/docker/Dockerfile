FROM eclipse-temurin:23-alpine AS deps

WORKDIR /orchestrator

ADD mc-edge-node-2.0.0-all.jar /orchestrator/

EXPOSE 25565

CMD ["java", "-jar", "mc-edge-node-2.0.0-all.jar"]
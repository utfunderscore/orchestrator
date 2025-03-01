FROM eclipse-temurin:23-alpine AS deps

WORKDIR /orchestrator

ADD edgenode.jar /orchestrator/

EXPOSE 25565

CMD ["java", "-jar", "edgenode.jar"]
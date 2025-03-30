FROM eclipse-temurin:21-jdk-jammy AS deps

WORKDIR /orchestrator

EXPOSE 25565

ADD https://api.papermc.io/v2/projects/velocity/versions/3.4.0-SNAPSHOT/builds/459/downloads/velocity-3.4.0-SNAPSHOT-459.jar /orchestrator/
ADD proxy.jar /orchestrator/plugins/
ADD velocity.toml /orchestrator/
ADD forwarding.secret /orchestrator/


CMD ["java", "-jar", "velocity-3.4.0-SNAPSHOT-459.jar"]
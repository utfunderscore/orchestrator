FROM eclipse-temurin:21-jdk-jammy AS deps

WORKDIR /orchestrator

EXPOSE 25565

ADD https://api.papermc.io/v2/projects/velocity/versions/3.4.0-SNAPSHOT/builds/459/downloads/velocity-3.4.0-SNAPSHOT-459.jar /orchestrator/
ADD mc-proxy-2.0.0-all.jar /orchestrator/plugins/

CMD ["java", "-jar", "-Dlog4j.logger.org.readutf.hermes.PacketManager=DEBUG", "velocity-3.4.0-SNAPSHOT-459.jar"]
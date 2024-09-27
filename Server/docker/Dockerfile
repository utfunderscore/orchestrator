FROM eclipse-temurin:21-jdk-jammy as deps

WORKDIR /orchestrator

ADD Orchestrator.jar /orchestrator/
ADD settings.yml /orchestrator/

EXPOSE 2980
EXPOSE 9393

CMD ["java", "-jar", "Orchestrator.jar"]

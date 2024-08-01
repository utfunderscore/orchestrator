FROM eclipse-temurin:21-jdk-jammy as deps

WORKDIR /orchestrator

ADD https://github.com/utfunderscore/orchestrator/releases/download/latest/Server-1.0.0-all.jar /orchestrator

EXPOSE 8410

CMD ["java", "-jar", "Server-1.0.0-all.jar"]
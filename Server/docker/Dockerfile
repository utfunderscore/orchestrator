FROM eclipse-temurin:21-jdk-jammy as deps

VOLUME ["orchestrator"]
WORKDIR /orchestrator

ADD https://github.com/utfunderscore/orchestrator/releases/download/latest/Server-1.4.5-all.jar /orchestrator

EXPOSE 2980
EXPOSE 9393


CMD ["java", "-jar", "Server-1.4.5-all.jar"]
FROM maven:3.9.8-amazoncorretto-21 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn package -DskipTests

FROM amazoncorretto:21.0.4

WORKDIR /app
COPY --from=build /app/target/*.jar task-scheduler.jar

ENTRYPOINT ["java", "-jar", "task-scheduler.jar"]
FROM openjdk:21-jdk-slim

WORKDIR /app

COPY .mvn/ .mv/
COPY mvnw pom.xml ./
COPY src src/

RUN chmod +x mvnw

RUN ./mvnw package -DskipTest

RUN cp target/*.jar app.jar

EXPOSE 8000

CMD ["java", "-jar", "app.jar"]
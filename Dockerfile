FROM eclipse-temurin:23-jdk

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 9000

LABEL authors="Admin"

ENTRYPOINT ["java", "-jar" , "app.jar"]
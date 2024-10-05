FROM openjdk:17-jdk-alpine
COPY aplikacja.jar aplikacja.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "aplikacja.jar"]

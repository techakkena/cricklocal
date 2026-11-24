FROM maven:3.9.16-eclipse-temurin-21

WORKDIR /app

COPY pom.xml .
COPY settings.xml .
COPY src ./src

RUN mvn -s settings.xml clean package -DskipTests

EXPOSE 8080

CMD ["sh", "-c", "java -jar target/*.jar"]

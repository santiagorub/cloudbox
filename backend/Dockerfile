FROM openjdk:17-jdk-slim
WORKDIR /app
COPY . /app
RUN apt-get update && apt-get install -y maven
RUN mvn clean package
CMD ["java", "-cp", "target/cloudbox-1.0-SNAPSHOT.jar", "ApiServer"]

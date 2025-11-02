FROM openjdk:17-jdk-slim

WORKDIR /app
COPY src /app/src

# Descargar dependencia MinIO SDK
RUN apt-get update && apt-get install -y wget && \
    wget https://repo1.maven.org/maven2/io/minio/minio/8.5.7/minio-8.5.7.jar -O /app/minio.jar

RUN javac -cp /app/minio.jar src/*.java

ENV MINIO_ENDPOINT="http://minio:9000"
ENV MINIO_ACCESS_KEY="admin"
ENV MINIO_SECRET_KEY="admin123"

CMD ["java", "-cp", "src:/app/minio.jar", "Main"]
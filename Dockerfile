FROM openjdk:17-jdk-slim

WORKDIR /app
COPY src /app/src

# Descargar todas las dependencias necesarias para MinIO
RUN apt-get update && apt-get install -y wget && \
    wget https://repo1.maven.org/maven2/io/minio/minio/8.5.7/minio-8.5.7.jar -O /app/minio.jar && \
    wget https://repo1.maven.org/maven2/com/squareup/okhttp3/okhttp/4.9.3/okhttp-4.9.3.jar -O /app/okhttp.jar && \
    wget https://repo1.maven.org/maven2/com/squareup/okio/okio/2.8.0/okio-2.8.0.jar -O /app/okio.jar && \
    wget https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.13.3/jackson-core-2.13.3.jar -O /app/jackson-core.jar && \
    wget https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.13.3/jackson-databind-2.13.3.jar -O /app/jackson-databind.jar && \
    wget https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.13.3/jackson-annotations-2.13.3.jar -O /app/jackson-annotations.jar && \
    wget https://repo1.maven.org/maven2/com/google/guava/guava/31.1-jre/guava-31.1-jre.jar -O /app/guava.jar && \
    wget https://repo1.maven.org/maven2/org/xerial/snappy/snappy-java/1.1.10.1/snappy-java-1.1.10.1.jar -O /app/snappy.jar && \
    wget https://repo1.maven.org/maven2/org/apache/commons/commons-compress/1.21/commons-compress-1.21.jar -O /app/commons-compress.jar && \
    wget https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/1.7.10/kotlin-stdlib-1.7.10.jar -O /app/kotlin-stdlib.jar

# Compilar el código Java
RUN javac -cp "/app/*" src/*.java

# Variables de entorno
ENV MINIO_ENDPOINT="http://minio:9000"
ENV MINIO_ACCESS_KEY="admin"
ENV MINIO_SECRET_KEY="admin123"

CMD ["java", "-cp", "src:/app/*", "Main"]

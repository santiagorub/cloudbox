# Imagen base con Java
FROM openjdk:17-jdk-slim

# Directorio de trabajo
WORKDIR /app

# Copiar el código fuente
COPY src /app/src

# Crear carpeta para archivos simulados
RUN mkdir /app/archivos

# Compilar
RUN javac src/*.java

# Ejecutar
CMD ["java", "-cp", "src", "Main"]
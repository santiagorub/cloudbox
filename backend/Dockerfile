# Imagen base con Java 17 y Maven
FROM eclipse-temurin:17-jdk

# Establecer el directorio de trabajo
WORKDIR /app

# Copiar archivos del backend (código fuente y pom.xml)
COPY . /app

# Instalar Maven (por si la imagen no lo trae por defecto)
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Compilar el proyecto con Maven, generando el jar con dependencias
RUN mvn -f pom.xml clean package -DskipTests

# Exponer el puerto en el que corre SparkJava
EXPOSE 4567

# Comando para ejecutar el JAR compilado con dependencias
CMD ["java", "-jar", "target/backend-1.0.0-jar-with-dependencies.jar"]

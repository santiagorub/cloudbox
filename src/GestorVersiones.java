import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.GetPresignedObjectUrlArgs;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class GestorVersiones {
    private static GestorVersiones instancia;
    private Map<String, List<Archivo>> archivos;
    private List<Observer> observadores;
    private MinioClient minioClient;
    private String bucketName = "cloudbox";

    //constructor privado (Singleton)
    private GestorVersiones(String endpoint, String accessKey, String secretKey) {
        archivos = new HashMap<>();
        observadores = new ArrayList<>();

        try {
            //crear el cliente de MinIO
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            //verificar o crear bucket
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                System.out.println("Bucket creado: " + bucketName);
            } else {
                System.out.println("Usando bucket existente: " + bucketName);
            }

        } catch (Exception e) {
            System.out.println("Error inicializando MinIO: " + e.getMessage());
        }
    }

    //patron Singleton
    public static GestorVersiones getInstancia(String endpoint, String accessKey, String secretKey) {
        if (instancia == null)
            instancia = new GestorVersiones(endpoint, accessKey, secretKey);
        return instancia;
    }

    //registro de observadores
    public void agregarObserver(Observer obs) {
        observadores.add(obs);
    }

    private void notificar(String mensaje) {
        for (Observer obs : observadores) obs.actualizar(mensaje);
    }

    //crea archivo temporal local antes de subirlo
    private Path crearArchivoTemporal(String nombre, int version) throws IOException {
        Path carpeta = Path.of("archivos");
        if (!Files.exists(carpeta)) Files.createDirectory(carpeta);

        String safeName = nombre.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path archivo = carpeta.resolve(safeName + "_v" + version + ".txt");
        Files.writeString(archivo, "Contenido de " + nombre + " - versión " + version + "\n");
        return archivo;
    }

    //sube archivo a MinIO y devuelve la url de acceso
    private String subirAMinIO(Path rutaArchivo) {
        try (FileInputStream fis = new FileInputStream(rutaArchivo.toFile())) {
            String objectName = rutaArchivo.getFileName().toString();

            //subir archivo
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(fis, rutaArchivo.toFile().length(), -1)
                            .contentType("text/plain")
                            .build()
            );

            //obtener url temporal
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

            return url;

        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            System.out.println("Error al subir a MinIO: " + e.getMessage());
            return "Error al subir a MinIO";
        }
    }

    //sube archivo y registra versión
    public Archivo subirArchivo(String nombre) {
        try {
            List<Archivo> versiones = archivos.getOrDefault(nombre, new ArrayList<>());
            int nuevaVersion = versiones.size() + 1;

            Path archivoTemp = crearArchivoTemporal(nombre, nuevaVersion);
            String url = subirAMinIO(archivoTemp);

            Archivo nuevoArchivo = new Archivo(nombre, nuevaVersion, url);
            versiones.add(nuevoArchivo);
            archivos.put(nombre, versiones);

            guardarRegistro(nuevoArchivo);
            notificar("Archivo '" + nombre + "' subido correctamente (versión " + nuevaVersion + ")");
            return nuevoArchivo;

        } catch (IOException e) {
            System.out.println("Error al crear archivo: " + e.getMessage());
            return null;
        }
    }

    //muestra historial en consola
    public void listarArchivos() {
        if (archivos.isEmpty()) {
            System.out.println("No hay archivos cargados.");
            return;
        }
        for (String nombre : archivos.keySet()) {
            System.out.println("📁 " + nombre + ":");
            for (Archivo a : archivos.get(nombre)) {
                System.out.println("   - " + a);
            }
        }
    }

    //guarda historial en archivo de texto
    private void guardarRegistro(Archivo archivo) {
        try (FileWriter fw = new FileWriter("historial.txt", true)) {
            fw.write(archivo.toString() + System.lineSeparator());
        } catch (IOException e) {
            System.out.println("Error al guardar el registro: " + e.getMessage());
        }
    }
}

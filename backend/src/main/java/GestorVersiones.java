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

import org.json.JSONArray;
import org.json.JSONObject;

public class GestorVersiones {
    private static GestorVersiones instancia;
    private Map<String, List<Archivo>> archivos;
    private List<Observer> observadores;
    private MinioClient minioClient;
    private String bucketName = "cloudbox";

    // Constructor privado (Singleton)
    private GestorVersiones(String endpoint, String accessKey, String secretKey) {
        archivos = new HashMap<>();
        observadores = new ArrayList<>();

        try {
            // Crear el cliente de MinIO
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            // Verificar o crear bucket
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

    // Patrón Singleton
    public static GestorVersiones getInstancia(String endpoint, String accessKey, String secretKey) {
        if (instancia == null)
            instancia = new GestorVersiones(endpoint, accessKey, secretKey);
        return instancia;
    }

    // Registro de observadores
    public void agregarObserver(Observer obs) {
        observadores.add(obs);
    }

    private void notificar(String mensaje) {
        for (Observer obs : observadores)
            obs.actualizar(mensaje);
    }

    // Crea archivo temporal local antes de subirlo
    private Path crearArchivoTemporal(String nombre, int version) throws IOException {
        Path carpeta = Path.of("archivos");
        if (!Files.exists(carpeta))
            Files.createDirectory(carpeta);

        String safeName = nombre.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path archivo = carpeta.resolve(safeName + "_v" + version + ".txt");
        Files.writeString(archivo, "Contenido de " + nombre + " - versión " + version + "\n");
        return archivo;
    }

    // Sube archivo a MinIO y devuelve la URL de acceso
    private String subirAMinIO(Path rutaArchivo) {
        try (FileInputStream fis = new FileInputStream(rutaArchivo.toFile())) {
            String objectName = rutaArchivo.getFileName().toString();

            // Subir archivo al bucket
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(fis, rutaArchivo.toFile().length(), -1)
                            .contentType("text/plain")
                            .build()
            );

            // Obtener URL temporal de acceso
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            System.out.println("Error al subir a MinIO: " + e.getMessage());
            return "Error al subir a MinIO";
        }
    }

    // Sube archivo y registra versión
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

    // Retorna lista de archivos en memoria
    public List<String> listarArchivos() {
        List<String> nombres = new ArrayList<>();
        if (archivos.isEmpty()) {
            System.out.println("No hay archivos cargados.");
            return nombres;
        }
        for (String nombre : archivos.keySet()) {
            System.out.println("📁 " + nombre + ":");
            for (Archivo a : archivos.get(nombre)) {
                System.out.println("   - " + a);
            }
            nombres.add(nombre);
        }
        return nombres;
    }

    // Guarda historial en archivo de texto
    private void guardarRegistro(Archivo archivo) {
        try (FileWriter fw = new FileWriter("historial.txt", true)) {
            fw.write(archivo.toString() + System.lineSeparator());
        } catch (IOException e) {
            System.out.println("Error al guardar el registro: " + e.getMessage());
        }
    }

    // Devuelve el historial en formato JSON
    public String listarArchivosComoJson() {
        List<String> listaArchivos = listarArchivos();
        JSONArray array = new JSONArray();

        for (String nombre : listaArchivos) {
            JSONObject obj = new JSONObject();
            obj.put("nombre", nombre);
            array.put(obj);
        }

        return array.toString();
    }
}

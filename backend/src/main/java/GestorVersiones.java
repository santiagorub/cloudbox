import io.minio.MinioClient;
import io.minio.PutObjectArgs;
//import io.minio.errors.MinioException;
import io.minio.http.Method;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.SetBucketPolicyArgs;

//import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class GestorVersiones {
    private static GestorVersiones instancia;
    private Map<String, List<Archivo>> archivos;
    private List<Observer> observadores;
    private MinioClient minioClientInternal; // Para operaciones internas
    private MinioClient minioClientPublic;   // Para generar URLs públicas
    private String bucketName = "cloudbox";

    // Constructor privado (Singleton)
    private GestorVersiones(String endpoint, String accessKey, String secretKey, String publicUrl) {
        archivos = new HashMap<>();
        observadores = new ArrayList<>();
        
        try {
            // Cliente para comunicación interna en Docker
            minioClientInternal = MinioClient.builder() // Usar el nombre del servicio docker
                    .endpoint("minio", 9000, false)
                    .credentials(accessKey, secretKey)
                    .build();

            // Cliente para generar URLs públicas (accesibles desde el navegador)
            if (publicUrl != null && !publicUrl.isEmpty()) {
                minioClientPublic = MinioClient.builder()
                    .endpoint("localhost", 9000, false) // Usar localhost para el navegador
                    .credentials(accessKey, secretKey)
                    .build();
            } else {
                minioClientPublic = minioClientInternal;
            }

            // Verificar o crear bucket
            boolean exists = minioClientInternal.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!exists) {
                minioClientInternal.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                System.out.println("Bucket creado: " + bucketName);
                configurarCors();
            } else {
                System.out.println("Usando bucket existente: " + bucketName);
            }

        } catch (Exception e) {
            System.out.println("Error inicializando MinIO: " + e.getMessage());
        }
    }

    // Configura la política de CORS en el bucket para permitir peticiones GET desde el frontend
    private void configurarCors() {
        try {
            String config = "{"
                + "\"Version\": \"2012-10-17\","
                + "\"Statement\": ["
                + "  {"
                + "    \"Effect\": \"Allow\","
                + "    \"Principal\": {\"AWS\": [\"*\"]},"
                + "    \"Action\": [\"s3:GetObject\", \"s3:ListBucket\"],"
                + "    \"Resource\": [\"arn:aws:s3:::" + bucketName + "/*\"]"
                + "  }"
                + "]"
                + "}";
            minioClientInternal.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(config).build());
            System.out.println("✅ Política de CORS configurada para el bucket: " + bucketName);
        } catch (Exception e) {
            System.out.println("❌ Error al configurar la política de CORS: " + e.getMessage());
        }
    }

    // Patrón Singleton
    public static GestorVersiones getInstancia(String endpoint, String accessKey, String secretKey, String publicUrl) {
        if (instancia == null)
            instancia = new GestorVersiones(endpoint, accessKey, secretKey, publicUrl);
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

    // Genera un nombre de archivo con la versión inyectada antes de la extensión.
    private String generarNombreVersionado(String nombreOriginal, int version) {
        int dotIndex = nombreOriginal.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < nombreOriginal.length() - 1) {
            String baseName = nombreOriginal.substring(0, dotIndex);
            String extension = nombreOriginal.substring(dotIndex);
            return baseName + "_v" + version + extension;
        } else {
            // Si no hay extensión, simplemente la añade al final.
            return nombreOriginal + "_v" + version;
        }
    }

    // Crea archivo temporal local antes de subirlo
    private Path crearArchivoTemporal(String nombre, int version) throws IOException {
        Path carpeta = Path.of("archivos");
        if (!Files.exists(carpeta))
            Files.createDirectory(carpeta);

        String nombreVersionado = generarNombreVersionado(nombre, version);
        Path archivo = carpeta.resolve(nombreVersionado);
        Files.writeString(archivo, "Contenido de " + nombre + " - versión " + version + "\n");
        return archivo;
    }

    // Sube archivo a MinIO y devuelve la URL de acceso
    private String subirAMinIO(String nombre, int version, Path rutaArchivo) {
        try {
            String objectName = generarNombreVersionado(nombre, version);

            // Subir archivo al bucket
            minioClientInternal.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(Files.newInputStream(rutaArchivo), rutaArchivo.toFile().length(), -1)
                            .build()
            );

            // Obtener URL temporal de acceso
            return minioClientPublic.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );

        } catch (Exception e) {
            System.out.println("Error al subir a MinIO: " + e.getMessage());
            return "Error al subir a MinIO";
        }
    }

    // Sobrecarga para la app de consola (crea un archivo temporal)
    public Archivo subirArchivo(String nombre) {
        try {
            List<Archivo> versiones = archivos.getOrDefault(nombre, new ArrayList<>());
            int nuevaVersion = versiones.size() + 1;
            Path archivoTemp = crearArchivoTemporal(nombre, nuevaVersion);
            return subirArchivo(nombre, archivoTemp);
        } catch (IOException e) {
            System.out.println("Error al crear archivo temporal: " + e.getMessage());
            return null;
        }
    }

    // Sube archivo y registra versión
    public Archivo subirArchivo(String nombre, Path archivoTemp) {
        try {
            List<Archivo> versiones = archivos.getOrDefault(nombre, new ArrayList<>());
            int nuevaVersion = versiones.size() + 1;

            String url = subirAMinIO(nombre, nuevaVersion, archivoTemp);

            Archivo nuevoArchivo = new Archivo(nombre, nuevaVersion, url);
            versiones.add(nuevoArchivo);
            archivos.put(nombre, versiones);

            guardarRegistro(nuevoArchivo);
            notificar("Archivo '" + nombre + "' subido correctamente (versión " + nuevaVersion + ")");
            return nuevoArchivo;

        } catch (Exception e) {
            System.out.println("Error al subir archivo: " + e.getMessage());
            return null;
        } finally {
            // Eliminar el archivo temporal después de usarlo
            try { Files.deleteIfExists(archivoTemp); } catch (IOException e) { e.printStackTrace(); }
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
        JSONArray array = new JSONArray();
        // Aplanar la lista de archivos para que sea más fácil de consumir en el frontend
        for (List<Archivo> versiones : archivos.values()) {
            for (Archivo a : versiones) {
                JSONObject versionObj = new JSONObject();
                versionObj.put("nombre", a.getNombre());
                versionObj.put("version", a.getVersion());
                versionObj.put("url", a.getUrl());
                versionObj.put("fecha", a.getFecha());
                array.put(versionObj);
            }
        }
        return array.toString();
    }
}

import static spark.Spark.*;
import java.nio.file.*;
import java.io.*;
import org.json.JSONObject;

public class ApiServer {

    public static void main(String[] args) {
        // Configuración del servidor
        port(8080);

        String endpoint = System.getenv("MINIO_ENDPOINT");
        String accessKey = System.getenv("MINIO_ACCESS_KEY");
        String secretKey = System.getenv("MINIO_SECRET_KEY");

        SistemaCloudBox sistema = new SistemaCloudBox(endpoint, accessKey, secretKey);

        // Endpoint para subir archivos
        post("/upload", (req, res) -> {
            res.type("application/json");

            // Obtener el archivo desde la petición
            Path temp = Files.createTempFile("upload-", ".txt");
            try (InputStream input = req.raw().getPart("file").getInputStream()) {
                Files.copy(input, temp, StandardCopyOption.REPLACE_EXISTING);
            }

            sistema.subirArchivo(temp.getFileName().toString());

            JSONObject response = new JSONObject();
            response.put("status", "ok");
            response.put("message", "Archivo subido correctamente");
            return response.toString();
        });

        // Endpoint para mostrar historial
        get("/historial", (req, res) -> {
            res.type("application/json");
            sistema.mostrarHistorial();
            JSONObject response = new JSONObject();
            response.put("status", "ok");
            response.put("message", "Historial mostrado en consola");
            return response.toString();
        });

        System.out.println("✅ Servidor iniciado en http://localhost:8080");
    }
}

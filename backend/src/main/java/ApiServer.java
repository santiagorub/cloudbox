import static spark.Spark.*;
import java.nio.file.*;
import java.io.*;
import org.json.JSONObject;
import javax.servlet.http.Part;

public class ApiServer {

    public static void main(String[] args) {
        // Configuración del servidor
        port(8080);

        // Habilitar CORS
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "http://localhost:3000");
            response.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
            response.header("Access-Control-Allow-Credentials", "true");
        });

        // Manejar peticiones OPTIONS para pre-vuelo de CORS
        options("/*", (request, response) -> {
            response.status(204); // No Content
            return "";
        });

        String endpoint = System.getenv("MINIO_ENDPOINT");
        String accessKey = System.getenv("MINIO_ACCESS_KEY");
        String secretKey = System.getenv("MINIO_SECRET_KEY");
        String publicUrl = System.getenv("MINIO_PUBLIC_URL");

        SistemaCloudBox sistema = new SistemaCloudBox(endpoint, accessKey, secretKey, publicUrl);

        // Endpoint para subir archivos
        post("/upload", (req, res) -> {
            // Configuración para multipart/form-data
            req.attribute("org.eclipse.jetty.multipartConfig", new javax.servlet.MultipartConfigElement("/temp"));
            
            res.type("application/json");

            // Obtener el archivo desde la petición
            Part filePart = req.raw().getPart("file");
            String fileName = filePart.getSubmittedFileName();
            if (fileName == null || fileName.isEmpty()) {
                JSONObject errorResponse = new JSONObject();
                errorResponse.put("status", "error");
                errorResponse.put("message", "Nombre de archivo no válido");
                res.status(400);
                return errorResponse.toString();
            }

            Path temp = Files.createTempFile("upload-", ".tmp");
            try (InputStream input = filePart.getInputStream()) {
                Files.copy(input, temp, StandardCopyOption.REPLACE_EXISTING);
            }

            sistema.subirArchivo(fileName, temp);

            JSONObject response = new JSONObject();
            response.put("status", "ok");
            response.put("message", "Archivo subido correctamente");
            return response.toString();
        });

        // Endpoint para mostrar historial
        get("/historial", (req, res) -> {
            res.type("application/json");
            String jsonHistorial = sistema.listarArchivosComoJson();
            return jsonHistorial;
        });

        System.out.println("✅ Servidor iniciado en http://localhost:8080");
    }
}

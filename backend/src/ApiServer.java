import static spark.Spark.*;
import java.nio.file.*;
import io.minio.errors.*;
import java.io.*;

public class ApiServer {
    public static void main(String[] args) {
        port(8080);
        SistemaCloudBox sistema = new SistemaCloudBox();

        post("/upload", (req, res) -> {
            Path temp = Files.createTempFile("upload-", ".tmp");
            Files.copy(req.raw().getPart("file").getInputStream(), temp, StandardCopyOption.REPLACE_EXISTING);
            sistema.getGestor().subirArchivo(temp.toString());
            return "Archivo subido correctamente a MinIO";
        });

        get("/historial", (req, res) -> {
            res.type("application/json");
            return sistema.getGestor().listarArchivosComoJson();
        });
    }
}

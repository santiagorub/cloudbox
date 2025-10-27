import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GestorVersiones {
    private static GestorVersiones instancia;
    private Map<String, List<Archivo>> archivos;
    private List<Observer> observadores;

    private GestorVersiones() {
        archivos = new HashMap<>();
        observadores = new ArrayList<>();
    }

    public static GestorVersiones getInstancia() {
        if (instancia == null) instancia = new GestorVersiones();
        return instancia;
    }

    public void agregarObserver(Observer obs) {
        observadores.add(obs);
    }

    private void notificar(String mensaje) {
        for (Observer obs : observadores) obs.actualizar(mensaje);
    }

    // Simula crear un archivo físico temporal
    private Path crearArchivoTemporal(String nombre, int version) throws IOException {
        Path carpeta = Path.of("archivos");
        if (!Files.exists(carpeta)) Files.createDirectory(carpeta);

        Path archivo = carpeta.resolve(nombre + "_v" + version + ".txt");
        Files.writeString(archivo, "Archivo " + nombre + " versión " + version);
        return archivo;
    }

    // Enviar archivo a Vercel Blob Storage
    private String subirAVercel(Path rutaArchivo) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://TU-API-VERCEL.vercel.app/api/upload")) // ⚠️ Cambia esto por tu URL
                    .header("Content-Type", "application/octet-stream")
                    .POST(HttpRequest.BodyPublishers.ofFile(rutaArchivo))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // Extrae la URL del JSON devuelto
            String body = response.body();
            int start = body.indexOf("http");
            int end = body.lastIndexOf("\"");
            return (start != -1 && end != -1) ? body.substring(start, end) : "URL no disponible";
        } catch (IOException | InterruptedException e) {
            System.out.println("Error al subir a Vercel: " + e.getMessage());
            return "Error de conexión";
        }
    }

    public Archivo subirArchivo(String nombre) {
        try {
            List<Archivo> versiones = archivos.getOrDefault(nombre, new ArrayList<>());
            int nuevaVersion = versiones.size() + 1;

            Path archivoTemp = crearArchivoTemporal(nombre, nuevaVersion);
            String url = subirAVercel(archivoTemp);

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

    private void guardarRegistro(Archivo archivo) {
        try (FileWriter fw = new FileWriter("historial.txt", true)) {
            fw.write(archivo.toString() + "\n");
        } catch (IOException e) {
            System.out.println("Error al guardar el registro: " + e.getMessage());
        }
    }
}

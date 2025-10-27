import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Archivo {
    private String nombre;
    private int version;
    private String fecha;
    private String url; // URL devuelta por Vercel

    public Archivo(String nombre, int version, String url) {
        this.nombre = nombre;
        this.version = version;
        this.fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        this.url = url;
    }

    public String getNombre() { return nombre; }
    public int getVersion() { return version; }
    public String getFecha() { return fecha; }
    public String getUrl() { return url; }

    @Override
    public String toString() {
        return nombre + " - versión " + version + " (" + fecha + ") -> " + url;
    }
}
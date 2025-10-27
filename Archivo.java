import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Archivo {
    private String nombre;
    private int version;
    private String fecha;

    public Archivo(String nombre, int version) {
        this.nombre = nombre;
        this.version = version;
        this.fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public String getNombre() {
        return nombre;
    }

    public int getVersion() {
        return version;
    }

    public String getFecha() {
        return fecha;
    }

    @Override
    public String toString() {
        return nombre + " - versión " + version + " (" + fecha + ")";
    }
}

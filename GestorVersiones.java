import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestorVersiones {
    private static GestorVersiones instancia;
    private Map<String, List<Archivo>> archivos;
    private List<Observer> observadores;

    private GestorVersiones() {
        archivos = new HashMap<>();
        observadores = new ArrayList<>();
    }

    public static GestorVersiones getInstancia() {
        if (instancia == null) {
            instancia = new GestorVersiones();
        }
        return instancia;
    }

    public void agregarObserver(Observer obs) {
        observadores.add(obs);
    }

    private void notificar(String mensaje) {
        for (Observer obs : observadores) {
            obs.actualizar(mensaje);
        }
    }

    private Archivo crearArchivo(String nombre, int version) {
        return new Archivo(nombre, version);
    }

    public Archivo subirArchivo(String nombre) {
        List<Archivo> versiones = archivos.getOrDefault(nombre, new ArrayList<>());
        int nuevaVersion = versiones.size() + 1;

        Archivo nuevoArchivo = crearArchivo(nombre, nuevaVersion);
        versiones.add(nuevoArchivo);
        archivos.put(nombre, versiones);

        notificar("Archivo '" + nombre + "' subido correctamente (versión " + nuevaVersion + ")");
        return nuevoArchivo;
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
}

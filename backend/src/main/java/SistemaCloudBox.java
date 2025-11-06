import java.nio.file.Path;

public class SistemaCloudBox {
    private GestorVersiones gestor;

    //constructor para la app de consola
    public SistemaCloudBox(String endpoint, String user, String password, String publicUrl) {
        gestor = GestorVersiones.getInstancia(endpoint, user, password, publicUrl);
        gestor.agregarObserver(new Notificador());
    }

    public Archivo subirArchivo(String nombre, Path archivoTemp) {
        return gestor.subirArchivo(nombre, archivoTemp);
    }

    // Sobrecarga para la app de consola
    public Archivo subirArchivo(String nombre) {
        return gestor.subirArchivo(nombre);
    }

    public void mostrarHistorial() {
        gestor.listarArchivos();
    }

    public String listarArchivosComoJson() {
        return gestor.listarArchivosComoJson();
    }

    //getter necesario para ApiServer
    public GestorVersiones getGestor() {
        return gestor;
    }
}

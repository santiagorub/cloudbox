public class SistemaCloudBox {
    private GestorVersiones gestor;

    //constructor para la app de consola
    public SistemaCloudBox(String endpoint, String user, String password) {
        gestor = GestorVersiones.getInstancia(endpoint, user, password);
        gestor.agregarObserver(new Notificador());
    }

    //constructor para el servidor web
    public SistemaCloudBox() {
        String endpoint = System.getenv("MINIO_ENDPOINT");
        String user = System.getenv("MINIO_ACCESS_KEY");
        String password = System.getenv("MINIO_SECRET_KEY");

        gestor = GestorVersiones.getInstancia(endpoint, user, password);
        gestor.agregarObserver(new Notificador());
    }

    public void subirArchivo(String nombre) {
        gestor.subirArchivo(nombre);
    }

    public void mostrarHistorial() {
        gestor.listarArchivos();
    }

    //getter necesario para ApiServer
    public GestorVersiones getGestor() {
        return gestor;
    }
}

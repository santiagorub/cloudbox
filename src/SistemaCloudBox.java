public class SistemaCloudBox {
    private GestorVersiones gestor;

    public SistemaCloudBox(String endpoint, String user, String password) {
        gestor = GestorVersiones.getInstancia(endpoint, user, password);
        gestor.agregarObserver(new Notificador());
    }

    public void subirArchivo(String nombre) {
        gestor.subirArchivo(nombre);
    }

    public void mostrarHistorial() {
        gestor.listarArchivos();
    }
}

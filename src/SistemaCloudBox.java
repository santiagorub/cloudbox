public class SistemaCloudBox {
    private GestorVersiones gestor;

    public SistemaCloudBox() {
        gestor = GestorVersiones.getInstancia();
        gestor.agregarObserver(new Notificador());
    }

    public void subirArchivo(String nombre) {
        gestor.subirArchivo(nombre);
    }

    public void mostrarHistorial() {
        gestor.listarArchivos();
    }
}

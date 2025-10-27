public class Notificador implements Observador {
    @Override
    public void actualizar(String mensaje) {
        System.out.println("[Notificación] " + mensaje);
    }
}
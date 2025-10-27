public interface Observer {
    void actualizar(String mensaje);
}

class Notificador implements Observer {
    @Override
    public void actualizar(String mensaje) {
        System.out.println("[Notificación] " + mensaje);
    }
}
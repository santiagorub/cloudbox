public class FabricaArchivos {
    public Archivo crearArchivo(String nombre, int version) {
        return new Archivo(nombre, version);
    }
}

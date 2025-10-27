import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        SistemaCloudBox sistema = new SistemaCloudBox();
        Scanner sc = new Scanner(System.in);
        int opcion;

        do {
            System.out.println("\n=== CLOUD BOX ===");
            System.out.println("1. Subir archivo");
            System.out.println("2. Mostrar historial");
            System.out.println("0. Salir");
            System.out.print("Elija una opción: ");
            opcion = sc.nextInt();
            sc.nextLine(); // limpiar buffer

            switch (opcion) {
                case 1:
                    System.out.print("Ingrese el nombre del archivo: ");
                    String nombre = sc.nextLine();
                    sistema.subirArchivo(nombre);
                    break;
                case 2:
                    sistema.mostrarHistorial();
                    break;
                case 0:
                    System.out.println("Saliendo del sistema...");
                    break;
                default:
                    System.out.println("Opción no válida.");
            }

        } while (opcion != 0);

        sc.close();
    }
}
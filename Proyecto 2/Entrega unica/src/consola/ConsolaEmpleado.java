package consola;

import java.util.Scanner;

import modelo.Club;
import modelo.Empleado;
import modelo.Usuario;
import persistencia.Persistencia;

public class ConsolaEmpleado {

    private static final String CARPETA_DATOS = "datos";
    private static final int INTENTOS_DE_LOGIN = 3;

    private final Club club;
    private final Entrada entrada;

    public ConsolaEmpleado(Club club, Entrada entrada) {
        this.club = club;
        this.entrada = entrada;
    }

    public static void main(String[] args) {
        Persistencia persistencia = new Persistencia(CARPETA_DATOS);
        Club club = persistencia.cargar();
        DatosTorneos.cargarSiHaceFalta(club);

        Entrada entrada = new Entrada(new Scanner(System.in));
        try {
            new ConsolaEmpleado(club, entrada).iniciar();
        } catch (IllegalStateException e) {
            System.out.println("\nSe cerro la entrada. Se guardan los datos y termina el programa.");
        } finally {
            club.guardar();
        }
    }

    public void iniciar() {
        System.out.println("=== Club Deportivo - Empleados ===");
        for (int intento = 1; intento <= INTENTOS_DE_LOGIN; intento++) {
            String login = entrada.leerTexto("Usuario");
            String clave = entrada.leerTexto("Clave");
            Usuario usuario = club.iniciarSesion(login, clave);
            if (usuario instanceof Empleado) {
                System.out.println("Bienvenido(a), " + usuario.getNombre() + ".");
                new MenuEmpleado(club, (Empleado) usuario, entrada).mostrar();
                System.out.println("Hasta pronto. Se guardan los datos.");
                return;
            }
            if (usuario != null) {
                System.out.println("Esta consola es solo para entrenadores y fisioterapeutas. Use la consola de su rol.");
                return;
            }
            System.out.println("Usuario o clave incorrectos. Intento " + intento + " de " + INTENTOS_DE_LOGIN);
        }
        System.out.println("Demasiados intentos. Termina el programa.");
    }
}

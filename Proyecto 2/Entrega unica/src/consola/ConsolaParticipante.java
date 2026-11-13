package consola;

import java.util.Scanner;

import modelo.Administrador;
import modelo.Club;
import modelo.Empleado;
import modelo.ReglaNegocioException;
import modelo.Usuario;
import modelo.Socio;
import persistencia.Persistencia;

/**
 * Consola para los participantes: socios, estudiantes y personas externas.
 * Los datos se cargan solos del archivo del club y se guardan al salir.
 * Ejecutar esta clase para usarla.
 */
public class ConsolaParticipante {

    /** Carpeta donde queda club.dat. No se le pregunta al usuario. */
    private static final String CARPETA_DATOS = "datos";
    private static final int INTENTOS_DE_LOGIN = 3;

    private final Club club;
    private final Entrada entrada;

    public ConsolaParticipante(Club club, Entrada entrada) {
        this.club = club;
        this.entrada = entrada;
    }

    public static void main(String[] args) {
        Persistencia persistencia = new Persistencia(CARPETA_DATOS);
        Club club = persistencia.cargar();
        DatosTorneos.cargarSiHaceFalta(club);

        Entrada entrada = new Entrada(new Scanner(System.in));
        try {
            new ConsolaParticipante(club, entrada).menuPrincipal();
        } catch (IllegalStateException e) {
            System.out.println("\nSe cerro la entrada. Se guardan los datos y termina el programa.");
        } finally {
            club.guardar();
        }
    }

    public void menuPrincipal() {
        System.out.println("=== Club Deportivo - Participantes ===");
        int opcion = -1;
        while (opcion != 0) {
            System.out.println();
            System.out.println("1. Iniciar sesion");
            System.out.println("2. Registrarme como estudiante");
            System.out.println("3. Registrarme como persona externa");
            System.out.println("0. Salir");
            opcion = entrada.leerEntero("Opcion", 0, 3);
            if (opcion == 1) {
                iniciarSesion();
            } else if (opcion == 2) {
                registrarEstudiante();
            } else if (opcion == 3) {
                registrarExterno();
            }
        }
        System.out.println("Hasta pronto. Se guardan los datos.");
    }

    private void iniciarSesion() {
        for (int intento = 1; intento <= INTENTOS_DE_LOGIN; intento++) {
            String login = entrada.leerTexto("Usuario");
            String clave = entrada.leerTexto("Clave");
            Usuario usuario = club.iniciarSesion(login, clave);
            if (usuario != null) {
                entrarComoParticipante(usuario);
                return;
            }
            System.out.println("Usuario o clave incorrectos. Intento " + intento + " de " + INTENTOS_DE_LOGIN);
        }
        System.out.println("Demasiados intentos. Volviendo al menu principal.");
    }

    private void entrarComoParticipante(Usuario usuario) {
        if (usuario instanceof Administrador || usuario instanceof Empleado) {
            System.out.println("Esta consola es para socios, estudiantes y externos. Use la consola de su rol.");
            return;
        }
        System.out.println("Bienvenido(a), " + usuario.getNombre() + ".");
        if (usuario instanceof Socio) {
            new MenuSocio(club, (Socio) usuario, entrada).mostrar();
        } else {
            new MenuTorneos(club, usuario, entrada).mostrar();
        }
    }

    private void registrarEstudiante() {
        String nombre = entrada.leerTexto("Nombre");
        String apellido = entrada.leerTexto("Apellido");
        String codigo = entrada.leerTexto("Codigo estudiantil");
        String login = entrada.leerTexto("Usuario nuevo");
        String clave = entrada.leerTexto("Clave nueva (minimo 4 caracteres)");
        try {
            club.registrarEstudiante(nombre, apellido, login, clave, codigo);
            System.out.println("LISTO: registro exitoso. Ya puede iniciar sesion.");
        } catch (ReglaNegocioException e) {
            System.out.println("NO SE PUDO: " + e.getMessage());
        }
    }

    private void registrarExterno() {
        String nombre = entrada.leerTexto("Nombre");
        String apellido = entrada.leerTexto("Apellido");
        String documento = entrada.leerTexto("Documento de identidad");
        String login = entrada.leerTexto("Usuario nuevo");
        String clave = entrada.leerTexto("Clave nueva (minimo 4 caracteres)");
        try {
            club.registrarExterno(nombre, apellido, login, clave, documento);
            System.out.println("LISTO: registro exitoso. Ya puede iniciar sesion.");
        } catch (ReglaNegocioException e) {
            System.out.println("NO SE PUDO: " + e.getMessage());
        }
    }
}
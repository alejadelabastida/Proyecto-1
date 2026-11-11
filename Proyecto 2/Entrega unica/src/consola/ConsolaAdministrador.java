package consola;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

import modelo.Administrador;
import modelo.Club;
import modelo.ReglaNegocioException;
import modelo.Torneo;
import modelo.Usuario;
import persistencia.Persistencia;

/**
 * Consola del ADMINISTRADOR (Proyecto 2 - Persona 1).
 *
 * PLANTILLA generada con IAG (uso permitido por el enunciado: "codigo base de un
 * InputStream o Scanner que le permita la interaccion basica en consola" y
 * "creacion/replicacion de plantillas"). Replica la estructura de ConsolaParticipante.
 *
 * Los metodos marcados con TODO son LOGICA CRITICA: deben implementarse sin IAG.
 * Cada TODO solo describe que entradas leer y que metodo del modelo llamar.
 */
public class ConsolaAdministrador {

    private static final String CARPETA_DATOS = "datos";
    private static final int INTENTOS_DE_LOGIN = 3;

    private final Club club;
    private final Entrada entrada;
    private Administrador administrador;

    public ConsolaAdministrador(Club club, Entrada entrada) {
        this.club = club;
        this.entrada = entrada;
    }

    public static void main(String[] args) {
        Persistencia persistencia = new Persistencia(CARPETA_DATOS);
        Club club = persistencia.cargar();
        DatosTorneos.cargarSiHaceFalta(club);

        Entrada entrada = new Entrada(new Scanner(System.in));
        try {
            new ConsolaAdministrador(club, entrada).iniciar();
        } catch (IllegalStateException e) {
            System.out.println("\nSe cerro la entrada. Se guardan los datos y termina el programa.");
        } finally {
            club.guardar();
        }
    }

    // ------------------------------------------------------------------
    //  Autenticacion
    // ------------------------------------------------------------------

    public void iniciar() {
        System.out.println("=== Club Deportivo - Administrador ===");
        if (!autenticar()) {
            System.out.println("No fue posible iniciar sesion. Termina el programa.");
            return;
        }
        menuPrincipal();
        System.out.println("Hasta pronto. Se guardan los datos.");
    }

    private boolean autenticar() {
        for (int intento = 1; intento <= INTENTOS_DE_LOGIN; intento++) {
            String login = entrada.leerTexto("Usuario");
            String clave = entrada.leerTexto("Clave");
            Usuario usuario = club.iniciarSesion(login, clave);
            if (usuario instanceof Administrador) {
                administrador = (Administrador) usuario;
                System.out.println("Bienvenido(a), " + usuario.getNombre() + ".");
                return true;
            }
            if (usuario != null) {
                System.out.println("Esta consola es solo para el administrador. Use la consola de su rol.");
                return false;
            }
            System.out.println("Usuario o clave incorrectos. Intento " + intento + " de " + INTENTOS_DE_LOGIN);
        }
        return false;
    }

    // ------------------------------------------------------------------
    //  Menu
    // ------------------------------------------------------------------

    private void menuPrincipal() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println();
            System.out.println("--- Gestion de torneos ---");
            System.out.println("1. Crear torneo");
            System.out.println("2. Ver torneos (por estado)");
            System.out.println("3. Ver inscritos de un torneo");
            System.out.println("4. Liberar cupos preferenciales");
            System.out.println("5. Iniciar torneo");
            System.out.println("6. Registrar resultado de un encuentro");
            System.out.println("7. Ver posiciones");
            System.out.println("8. Finalizar torneo y entregar premios");
            System.out.println("9. Ver historial detallado de un torneo");
            System.out.println("0. Salir");
            opcion = entrada.leerEntero("Opcion", 0, 9);
            ejecutar(opcion);
        }
    }

    private void ejecutar(int opcion) {
        try {
            switch (opcion) {
                case 1: crearTorneo(); break;
                case 2: verTorneos(); break;
                case 3: verInscritos(); break;
                case 4: liberarCuposPreferenciales(); break;
                case 5: iniciarTorneo(); break;
                case 6: registrarResultado(); break;
                case 7: verPosiciones(); break;
                case 8: finalizarTorneo(); break;
                case 9: verHistorial(); break;
                default: break;
            }
        } catch (ReglaNegocioException e) {
            System.out.println("NO SE PUDO: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    //  Opciones que ya existen en el modelo (de los compañeros)
    // ------------------------------------------------------------------

    private void verInscritos() {
        Torneo torneo = elegirTorneo();
        if (torneo == null) {
            return;
        }
        List<String> lineas = club.consultarInscripciones(torneo);
        if (lineas.isEmpty()) {
            System.out.println("Todavia no hay inscritos.");
        }
        for (String linea : lineas) {
            System.out.println(" - " + linea);
        }
        System.out.println("Cupos libres: " + torneo.cuposLibres() + " de " + torneo.getCupos()
                + " (preferenciales: " + torneo.cuposPreferenciales() + ")");
    }

    private void liberarCuposPreferenciales() {
        Torneo torneo = elegirTorneo();
        if (torneo == null) {
            return;
        }
        if (entrada.confirmar("Liberar los cupos preferenciales de " + torneo.getNombre())) {
            club.liberarCuposPreferenciales(administrador.getCodigo(), torneo);
            System.out.println("LISTO: los cupos preferenciales quedaron como regulares.");
        }
    }

    // ------------------------------------------------------------------
    //  Opciones de Persona 1: LOGICA CRITICA -> implementar sin IAG
    // ------------------------------------------------------------------

    private void crearTorneo() {
        // TODO (Persona 1):
        // 1. Leer nombre (entrada.leerTexto) y elegir TipoTorneo, Deporte (elegirValor).
        // 2. Segun el deporte: si es de conjunto pedir Categoria; si es individual pedir Nivel y Modalidad.
        // 3. Leer cupos (entrada.leerEntero), tarifa solo si es COMPETITIVO, y fecha (leerFecha).
        // 4. Llamar al metodo del Club que crea el torneo (debe exigir administrador) y mostrar el resultado.
        System.out.println("Pendiente de implementar.");
    }

    private void verTorneos() {
        // TODO (Persona 1): permitir filtrar por EstadoTorneo (elegirValor) o ver todos,
        // y mostrar cada torneo con su estado.
        System.out.println("Pendiente de implementar.");
    }

    private void iniciarTorneo() {
        // TODO (Persona 1): elegirTorneo(), confirmar, llamar al metodo del Club que lo inicia.
        System.out.println("Pendiente de implementar.");
    }

    private void registrarResultado() {
        // TODO (Persona 1): elegirTorneo(), elegir un encuentro pendiente, leer marcadores, registrar.
        System.out.println("Pendiente de implementar.");
    }

    private void verPosiciones() {
        // TODO (Persona 1): elegirTorneo() y mostrar la tabla de posiciones.
        System.out.println("Pendiente de implementar.");
    }

    private void finalizarTorneo() {
        // TODO (Persona 1): elegirTorneo(), confirmar, finalizar y mostrar premios/bonos entregados.
        System.out.println("Pendiente de implementar.");
    }

    private void verHistorial() {
        // TODO (Persona 1): elegirTorneo() y mostrar encuentros, participantes y posiciones,
        // sin importar el estado del torneo.
        System.out.println("Pendiente de implementar.");
    }

    // ------------------------------------------------------------------
    //  Utilidades de entrada (codigo base de consola)
    // ------------------------------------------------------------------

    private Torneo elegirTorneo() {
        List<Torneo> torneos = club.getTorneos();
        if (torneos.isEmpty()) {
            throw new ReglaNegocioException("No hay torneos registrados");
        }
        for (int i = 0; i < torneos.size(); i++) {
            System.out.println((i + 1) + ". " + torneos.get(i));
        }
        int numero = entrada.leerEntero("Numero de torneo (0 para cancelar)", 0, torneos.size());
        return (numero == 0) ? null : torneos.get(numero - 1);
    }

    /** Muestra los valores de un enum numerados y devuelve el elegido. Ej: elegirValor("Deporte", Deporte.values()). */
    private <E extends Enum<E>> E elegirValor(String titulo, E[] valores) {
        System.out.println(titulo + ":");
        for (int i = 0; i < valores.length; i++) {
            System.out.println("  " + (i + 1) + ". " + valores[i]);
        }
        int numero = entrada.leerEntero(titulo, 1, valores.length);
        return valores[numero - 1];
    }

    /** Lee una fecha con formato AAAA-MM-DD y la vuelve a pedir si es invalida o ya paso. */
    private LocalDate leerFecha(String pregunta) {
        while (true) {
            String texto = entrada.leerTexto(pregunta + " (AAAA-MM-DD)");
            try {
                LocalDate fecha = LocalDate.parse(texto);
                if (!fecha.isBefore(LocalDate.now())) {
                    return fecha;
                }
                System.out.println("  La fecha no puede estar en el pasado.");
            } catch (DateTimeParseException e) {
                System.out.println("  Formato invalido. Ejemplo: 2026-12-05");
            }
        }
    }

    /** Lee un valor en pesos mayor o igual a cero. */
    private double leerDinero(String pregunta) {
        while (true) {
            String texto = entrada.leerTexto(pregunta);
            try {
                double valor = Double.parseDouble(texto);
                if (valor >= 0) {
                    return valor;
                }
                System.out.println("  El valor no puede ser negativo.");
            } catch (NumberFormatException e) {
                System.out.println("  Escriba un numero, por ejemplo 30000");
            }
        }
    }
}

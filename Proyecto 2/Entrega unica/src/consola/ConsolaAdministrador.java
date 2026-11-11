package consola;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import modelo.Administrador;
import modelo.Club;
import modelo.EncuentroTorneo;
import modelo.EstadoTorneo;
import modelo.InscripcionTorneo;
import modelo.ReglaNegocioException;
import modelo.Torneo;
import modelo.Usuario;
import persistencia.Persistencia;

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

    private void crearTorneo() {
        System.out.println("Pendiente de implementar.");
    }

    private void verTorneos() {
        EstadoTorneo estado = elegirValor("Estado", EstadoTorneo.values());
        List<Torneo> lista = club.consultarTorneosPorEstado(estado);
        if (lista.isEmpty()) {
            System.out.println("No hay torneos en estado " + estado);
        } else {
            for (Torneo t : lista) {
                System.out.println(" - " + t);
            }
        }
    }

    private void iniciarTorneo() {
        Torneo torneo = elegirTorneo();
        if (torneo == null) {
            return;
        }
        if (entrada.confirmar("Iniciar " + torneo.getNombre())) {
            try {
                club.iniciarTorneo(administrador.getCodigo(), torneo);
                System.out.println("LISTO: torneo iniciado con " + torneo.getEncuentros().size() + " encuentros.");
            } catch (ReglaNegocioException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void registrarResultado() {
        Torneo torneo = elegirTorneo();
        if (torneo == null) {
            return;
        }

        List<EncuentroTorneo> pendientes = new ArrayList<>();
        for (EncuentroTorneo e : torneo.getEncuentros()) {
            if (!e.isJugado()) {
                pendientes.add(e);
            }
        }

        if (pendientes.isEmpty()) {
            System.out.println("No hay encuentros pendientes.");
            return;
        }

        for (int i = 0; i < pendientes.size(); i++) {
            EncuentroTorneo e = pendientes.get(i);
            System.out.println((i + 1) + ". " + e.getLocal().getNombreParticipantes() + " vs " + e.getVisitante().getNombreParticipantes());
        }

        int numero = entrada.leerEntero("Numero de encuentro (0 para cancelar)", 0, pendientes.size());
        if (numero == 0) {
            return;
        }

        EncuentroTorneo encuentro = pendientes.get(numero - 1);
        int marcadorLocal = entrada.leerEntero("Marcador de " + encuentro.getLocal().getNombreParticipantes(), 0, 99);
        int marcadorVisitante = entrada.leerEntero("Marcador de " + encuentro.getVisitante().getNombreParticipantes(), 0, 99);

        try {
            club.registrarResultado(administrador.getCodigo(), torneo, encuentro, marcadorLocal, marcadorVisitante);
            System.out.println("LISTO: resultado registrado.");
        } catch (ReglaNegocioException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private void verPosiciones() {
        Torneo torneo = elegirTorneo();
        if (torneo == null) {
            return;
        }
        List<InscripcionTorneo> tabla = torneo.getPosiciones();
        if (tabla.isEmpty()) {
            System.out.println("Todavia no hay participantes.");
        } else {
            for (int i = 0; i < tabla.size(); i++) {
                InscripcionTorneo inscripcion = tabla.get(i);
                System.out.println((i + 1) + ". " + inscripcion.getNombreParticipantes() + " - " + torneo.puntosDe(inscripcion) + " pts");
            }
        }
    }

    private void finalizarTorneo() {
        Torneo torneo = elegirTorneo();
        if (torneo == null) {
            return;
        }
        if (entrada.confirmar("Finalizar " + torneo.getNombre())) {
            try {
                club.finalizarTorneo(administrador.getCodigo(), torneo);
                System.out.println("LISTO: torneo finalizado.");
                for (String linea : club.consultarHistorialTorneo(torneo)) {
                    System.out.println(linea);
                }
            } catch (ReglaNegocioException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void verHistorial() {
        Torneo torneo = elegirTorneo();
        if (torneo == null) {
            return;
        }
        try {
            for (String linea : club.consultarHistorialTorneo(torneo)) {
                System.out.println(linea);
            }
        } catch (ReglaNegocioException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

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

    private <E extends Enum<E>> E elegirValor(String titulo, E[] valores) {
        System.out.println(titulo + ":");
        for (int i = 0; i < valores.length; i++) {
            System.out.println("  " + (i + 1) + ". " + valores[i]);
        }
        int numero = entrada.leerEntero(titulo, 1, valores.length);
        return valores[numero - 1];
    }

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

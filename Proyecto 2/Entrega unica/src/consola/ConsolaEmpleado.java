
package consola;


import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;

import modelo.Asistencia;
import modelo.Club;
import modelo.Convocatoria;
import modelo.Deporte;
import modelo.Empleado;
import modelo.Entrenador;
import modelo.EstadoSolicitud;
import modelo.InscripcionDisciplina;
import modelo.Instalacion;
import modelo.ReglaNegocioException;
import modelo.SesionEntrenamiento;
import modelo.Socio;
import modelo.SolicitudCambioTurno;
import modelo.Turno;
import modelo.Usuario;
import modelo.Equipo;
import modelo.PartidoInterno;
import modelo.PartidoPractica;
import modelo.Jugador;
import modelo.Partido;
import modelo.EstadisticaPartido;
import modelo.Competencia;
import modelo.InscripcionEquipo;
import modelo.PartidoCompeticion;

import persistencia.Persistencia;


public class ConsolaEmpleado {

    private static final String CARPETA_DATOS = "datos";
    private static final int INTENTOS_DE_LOGIN = 3;

    private final Club club;
    private final Entrada entrada;
    private Empleado empleado;

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
            System.out.println(
                "\nSe cerro la entrada. Se guardan los datos y termina el programa."
            );

        } finally {
            club.guardar();
        }
    }

    public void iniciar() {

        System.out.println("=== Club Deportivo - Empleados ===");

        if (!autenticar()) {
            System.out.println(
                "No fue posible iniciar sesion. Termina el programa."
            );
            return;
        }

        menuPrincipal();

        System.out.println(
            "Hasta pronto. Se guardan los datos."
        );
    }

    private boolean autenticar() {

        for (int intento = 1;
             intento <= INTENTOS_DE_LOGIN;
             intento++) {

            String login = entrada.leerTexto("Usuario");
            String clave = entrada.leerTexto("Clave");

            Usuario usuario = club.iniciarSesion(login, clave);

            if (usuario instanceof Empleado) {

                empleado = (Empleado) usuario;

                System.out.println(
                    "Bienvenido(a), " + empleado.getNombre() + "."
                );

                return true;
            }

            if (usuario != null) {

                System.out.println(
                    "Esta consola es solo para empleados. "
                    + "Use la consola de su rol."
                );

                return false;
            }

            System.out.println(
                "Usuario o clave incorrectos. Intento "
                + intento + " de " + INTENTOS_DE_LOGIN
            );
        }

        return false;
    }

    
private void menuPrincipal() {

    int opcion = -1;

    while (opcion != 0) {

        System.out.println();
        System.out.println(
            "--- Menu de empleados: "
            + empleado.getNombre() + " ---"
        );

        boolean esEntrenador = empleado instanceof Entrenador;

        if (esEntrenador) {
            System.out.println("Rol: Entrenador");
        } else {
            System.out.println("Rol: Fisioterapeuta");
        }

        System.out.println("1. Consultar mis turnos");
        System.out.println("2. Consultar mis solicitudes de cambio");
        System.out.println("3. Solicitar cambio de turno");
        System.out.println("4. Sugerir producto");
        System.out.println("5. Torneos");
        System.out.println("6. Compras");

        if (esEntrenador) {
            System.out.println("7. Gestion deportiva");
        }

        System.out.println("0. Salir");

        int maximo = esEntrenador ? 7 : 6;


        opcion = entrada.leerEntero("Opcion", 0, maximo);

        ejecutar(opcion);
    }
}


    
private void ejecutar(int opcion) {

    try {

        switch (opcion) {

            case 1:
                consultarTurnos();
                break;

            case 2:
                consultarSolicitudes();
                break;

            case 3:
                solicitarCambio();
                break;

            case 4:
                sugerirProducto();
                break;

            case 5:
                new MenuTorneos(
                    club, empleado, entrada
                ).mostrar();
                break;

            
            case 6:
                new MenuCompras(
                    club, empleado, entrada
                ).mostrar();
                break;

            case 7:
                if (empleado instanceof Entrenador) {
                    menuGestionDeportiva();
                }
                break;


            default:
                break;
        }

    } catch (ReglaNegocioException e) {

        System.out.println(
            "NO SE PUDO: " + e.getMessage()
        );
    }
}


    private void consultarTurnos() {

        List<Turno> turnos = empleado.getTurnos();

        if (turnos.isEmpty()) {
            System.out.println(
                "No tiene turnos asignados."
            );
            return;
        }

        for (int i = 0; i < turnos.size(); i++) {

            Turno turno = turnos.get(i);

            System.out.println(
                (i + 1) + ". "
                + turno.getDiaSemana() + " "
                + turno.getHoraInicio() + " - "
                + turno.getHoraFin()
            );
        }
    }

    private void consultarSolicitudes() {

        boolean haySolicitudes = false;

        for (SolicitudCambioTurno solicitud :
                club.getSolicitudes()) {

            if (solicitud.getEmpleado().getCodigo()
                    == empleado.getCodigo()) {

                haySolicitudes = true;

                System.out.println(solicitud);

                if (solicitud.getEstado()
                        == EstadoSolicitud.RECHAZADA
                        && solicitud.getObservacionEvaluacion() != null) {

                    System.out.println(
                        "Motivo: "
                        + solicitud.getObservacionEvaluacion()
                    );
                }
            }
        }

        if (!haySolicitudes) {
            System.out.println(
                "No tiene solicitudes registradas."
            );
        }
    }

    private void solicitarCambio() {

        List<Turno> turnos = empleado.getTurnos();

        if (turnos.isEmpty()) {
            System.out.println(
                "No tiene turnos para cambiar."
            );
            return;
        }

        consultarTurnos();

        int numero = entrada.leerEntero(
            "Turno (0 para cancelar)",
            0,
            turnos.size()
        );

        if (numero == 0) {
            return;
        }

        Turno turno = turnos.get(numero - 1);

        String nuevoDia = entrada.leerTexto(
            "Nuevo dia de la semana"
        );

        LocalTime inicio = leerHora(
            "Hora de inicio (HH:mm)"
        );

        LocalTime fin = leerHora(
            "Hora de fin (HH:mm)"
        );

        String motivo = entrada.leerTexto("Motivo");

        if (!entrada.confirmar("Confirmar solicitud")) {
            return;
        }

        club.solicitarCambioTurno(
            empleado.getCodigo(),
            turno,
            motivo,
            nuevoDia,
            inicio,
            fin
        );

        System.out.println(
            "Solicitud registrada. "
            + "El turno original sigue vigente."
        );
    }

    private LocalTime leerHora(String pregunta) {

        while (true) {

            String valor = entrada.leerTexto(pregunta);

            try {
                return LocalTime.parse(valor);

            } catch (DateTimeParseException e) {

                System.out.println(
                    "Hora invalida. Ejemplo: 08:30"
                );
            }
        }
    }

    private void sugerirProducto() {

        String nombre = entrada.leerTexto(
            "Nombre del producto"
        );

        String descripcion = entrada.leerTexto(
            "Descripcion"
        );

        if (!entrada.confirmar("Enviar sugerencia")) {
            return;
        }

        club.sugerirProducto(
            empleado.getCodigo(),
            nombre,
            descripcion
        );

        System.out.println("Sugerencia enviada.");
    }


private void menuGestionDeportiva() {

    if (!(empleado instanceof Entrenador)) {
        System.out.println(
            "Solo los entrenadores pueden acceder."
        );
        return;
    }

    int opcion = -1;

    while (opcion != 0) {

        System.out.println();
        System.out.println("--- Gestion deportiva ---");

        System.out.println("1. Organizar entrenamiento");
        System.out.println("2. Registrar asistencia y evaluacion");
        System.out.println("3. Programar partido interno");
        System.out.println("4. Definir alineacion");
        System.out.println("5. Registrar resultado interno");
        System.out.println("6. Registrar estadisticas y tarjetas");
        System.out.println("7. Registrar resultado de competencia");
        System.out.println("8. Registrar resultado de practica");
        System.out.println("9. Consultar mis sesiones");
        System.out.println("0. Volver al menu principal");

        opcion = entrada.leerEntero("Opcion", 0, 9);

        ejecutarGestionDeportiva(opcion);
    }
}

private void ejecutarGestionDeportiva(int opcion) {

    try {

        switch (opcion) {

            case 1:
                organizarEntrenamiento();
                break;

            case 2:
                registrarAsistenciaEvaluacion();
                break;

            case 3:
                programarPartidoInterno();
                break;

            case 4:
                definirAlineacion();
                break;

            case 5:
                registrarResultadoInterno();
                break;

            case 6:
                registrarEstadisticasTarjetas();
                break;

            case 7:
                registrarResultadoCompetencia();
                break;

            case 8:
                registrarResultadoPractica();
                break;

            case 9:
                consultarMisSesiones();
                break;

            case 0:
                break;

            default:
                System.out.println(
                    "Esta funcionalidad aun no esta conectada."
                );
                break;
        }

    } catch (ReglaNegocioException e) {

        System.out.println(
            "NO SE PUDO: " + e.getMessage()
        );
    }
}

private void consultarMisSesiones() {

    boolean haySesiones = false;

    for (SesionEntrenamiento sesion : club.getSesiones()) {

        if (sesion.getEntrenador().getCodigo()
                == empleado.getCodigo()) {

            haySesiones = true;
            System.out.println(" - " + sesion);
        }
    }

    if (!haySesiones) {
        System.out.println(
            "No tienes sesiones de entrenamiento registradas."
        );
    }
}


private void organizarEntrenamiento() {

    System.out.println("\n--- Organizar entrenamiento ---");

    List<Instalacion> disponibles = new ArrayList<>();

    for (Instalacion instalacion : club.getInstalaciones()) {
        Deporte deporte = instalacion.getDisciplina();

        if (deporte == Deporte.TENIS
                || deporte == Deporte.PADEL
                || deporte == Deporte.TENIS_DE_MESA) {

            disponibles.add(instalacion);
        }
    }

    if (disponibles.isEmpty()) {
        System.out.println("No hay instalaciones de academia.");
        return;
    }

    for (int i = 0; i < disponibles.size(); i++) {
        System.out.println(
            (i + 1) + ". " + disponibles.get(i)
        );
    }

    int numero = entrada.leerEntero(
        "Instalacion (0 cancela)", 0, disponibles.size()
    );

    if (numero == 0) {
        return;
    }

    Instalacion instalacion = disponibles.get(numero - 1);

    LocalDate fecha = leerFechaEntrenamiento();
    LocalTime hora = leerHora(
        "Hora de inicio (HH:mm)"
    );

    int duracion = entrada.leerEntero(
        "Duracion en minutos", 1, 480
    );

    List<Socio> elegibles = new ArrayList<>();

    for (Usuario usuario : club.getUsuarios()) {
        if (usuario instanceof Socio) {

            Socio socio = (Socio) usuario;

            for (InscripcionDisciplina inscripcion
                    : socio.getInscripciones()) {

                if (inscripcion.getDisciplina()
                        == instalacion.getDisciplina()) {

                    elegibles.add(socio);
                    break;
                }
            }
        }
    }

    if (elegibles.isEmpty()) {
        System.out.println(
            "No hay socios inscritos en esta disciplina."
        );
        return;
    }

    System.out.println("\nSocios disponibles:");

    for (int i = 0; i < elegibles.size(); i++) {
        System.out.println(
            (i + 1) + ". " + elegibles.get(i).getNombre()
        );
    }

    List<Socio> convocados = new ArrayList<>();

    while (true) {

        int elegido = entrada.leerEntero(
            "Socio (0 termina seleccion)",
            0,
            elegibles.size()
        );

        if (elegido == 0) {
            break;
        }

        Socio socio = elegibles.get(elegido - 1);

        if (convocados.contains(socio)) {
            System.out.println("Ese socio ya fue seleccionado.");
        } else {
            convocados.add(socio);
            System.out.println("Socio agregado.");
        }
    }

    if (convocados.isEmpty()) {
        System.out.println("No se seleccionaron convocados.");
        return;
    }

    if (!entrada.confirmar("Crear entrenamiento")) {
        return;
    }

    SesionEntrenamiento sesion = club.organizarSesion(
        empleado.getCodigo(),
        instalacion,
        fecha,
        hora,
        duracion,
        convocados
    );

    System.out.println("Entrenamiento creado:");
    System.out.println(sesion);
}


private LocalDate leerFechaEntrenamiento() {

    while (true) {

        String texto = entrada.leerTexto(
            "Fecha (AAAA-MM-DD)"
        );

        try {
            LocalDate fecha = LocalDate.parse(texto);

            if (fecha.isBefore(LocalDate.now())) {
                System.out.println(
                    "La fecha no puede estar en el pasado."
                );
                continue;
            }

            return fecha;

        } catch (DateTimeParseException e) {
            System.out.println(
                "Fecha invalida. Ejemplo: 2026-12-05"
            );
        }
    }
}


private void registrarAsistenciaEvaluacion() {

    System.out.println(
        "\n--- Registrar asistencia y evaluacion ---"
    );

    List<SesionEntrenamiento> sesiones = new ArrayList<>();

    for (SesionEntrenamiento sesion : club.getSesiones()) {

        if (sesion.getEntrenador().getCodigo()
                == empleado.getCodigo()
                && sesion.haTerminado()) {

            sesiones.add(sesion);
        }
    }

    if (sesiones.isEmpty()) {
        System.out.println(
            "No tienes sesiones finalizadas."
        );
        return;
    }

    System.out.println("\nSesiones finalizadas:");

    for (int i = 0; i < sesiones.size(); i++) {
        System.out.println(
            (i + 1) + ". " + sesiones.get(i)
        );
    }

    int numeroSesion = entrada.leerEntero(
        "Seleccione sesion (0 cancela)",
        0,
        sesiones.size()
    );

    if (numeroSesion == 0) {
        return;
    }

    SesionEntrenamiento sesion =
        sesiones.get(numeroSesion - 1);

    List<Convocatoria> convocatorias =
        sesion.getConvocatorias();

    if (convocatorias.isEmpty()) {
        System.out.println(
            "Esta sesion no tiene jugadores convocados."
        );
        return;
    }

    System.out.println("\nJugadores convocados:");

    for (int i = 0; i < convocatorias.size(); i++) {

        Convocatoria convocatoria = convocatorias.get(i);

        System.out.println(
            (i + 1) + ". "
            + convocatoria.getSocio().getNombre()
            + " - "
            + (convocatoria.getAsistencia() == null
                ? "Pendiente"
                : convocatoria.getAsistencia())
        );
    }

    int numeroJugador = entrada.leerEntero(
        "Seleccione jugador (0 cancela)",
        0,
        convocatorias.size()
    );

    if (numeroJugador == 0) {
        return;
    }

    Convocatoria convocatoria =
        convocatorias.get(numeroJugador - 1);

    Socio socio = convocatoria.getSocio();

    if (convocatoria.getAsistencia() != null) {
        System.out.println(
            "Este jugador ya tiene asistencia registrada."
        );
        return;
    }

    System.out.println("\nEstado de asistencia:");
    System.out.println("1. Asistio");
    System.out.println("2. Falto");
    System.out.println("3. Llego tarde");

    int opcionAsistencia = entrada.leerEntero(
        "Seleccione estado", 1, 3
    );

    Asistencia asistencia;

    switch (opcionAsistencia) {

        case 1:
            asistencia = Asistencia.ASISTIO;
            break;

        case 2:
            asistencia = Asistencia.FALTO;
            break;

        default:
            asistencia = Asistencia.LLEGO_TARDE;
            break;
    }

    int nota = 0;
    String comentario = null;

    if (asistencia != Asistencia.FALTO) {

        if (entrada.confirmar("Registrar evaluacion")) {

            nota = entrada.leerEntero(
                "Nota", 1, 5
            );

            comentario = entrada.leerTexto(
                "Comentario"
            );
        }
    }

    if (!entrada.confirmar("Guardar asistencia")) {
        return;
    }

    club.registrarAsistencia(
        empleado.getCodigo(),
        sesion,
        socio,
        asistencia,
        nota,
        comentario
    );

    System.out.println(
        "Asistencia registrada correctamente."
    );

    if (nota > 0) {
        System.out.println(
            "Evaluacion registrada. Nota: " + nota
        );
    }
}


private void programarPartidoInterno() {

    System.out.println("\n--- Programar partido interno ---");

    Equipo local = seleccionarEquipo("Equipo local");
    if (local == null) return;

    Equipo visitante;

    while (true) {

        visitante = seleccionarEquipo("Equipo visitante");
        if (visitante == null) return;

        if (local == visitante) {
            System.out.println(
                "Un equipo no puede jugar contra si mismo."
            );
            continue;
        }

        if (local.getDeporte() != visitante.getDeporte()
                || local.getCategoria() != visitante.getCategoria()) {
            System.out.println(
                "Los equipos deben ser del mismo deporte y categoria."
            );
            continue;
        }

        break;
    }

    LocalDate fecha = leerFechaPartido();

    if (!entrada.confirmar("Confirmar programacion")) return;

    PartidoInterno partido = club.programarPartidoInterno(
        empleado.getCodigo(), local, visitante, fecha
    );

    System.out.println("Partido programado correctamente.");
    System.out.println(
        partido.getEquipo1().getNombre() + " vs "
        + partido.getEquipo2().getNombre()
        + " | " + partido.getFecha()
    );
}


private void definirAlineacion() {

    System.out.println("\n--- Definir alineacion ---");

    PartidoInterno partido = seleccionarPartidoInterno(false);
    if (partido == null) return;

    System.out.println("1. " + partido.getEquipo1().getNombre());
    System.out.println("2. " + partido.getEquipo2().getNombre());

    int opcion = entrada.leerEntero("Equipo", 1, 2);

    Equipo equipo = opcion == 1
        ? partido.getEquipo1()
        : partido.getEquipo2();

    List<Jugador> habilitados = new ArrayList<>();

    for (Jugador jugador : equipo.getJugadores()) {
        if (!jugador.estaSuspendido()) {
            habilitados.add(jugador);
        }
    }

    if (habilitados.isEmpty()) {
        System.out.println("No hay jugadores habilitados.");
        return;
    }

    List<Jugador> alineacion = new ArrayList<>();

    while (true) {

        System.out.println("\nJugadores habilitados:");

        for (int i = 0; i < habilitados.size(); i++) {
            Jugador jugador = habilitados.get(i);

            System.out.println(
                (i + 1) + ". " + jugador.getNombre()
                + " | Camiseta: " + jugador.getNumeroCamiseta()
                + (alineacion.contains(jugador) ? " [SELECCIONADO]" : "")
            );
        }

        int numero = entrada.leerEntero(
            "Agregar jugador (0 terminar)", 0, habilitados.size()
        );

        if (numero == 0) break;

        Jugador jugador = habilitados.get(numero - 1);

        if (alineacion.contains(jugador)) {
            System.out.println("Jugador ya seleccionado.");
        } else {
            alineacion.add(jugador);
            System.out.println("Jugador agregado.");
        }
    }

    if (alineacion.isEmpty()) return;

    System.out.println("Jugadores seleccionados: " + alineacion.size());

    if (!entrada.confirmar("Guardar alineacion")) return;

    club.definirAlineacionInterna(
        empleado.getCodigo(), partido, equipo, alineacion
    );

    System.out.println("Alineacion guardada correctamente.");
}

private void registrarResultadoInterno() {

    System.out.println("\n--- Registrar resultado interno ---");

    PartidoInterno partido = seleccionarPartidoInterno(true);
    if (partido == null) return;

    System.out.println(
        partido.getEquipo1().getNombre() + " vs "
        + partido.getEquipo2().getNombre()
    );

    int marcadorLocal = entrada.leerEntero(
        "Marcador " + partido.getEquipo1().getNombre(), 0, 999
    );

    int marcadorVisitante = entrada.leerEntero(
        "Marcador " + partido.getEquipo2().getNombre(), 0, 999
    );

    if (!entrada.confirmar("Confirmar resultado")) return;

    club.registrarResultadoInterno(
        empleado.getCodigo(), partido, marcadorLocal, marcadorVisitante
    );

    System.out.println("Resultado registrado correctamente.");
}

private Equipo seleccionarEquipo(String titulo) {

    List<Equipo> equipos = club.getEquipos();

    if (equipos.isEmpty()) {
        System.out.println("No hay equipos registrados.");
        return null;
    }

    System.out.println("\n" + titulo);

    for (int i = 0; i < equipos.size(); i++) {
        Equipo equipo = equipos.get(i);

        System.out.println(
            (i + 1) + ". " + equipo.getNombre()
            + " | " + equipo.getDeporte()
            + " | " + equipo.getCategoria()
        );
    }

    int numero = entrada.leerEntero(
        "Seleccione equipo (0 cancela)", 0, equipos.size()
    );

    return numero == 0 ? null : equipos.get(numero - 1);
}

private PartidoInterno seleccionarPartidoInterno(
        boolean paraResultado) {

    List<PartidoInterno> disponibles = new ArrayList<>();

    for (Partido partido : club.getPartidos()) {

        if (!(partido instanceof PartidoInterno)) continue;

        PartidoInterno interno = (PartidoInterno) partido;

        if (interno.getCodigoEntrenadorResponsable()
                != empleado.getCodigo()) continue;

        if (interno.tieneResultado()) continue;

        if (paraResultado
                && interno.getFecha().isAfter(LocalDate.now())) {
            continue;
        }

        disponibles.add(interno);
    }

    if (disponibles.isEmpty()) {
        System.out.println("No hay partidos disponibles.");
        return null;
    }

    for (int i = 0; i < disponibles.size(); i++) {
        PartidoInterno partido = disponibles.get(i);

        System.out.println(
            (i + 1) + ". "
            + partido.getEquipo1().getNombre() + " vs "
            + partido.getEquipo2().getNombre()
            + " | " + partido.getFecha()
        );
    }

    int numero = entrada.leerEntero(
        "Seleccione partido (0 cancela)", 0, disponibles.size()
    );

    return numero == 0 ? null : disponibles.get(numero - 1);
}

private LocalDate leerFechaPartido() {

    while (true) {

        String texto = entrada.leerTexto(
            "Fecha futura (AAAA-MM-DD)"
        );

        try {
            LocalDate fecha = LocalDate.parse(texto);

            if (!fecha.isAfter(LocalDate.now())) {
                System.out.println("Debe ser una fecha futura.");
                continue;
            }

            return fecha;

        } catch (DateTimeParseException e) {
            System.out.println("Formato de fecha invalido.");
        }
    }
}


private void registrarEstadisticasTarjetas() {

    System.out.println("\n--- Registrar estadisticas y tarjetas ---");

    List<PartidoInterno> partidosDisponibles = new ArrayList<>();

    for (Partido partido : club.getPartidos()) {
        if (partido instanceof PartidoInterno) {
            PartidoInterno interno = (PartidoInterno) partido;

            if (interno.getCodigoEntrenadorResponsable() == empleado.getCodigo()
                    && interno.tieneResultado()) {
                partidosDisponibles.add(interno);
            }
        }
    }

    if (partidosDisponibles.isEmpty()) {
        System.out.println("No tienes partidos finalizados.");
        return;
    }

    System.out.println("\nPartidos finalizados:");

    for (int i = 0; i < partidosDisponibles.size(); i++) {
        PartidoInterno partido = partidosDisponibles.get(i);

        System.out.println(
            (i + 1) + ". "
            + partido.getEquipo1().getNombre() + " vs "
            + partido.getEquipo2().getNombre()
            + " | " + partido.getFecha()
            + " | " + partido.getDeporte()
        );
    }

    int numeroPartido = entrada.leerEntero(
        "Seleccione partido (0 cancela)", 0, partidosDisponibles.size()
    );

    if (numeroPartido == 0) return;

    PartidoInterno partido = partidosDisponibles.get(numeroPartido - 1);

    List<Jugador> jugadores = new ArrayList<>();

    jugadores.addAll(partido.getAlineacionLocal());
    jugadores.addAll(partido.getAlineacionVisitante());

    if (jugadores.isEmpty()) {
        System.out.println("El partido no tiene jugadores alineados.");
        return;
    }

    System.out.println("\nJugadores:");

    for (int i = 0; i < jugadores.size(); i++) {
        Jugador jugador = jugadores.get(i);
        boolean registrado = false;

        for (EstadisticaPartido estadistica : partido.getEstadisticas()) {
            if (estadistica.getJugador() == jugador) {
                registrado = true;
                break;
            }
        }

        System.out.println(
            (i + 1) + ". " + jugador.getNombre()
            + (registrado ? " [REGISTRADO]" : " [PENDIENTE]")
        );
    }

    int numeroJugador = entrada.leerEntero(
        "Seleccione jugador (0 cancela)", 0, jugadores.size()
    );

    if (numeroJugador == 0) return;

    Jugador jugador = jugadores.get(numeroJugador - 1);

    for (EstadisticaPartido estadistica : partido.getEstadisticas()) {
        if (estadistica.getJugador() == jugador) {
            System.out.println("Este jugador ya tiene estadisticas registradas.");
            return;
        }
    }

    Map<String, Integer> valores = new HashMap<>();

    if (partido.getDeporte() == Deporte.FUTBOL) {

        valores.put("goles",
            entrada.leerEntero("Goles", 0, 999));

        valores.put("asistencias",
            entrada.leerEntero("Asistencias", 0, 999));

        valores.put("tarjetasAmarillas",
            entrada.leerEntero("Tarjetas amarillas", 0, 2));

        valores.put("tarjetasRojas",
            entrada.leerEntero("Tarjetas rojas", 0, 1));

    } else if (partido.getDeporte() == Deporte.BALONCESTO) {

        valores.put("puntos",
            entrada.leerEntero("Puntos", 0, 999));

        valores.put("rebotes",
            entrada.leerEntero("Rebotes", 0, 999));

    } else if (partido.getDeporte() == Deporte.VOLEIBOL) {

        valores.put("aces",
            entrada.leerEntero("Aces", 0, 999));

        valores.put("bloqueos",
            entrada.leerEntero("Bloqueos", 0, 999));

    } else {
        System.out.println("Deporte no compatible.");
        return;
    }

    Map<Jugador, Map<String, Integer>> estadisticas = new HashMap<>();
    estadisticas.put(jugador, valores);

    if (!entrada.confirmar("Guardar estadisticas")) return;

    club.registrarEstadisticasPartidoInterno(
        empleado.getCodigo(), partido, estadisticas
    );

    System.out.println("Estadisticas registradas correctamente.");
}


private void registrarResultadoCompetencia() {

    System.out.println("\n--- Registrar resultado de competencia ---");

    List<InscripcionEquipo> inscripciones = new ArrayList<>();

    for (Competencia competencia : club.getCompetencias()) {
        inscripciones.addAll(competencia.getInscripciones());
    }

    if (inscripciones.isEmpty()) {
        System.out.println("No hay equipos inscritos en competencias.");
        return;
    }

    System.out.println("\nEquipos inscritos:");

    for (int i = 0; i < inscripciones.size(); i++) {
        InscripcionEquipo inscripcion = inscripciones.get(i);

        System.out.println(
            (i + 1) + ". "
            + inscripcion.getEquipo().getNombre()
            + " | Competencia: "
            + inscripcion.getCompetencia().getNombre()
            + " | Deporte: "
            + inscripcion.getEquipo().getDeporte()
        );
    }

    int numero = entrada.leerEntero(
        "Seleccione inscripcion (0 cancela)",
        0, inscripciones.size()
    );

    if (numero == 0) return;

    InscripcionEquipo inscripcion = inscripciones.get(numero - 1);

    String rival = entrada.leerTexto("Institucion rival");
    String escenario = entrada.leerTexto("Escenario");

    LocalDate fecha;

    while (true) {
        String texto = entrada.leerTexto(
            "Fecha del partido (AAAA-MM-DD)"
        );

        try {
            fecha = LocalDate.parse(texto);

            if (fecha.isAfter(LocalDate.now())) {
                System.out.println(
                    "No se puede registrar un resultado futuro."
                );
                continue;
            }

            break;

        } catch (DateTimeParseException e) {
            System.out.println("Fecha invalida.");
        }
    }

    int marcadorPropio = entrada.leerEntero(
        "Marcador de " + inscripcion.getEquipo().getNombre(),
        0, 999
    );

    int marcadorRival = entrada.leerEntero(
        "Marcador del rival", 0, 999
    );

    System.out.println("\nResumen:");
    System.out.println(
        inscripcion.getEquipo().getNombre()
        + " " + marcadorPropio
        + " - " + marcadorRival
        + " " + rival
    );

    System.out.println("Competencia: "
        + inscripcion.getCompetencia().getNombre());

    if (!entrada.confirmar("Registrar resultado")) return;

    PartidoCompeticion partido = club.registrarPartidoCompeticion(
        empleado.getCodigo(),
        inscripcion,
        rival,
        escenario,
        fecha,
        marcadorPropio,
        marcadorRival,
        null
    );

    System.out.println("Resultado registrado correctamente.");
    System.out.println("Rival: " + partido.getInstitucionRival());
    System.out.println("Fecha: " + partido.getFecha());
    System.out.println("Puntos acumulados: " + inscripcion.getPuntos());
}


private void registrarResultadoPractica() {

    System.out.println("\n--- Registrar resultado de practica ---");

    List<PartidoPractica> disponibles = new ArrayList<>();

    for (Partido partido : club.getPartidos()) {

        if (partido instanceof PartidoPractica) {
            PartidoPractica practica = (PartidoPractica) partido;

            if (!practica.resuelto()
                    && !practica.getFecha().isAfter(LocalDate.now())) {
                disponibles.add(practica);
            }
        }
    }

    if (disponibles.isEmpty()) {
        System.out.println("No hay practicas pendientes de resultado.");
        return;
    }

    System.out.println("\nPracticas pendientes:");

    for (int i = 0; i < disponibles.size(); i++) {
        PartidoPractica practica = disponibles.get(i);

        System.out.println(
            (i + 1) + ". "
            + practica.getDeporte()
            + " | " + practica.getModalidad()
            + " | " + practica.getFecha()
        );

        for (InscripcionDisciplina inscripcion : practica.getParticipantes()) {
            System.out.println(
                "   - " + inscripcion.getSocio().getNombre()
            );
        }
    }

    int numero = entrada.leerEntero(
        "Seleccione practica (0 cancela)", 0, disponibles.size()
    );

    if (numero == 0) return;

    PartidoPractica practica = disponibles.get(numero - 1);

    Map<InscripcionDisciplina, Map<String, Integer>> estadisticas =
        new HashMap<>();

    for (InscripcionDisciplina inscripcion : practica.getParticipantes()) {

        System.out.println(
            "\nJugador: " + inscripcion.getSocio().getNombre()
        );

        Map<String, Integer> valores = new HashMap<>();

        valores.put("sets",
            entrada.leerEntero("Sets ganados", 0, 5));

        if (practica.getDeporte() == Deporte.TENIS_DE_MESA) {

            valores.put("puntosTotales",
                entrada.leerEntero("Puntos totales", 0, 999));

        } else {

            valores.put("games",
                entrada.leerEntero("Games ganados", 0, 99));

            valores.put("aces",
                entrada.leerEntero("Aces", 0, 999));

            valores.put("erroresNoForzados",
                entrada.leerEntero("Errores no forzados", 0, 999));
        }

        estadisticas.put(inscripcion, valores);
    }

    System.out.println("\nResumen de estadisticas:");

    for (InscripcionDisciplina inscripcion : practica.getParticipantes()) {
        System.out.println(
            inscripcion.getSocio().getNombre()
            + ": " + estadisticas.get(inscripcion)
        );
    }

    if (!entrada.confirmar("Registrar resultado y actualizar ranking")) {
        return;
    }

    club.registrarPractica(practica, estadisticas);

    System.out.println("Resultado registrado correctamente.");

    System.out.println("\nRanking actualizado:");

    for (InscripcionDisciplina inscripcion : practica.getParticipantes()) {
        System.out.println(
            inscripcion.getSocio().getNombre()
            + " | Ranking: " + inscripcion.getPuntajeRanking()
        );
    }
}


}

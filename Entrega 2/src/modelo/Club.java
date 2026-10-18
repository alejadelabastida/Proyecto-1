package modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import persistencia.Persistencia;

public class Club implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Usuario> usuarios;
    private List<Equipo> equipos;
    private List<Competencia> competencias;
    private List<Instalacion> instalaciones;
    private List<Reserva> reservas;
    private List<SesionEntrenamiento> sesiones;
    private List<Partido> partidos;
    private List<Venta> ventas;
    private List<Producto> productos;
    private List<UbicacionInventario> ubicaciones;
    private List<ReglaDeporte> reglas;
    private List<SolicitudCambioTurno> solicitudes;

    private transient Persistencia persistencia;

    public Club() {
        usuarios = new ArrayList<>();
        equipos = new ArrayList<>();
        competencias = new ArrayList<>();
        instalaciones = new ArrayList<>();
        reservas = new ArrayList<>();
        sesiones = new ArrayList<>();
        partidos = new ArrayList<>();
        ventas = new ArrayList<>();
        productos = new ArrayList<>();
        ubicaciones = new ArrayList<>();
        reglas = new ArrayList<>();
        solicitudes = new ArrayList<>();
    }

    public void setPersistencia(Persistencia persistencia) {
        this.persistencia = persistencia;
    }

    public void guardar() {
        if (persistencia != null) {
            persistencia.guardar(this);
        }
    }

    public List<Usuario> getUsuarios() { return usuarios; }
    public List<Equipo> getEquipos() { return equipos; }
    public List<Competencia> getCompetencias() { return competencias; }
    public List<Instalacion> getInstalaciones() { return instalaciones; }
    public List<Reserva> getReservas() { return reservas; }
    public List<SesionEntrenamiento> getSesiones() { return sesiones; }
    public List<Partido> getPartidos() { return partidos; }
    public List<Venta> getVentas() { return ventas; }
    public List<Producto> getProductos() { return productos; }
    public List<UbicacionInventario> getUbicaciones() { return ubicaciones; }
    public List<ReglaDeporte> getReglas() { return reglas; }
    public List<SolicitudCambioTurno> getSolicitudes() { return solicitudes; }

    public ReglaDeporte buscarRegla(Deporte deporte) {
        for (ReglaDeporte r : reglas) {
            if (r.getDeporte() == deporte) {
                return r;
            }
        }
        throw new ReglaNegocioException("No hay reglas definidas para " + deporte);
    }

    private Usuario buscarPorCodigo(int codigo) {
        for (Usuario u : usuarios) {
            if (u.getCodigo() == codigo) {
                return u;
            }
        }
        return null;
    }

    private Entrenador buscarEntrenador(int codigo) {
        Usuario u = buscarPorCodigo(codigo);
        if (u instanceof Entrenador) {
            return (Entrenador) u;
        }
        return null;
    }

    private Empleado buscarEmpleado(int codigo) {
        Usuario u = buscarPorCodigo(codigo);
        if (u instanceof Empleado) {
            return (Empleado) u;
        }
        return null;
    }

    private void validarPeriodo(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null || hasta.isBefore(desde)) {
            throw new ReglaNegocioException("La fecha final es anterior a la inicial");
        }
    }

    private boolean enPeriodo(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
    }

    private List<String> sinVacio(List<String> lineas) {
        if (lineas.isEmpty()) {
            throw new ReglaNegocioException("No hay datos en el periodo seleccionado");
        }
        return lineas;
    }

    // =====================================================================
    //  PERSONA 1: usuarios, deportes de conjunto y competencias
    //  (cada quien reemplaza el cuerpo de sus metodos; no cambien las firmas)
    // =====================================================================

    public Usuario iniciarSesion(String login, String password) {
        throw new UnsupportedOperationException("Pendiente: Persona 1");
    }

    public Jugador inscribirJugador(int codigoSocio, String nombreEquipo, String posicion, int numeroCamiseta) {
        throw new UnsupportedOperationException("Pendiente: Persona 1");
    }

    public PartidoInterno registrarPartidoInterno(int codigoEntrenador, Equipo local, Equipo visitante,
            LocalDate fecha, int marcadorLocal, int marcadorVisitante,
            Map<Jugador, Map<String, Integer>> estadisticas) {
        throw new UnsupportedOperationException("Pendiente: Persona 1");
    }

    public InscripcionEquipo inscribirEquipo(int codigoEntrenador, Equipo equipo,
            Competencia competencia, List<Jugador> jugadores) {
        throw new UnsupportedOperationException("Pendiente: Persona 1");
    }

    public PartidoCompeticion registrarPartidoCompeticion(int codigoEntrenador, InscripcionEquipo inscripcion,
            String institucionRival, String escenario, LocalDate fecha, int marcadorPropio, int marcadorRival,
            Map<Jugador, Map<String, Integer>> estadisticas) {
        throw new UnsupportedOperationException("Pendiente: Persona 1");
    }

    // =====================================================================
    //  PERSONA 3: individuales, tienda, cafeteria, inventario y ventas
    // =====================================================================

    public Reserva reservar(int codigoSocio, Instalacion instalacion, LocalDate fecha,
            LocalTime horaInicio, LocalTime horaFin, int numeroJugadores, Modalidad modalidad) {
        throw new UnsupportedOperationException("Pendiente: Persona 3");
    }

    public PartidoPractica programarPractica(List<InscripcionDisciplina> inscripciones,
            Modalidad modalidad, LocalDate fecha) {
        throw new UnsupportedOperationException("Pendiente: Persona 3");
    }

    public void registrarPractica(PartidoPractica partido,
            Map<InscripcionDisciplina, Map<String, Integer>> estadisticas) {
        throw new UnsupportedOperationException("Pendiente: Persona 3");
    }

    public VentaCafeteria comprarCafeteria(int codigoSocio, Map<Producto, Integer> productos,
            double propina, int puntosAUsar) {
        throw new UnsupportedOperationException("Pendiente: Persona 3");
    }

    public VentaTienda comprarTienda(int codigoComprador, Map<ArticuloTienda, Integer> articulos,
            String codigoDescuento) {
        throw new UnsupportedOperationException("Pendiente: Persona 3");
    }

    // =====================================================================
    //  PERSONA 2: entrenamientos
    // =====================================================================

    public SesionEntrenamiento organizarSesion(int codigoEntrenador, Instalacion instalacion,
            LocalDate fecha, LocalTime hora, int duracionMinutos, List<Socio> convocados) {
        Entrenador entrenador = buscarEntrenador(codigoEntrenador);
        if (entrenador == null) {
            throw new ReglaNegocioException("Solo un entrenador puede organizar sesiones");
        }
        if (duracionMinutos <= 0 || hora.plusMinutes(duracionMinutos).isBefore(hora)) {
            throw new ReglaNegocioException("Duracion invalida: la sesion debe terminar el mismo dia");
        }
        if (convocados == null || convocados.isEmpty()) {
            throw new ReglaNegocioException("Debe convocar al menos un socio");
        }
        if (!instalacion.disponible(fecha, hora, duracionMinutos)) {
            throw new ReglaNegocioException("La instalacion esta ocupada en ese horario");
        }

        SesionEntrenamiento sesion = new SesionEntrenamiento(entrenador, instalacion, fecha, hora, duracionMinutos);
        for (Socio socio : convocados) {
            new Convocatoria(sesion, socio);
        }
        sesiones.add(sesion);
        guardar();
        return sesion;
    }

    public void registrarAsistencia(int codigoEntrenador, SesionEntrenamiento sesion, Socio socio,
            Asistencia asistencia, int nota, String comentario) {
        Entrenador entrenador = buscarEntrenador(codigoEntrenador);
        if (entrenador == null) {
            throw new ReglaNegocioException("Solo un entrenador puede registrar asistencia");
        }
        if (sesion.getEntrenador() != entrenador) {
            throw new ReglaNegocioException("Solo el entrenador de la sesion puede registrar la asistencia");
        }
        if (!sesion.haTerminado()) {
            throw new ReglaNegocioException("La sesion aun no ha terminado");
        }
        Convocatoria convocatoria = sesion.buscarConvocatoria(socio);
        if (convocatoria == null) {
            throw new ReglaNegocioException("El socio no fue convocado a esta sesion");
        }
        convocatoria.registrar(asistencia, nota, comentario);
        guardar();
    }

    public List<Convocatoria> consultarConvocatorias(Socio socio) {
        List<Convocatoria> resultado = new ArrayList<>();
        for (SesionEntrenamiento s : sesiones) {
            Convocatoria c = s.buscarConvocatoria(socio);
            if (c != null) {
                resultado.add(c);
            }
        }
        if (resultado.isEmpty()) {
            throw new ReglaNegocioException("No existe ninguna convocatoria para este socio");
        }
        return resultado;
    }


    public Convocatoria consultarAsistencia(Socio socio, SesionEntrenamiento sesion) {
        Convocatoria convocatoria = sesion.buscarConvocatoria(socio);
        if (convocatoria == null) {
            throw new ReglaNegocioException("El socio no fue convocado a esta sesion");
        }
        if (!sesion.haTerminado()) {
            throw new ReglaNegocioException("La sesion esta en proceso: aun no hay asistencia");
        }
        if (convocatoria.getAsistencia() == null) {
            throw new ReglaNegocioException("La asistencia de esta sesion aun no se ha registrado");
        }
        return convocatoria;
    }

    public List<Convocatoria> consultarDesempenoJugador(int codigoEmpleado, Socio socio) {
        Empleado empleado = buscarEmpleado(codigoEmpleado);
        if (empleado == null) {
            throw new ReglaNegocioException("Solo un entrenador o fisioterapeuta puede consultar el desempeno");
        }
        List<Convocatoria> resultado = new ArrayList<>();
        for (SesionEntrenamiento s : sesiones) {
            Convocatoria c = s.buscarConvocatoria(socio);
            if (c != null && c.getAsistencia() != null) {
                resultado.add(c);
            }
        }
        if (resultado.isEmpty()) {
            throw new ReglaNegocioException("El jugador no tiene asistencias registradas");
        }
        return resultado;
    }

    // =====================================================================
    //  PERSONA 2: turnos
    // =====================================================================

    public SolicitudCambioTurno solicitarCambioTurno(int codigoEmpleado, Turno turno, String motivo,
            String nuevoDiaSemana, LocalTime nuevaHoraInicio, LocalTime nuevaHoraFin) {
        Empleado empleado = buscarEmpleado(codigoEmpleado);
        if (empleado == null) {
            throw new ReglaNegocioException("Solo un entrenador o fisioterapeuta puede pedir cambio de turno");
        }
        if (turno.getEmpleado() != empleado) {
            throw new ReglaNegocioException("El turno no pertenece a este empleado");
        }
        if (!nuevaHoraInicio.isBefore(nuevaHoraFin)) {
            throw new ReglaNegocioException("El horario nuevo es invalido");
        }
        SolicitudCambioTurno solicitud = new SolicitudCambioTurno(empleado, turno, motivo,
                nuevoDiaSemana, nuevaHoraInicio, nuevaHoraFin);
        solicitudes.add(solicitud);
        guardar();
        return solicitud;
    }

    public void aprobarCambio(SolicitudCambioTurno solicitud) {
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ReglaNegocioException("La solicitud ya fue evaluada");
        }
        if (cumpleMinimos(solicitud)) {
            solicitud.aprobar();
            solicitud.getTurno().cambiar(solicitud);
            guardar();
        } else {
            solicitud.rechazar();
            guardar();
            throw new ReglaNegocioException(
                    "El cambio dejaria el horario por debajo del minimo (2 entrenadores y 1 fisioterapeuta)");
        }
    }

  
    public void rechazarCambio(SolicitudCambioTurno solicitud, String observacion) {
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ReglaNegocioException("La solicitud ya fue evaluada");
        }
        solicitud.setObservacionEvaluacion(observacion);
        solicitud.rechazar();
        guardar();
    }

    public List<Turno> consultarTurnos(int codigoAdministrador) {
        if (!(buscarPorCodigo(codigoAdministrador) instanceof Administrador)) {
            throw new ReglaNegocioException("Solo el administrador puede consultar los turnos");
        }
        List<Turno> resultado = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (u instanceof Empleado) {
                resultado.addAll(((Empleado) u).getTurnos());
            }
        }
        return resultado;
    }
    
    private boolean cumpleMinimos(SolicitudCambioTurno solicitud) {
        Turno turno = solicitud.getTurno();
        boolean esEntrenador = turno.getEmpleado() instanceof Entrenador;
        int minimo = esEntrenador ? 2 : 1;

        LocalTime hora = turno.getHoraInicio();
        while (hora.isBefore(turno.getHoraFin())) {
            int cubiertos = 0;
            for (Usuario u : usuarios) {
                if (u instanceof Empleado && (u instanceof Entrenador) == esEntrenador) {
                    for (Turno t : ((Empleado) u).getTurnos()) {
                        if (t == turno) {
                            if (solicitud.cubreNuevo(turno.getDiaSemana(), hora)) {
                                cubiertos++;
                            }
                        } else if (t.cubre(turno.getDiaSemana(), hora)) {
                            cubiertos++;
                        }
                    }
                }
            }
            if (cubiertos < minimo) {
                return false;
            }
            LocalTime siguiente = hora.plusMinutes(30);
            if (siguiente.isBefore(hora)) {
                break; 
            }
            hora = siguiente;
        }
        return true;
    }

    // =====================================================================
    //  PERSONA 2: reportes deportivos e historiales (RF-34, RF-40, RF-41)
    //  Todos lanzan ReglaNegocioException si el periodo es invalido o no hay datos.
    // =====================================================================

    public List<String> reporteAsistencia(LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        List<String> lineas = new ArrayList<>();
        for (SesionEntrenamiento s : sesiones) {
            if (enPeriodo(s.getFecha(), desde, hasta) && s.haTerminado()) {
                int asistio = 0;
                int falto = 0;
                int tarde = 0;
                int sinRegistro = 0;
                for (Convocatoria c : s.getConvocatorias()) {
                    if (c.getAsistencia() == null) {
                        sinRegistro++;
                    } else if (c.getAsistencia() == Asistencia.ASISTIO) {
                        asistio++;
                    } else if (c.getAsistencia() == Asistencia.FALTO) {
                        falto++;
                    } else {
                        tarde++;
                    }
                }
                lineas.add(s.getFecha() + " " + s.getHora() + " | " + s.getInstalacion().getNombre()
                        + " | asistio: " + asistio + " | falto: " + falto + " | llego tarde: " + tarde
                        + " | sin registrar: " + sinRegistro);
            }
        }
        return sinVacio(lineas);
    }


    public List<String> reporteResultados(LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        List<String> lineas = new ArrayList<>();
        for (Partido p : partidos) {
            if (enPeriodo(p.getFecha(), desde, hasta)) {
                String linea = p.getFecha() + " | " + p.getDeporte() + " | " + p.getClass().getSimpleName();
                if (p instanceof PartidoEquipo) {
                    PartidoEquipo pe = (PartidoEquipo) p;
                    linea += " | marcador: " + pe.getMarcadorEquipo1() + " - " + pe.getMarcadorEquipo2();
                } else if (p instanceof PartidoPractica) {
                    linea += " | resultado registrado: " + (((PartidoPractica) p).resuelto() ? "si" : "no");
                }
                lineas.add(linea);
            }
        }
        return sinVacio(lineas);
    }

    public List<String> reporteEvolucionRanking(Deporte deporte, LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        List<String> lineas = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (u instanceof Socio) {
                Socio socio = (Socio) u;
                for (InscripcionDisciplina inscripcion : socio.getInscripciones()) {
                    if (inscripcion.getDisciplina() == deporte) {
                        for (RegistroRanking r : inscripcion.getHistorial()) {
                            if (enPeriodo(r.getFecha(), desde, hasta)) {
                                lineas.add(r.getFecha() + " | " + socio.getNombre() + " | "
                                        + r.getPuntajeAnterior() + " -> " + r.getPuntajeNuevo());
                            }
                        }
                    }
                }
            }
        }
        return sinVacio(lineas);
    }


    public List<String> historialReservas(LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        List<String> lineas = new ArrayList<>();
        for (Reserva r : reservas) {
            if (enPeriodo(r.getFecha(), desde, hasta)) {
                lineas.add(r.getFecha() + " " + r.getHoraInicio() + "-" + r.getHoraFin() + " | "
                        + r.getInstalacion().getNombre() + " | " + r.getSocio().getNombre());
            }
        }
        return sinVacio(lineas);
    }

    public List<String> historialPartidos(LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        List<String> lineas = new ArrayList<>();
        for (Partido p : partidos) {
            if (enPeriodo(p.getFecha(), desde, hasta)) {
                lineas.add(p.getFecha() + " | " + p.getDeporte() + " | " + p.getClass().getSimpleName());
            }
        }
        return sinVacio(lineas);
    }

    public List<String> historialSesiones(LocalDate desde, LocalDate hasta) {
        validarPeriodo(desde, hasta);
        List<String> lineas = new ArrayList<>();
        for (SesionEntrenamiento s : sesiones) {
            if (enPeriodo(s.getFecha(), desde, hasta)) {
                lineas.add(s.getFecha() + " " + s.getHora() + " | " + s.getInstalacion().getNombre()
                        + " | " + s.getEntrenador().getNombre()
                        + " | convocados: " + s.getConvocatorias().size());
            }
        }
        return sinVacio(lineas);
    }
}

package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import consola.DatosBase;
import modelo.Categoria;
import modelo.Club;
import modelo.Deporte;
import modelo.Entrenador;
import modelo.Equipo;
import modelo.EstadoTorneo;
import modelo.Estudiante;
import modelo.InscripcionDisciplina;
import modelo.InscripcionTorneo;
import modelo.Jugador;
import modelo.Modalidad;
import modelo.Nivel;
import modelo.ReglaNegocioException;
import modelo.Socio;
import modelo.TipoCupo;
import modelo.TipoTorneo;
import modelo.Torneo;
import modelo.Turno;
import modelo.Usuario;

public class InscripcionesTorneoTest {

    private static final LocalDate LUNES = LocalDate.of(2026, 11, 9);

    private Club club;
    private Torneo tenis;
    private Torneo padel;
    private Torneo futbol;

    @BeforeEach
    public void preparar() {
        club = new Club();
        DatosBase.cargar(club);
        club.setFechaActual(LocalDate.of(2026, 10, 1));
        socioConDisciplina(DatosBase.SOCIO1, Deporte.TENIS, Nivel.INTERMEDIO);
        socioConDisciplina(DatosBase.SOCIO2, Deporte.TENIS, Nivel.INTERMEDIO);
        socioConDisciplina(DatosBase.SOCIO3, Deporte.PADEL, Nivel.AVANZADO);
        socioConDisciplina(DatosBase.SOCIO4, Deporte.PADEL, Nivel.AVANZADO);

        tenis = new Torneo("Copa Tenis", TipoTorneo.COMPETITIVO, Deporte.TENIS, null, Nivel.INTERMEDIO,
                Modalidad.SENCILLOS, 2, 30000, LUNES);
        padel = new Torneo("Dobles Padel", TipoTorneo.RECREATIVO, Deporte.PADEL, null, Nivel.AVANZADO,
                Modalidad.DOBLES, 4, 0, LUNES);
        futbol = new Torneo("Futbol Sub-15", TipoTorneo.RECREATIVO, Deporte.FUTBOL, Categoria.SUB_15, null,
                null, 4, 0, LUNES);
        club.getTorneos().add(tenis);
        club.getTorneos().add(padel);
        club.getTorneos().add(futbol);
    }

    private InscripcionDisciplina socioConDisciplina(int codigo, Deporte deporte, Nivel nivel) {
        return new InscripcionDisciplina((Socio) DatosBase.usuario(club, codigo), deporte, nivel);
    }

    private Equipo crearEquipoCompleto(String nombre) {
        Equipo equipo = new Equipo(nombre, Deporte.FUTBOL, Categoria.SUB_15, 12);
        for (int i = 0; i < 7; i++) {
            Jugador jugador = new Jugador(200 + i, "J" + i, "Apellido", "j" + nombre + i, "1234",
                    LocalDate.of(2012, 1, 1 + i), "Defensa", i + 1);
            equipo.inscribirJugador(jugador);
            club.getUsuarios().add(jugador);
        }
        equipo.setEntrenadorResponsable((Entrenador) DatosBase.usuario(club, DatosBase.ENTRENADOR1));
        club.getEquipos().add(equipo);
        return equipo;
    }

    // ---------- HU1: registrarse como estudiante o externo ----------

    @Test
    public void estudianteSePuedeRegistrarYEntrar() {
        club.registrarEstudiante("Diana", "Castro", "diana", "1234", "202110001");
        Usuario usuario = club.iniciarSesion("diana", "1234");
        assertTrue(usuario instanceof Estudiante);
    }

    @Test
    public void externoSePuedeRegistrarYEntrar() {
        club.registrarExterno("Esteban", "Mora", "esteban", "1234", "1020304050");
        assertNotNull(club.iniciarSesion("esteban", "1234"));
    }

    @Test
    public void noSePuedeRegistrarUnLoginRepetido() {
        assertThrows(ReglaNegocioException.class,
                () -> club.registrarEstudiante("Otra", "Persona", "sofia", "1234", "999"));
    }

    @Test
    public void claveMuyCortaSeRechaza() {
        assertThrows(ReglaNegocioException.class,
                () -> club.registrarExterno("Ana", "Gil", "anagil", "12", "555"));
    }

    // ---------- HU2: inscribir un jugador ----------

    @Test
    public void socioSeInscribeYPagaLaTarifa() {
        InscripcionTorneo inscripcion = club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, tenis);
        assertEquals(30000, inscripcion.getTarifaCobrada());
        assertEquals(1, tenis.cuposOcupados());
    }

    @Test
    public void socioSinLaDisciplinaNoPuedeInscribirse() {
        assertThrows(ReglaNegocioException.class, () -> club.inscribirJugadorEnTorneo(DatosBase.SOCIO3, tenis));
    }

    @Test
    public void noSePuedeInscribirDosVeces() {
        club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, tenis);
        assertThrows(ReglaNegocioException.class, () -> club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, tenis));
    }

    @Test
    public void elAdministradorNoPuedeInscribirse() {
        assertThrows(ReglaNegocioException.class, () -> club.inscribirJugadorEnTorneo(DatosBase.ADMIN, tenis));
    }

    @Test
    public void estudianteYExternoPuedenInscribirseEnTorneoIndividual() {
        club.registrarEstudiante("Diana", "Castro", "diana", "1234", "202110001");
        club.registrarExterno("Esteban", "Mora", "esteban", "1234", "1020304050");
        Usuario diana = club.iniciarSesion("diana", "1234");
        Usuario esteban = club.iniciarSesion("esteban", "1234");
        Torneo abierto = new Torneo("Tenis abierto", TipoTorneo.COMPETITIVO, Deporte.TENIS, null,
                Nivel.PRINCIPIANTE, Modalidad.SENCILLOS, 10, 20000, LUNES);
        club.getTorneos().add(abierto);
        club.inscribirJugadorEnTorneo(diana.getCodigo(), abierto);
        club.inscribirJugadorEnTorneo(esteban.getCodigo(), abierto);
        assertEquals(2, abierto.cuposOcupados());
        assertEquals(40000, abierto.totalRecaudado());
    }

    @Test
    public void noSeInscribeUnJugadorEnTorneoDeConjunto() {
        assertThrows(ReglaNegocioException.class, () -> club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, futbol));
    }

    // ---------- HU3: inscribir parejas ----------

    @Test
    public void parejaUsaUnSoloCupoYPagaPorLosDos() {
        Torneo padelDePago = new Torneo("Padel pago", TipoTorneo.COMPETITIVO, Deporte.PADEL, null,
                Nivel.AVANZADO, Modalidad.DOBLES, 4, 10000, LUNES);
        club.getTorneos().add(padelDePago);
        InscripcionTorneo inscripcion = club.inscribirParejaEnTorneo(DatosBase.SOCIO3, DatosBase.SOCIO4, padelDePago);
        assertEquals(1, padelDePago.cuposOcupados());
        assertEquals(20000, inscripcion.getTarifaCobrada());
    }

    @Test
    public void parejaEnTorneoRecreativoNoPaga() {
        InscripcionTorneo inscripcion = club.inscribirParejaEnTorneo(DatosBase.SOCIO3, DatosBase.SOCIO4, padel);
        assertEquals(0, inscripcion.getTarifaCobrada());
    }

    @Test
    public void parejaConLaMismaPersonaSeRechaza() {
        assertThrows(ReglaNegocioException.class,
                () -> club.inscribirParejaEnTorneo(DatosBase.SOCIO3, DatosBase.SOCIO3, padel));
    }

    @Test
    public void parejaEnTorneoDeSencillosSeRechaza() {
        assertThrows(ReglaNegocioException.class,
                () -> club.inscribirParejaEnTorneo(DatosBase.SOCIO1, DatosBase.SOCIO2, tenis));
    }

    // ---------- HU4: inscribir equipos ----------

    @Test
    public void entrenadorInscribeEquipoCompleto() {
        Equipo equipo = crearEquipoCompleto("Halcones");
        InscripcionTorneo inscripcion = club.inscribirEquipoEnTorneo(DatosBase.ENTRENADOR1, equipo, futbol);
        assertEquals(equipo, inscripcion.getEquipo());
        assertEquals(1, futbol.cuposOcupados());
    }

    @Test
    public void equipoSinJugadoresSuficientesSeRechaza() {
        Equipo incompleto = new Equipo("Pocos", Deporte.FUTBOL, Categoria.SUB_15, 12);
        club.getEquipos().add(incompleto);
        assertThrows(ReglaNegocioException.class,
                () -> club.inscribirEquipoEnTorneo(DatosBase.ENTRENADOR1, incompleto, futbol));
    }

    @Test
    public void soloElEntrenadorInscribeEquipos() {
        Equipo equipo = crearEquipoCompleto("Halcones");
        assertThrows(ReglaNegocioException.class,
                () -> club.inscribirEquipoEnTorneo(DatosBase.SOCIO1, equipo, futbol));
    }

    @Test
    public void equipoNoSeInscribeDosVeces() {
        Equipo equipo = crearEquipoCompleto("Halcones");
        club.inscribirEquipoEnTorneo(DatosBase.ENTRENADOR1, equipo, futbol);
        assertThrows(ReglaNegocioException.class,
                () -> club.inscribirEquipoEnTorneo(DatosBase.ENTRENADOR1, equipo, futbol));
    }

    // ---------- HU5: retirarse ----------

    @Test
    public void alRetirarseElCupoQuedaLibre() {
        club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, tenis);
        club.retirarDeTorneo(DatosBase.SOCIO1, tenis);
        assertEquals(0, tenis.cuposOcupados());
        assertNull(tenis.buscarInscripcionDe(DatosBase.usuario(club, DatosBase.SOCIO1)));
    }

    @Test
    public void quienSeRetiroPuedeVolverAInscribirse() {
        club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, tenis);
        club.retirarDeTorneo(DatosBase.SOCIO1, tenis);
        club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, tenis);
        assertEquals(1, tenis.cuposOcupados());
    }

    @Test
    public void noSeRetiraQuienNoEstaInscrito() {
        assertThrows(ReglaNegocioException.class, () -> club.retirarDeTorneo(DatosBase.SOCIO1, tenis));
    }

    @Test
    public void conElTorneoEnCursoNoSePuedeInscribirNiRetirar() {
        club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, tenis);
        tenis.iniciar();
        assertEquals(EstadoTorneo.EN_CURSO, tenis.getEstado());
        assertThrows(ReglaNegocioException.class, () -> club.retirarDeTorneo(DatosBase.SOCIO1, tenis));
        assertThrows(ReglaNegocioException.class, () -> club.inscribirJugadorEnTorneo(DatosBase.SOCIO2, tenis));
    }

    @Test
    public void entrenadorRetiraEquipoYSeLiberaElCupo() {
        Equipo equipo = crearEquipoCompleto("Halcones");
        club.inscribirEquipoEnTorneo(DatosBase.ENTRENADOR1, equipo, futbol);
        club.retirarEquipoDeTorneo(DatosBase.ENTRENADOR1, equipo, futbol);
        assertEquals(0, futbol.cuposOcupados());
    }

    // ---------- HU6: cupos preferenciales ----------

    @Test
    public void elMejorRankingTomaElCupoPreferente() {
        InscripcionDisciplina tenisJuan = socioConDisciplinaYa(DatosBase.SOCIO2);
        tenisJuan.actualizarRanking(1010, LocalDate.of(2026, 10, 1));
        InscripcionTorneo inscripcion = club.inscribirJugadorEnTorneo(DatosBase.SOCIO2, tenis);
        assertEquals(TipoCupo.PREFERENTE, inscripcion.getTipoCupo());
    }

    private InscripcionDisciplina socioConDisciplinaYa(int codigo) {
        Socio socio = (Socio) DatosBase.usuario(club, codigo);
        return socio.getInscripciones().get(0);
    }

    @Test
    public void quienNoEsDeLosMejoresNoTomaElPreferente() {
        club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, tenis);
        assertThrows(ReglaNegocioException.class, () -> club.inscribirJugadorEnTorneo(DatosBase.SOCIO2, tenis));
    }

    @Test
    public void losPreferentesSeLiberanSolosCuandoFaltanTresDiasOMenos() {
        club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, tenis);
        club.setFechaActual(LUNES.minusDays(Torneo.DIAS_DE_RESERVA));
        InscripcionTorneo inscripcion = club.inscribirJugadorEnTorneo(DatosBase.SOCIO2, tenis);
        assertEquals(TipoCupo.REGULAR, inscripcion.getTipoCupo());
        assertTrue(tenis.getPreferencialesLiberados());
    }

    @Test
    public void unDiaAntesDeLaFechaDeLiberacionSiguenReservados() {
        club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, tenis);
        club.setFechaActual(LUNES.minusDays(Torneo.DIAS_DE_RESERVA + 1));
        assertThrows(ReglaNegocioException.class, () -> club.inscribirJugadorEnTorneo(DatosBase.SOCIO2, tenis));
    }

    @Test
    public void alRetirarseDespuesDeLiberarElCupoQuedaDisponible() {
        club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, tenis);
        club.setFechaActual(LUNES.minusDays(1));
        club.inscribirJugadorEnTorneo(DatosBase.SOCIO2, tenis);
        club.retirarDeTorneo(DatosBase.SOCIO1, tenis);
        assertEquals(1, tenis.cuposLibres());
    }

    private void darHistorial(int codigoSocio, int puntaje) {
        socioConDisciplinaYa(codigoSocio).actualizarRanking(puntaje, LocalDate.of(2026, 10, 1));
    }

    @Test
    public void conEmpateEnElPuntajeDeCorteLosEmpatadosCalifican() {
        Torneo cinco = new Torneo("Cinco", TipoTorneo.COMPETITIVO, Deporte.TENIS, null, Nivel.INTERMEDIO,
                Modalidad.SENCILLOS, 5, 1000, LUNES);
        club.getTorneos().add(cinco);
        darHistorial(DatosBase.SOCIO1, 1050);
        darHistorial(DatosBase.SOCIO2, 1050);
        assertEquals(TipoCupo.PREFERENTE, club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, cinco).getTipoCupo());
        assertEquals(TipoCupo.REGULAR, club.inscribirJugadorEnTorneo(DatosBase.SOCIO2, cinco).getTipoCupo());
    }

    @Test
    public void quienTieneMenosPuntajeQueElCorteNoTomaPreferente() {
        Torneo cinco = new Torneo("Cinco", TipoTorneo.COMPETITIVO, Deporte.TENIS, null, Nivel.INTERMEDIO,
                Modalidad.SENCILLOS, 5, 1000, LUNES);
        club.getTorneos().add(cinco);
        darHistorial(DatosBase.SOCIO1, 1020);
        darHistorial(DatosBase.SOCIO2, 1050);
        assertEquals(TipoCupo.REGULAR, club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, cinco).getTipoCupo());
    }

    @Test
    public void siSoloUnoTieneHistorialEseEsElMejor() {
        Torneo cinco = new Torneo("Cinco", TipoTorneo.COMPETITIVO, Deporte.TENIS, null, Nivel.INTERMEDIO,
                Modalidad.SENCILLOS, 5, 1000, LUNES);
        club.getTorneos().add(cinco);
        darHistorial(DatosBase.SOCIO2, 990);
        assertEquals(TipoCupo.PREFERENTE, club.inscribirJugadorEnTorneo(DatosBase.SOCIO2, cinco).getTipoCupo());
    }

    @Test
    public void sinNadieConHistorialNadieTomaPreferente() {
        Torneo cinco = new Torneo("Cinco", TipoTorneo.COMPETITIVO, Deporte.TENIS, null, Nivel.INTERMEDIO,
                Modalidad.SENCILLOS, 5, 1000, LUNES);
        club.getTorneos().add(cinco);
        assertEquals(TipoCupo.REGULAR, club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, cinco).getTipoCupo());
    }

    @Test
    public void losCuposYaOcupadosNoCambianQuienEsDeLosMejores() {
        Torneo cinco = new Torneo("Cinco", TipoTorneo.COMPETITIVO, Deporte.TENIS, null, Nivel.INTERMEDIO,
                Modalidad.SENCILLOS, 5, 1000, LUNES);
        club.getTorneos().add(cinco);
        darHistorial(DatosBase.SOCIO2, 1050);
        club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, cinco);
        assertEquals(TipoCupo.PREFERENTE, club.inscribirJugadorEnTorneo(DatosBase.SOCIO2, cinco).getTipoCupo());
    }

    @Test
    public void parejaDestacadaTomaCuposPreferentes() {
        darHistorial(DatosBase.SOCIO3, 1100);
        darHistorial(DatosBase.SOCIO4, 1080);
        Torneo dobles = new Torneo("Dobles", TipoTorneo.RECREATIVO, Deporte.PADEL, null, Nivel.AVANZADO,
                Modalidad.DOBLES, 5, 0, LUNES);
        club.getTorneos().add(dobles);
        assertEquals(TipoCupo.PREFERENTE,
                club.inscribirParejaEnTorneo(DatosBase.SOCIO3, DatosBase.SOCIO4, dobles).getTipoCupo());
    }

    @Test
    public void parejaConUnSoloJugadorDeHistorialNoEsDestacada() {
        darHistorial(DatosBase.SOCIO3, 1100);
        Torneo dobles = new Torneo("Dobles", TipoTorneo.RECREATIVO, Deporte.PADEL, null, Nivel.AVANZADO,
                Modalidad.DOBLES, 5, 0, LUNES);
        club.getTorneos().add(dobles);
        assertEquals(TipoCupo.REGULAR,
                club.inscribirParejaEnTorneo(DatosBase.SOCIO3, DatosBase.SOCIO4, dobles).getTipoCupo());
    }
    
    @Test
    public void entrenadorNoResponsableNoPuedeInscribirEquipoAjeno() {
        Equipo equipo = crearEquipoCompleto("Halcones");
        assertThrows(ReglaNegocioException.class,
                () -> club.inscribirEquipoEnTorneo(DatosBase.ENTRENADOR2, equipo, futbol));
        assertEquals(0, futbol.cuposOcupados());
    }

    @Test
    public void entrenadorNoResponsableNoPuedeRetirarEquipoAjeno() {
        Equipo equipo = crearEquipoCompleto("Halcones");
        club.inscribirEquipoEnTorneo(DatosBase.ENTRENADOR1, equipo, futbol);
        assertThrows(ReglaNegocioException.class,
                () -> club.retirarEquipoDeTorneo(DatosBase.ENTRENADOR2, equipo, futbol));
        assertEquals(1, futbol.cuposOcupados());
    }

    @Test
    public void equipoSinEntrenadorResponsableNoSeInscribe() {
        Equipo equipo = crearEquipoCompleto("Halcones");
        equipo.setEntrenadorResponsable(null);
        assertThrows(ReglaNegocioException.class,
                () -> club.inscribirEquipoEnTorneo(DatosBase.ENTRENADOR1, equipo, futbol));
    }

    @Test
    public void soloElAdministradorAsignaElEntrenadorDeUnEquipo() {
        Equipo equipo = crearEquipoCompleto("Halcones");
        assertThrows(ReglaNegocioException.class,
                () -> club.asignarEntrenadorAEquipo(DatosBase.SOCIO1, equipo, DatosBase.ENTRENADOR2));
        club.asignarEntrenadorAEquipo(DatosBase.ADMIN, equipo, DatosBase.ENTRENADOR2);
        club.inscribirEquipoEnTorneo(DatosBase.ENTRENADOR2, equipo, futbol);
        assertEquals(1, futbol.cuposOcupados());
    }

    @Test
    public void unSocioNoPuedeSerEntrenadorResponsable() {
        Equipo equipo = crearEquipoCompleto("Halcones");
        assertThrows(ReglaNegocioException.class,
                () -> club.asignarEntrenadorAEquipo(DatosBase.ADMIN, equipo, DatosBase.SOCIO1));
    }

    // ---------- HU7: empleados ----------

    private Entrenador carlosConTurnoElLunes() {
        Entrenador carlos = (Entrenador) DatosBase.usuario(club, DatosBase.ENTRENADOR1);
        new Turno(carlos, "LUNES", LocalTime.of(8, 0), LocalTime.of(16, 0));
        return carlos;
    }

    @Test
    public void empleadoDeTurnoElDiaDelTorneoNoPuedeInscribirse() {
        Entrenador carlos = carlosConTurnoElLunes();
        Torneo abierto = new Torneo("Tenis abierto", TipoTorneo.COMPETITIVO, Deporte.TENIS, null,
                Nivel.PRINCIPIANTE, Modalidad.SENCILLOS, 10, 20000, LUNES);
        club.getTorneos().add(abierto);
        assertThrows(ReglaNegocioException.class, () -> club.inscribirJugadorEnTorneo(carlos.getCodigo(), abierto));
    }

    @Test
    public void empleadoLibreEseDiaJuegaGratisEnCompetitivoYNoGanaPremioEnEfectivo() {
        carlosConTurnoElLunes();
        Entrenador laura = (Entrenador) DatosBase.usuario(club, DatosBase.ENTRENADOR2);
        Torneo abierto = new Torneo("Tenis abierto", TipoTorneo.COMPETITIVO, Deporte.TENIS, null,
                Nivel.PRINCIPIANTE, Modalidad.SENCILLOS, 10, 20000, LUNES);
        club.getTorneos().add(abierto);
        InscripcionTorneo inscripcion = club.inscribirJugadorEnTorneo(laura.getCodigo(), abierto);
        assertEquals(0, inscripcion.getTarifaCobrada());
        assertTrue(!inscripcion.recibePremioEfectivo(laura));
    }

    @Test
    public void empleadoNoPuedeInscribirseEnTorneoDeConjunto() {
        Entrenador laura = (Entrenador) DatosBase.usuario(club, DatosBase.ENTRENADOR2);
        assertThrows(ReglaNegocioException.class, () -> club.inscribirJugadorEnTorneo(laura.getCodigo(), futbol));
    }

    // ---------- Banco oficial: S01/E01, A20 y rechazos sin cambios ----------

    @Test
    public void inicioDeSesionFallidoNoDiceQueDatoFallo() {
        assertNull(club.iniciarSesion("sofia", "claveMala"));
        assertNull(club.iniciarSesion("noExiste", "1234"));
    }

    @Test
    public void elAdministradorNoPuedeSerParteDeUnaPareja() {
        assertThrows(ReglaNegocioException.class,
                () -> club.inscribirParejaEnTorneo(DatosBase.SOCIO3, DatosBase.ADMIN, padel));
        assertEquals(0, padel.cuposOcupados());
    }

    @Test
    public void rechazoNoCambiaLosCupos() {
        assertThrows(ReglaNegocioException.class, () -> club.inscribirJugadorEnTorneo(DatosBase.SOCIO3, tenis));
        assertThrows(ReglaNegocioException.class,
                () -> club.inscribirParejaEnTorneo(DatosBase.SOCIO3, DatosBase.SOCIO3, padel));
        assertEquals(0, tenis.cuposOcupados());
        assertEquals(0, padel.cuposOcupados());
        assertEquals(0, tenis.totalRecaudado());
    }

    @Test
    public void fisioterapeutaTambienPuedeInscribirseEnTorneoIndividual() {
        Torneo abierto = new Torneo("Tenis abierto", TipoTorneo.COMPETITIVO, Deporte.TENIS, null,
                Nivel.PRINCIPIANTE, Modalidad.SENCILLOS, 10, 20000, LUNES);
        club.getTorneos().add(abierto);
        InscripcionTorneo inscripcion = club.inscribirJugadorEnTorneo(DatosBase.FISIO, abierto);
        assertEquals(0, inscripcion.getTarifaCobrada());
    }

    @Test
    public void fisioterapeutaDeTurnoNoPuedeInscribirse() {
        Usuario mateo = DatosBase.usuario(club, DatosBase.FISIO);
        new Turno((modelo.Empleado) mateo, "LUNES", LocalTime.of(8, 0), LocalTime.of(16, 0));
        Torneo abierto = new Torneo("Tenis abierto", TipoTorneo.COMPETITIVO, Deporte.TENIS, null,
                Nivel.PRINCIPIANTE, Modalidad.SENCILLOS, 10, 20000, LUNES);
        club.getTorneos().add(abierto);
        assertThrows(ReglaNegocioException.class, () -> club.inscribirJugadorEnTorneo(DatosBase.FISIO, abierto));
    }

    @Test
    public void empleadoDeTurnoOtroDiaSiPuedeInscribirse() {
        Entrenador carlos = (Entrenador) DatosBase.usuario(club, DatosBase.ENTRENADOR1);
        new Turno(carlos, "MARTES", LocalTime.of(8, 0), LocalTime.of(16, 0));
        Torneo abierto = new Torneo("Tenis abierto", TipoTorneo.COMPETITIVO, Deporte.TENIS, null,
                Nivel.PRINCIPIANTE, Modalidad.SENCILLOS, 10, 20000, LUNES);
        club.getTorneos().add(abierto);
        club.inscribirJugadorEnTorneo(DatosBase.ENTRENADOR1, abierto);
        assertEquals(1, abierto.cuposOcupados());
    }

    @Test
    public void parejaDeEmpleadoYEstudiantePagaSoloLaTarifaDelEstudiante() {
        club.registrarEstudiante("Diana", "Castro", "diana", "1234", "202110001");
        int diana = club.buscarCodigoPorLogin("diana");
        Torneo dobles = new Torneo("Dobles pago", TipoTorneo.COMPETITIVO, Deporte.TENIS, null,
                Nivel.PRINCIPIANTE, Modalidad.DOBLES, 4, 20000, LUNES);
        club.getTorneos().add(dobles);
        InscripcionTorneo inscripcion = club.inscribirParejaEnTorneo(DatosBase.ENTRENADOR2, diana, dobles);
        assertEquals(20000, inscripcion.getTarifaCobrada());
        assertTrue(!inscripcion.recibePremioEfectivo(DatosBase.usuario(club, DatosBase.ENTRENADOR2)));
        assertTrue(inscripcion.recibePremioEfectivo(DatosBase.usuario(club, diana)));
    }

    @Test
    public void parejaDeDosEmpleadosNoPagaNada() {
        Torneo dobles = new Torneo("Dobles pago", TipoTorneo.COMPETITIVO, Deporte.TENIS, null,
                Nivel.PRINCIPIANTE, Modalidad.DOBLES, 4, 20000, LUNES);
        club.getTorneos().add(dobles);
        InscripcionTorneo inscripcion = club.inscribirParejaEnTorneo(DatosBase.ENTRENADOR2, DatosBase.FISIO, dobles);
        assertEquals(0, inscripcion.getTarifaCobrada());
    }

    @Test
    public void parejaConEmpleadoDeTurnoSeRechazaCompleta() {
        carlosConTurnoElLunes();
        Torneo dobles = new Torneo("Dobles pago", TipoTorneo.COMPETITIVO, Deporte.TENIS, null,
                Nivel.PRINCIPIANTE, Modalidad.DOBLES, 4, 20000, LUNES);
        club.getTorneos().add(dobles);
        assertThrows(ReglaNegocioException.class,
                () -> club.inscribirParejaEnTorneo(DatosBase.ENTRENADOR1, DatosBase.ENTRENADOR2, dobles));
        assertEquals(0, dobles.cuposOcupados());
    }

    // ---------- HU8: consultas ----------

    @Test
    public void consultarTorneosListaTodos() {
        assertEquals(3, club.consultarTorneos().size());
    }

    @Test
    public void consultarTorneosSinTorneosAvisa() {
        Club vacio = new Club();
        assertThrows(ReglaNegocioException.class, () -> vacio.consultarTorneos());
    }

    @Test
    public void consultarInscripcionesYMisInscripciones() {
        club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, tenis);
        club.inscribirParejaEnTorneo(DatosBase.SOCIO3, DatosBase.SOCIO4, padel);
        assertEquals(1, club.consultarInscripciones(tenis).size());
        assertEquals(1, club.consultarMisInscripciones(DatosBase.SOCIO1).size());
        assertEquals(1, club.consultarMisInscripciones(DatosBase.SOCIO4).size());
        assertEquals(0, club.consultarMisInscripciones(DatosBase.SOCIO2).size());
    }
}

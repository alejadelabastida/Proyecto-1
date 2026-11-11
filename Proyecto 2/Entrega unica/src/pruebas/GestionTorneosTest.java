package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import consola.DatosBase;
import modelo.Bono;
import modelo.Club;
import modelo.Deporte;
import modelo.EncuentroTorneo;
import modelo.EstadoTorneo;
import modelo.InscripcionDisciplina;
import modelo.Modalidad;
import modelo.Nivel;
import modelo.ReglaNegocioException;
import modelo.Socio;
import modelo.TipoTorneo;
import modelo.Torneo;
import modelo.Usuario;


/**
 * Pruebas unitarias de GESTION DE TORNEOS (Proyecto 2 - Persona 1).
 *
 * PLANTILLA generada con IAG: el fixture (datos mock-up) y la estructura de la clase
 * estan permitidos por el enunciado. Los cuerpos marcados con TODO son de la
 * logica critica y se escriben a mano, ANTES de implementar (TDD).
 *
 * Cada prueba sigue el patron de los criterios de aceptacion:
 *   Dado (preparacion)  ->  Cuando (accion)  ->  Entonces (verificacion)
 */
class GestionTorneosTest {

    private Club club;
    private Torneo tenisCompetitivo;  
    private Torneo padelRecreativo;  

    @BeforeEach
    void setUp() {
        club = new Club();   
        DatosBase.cargar(club);

        LocalDate enDosSemanas = LocalDate.now().plusDays(14);
        tenisCompetitivo = new Torneo("Copa Tenis", TipoTorneo.COMPETITIVO, Deporte.TENIS, null,
                Nivel.INTERMEDIO, Modalidad.SENCILLOS, 10, 30000, enDosSemanas);
        padelRecreativo = new Torneo("Padel Amistoso", TipoTorneo.RECREATIVO, Deporte.PADEL, null,
                Nivel.AVANZADO, Modalidad.DOBLES, 4, 0, enDosSemanas);
        club.getTorneos().add(tenisCompetitivo);
        club.getTorneos().add(padelRecreativo);
    }
    private void iniciarCopaTenisConDosSocios() {
        new InscripcionDisciplina((Socio) DatosBase.usuario(club, DatosBase.SOCIO1), Deporte.TENIS, Nivel.INTERMEDIO);
        new InscripcionDisciplina((Socio) DatosBase.usuario(club, DatosBase.SOCIO2), Deporte.TENIS, Nivel.INTERMEDIO);
        club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, tenisCompetitivo);
        club.inscribirJugadorEnTorneo(DatosBase.SOCIO2, tenisCompetitivo);
        club.iniciarTorneo(DatosBase.ADMIN, tenisCompetitivo);
    }

    @Test
    void cuposPreferencialesSonEl20PorCientoRedondeadoHaciaArriba() {
        assertEquals(2, tenisCompetitivo.cuposPreferenciales());
        assertEquals(1, padelRecreativo.cuposPreferenciales());
    }

    @Test
    void torneoRecreativoNoPuedeCobrarTarifa() {
        assertThrows(IllegalArgumentException.class, () -> new Torneo("Malo", TipoTorneo.RECREATIVO,
                Deporte.TENIS, null, Nivel.INTERMEDIO, Modalidad.SENCILLOS, 8, 5000, LocalDate.now()));
    }

    // =================================================================
    //  HU: Crear torneo
    // =================================================================

    @Test
    void administradorCreaTorneoValido() {
        Torneo creado = club.crearTorneo(DatosBase.ADMIN, "Torneo Prueba", TipoTorneo.RECREATIVO, Deporte.TENIS, null, Nivel.INTERMEDIO, Modalidad.SENCILLOS, 16, 0.0, LocalDate.now().plusDays(14));
        assertEquals(EstadoTorneo.ABIERTO, creado.getEstado());
        assertEquals(3, club.getTorneos().size());
    }

    @Test
    void usuarioQueNoEsAdministradorNoPuedeCrearTorneo() {
        assertThrows(ReglaNegocioException.class, () -> {
            club.crearTorneo(DatosBase.SOCIO1, "Torneo Prueba", TipoTorneo.RECREATIVO, Deporte.TENIS, null, Nivel.INTERMEDIO, Modalidad.SENCILLOS, 16, 0.0, LocalDate.now().plusDays(14));
        });
    }

    @Test
    void noSePuedenCrearDosTorneosConElMismoNombre() {
        assertThrows(ReglaNegocioException.class, () -> {
            club.crearTorneo(DatosBase.ADMIN, "copa tenis", TipoTorneo.RECREATIVO, Deporte.TENIS, null, Nivel.INTERMEDIO, Modalidad.SENCILLOS, 16, 0.0, LocalDate.now().plusDays(14));
        });
    }

    // =================================================================
    //  HU: Iniciar torneo / estados
    // =================================================================

    @Test
    void iniciarTorneoLoPasaAEnCursoYBloqueaInscripciones()  {
        
        new InscripcionDisciplina((Socio) DatosBase.usuario(club, DatosBase.SOCIO1), Deporte.TENIS, Nivel.INTERMEDIO);
        new InscripcionDisciplina((Socio) DatosBase.usuario(club, DatosBase.SOCIO2), Deporte.TENIS, Nivel.INTERMEDIO);
        club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, tenisCompetitivo);
        club.inscribirJugadorEnTorneo(DatosBase.SOCIO2, tenisCompetitivo);

        
        club.iniciarTorneo(DatosBase.ADMIN, tenisCompetitivo);

        
        assertEquals(EstadoTorneo.EN_CURSO, tenisCompetitivo.getEstado());
        assertThrows(ReglaNegocioException.class, () -> {
            club.inscribirJugadorEnTorneo(DatosBase.SOCIO3, tenisCompetitivo);
        });
    }

    @Test
    void noSePuedeIniciarTorneoConMenosDeDosParticipantes() {
    	   assertThrows(ReglaNegocioException.class, () -> {
    	       club.iniciarTorneo(DatosBase.ADMIN, tenisCompetitivo);
           });
    }

    @Test
    void noSePuedeFinalizarTorneoQueNoHaIniciado() {
        assertThrows(ReglaNegocioException.class, () -> {
            club.finalizarTorneo(DatosBase.ADMIN, tenisCompetitivo);
        });
    }

    // =================================================================
    //  HU: Resultados y posiciones
    // =================================================================

    @Test
    void registrarResultadoActualizaLasPosiciones() {
        iniciarCopaTenisConDosSocios();
        EncuentroTorneo partido = tenisCompetitivo.getEncuentros().get(0);

        club.registrarResultado(DatosBase.ADMIN, tenisCompetitivo, partido, 2, 0);

        assertEquals(partido.getLocal(), tenisCompetitivo.getPosiciones().get(0));
        assertEquals(3, tenisCompetitivo.puntosDe(partido.getLocal()));
    }

    @Test
    void noSeRegistraResultadoEnTorneoQueNoEstaEnCurso() {
        assertThrows(ReglaNegocioException.class, () -> {
            club.registrarResultado(DatosBase.ADMIN, tenisCompetitivo, null, 2, 0);
        });    }

    @Test
    void noSeAceptaMarcadorNegativo() {
        iniciarCopaTenisConDosSocios();
        EncuentroTorneo partido = tenisCompetitivo.getEncuentros().get(0);
        assertThrows(ReglaNegocioException.class, () -> {
            club.registrarResultado(DatosBase.ADMIN, tenisCompetitivo, partido, -1, 2);
        });
    }

    // =================================================================
    //  HU: Premios
    // =================================================================

    @Test
    void premioCompetitivoSeCalculaSobreElTotalRecaudado() {
        iniciarCopaTenisConDosSocios();
        EncuentroTorneo partido = tenisCompetitivo.getEncuentros().get(0);
        Usuario ganador = partido.getLocal().getParticipantes().get(0);
        Usuario segundo = partido.getVisitante().getParticipantes().get(0);

        club.registrarResultado(DatosBase.ADMIN, tenisCompetitivo, partido, 2, 0);
        club.finalizarTorneo(DatosBase.ADMIN, tenisCompetitivo);

        assertEquals(36000.0, tenisCompetitivo.premioEfectivoDe(ganador));
        assertEquals(18000.0, tenisCompetitivo.premioEfectivoDe(segundo));
    }

    @Test
    void empleadoGanadorDeCompetitivoNoRecibePremioEnEfectivo() {
        new InscripcionDisciplina((Socio) DatosBase.usuario(club, DatosBase.SOCIO1), Deporte.TENIS, Nivel.INTERMEDIO);
        club.inscribirJugadorEnTorneo(DatosBase.ENTRENADOR1, tenisCompetitivo);
        club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, tenisCompetitivo);
        club.iniciarTorneo(DatosBase.ADMIN, tenisCompetitivo);
        EncuentroTorneo partido = tenisCompetitivo.getEncuentros().get(0);
        Usuario empleado = DatosBase.usuario(club, DatosBase.ENTRENADOR1);

        club.registrarResultado(DatosBase.ADMIN, tenisCompetitivo, partido, 2, 0);
        club.finalizarTorneo(DatosBase.ADMIN, tenisCompetitivo);

        assertEquals(0.0, tenisCompetitivo.premioEfectivoDe(empleado));
    }

    @Test
    void ganadorDeRecreativoRecibeUnBonoSinRedimir() {
        Torneo amistoso = club.crearTorneo(DatosBase.ADMIN, "Tenis Amistoso", TipoTorneo.RECREATIVO, Deporte.TENIS,
                null, Nivel.INTERMEDIO, Modalidad.SENCILLOS, 8, 0, LocalDate.now().plusDays(14));
        new InscripcionDisciplina((Socio) DatosBase.usuario(club, DatosBase.SOCIO1), Deporte.TENIS, Nivel.INTERMEDIO);
        new InscripcionDisciplina((Socio) DatosBase.usuario(club, DatosBase.SOCIO2), Deporte.TENIS, Nivel.INTERMEDIO);
        club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, amistoso);
        club.inscribirJugadorEnTorneo(DatosBase.SOCIO2, amistoso);
        club.iniciarTorneo(DatosBase.ADMIN, amistoso);
        EncuentroTorneo partido = amistoso.getEncuentros().get(0);

        club.registrarResultado(DatosBase.ADMIN, amistoso, partido, 2, 0);
        club.finalizarTorneo(DatosBase.ADMIN, amistoso);

        assertEquals(1, club.getBonos().size());
        Bono bono = club.getBonos().get(0);
        assertFalse(bono.isRedimido());
        assertEquals(partido.getLocal().getParticipantes().get(0), bono.getDueno());
    }
    // =================================================================
    //  HU: Historial
    // =================================================================

    @Test
    void historialSeConsultaAunqueElTorneoEsteFinalizado()  {
        iniciarCopaTenisConDosSocios();
        EncuentroTorneo partido = tenisCompetitivo.getEncuentros().get(0);
        club.registrarResultado(DatosBase.ADMIN, tenisCompetitivo, partido, 2, 0);
        club.finalizarTorneo(DatosBase.ADMIN, tenisCompetitivo);

        List<String> historial = club.consultarHistorialTorneo(tenisCompetitivo);

        // 
        assertTrue(historial.get(0).contains("FINALIZADO"));

        assertTrue(historial.contains("Posiciones:"));
    }}

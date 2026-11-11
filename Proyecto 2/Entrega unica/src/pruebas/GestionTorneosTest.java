package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import consola.DatosBase;
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
        fail("TODO");
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
        fail("TODO: usar la regla de reparto que defina el grupo");
    }

    @Test
    void empleadoGanadorDeCompetitivoNoRecibePremioEnEfectivo() {
        fail("TODO");
    }

    @Test
    void ganadorDeRecreativoRecibeUnBonoSinRedimir() {
        fail("TODO");
    }

    // =================================================================
    //  HU: Historial
    // =================================================================

    @Test
    void historialSeConsultaAunqueElTorneoEsteFinalizado() {
        fail("TODO");
    }
}

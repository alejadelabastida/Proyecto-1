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
    private Torneo tenisCompetitivo;   // 10 cupos, tarifa 30.000
    private Torneo padelRecreativo;    // 4 cupos, dobles, sin tarifa

    @BeforeEach
    void setUp() {
        club = new Club();             // sin persistencia: guardar() no escribe archivos
        DatosBase.cargar(club);        // admin (codigo 1), entrenadores, fisio, socios 11-14

        LocalDate enDosSemanas = LocalDate.now().plusDays(14);
        tenisCompetitivo = new Torneo("Copa Tenis", TipoTorneo.COMPETITIVO, Deporte.TENIS, null,
                Nivel.INTERMEDIO, Modalidad.SENCILLOS, 10, 30000, enDosSemanas);
        padelRecreativo = new Torneo("Padel Amistoso", TipoTorneo.RECREATIVO, Deporte.PADEL, null,
                Nivel.AVANZADO, Modalidad.DOBLES, 4, 0, enDosSemanas);
        club.getTorneos().add(tenisCompetitivo);
        club.getTorneos().add(padelRecreativo);
    }

    // =================================================================
    //  EJEMPLO COMPLETO (sobre codigo que ya existe en Torneo)
    // =================================================================

    @Test
    void cuposPreferencialesSonEl20PorCientoRedondeadoHaciaArriba() {
        // Dado un torneo de 10 cupos y otro de 4
        // Cuando se consultan sus cupos preferenciales
        // Entonces son 2 (10 * 0,2) y 1 (4 * 0,2 = 0,8 -> 1)
        assertEquals(2, tenisCompetitivo.cuposPreferenciales());
        assertEquals(1, padelRecreativo.cuposPreferenciales());
    }

    @Test
    void torneoRecreativoNoPuedeCobrarTarifa() {
        // Dado un torneo recreativo
        // Cuando se intenta crear con tarifa mayor que cero
        // Entonces se rechaza
        assertThrows(IllegalArgumentException.class, () -> new Torneo("Malo", TipoTorneo.RECREATIVO,
                Deporte.TENIS, null, Nivel.INTERMEDIO, Modalidad.SENCILLOS, 8, 5000, LocalDate.now()));
    }

    // =================================================================
    //  HU: Crear torneo
    // =================================================================

    @Test
    void administradorCreaTorneoValido() {
        // Dado el administrador autenticado
        // Cuando crea un torneo con datos validos
        Torneo creado = club.crearTorneo(DatosBase.ADMIN, "Torneo Prueba", TipoTorneo.RECREATIVO, Deporte.TENIS, null, Nivel.INTERMEDIO, Modalidad.SENCILLOS, 16, 0.0, LocalDate.now().plusDays(14));

        // Entonces el torneo queda registrado en el club con estado ABIERTO
        assertEquals(EstadoTorneo.ABIERTO, creado.getEstado());
        assertEquals(3, club.getTorneos().size());
    }

    @Test
    void usuarioQueNoEsAdministradorNoPuedeCrearTorneo() {
        // Dado un usuario que no es admin (socio)
        // Cuando / Entonces intenta crear un torneo, se lanza ReglaNegocioException
        assertThrows(ReglaNegocioException.class, () -> {
            club.crearTorneo(DatosBase.SOCIO1, "Torneo Prueba", TipoTorneo.RECREATIVO, Deporte.TENIS, null, Nivel.INTERMEDIO, Modalidad.SENCILLOS, 16, 0.0, LocalDate.now().plusDays(14));
        });
    }

    @Test
    void noSePuedenCrearDosTorneosConElMismoNombre() {
        // Dado que en el setUp ya existe un torneo llamado "Copa Tenis"
        // Cuando / Entonces el admin intenta crear "copa tenis" (en minusculas), se lanza ReglaNegocioException
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
        // Dado un torneo ABIERTO que nunca se inicio
        // Cuando / Entonces el admin intenta finalizarlo, se lanza ReglaNegocioException
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
        fail("TODO");
    }

    @Test
    void noSeAceptaMarcadorNegativo() {
        fail("TODO");
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

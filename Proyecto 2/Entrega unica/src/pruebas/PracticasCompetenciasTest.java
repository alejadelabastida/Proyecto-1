
package pruebas;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import consola.DatosBase;

import modelo.*;

public class PracticasCompetenciasTest {

    private Club club;
    private InscripcionDisciplina sofia;
    private InscripcionDisciplina juan;

    @BeforeEach
    public void preparar() {

        club = new Club();
        DatosBase.cargar(club);

        Socio socio1 = (Socio) DatosBase.usuario(
            club, DatosBase.SOCIO1
        );

        Socio socio2 = (Socio) DatosBase.usuario(
            club, DatosBase.SOCIO2
        );

        sofia = new InscripcionDisciplina(
            socio1, Deporte.TENIS, Nivel.INTERMEDIO
        );

        juan = new InscripcionDisciplina(
            socio2, Deporte.TENIS, Nivel.INTERMEDIO
        );
    }

    private PartidoPractica crearPractica() {

        List<InscripcionDisciplina> participantes =
            new ArrayList<>();

        participantes.add(sofia);
        participantes.add(juan);

        return club.programarPractica(
            participantes,
            Modalidad.SENCILLOS,
            LocalDate.now().minusDays(1)
        );
    }

    private Map<String, Integer> datosTenis(
            int sets, int games, int aces, int errores) {

        Map<String, Integer> datos = new HashMap<>();

        datos.put("sets", sets);
        datos.put("games", games);
        datos.put("aces", aces);
        datos.put("erroresNoForzados", errores);

        return datos;
    }

    private Map<InscripcionDisciplina, Map<String, Integer>>
            estadisticasValidas() {

        Map<InscripcionDisciplina, Map<String, Integer>>
            estadisticas = new HashMap<>();

        estadisticas.put(
            sofia, datosTenis(2, 12, 3, 2)
        );

        estadisticas.put(
            juan, datosTenis(0, 5, 1, 4)
        );

        return estadisticas;
    }

    @Test
    public void sePuedeProgramarUnaPracticaValida() {

        PartidoPractica partido = crearPractica();

        assertNotNull(partido);
        assertTrue(club.getPartidos().contains(partido));
        assertEquals(2, partido.getParticipantes().size());
        assertFalse(partido.resuelto());
    }

    @Test
    public void practicaSinParticipantesSeRechaza() {

        assertThrows(
            ReglaNegocioException.class,
            () -> club.programarPractica(
                new ArrayList<>(),
                Modalidad.SENCILLOS,
                LocalDate.now()
            )
        );

        assertTrue(club.getPartidos().isEmpty());
    }

    @Test
    public void practicaSinFechaSeRechaza() {

        List<InscripcionDisciplina> participantes =
            new ArrayList<>();

        participantes.add(sofia);
        participantes.add(juan);

        assertThrows(
            ReglaNegocioException.class,
            () -> club.programarPractica(
                participantes,
                Modalidad.SENCILLOS,
                null
            )
        );
    }

    @Test
    public void resultadoDePracticaSeRegistra() {

        PartidoPractica partido = crearPractica();

        club.registrarPractica(
            partido, estadisticasValidas()
        );

        assertTrue(partido.resuelto());

        assertEquals(
            2,
            partido.getEstadisticasPractica().size()
        );
    }

    @Test
    public void ganadorAumentaSuRanking() {

        PartidoPractica partido = crearPractica();

        int rankingAnterior = sofia.getPuntajeRanking();

        club.registrarPractica(
            partido, estadisticasValidas()
        );

        assertEquals(
            rankingAnterior + 10,
            sofia.getPuntajeRanking()
        );
    }

    @Test
    public void perdedorDisminuyeSuRanking() {

        PartidoPractica partido = crearPractica();

        int rankingAnterior = juan.getPuntajeRanking();

        club.registrarPractica(
            partido, estadisticasValidas()
        );

        assertEquals(
            rankingAnterior - 10,
            juan.getPuntajeRanking()
        );
    }

    @Test
    public void estadisticasIncompletasSeRechazan() {

        PartidoPractica partido = crearPractica();

        Map<InscripcionDisciplina, Map<String, Integer>>
            incompletas = new HashMap<>();

        incompletas.put(
            sofia, datosTenis(2, 12, 3, 2)
        );

        assertThrows(
            ReglaNegocioException.class,
            () -> club.registrarPractica(
                partido, incompletas
            )
        );

        assertFalse(partido.resuelto());
    }

    @Test
    public void empateEnSetsSeRechaza() {

        PartidoPractica partido = crearPractica();

        Map<InscripcionDisciplina, Map<String, Integer>>
            estadisticas = estadisticasValidas();

        estadisticas.put(
            juan, datosTenis(2, 12, 1, 4)
        );

        assertThrows(
            ReglaNegocioException.class,
            () -> club.registrarPractica(
                partido, estadisticas
            )
        );

        assertFalse(partido.resuelto());
    }

    @Test
    public void estadisticaNegativaSeRechaza() {

        PartidoPractica partido = crearPractica();

        Map<InscripcionDisciplina, Map<String, Integer>>
            estadisticas = estadisticasValidas();

        estadisticas.put(
            sofia, datosTenis(2, 12, -1, 2)
        );

        assertThrows(
            ReglaNegocioException.class,
            () -> club.registrarPractica(
                partido, estadisticas
            )
        );

        assertFalse(partido.resuelto());
    }

    @Test
    public void resultadoNoPuedeRegistrarseDosVeces() {

        PartidoPractica partido = crearPractica();

        club.registrarPractica(
            partido, estadisticasValidas()
        );

        assertThrows(
            ReglaNegocioException.class,
            () -> club.registrarPractica(
                partido, estadisticasValidas()
            )
        );

        assertEquals(
            2,
            partido.getEstadisticasPractica().size()
        );
    }

    @Test
    public void socioNoPuedeRegistrarResultadoCompetencia() {

        assertThrows(
            ReglaNegocioException.class,
            () -> club.registrarPartidoCompeticion(
                DatosBase.SOCIO1,
                null,
                "Universidad Rival",
                "Cancha principal",
                LocalDate.now(),
                2,
                1,
                null
            )
        );

        assertTrue(club.getPartidos().isEmpty());
    }

    @Test
    public void competenciaSinInscripcionSeRechaza() {

        assertThrows(
            ReglaNegocioException.class,
            () -> club.registrarPartidoCompeticion(
                DatosBase.ENTRENADOR1,
                null,
                "Universidad Rival",
                "Cancha principal",
                LocalDate.now(),
                2,
                1,
                null
            )
        );

        assertTrue(club.getPartidos().isEmpty());
    }


@Test
public void resultadoCompetenciaSeRegistraCorrectamente() {

    // DADO: una competencia de futbol registrada.
    Competencia competencia = new ASCUN(
        "Copa ASCUN Prueba",
        "ASCUN",
        "2026",
        Deporte.FUTBOL,
        Categoria.MAYORES,
        7
    );

    club.agregarCompetencia(competencia);

    // DADO: un equipo con siete jugadores.
    Equipo equipo = new Equipo(
        "Equipo Competencia Prueba",
        Deporte.FUTBOL,
        Categoria.MAYORES,
        15
    );

    club.getEquipos().add(equipo);

    List<Jugador> jugadores = new ArrayList<>();

    for (int i = 0; i < 7; i++) {

        Jugador jugador = new Jugador(
            5000 + i,
            "Jugador" + i,
            "Prueba",
            "competencia" + i,
            "1234",
            LocalDate.of(2000, 1, 1),
            "Defensa",
            i + 1
        );

        club.getUsuarios().add(jugador);
        equipo.inscribirJugador(jugador);
        jugadores.add(jugador);
    }

    // DADO: el equipo tiene inscripcion y nomina oficial.
    InscripcionEquipo inscripcion = club.inscribirEquipo(
        DatosBase.ENTRENADOR1,
        equipo,
        competencia,
        jugadores
    );

    assertTrue(inscripcion.cumpleNominaMinima());

    int partidosAntes = competencia.getPartidos().size();

    // CUANDO: el entrenador registra una victoria 2-1.
    PartidoCompeticion partido =
        club.registrarPartidoCompeticion(
            DatosBase.ENTRENADOR1,
            inscripcion,
            "Universidad Rival",
            "Cancha Principal",
            LocalDate.now().minusDays(1),
            2,
            1,
            null
        );

    // ENTONCES: el resultado queda registrado.
    assertNotNull(partido);
    assertTrue(partido.tieneResultado());

    assertEquals(
        partidosAntes + 1,
        competencia.getPartidos().size()
    );

    assertTrue(
        competencia.getPartidos().contains(partido)
    );

    assertTrue(
        club.getPartidos().contains(partido)
    );
}


}


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

public class PartidosInternosTest {

    private Club club;
    private Equipo azules;
    private Equipo rojos;
    private PartidoInterno partido;

    @BeforeEach
    public void preparar() {

        club = new Club();
        DatosBase.cargar(club);

        azules = crearEquipo("Azules", 100);
        rojos = crearEquipo("Rojos", 200);
    }

    private Equipo crearEquipo(String nombre, int primerCodigo) {

        Equipo equipo = new Equipo(
            nombre,
            Deporte.FUTBOL,
            Categoria.MAYORES,
            15
        );

        club.getEquipos().add(equipo);

        for (int i = 0; i < 7; i++) {

            int codigo = primerCodigo + i;

            Socio socio = new Socio(
                codigo,
                "Jugador" + codigo,
                "Prueba",
                "s" + codigo,
                "1234",
                LocalDate.of(2003, 1, 1)
            );

            club.getUsuarios().add(socio);

            club.inscribirJugador(
                codigo,
                nombre,
                "Defensa",
                i + 1
            );
        }

        return equipo;
    }

    private PartidoInterno programarPartido() {

        partido = club.programarPartidoInterno(
            DatosBase.ENTRENADOR1,
            azules,
            rojos,
            LocalDate.now().plusDays(3)
        );

        return partido;
    }

    private void definirAmbasAlineaciones() {

        club.definirAlineacionInterna(
            DatosBase.ENTRENADOR1,
            partido,
            azules,
            new ArrayList<>(azules.getJugadores())
        );

        club.definirAlineacionInterna(
            DatosBase.ENTRENADOR1,
            partido,
            rojos,
            new ArrayList<>(rojos.getJugadores())
        );
    }

    @Test
    public void entrenadorPuedeProgramarPartidoInterno() {

        PartidoInterno creado = programarPartido();

        assertNotNull(creado);
        assertTrue(club.getPartidos().contains(creado));
        assertEquals(azules, creado.getEquipo1());
        assertEquals(rojos, creado.getEquipo2());
        assertFalse(creado.tieneResultado());

        assertEquals(
            DatosBase.ENTRENADOR1,
            creado.getCodigoEntrenadorResponsable()
        );
    }

    @Test
    public void socioNoPuedeProgramarPartidos() {

        assertThrows(
            ReglaNegocioException.class,
            () -> club.programarPartidoInterno(
                DatosBase.SOCIO1,
                azules,
                rojos,
                LocalDate.now().plusDays(3)
            )
        );

        assertTrue(club.getPartidos().isEmpty());
    }

    @Test
    public void partidoConFechaPasadaSeRechaza() {

        assertThrows(
            ReglaNegocioException.class,
            () -> club.programarPartidoInterno(
                DatosBase.ENTRENADOR1,
                azules,
                rojos,
                LocalDate.now().minusDays(1)
            )
        );
    }

    @Test
    public void unEquipoNoPuedeJugarContraSiMismo() {

        assertThrows(
            ReglaNegocioException.class,
            () -> club.programarPartidoInterno(
                DatosBase.ENTRENADOR1,
                azules,
                azules,
                LocalDate.now().plusDays(3)
            )
        );
    }

    @Test
    public void equiposIncompatiblesSeRechazan() {

        Equipo voleibol = new Equipo(
            "Voleibol",
            Deporte.VOLEIBOL,
            Categoria.MAYORES,
            15
        );

        club.getEquipos().add(voleibol);

        assertThrows(
            ReglaNegocioException.class,
            () -> club.programarPartidoInterno(
                DatosBase.ENTRENADOR1,
                azules,
                voleibol,
                LocalDate.now().plusDays(3)
            )
        );
    }

    @Test
    public void equipoIncompletoNoPuedeProgramarPartido() {

        Equipo incompleto = new Equipo(
            "Incompleto",
            Deporte.FUTBOL,
            Categoria.MAYORES,
            15
        );

        club.getEquipos().add(incompleto);

        assertThrows(
            ReglaNegocioException.class,
            () -> club.programarPartidoInterno(
                DatosBase.ENTRENADOR1,
                azules,
                incompleto,
                LocalDate.now().plusDays(3)
            )
        );
    }

    @Test
    public void entrenadorPuedeDefinirAlineaciones() {

        programarPartido();
        definirAmbasAlineaciones();

        assertEquals(
            7,
            partido.getAlineacionLocal().size()
        );

        assertEquals(
            7,
            partido.getAlineacionVisitante().size()
        );

        assertTrue(partido.tieneAmbasAlineaciones());
    }

    @Test
    public void alineacionInsuficienteSeRechaza() {

        programarPartido();

        List<Jugador> incompleta = new ArrayList<>(
            azules.getJugadores().subList(0, 6)
        );

        assertThrows(
            ReglaNegocioException.class,
            () -> club.definirAlineacionInterna(
                DatosBase.ENTRENADOR1,
                partido,
                azules,
                incompleta
            )
        );
    }

    @Test
    public void alineacionConJugadorRepetidoSeRechaza() {

        programarPartido();

        List<Jugador> repetidos = new ArrayList<>(
            azules.getJugadores()
        );

        repetidos.set(6, repetidos.get(0));

        assertThrows(
            ReglaNegocioException.class,
            () -> club.definirAlineacionInterna(
                DatosBase.ENTRENADOR1,
                partido,
                azules,
                repetidos
            )
        );
    }

    @Test
    public void otroEntrenadorNoPuedeModificarPartido() {

        programarPartido();

        assertThrows(
            ReglaNegocioException.class,
            () -> club.definirAlineacionInterna(
                DatosBase.ENTRENADOR2,
                partido,
                azules,
                new ArrayList<>(azules.getJugadores())
            )
        );
    }

    @Test
    public void resultadoFuturoSeRechaza() {

        programarPartido();
        definirAmbasAlineaciones();

        assertThrows(
            ReglaNegocioException.class,
            () -> club.registrarResultadoInterno(
                DatosBase.ENTRENADOR1,
                partido,
                2,
                1
            )
        );

        assertFalse(partido.tieneResultado());
    }

    @Test
    public void resultadoSinAlineacionesSeRechaza() {

        programarPartido();

        assertThrows(
            ReglaNegocioException.class,
            () -> club.registrarResultadoInterno(
                DatosBase.ENTRENADOR1,
                partido,
                2,
                1
            )
        );

        assertFalse(partido.tieneResultado());
    }

    @Test
    public void estadisticasSinResultadoSeRechazan() {

        programarPartido();
        definirAmbasAlineaciones();

        Map<Jugador, Map<String, Integer>> estadisticas =
            new HashMap<>();

        Map<String, Integer> datos = new HashMap<>();
        datos.put("goles", 2);

        estadisticas.put(
            azules.getJugadores().get(0),
            datos
        );

        assertThrows(
            ReglaNegocioException.class,
            () -> club.registrarEstadisticasPartidoInterno(
                DatosBase.ENTRENADOR1,
                partido,
                estadisticas
            )
        );
    }

    @Test
    public void estadisticasDeJugadorNoAlineadoSeRechazan() {

        programarPartido();
        definirAmbasAlineaciones();

        // Se simula un partido ya resuelto para aislar
        // las validaciones de estadisticas.
        partido.registrarResultado(2, 1);

        Equipo otroEquipo = crearEquipo("Verdes", 300);

        Jugador ajeno = otroEquipo.getJugadores().get(0);

        Map<String, Integer> datos = new HashMap<>();
        datos.put("goles", 1);

        Map<Jugador, Map<String, Integer>> estadisticas =
            new HashMap<>();

        estadisticas.put(ajeno, datos);

        assertThrows(
            ReglaNegocioException.class,
            () -> club.registrarEstadisticasPartidoInterno(
                DatosBase.ENTRENADOR1,
                partido,
                estadisticas
            )
        );
    }

    @Test
    public void entrenadorPuedeRegistrarEstadisticas() {

        programarPartido();
        definirAmbasAlineaciones();

        // Preparacion directa del resultado para probar
        // exclusivamente el registro de estadisticas.
        partido.registrarResultado(2, 1);

        Jugador destacado = azules.getJugadores().get(0);

        Map<String, Integer> datos = new HashMap<>();
        datos.put("goles", 2);
        datos.put("asistencias", 0);
        datos.put("tarjetasAmarillas", 0);
        datos.put("tarjetasRojas", 0);

        Map<Jugador, Map<String, Integer>> estadisticas =
            new HashMap<>();

        estadisticas.put(destacado, datos);

        club.registrarEstadisticasPartidoInterno(
            DatosBase.ENTRENADOR1,
            partido,
            estadisticas
        );

        assertEquals(
            1,
            partido.getEstadisticas().size()
        );

        assertEquals(
            destacado,
            partido.getEstadisticas().get(0).getJugador()
        );
    }

    @Test
    public void estadisticasDuplicadasSeRechazan() {

        programarPartido();
        definirAmbasAlineaciones();

        partido.registrarResultado(2, 1);

        Jugador destacado = azules.getJugadores().get(0);

        Map<String, Integer> datos = new HashMap<>();
        datos.put("goles", 1);
        datos.put("tarjetasAmarillas", 0);
        datos.put("tarjetasRojas", 0);

        Map<Jugador, Map<String, Integer>> estadisticas =
            new HashMap<>();

        estadisticas.put(destacado, datos);

        club.registrarEstadisticasPartidoInterno(
            DatosBase.ENTRENADOR1,
            partido,
            estadisticas
        );

        assertThrows(
            ReglaNegocioException.class,
            () -> club.registrarEstadisticasPartidoInterno(
                DatosBase.ENTRENADOR1,
                partido,
                estadisticas
            )
        );

        assertEquals(1, partido.getEstadisticas().size());
    }


@Test
public void resultadoInternoSeRegistraCorrectamente() {

    // Dado: dos equipos completos y un partido
    // programado para una fecha que ya paso.
    PartidoInterno historico = new PartidoInterno(
        LocalDate.now().minusDays(1),
        Deporte.FUTBOL,
        azules,
        rojos,
        DatosBase.ENTRENADOR1
    );

    club.getPartidos().add(historico);

    // Dado: ambos equipos tienen alineacion.
    club.definirAlineacionInterna(
        DatosBase.ENTRENADOR1,
        historico,
        azules,
        new ArrayList<>(azules.getJugadores())
    );

    club.definirAlineacionInterna(
        DatosBase.ENTRENADOR1,
        historico,
        rojos,
        new ArrayList<>(rojos.getJugadores())
    );

    // Cuando: el entrenador registra el resultado.
    club.registrarResultadoInterno(
        DatosBase.ENTRENADOR1,
        historico,
        2,
        1
    );

    // Entonces: el partido queda registrado
    // correctamente en el club.
    assertTrue(historico.tieneResultado());
    assertTrue(club.getPartidos().contains(historico));
    }
}

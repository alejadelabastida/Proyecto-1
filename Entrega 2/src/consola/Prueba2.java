package consola;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import modelo.*;

public class Prueba2 {

    public static void main(String[] args) {
        Club club = new Club();
        DatosBase.cargar(club);
        Salida.titulo("PRUEBA 2: Partidos internos y sanciones");
        ejecutar(club);
    }

    private static void ejecutar(Club club) {
        Equipo azules = crearEquipo(club, "Azules", 100);
        Equipo rojos = crearEquipo(club, "Rojos", 200);

        Salida.seccion("Estado antes");
        Salida.linea("Azules: " + azules.getJugadores().size() + " jugadores");
        Salida.linea("Rojos: " + rojos.getJugadores().size() + " jugadores");

        Salida.seccion("RF-02 / RF-22: partido interno y estadisticas");
        Salida.accion("Carlos registra Azules 2 - 1 Rojos con las estadisticas individuales");
        Jugador destacado = azules.getJugadores().get(0);
        Map<Jugador, Map<String, Integer>> estadisticas = new HashMap<>();
        Map<String, Integer> datosJugador = new HashMap<>();
        datosJugador.put("goles", 2);
        datosJugador.put("tarjetasAmarillas", 2);
        estadisticas.put(destacado, datosJugador);

        try {
            PartidoInterno partido = club.registrarPartidoInterno(DatosBase.ENTRENADOR1,
                    azules, rojos, LocalDate.now(), 2, 1, estadisticas);
            Salida.linea("Marcador: " + partido.getMarcadorEquipo1() + " - " + partido.getMarcadorEquipo2());
            Salida.ok();
        } catch (ReglaNegocioException e) {
            Salida.inesperado(e);
        }

        Salida.seccion("RF-24: consulta de sanciones");
        Salida.accion("Consultar las sanciones de " + destacado.getNombre());
        Salida.linea("Sanciones: " + destacado.getSanciones().size());
        Salida.linea("Suspendido: " + destacado.estaSuspendido());

        Salida.seccion("RF-22: alineacion insuficiente");
        Salida.accion("Intentar un partido con un equipo de solo seis jugadores");
        Equipo incompleto = crearEquipoIncompleto(club);
        try {
            club.registrarPartidoInterno(DatosBase.ENTRENADOR1, incompleto,
                    rojos, LocalDate.now(), 0, 1, new HashMap<Jugador, Map<String, Integer>>());
            Salida.debiaRechazarse();
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.seccion("Estado despues");
        Salida.linea("Partidos registrados: " + club.getPartidos().size());
        Salida.linea("Sanciones del jugador: " + destacado.getSanciones().size());
    }

    private static Equipo crearEquipo(Club club, String nombre, int primerCodigo) {
        Equipo equipo = new Equipo(nombre, Deporte.FUTBOL, Categoria.MAYORES, 15);
        club.getEquipos().add(equipo);
        for (int i = 0; i < 7; i++) {
            int codigo = primerCodigo + i;
            Socio socio = new Socio(codigo, "Jugador" + codigo, "Prueba", "s" + codigo,
                    "1234", LocalDate.of(2003, 1, 1));
            club.getUsuarios().add(socio);
            club.inscribirJugador(codigo, nombre, "Defensa", i + 1);
        }
        return equipo;
    }

    private static Equipo crearEquipoIncompleto(Club club) {
        Equipo equipo = new Equipo("Incompleto", Deporte.FUTBOL, Categoria.MAYORES, 15);
        club.getEquipos().add(equipo);
        for (int i = 0; i < 6; i++) {
            int codigo = 300 + i;
            Socio socio = new Socio(codigo, "Jugador" + codigo, "Prueba", "s" + codigo,
                    "1234", LocalDate.of(2003, 1, 1));
            club.getUsuarios().add(socio);
            club.inscribirJugador(codigo, "Incompleto", "Defensa", i + 1);
        }
        return equipo;
    }
}

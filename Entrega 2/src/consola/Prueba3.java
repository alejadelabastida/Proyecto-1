package consola;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import modelo.*;

public class Prueba3 {

    public static void main(String[] args) {
        Club club = new Club();
        DatosBase.cargar(club);
        Salida.titulo("PRUEBA 3: Competencias y nominas");
        ejecutar(club);
    }

    private static void ejecutar(Club club) {
        Equipo equipo = new Equipo("Universidad", Deporte.FUTBOL, Categoria.MAYORES, 15);
        club.getEquipos().add(equipo);
        for (int i = 0; i < 7; i++) {
            int codigo = 400 + i;
            Socio socio = new Socio(codigo, "Jugador" + codigo, "Prueba", "s" + codigo,
                    "1234", LocalDate.of(2003, 1, 1));
            club.getUsuarios().add(socio);
            club.inscribirJugador(codigo, "Universidad", "Defensa", i + 1);
        }
        Competencia torneo = new TorneoDeLosCerros("Cerros", "Organizacion", "2026",
                Deporte.FUTBOL, Categoria.MAYORES, 7);
        club.getCompetencias().add(torneo);

        Salida.seccion("Estado antes");
        mostrarCompetencia(torneo);

        Salida.seccion("RF-20 / RF-21: inscribir equipo y nomina");
        Salida.accion("Carlos inscribe los siete jugadores de Universidad en Cerros");
        InscripcionEquipo inscripcion = null;
        try {
            inscripcion = club.inscribirEquipo(DatosBase.ENTRENADOR1, equipo, torneo,
                    equipo.getJugadores());
            Salida.linea("Jugadores en nomina: " + inscripcion.getNomina().size());
            Salida.ok();
        } catch (ReglaNegocioException e) {
            Salida.inesperado(e);
        }

        if (inscripcion != null) {
            Salida.seccion("RF-23: partido de competencia");
            Salida.accion("Universidad juega contra Otra universidad y gana 2 - 0");
            Map<Jugador, Map<String, Integer>> estadisticas = new HashMap<>();
            try {
                PartidoCompeticion partido = club.registrarPartidoCompeticion(DatosBase.ENTRENADOR1,
                        inscripcion, "Otra universidad", "Estadio", LocalDate.now(), 2, 0, estadisticas);
                Salida.linea("Rival: " + partido.getInstitucionRival());
                Salida.ok();
            } catch (ReglaNegocioException e) {
                Salida.inesperado(e);
            }

            Salida.seccion("RF-05 / RF-07: tabla de posiciones");
            Salida.linea("Puntos obtenidos: " + inscripcion.getPuntos());
            Salida.linea("Partidos en competencia: " + torneo.getPartidos().size());
        }

        Salida.seccion("Estado despues");
        mostrarCompetencia(torneo);
    }

    private static void mostrarCompetencia(Competencia competencia) {
        Salida.linea("Torneo: " + competencia.getNombre());
        Salida.linea("Equipos inscritos: " + competencia.getInscripciones().size());
        Salida.linea("Partidos registrados: " + competencia.getPartidos().size());
    }
}

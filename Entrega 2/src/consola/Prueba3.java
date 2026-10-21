package consola;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import modelo.*;

public class Prueba3 {

    public static void main(String[] args) {
        String carpeta = "datos/prueba3";
        if (args.length > 0 && !args[0].trim().isEmpty()) {
            carpeta = args[0].trim();
        } else {
            System.out.print("Carpeta de datos (Enter = datos/prueba3): ");
            java.util.Scanner sc = new java.util.Scanner(System.in);
            if (sc.hasNextLine()) {
                String elegida = sc.nextLine().trim();
                sc.close();
                if (!elegida.isEmpty()) {
                    carpeta = elegida;
                }
            }
        }

        persistencia.Persistencia persistencia = new persistencia.Persistencia(carpeta);
        boolean yaEjecutada = persistencia.existe();
        Club club = persistencia.cargar();
        Salida.titulo("PRUEBA 3: persistencia y reglas de negocio");

        if (yaEjecutada) {
            Salida.linea("Cargando datos previamente guardados en " + carpeta);
            verificarPersistencia(club);
            return;
        }

        DatosBase.cargar(club);
        ejecutar(club);
        club.guardar();

        Club recuperado = new persistencia.Persistencia(carpeta).cargar();
        verificarPersistencia(recuperado);
        Salida.linea("Datos guardados en " + carpeta
                + ". Ejecute de nuevo para consultar los datos persistidos.");
    }

    private static void verificarPersistencia(Club club) {
        if (club.getCompetencias().size() != 1) {
            throw new IllegalStateException("No se recupero la competencia de la prueba 3");
        }
        Competencia c = club.getCompetencias().get(0);
        if (c.getInscripciones().size() != 1 || c.getPartidos().size() != 1) {
            throw new IllegalStateException("No se conservaron la nomina o el partido oficial");
        }
        InscripcionEquipo ie = c.getInscripciones().get(0);
        if (ie.getNomina().size() != 7) {
            throw new IllegalStateException("La nomina no se recupero completa");
        }
        Salida.linea("Persistencia verificada: 7 jugadores en nomina y 1 partido en competencia.");
        Salida.ok();
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

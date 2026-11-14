
package consola;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import modelo.*;
import persistencia.Persistencia;

public class Prueba2 {

    public static void main(String[] args) {

        String carpeta = "datos/prueba2_programacion";

        if (args.length > 0 && !args[0].trim().isEmpty()) {
            carpeta = args[0].trim();
        }

        Persistencia persistencia = new Persistencia(carpeta);
        boolean yaEjecutada = persistencia.existe();
        Club club = persistencia.cargar();

        Salida.titulo("PRUEBA 2: programacion de partidos y persistencia");

        if (yaEjecutada) {
            verificarPersistencia(club);
            return;
        }

        DatosBase.cargar(club);
        ejecutar(club);
        club.guardar();

        Club recuperado = new Persistencia(carpeta).cargar();
        verificarPersistencia(recuperado);

        Salida.linea("Datos guardados en " + carpeta);
        Salida.ok();
    }

    private static void ejecutar(Club club) {

        Equipo azules = crearEquipo(club, "Azules", 100);
        Equipo rojos = crearEquipo(club, "Rojos", 200);

        Salida.seccion("RF-22: programar partido interno");

        LocalDate fecha = LocalDate.now().plusDays(2);

        PartidoInterno partido = club.programarPartidoInterno(
            DatosBase.ENTRENADOR1,
            azules,
            rojos,
            fecha
        );

        Salida.linea("Partido programado para: " + partido.getFecha());

        if (partido.tieneResultado()) {
            throw new IllegalStateException(
                "Un partido programado no debe tener resultado"
            );
        }

        Salida.ok();

        Salida.seccion("Definir alineaciones");

        List<Jugador> alineacionAzules =
            new ArrayList<>(azules.getJugadores());

        List<Jugador> alineacionRojos =
            new ArrayList<>(rojos.getJugadores());

        club.definirAlineacionInterna(
            DatosBase.ENTRENADOR1,
            partido,
            azules,
            alineacionAzules
        );

        club.definirAlineacionInterna(
            DatosBase.ENTRENADOR1,
            partido,
            rojos,
            alineacionRojos
        );

        if (!partido.tieneAmbasAlineaciones()) {
            throw new IllegalStateException(
                "No se guardaron las dos alineaciones"
            );
        }

        Salida.linea("Alineaciones registradas correctamente.");
        Salida.ok();

        Salida.seccion("Rechazar resultado anticipado");

        try {
            club.registrarResultadoInterno(
                DatosBase.ENTRENADOR1,
                partido,
                2,
                1
            );

            throw new IllegalStateException(
                "Se permitio registrar un resultado futuro"
            );

        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.seccion("Rechazar equipo incompleto");

        Equipo incompleto = crearEquipoIncompleto(club);

        try {
            club.programarPartidoInterno(
                DatosBase.ENTRENADOR1,
                incompleto,
                rojos,
                fecha
            );

            throw new IllegalStateException(
                "Se permitio programar un equipo incompleto"
            );

        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.seccion("Estado final");

        Salida.linea("Partidos registrados: " + club.getPartidos().size());
        Salida.linea("Resultado pendiente: " + !partido.tieneResultado());
        Salida.linea("Alineacion local: " + partido.getAlineacionLocal().size());
        Salida.linea("Alineacion visitante: " + partido.getAlineacionVisitante().size());
    }

    private static void verificarPersistencia(Club club) {

        Salida.seccion("Verificar persistencia");

        if (club.getPartidos().size() != 1) {
            throw new IllegalStateException(
                "No se recupero el partido programado"
            );
        }

        PartidoInterno partido =
            (PartidoInterno) club.getPartidos().get(0);

        if (partido.tieneResultado()) {
            throw new IllegalStateException(
                "El partido no deberia tener resultado"
            );
        }

        if (partido.getCodigoEntrenadorResponsable()
                != DatosBase.ENTRENADOR1) {
            throw new IllegalStateException(
                "No se conservo el entrenador responsable"
            );
        }

        if (partido.getAlineacionLocal().size() != 7
                || partido.getAlineacionVisitante().size() != 7) {
            throw new IllegalStateException(
                "No se conservaron las alineaciones"
            );
        }

        Salida.linea("Partido pendiente recuperado correctamente.");
        Salida.linea("Entrenador responsable conservado.");
        Salida.linea("Alineaciones conservadas.");
        Salida.ok();
    }

    private static Equipo crearEquipo(
            Club club, String nombre, int primerCodigo) {

        Equipo equipo = new Equipo(
            nombre, Deporte.FUTBOL, Categoria.MAYORES, 15
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
                codigo, nombre, "Defensa", i + 1
            );
        }

        return equipo;
    }

    private static Equipo crearEquipoIncompleto(Club club) {

        Equipo equipo = new Equipo(
            "Incompleto",
            Deporte.FUTBOL,
            Categoria.MAYORES,
            15
        );

        club.getEquipos().add(equipo);

        for (int i = 0; i < 6; i++) {

            int codigo = 300 + i;

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
                codigo, "Incompleto", "Defensa", i + 1
            );
        }

        return equipo;
    }
}

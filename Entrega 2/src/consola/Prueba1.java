package consola;

import java.time.LocalDate;
import modelo.*;

public class Prueba1 {

    public static void main(String[] args) {
        String carpeta = "datos/prueba1";
        if (args.length > 0 && !args[0].trim().isEmpty()) {
            carpeta = args[0].trim();
        } else {
            System.out.print("Carpeta de datos (Enter = datos/prueba1): ");
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
        Salida.titulo("PRUEBA 1: persistencia y reglas de negocio");

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
        int azules = -1;
        for (Equipo e : club.getEquipos()) {
            if ("Azules".equals(e.getNombre())) {
                azules = e.getJugadores().size();
            }
        }
        if (azules != 2 || club.getUsuarios().size() < 9) {
            throw new IllegalStateException("No se recuperaron las inscripciones de la prueba 1");
        }
        Salida.linea("Persistencia verificada: Azules tiene " + azules + " jugadores; "
                + club.getUsuarios().size() + " usuarios registrados.");
        Salida.ok();
    }

    private static void ejecutar(Club club) {
        Equipo azules = new Equipo("Azules", Deporte.FUTBOL, Categoria.MAYORES, 2);
        Equipo rojos = new Equipo("Rojos", Deporte.FUTBOL, Categoria.MAYORES, 10);
        club.getEquipos().add(azules);
        club.getEquipos().add(rojos);

        Salida.seccion("Estado antes");
        mostrarEquipo(azules);

        Salida.seccion("RF-01: inscripciones validas");
        Salida.accion("Juan y Pedro se inscriben en el equipo Azules");
        try {
            club.inscribirJugador(DatosBase.SOCIO2, "Azules", "Defensa", 4);
            club.inscribirJugador(DatosBase.SOCIO4, "Azules", "Delantero", 9);
            Salida.linea("Jugadores registrados: " + azules.getJugadores().size());
            Salida.ok();
        } catch (ReglaNegocioException e) {
            Salida.inesperado(e);
        }

        Salida.seccion("RF-01: inscripciones invalidas");
        Socio lucas = new Socio(30, "Lucas", "Prueba", "lucas", "1234", LocalDate.of(2003, 2, 4));
        club.getUsuarios().add(lucas);
        Salida.accion("Lucas intenta inscribirse en un equipo lleno");
        try {
            club.inscribirJugador(30, "Azules", "Defensa", 5);
            Salida.debiaRechazarse();
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.accion("Juan intenta inscribirse en otro equipo del mismo deporte");
        try {
            club.inscribirJugador(DatosBase.SOCIO2, "Rojos", "Defensa", 4);
            Salida.debiaRechazarse();
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.accion("Maria intenta entrar a la categoria Mayores, pero no tiene la edad");
        try {
            club.inscribirJugador(DatosBase.SOCIO3, "Rojos", "Defensa", 8);
            Salida.debiaRechazarse();
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.seccion("Estado despues");
        mostrarEquipo(azules);
        mostrarEquipo(rojos);
    }

    private static void mostrarEquipo(Equipo equipo) {
        Salida.linea(equipo.getNombre() + ": " + equipo.getJugadores().size() + " jugadores");
        for (Jugador jugador : equipo.getJugadores()) {
            Salida.linea("- " + jugador.getNombre() + " (" + jugador.getPosicion() + ")");
        }
    }
}

package consola;

import java.time.LocalDate;
import modelo.*;

public class Prueba1 {

    public static void main(String[] args) {
        Club club = new Club();
        DatosBase.cargar(club);
        Salida.titulo("PRUEBA 1: Equipos, jugadores e inscripciones");
        ejecutar(club);
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

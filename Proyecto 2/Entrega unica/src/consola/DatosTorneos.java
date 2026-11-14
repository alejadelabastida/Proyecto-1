package consola;

import java.time.LocalDate;
import java.time.LocalTime;

import modelo.CanchaPadel;
import modelo.CanchaTenis;
import modelo.Categoria;
import modelo.Club;
import modelo.Deporte;
import modelo.Empleado;
import modelo.Entrenador;
import modelo.Equipo;
import modelo.Turno;
import modelo.MesaTenisDeMesa;
import modelo.InscripcionDisciplina;
import modelo.Jugador;
import modelo.Modalidad;
import modelo.Nivel;
import modelo.Socio;
import modelo.TipoTorneo;
import modelo.Torneo;

public class DatosTorneos {

    public static void cargarSiHaceFalta(Club club) {
        if (club.getUsuarios().isEmpty()) {
            DatosBase.cargar(club);
        }
        if (club.getProductos().isEmpty()) {
            DatosTienda.cargar(club);
        }
        if (club.getInstalaciones().isEmpty()) {
            cargarInstalacionesYTurnos(club);
        }
        if (club.getTorneos().isEmpty()) {
            cargarTorneosDeEjemplo(club);
        }
    }

    private static void cargarInstalacionesYTurnos(Club club) {
        club.getInstalaciones().add(new CanchaTenis("T-01", "Cancha de tenis 1", false));
        club.getInstalaciones().add(new CanchaPadel("P-01", "Cancha de padel techada", true));
        club.getInstalaciones().add(new MesaTenisDeMesa("M-01", "Mesa de tenis de mesa 1", false));
        new Turno((Empleado) DatosBase.usuario(club, DatosBase.ENTRENADOR1), "MARTES",
                LocalTime.of(8, 0), LocalTime.of(16, 0));
        new Turno((Empleado) DatosBase.usuario(club, DatosBase.ENTRENADOR2), "MIERCOLES",
                LocalTime.of(8, 0), LocalTime.of(16, 0));
        new Turno((Empleado) DatosBase.usuario(club, DatosBase.FISIO), "JUEVES",
                LocalTime.of(8, 0), LocalTime.of(16, 0));
    }

    private static void cargarTorneosDeEjemplo(Club club) {
        club.registrarEstudiante("Diana", "Castro", "diana", "1234", "202110001");
        club.registrarExterno("Esteban", "Mora", "esteban", "1234", "1020304050");

        Socio sofia = (Socio) DatosBase.usuario(club, DatosBase.SOCIO1);
        Socio juan = (Socio) DatosBase.usuario(club, DatosBase.SOCIO2);
        Socio maria = (Socio) DatosBase.usuario(club, DatosBase.SOCIO3);
        Socio pedro = (Socio) DatosBase.usuario(club, DatosBase.SOCIO4);
        new InscripcionDisciplina(sofia, Deporte.TENIS, Nivel.INTERMEDIO);
        InscripcionDisciplina tenisJuan = new InscripcionDisciplina(juan, Deporte.TENIS, Nivel.INTERMEDIO);
        tenisJuan.actualizarRanking(1010, LocalDate.now());
        new InscripcionDisciplina(maria, Deporte.PADEL, Nivel.AVANZADO);
        new InscripcionDisciplina(pedro, Deporte.PADEL, Nivel.AVANZADO);

        Equipo halcones = new Equipo("Halcones Sub-15", Deporte.FUTBOL, Categoria.SUB_15, 12);
        for (int i = 0; i < 7; i++) {
            Jugador jugador = new Jugador(101 + i, "Jugador" + (i + 1), "Halcon", "halcon" + (i + 1), "1234",
                    LocalDate.of(2012, 1, 1 + i), "Delantero", i + 1);
            halcones.inscribirJugador(jugador);
            club.getUsuarios().add(jugador);
        }
        halcones.setEntrenadorResponsable((Entrenador) DatosBase.usuario(club, DatosBase.ENTRENADOR1));
        club.getEquipos().add(halcones);

        LocalDate hoy = LocalDate.now();
        club.getTorneos().add(new Torneo("Copa Tenis Intermedio", TipoTorneo.COMPETITIVO, Deporte.TENIS,
                null, Nivel.INTERMEDIO, Modalidad.SENCILLOS, 8, 30000, hoy.plusDays(14)));
        club.getTorneos().add(new Torneo("Dobles Padel Avanzado", TipoTorneo.RECREATIVO, Deporte.PADEL,
                null, Nivel.AVANZADO, Modalidad.DOBLES, 4, 0, hoy.plusDays(21)));
        club.getTorneos().add(new Torneo("Mini Campeonato Futbol Sub-15", TipoTorneo.RECREATIVO,
                Deporte.FUTBOL, Categoria.SUB_15, null, null, 4, 0, hoy.plusDays(10)));
        club.guardar();
    }
}

package consola;

import java.time.LocalDate;

import modelo.Categoria;
import modelo.Club;
import modelo.Deporte;
import modelo.Equipo;
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
        if (club.getTorneos().isEmpty()) {
            cargarTorneosDeEjemplo(club);
        }
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
        tenisJuan.actualizarRanking(1010, LocalDate.now());   // Juan ya jugo y gano
        new InscripcionDisciplina(maria, Deporte.PADEL, Nivel.AVANZADO);
        new InscripcionDisciplina(pedro, Deporte.PADEL, Nivel.AVANZADO);

        Equipo halcones = new Equipo("Halcones Sub-15", Deporte.FUTBOL, Categoria.SUB_15, 12);
        for (int i = 0; i < 7; i++) {
            Jugador jugador = new Jugador(101 + i, "Jugador" + (i + 1), "Halcon", "halcon" + (i + 1), "1234",
                    LocalDate.of(2012, 1, 1 + i), "Delantero", i + 1);
            halcones.inscribirJugador(jugador);
            club.getUsuarios().add(jugador);
        }
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

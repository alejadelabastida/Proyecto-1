package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import consola.DatosBase;
import modelo.CanchaTenis;
import modelo.Club;
import modelo.Modalidad;
import modelo.ReglaNegocioException;
import modelo.Reserva;
import modelo.Socio;
import modelo.Usuario;

/**
 * Pruebas de reservas de instalaciones (RF-09), de la restriccion del
 * administrador (RF-43) y de la consulta del perfil (RF-45).
 */
class ReservasPerfilTest {

    private Club club;
    private CanchaTenis cancha;
    private LocalDate manana;

    @BeforeEach
    void setUp() {
        club = new Club();
        DatosBase.cargar(club);
        cancha = new CanchaTenis("T-RES", "Cancha de reservas", false);
        club.getInstalaciones().add(cancha);
        manana = LocalDate.now().plusDays(1);
    }

    // RF-09

    @Test
    void socioReservaUnaInstalacionDisponible() {
        Reserva reserva = club.reservar(DatosBase.SOCIO2, cancha, manana, LocalTime.of(9, 0), LocalTime.of(10, 0),
                2, Modalidad.SENCILLOS);
        assertTrue(club.getReservas().contains(reserva));
        assertEquals(1, cancha.getReservas().size());
    }

    @Test
    void reservarEnUnHorarioOcupadoSeRechaza() {
        club.reservar(DatosBase.SOCIO2, cancha, manana, LocalTime.of(9, 0), LocalTime.of(10, 0), 2, Modalidad.SENCILLOS);
        assertThrows(ReglaNegocioException.class, () -> club.reservar(DatosBase.SOCIO4, cancha, manana,
                LocalTime.of(9, 30), LocalTime.of(10, 30), 2, Modalidad.SENCILLOS));
        assertEquals(1, club.getReservas().size());
    }

    @Test
    void reservaConJugadoresQueNoCorrespondenALaModalidadSeRechaza() {
        assertThrows(ReglaNegocioException.class, () -> club.reservar(DatosBase.SOCIO2, cancha, manana,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 3, Modalidad.SENCILLOS));
    }

    // RF-43

    @Test
    void elAdministradorNoPuedeReservar() {
        assertThrows(ReglaNegocioException.class, () -> club.reservar(DatosBase.ADMIN, cancha, manana,
                LocalTime.of(9, 0), LocalTime.of(10, 0), 2, Modalidad.SENCILLOS));
    }

    // RF-45

    @Test
    void elPerfilMuestraCodigoYNombreDelUsuario() {
        Usuario sofia = DatosBase.usuario(club, DatosBase.SOCIO1);
        String perfil = club.consultarPerfil(sofia);
        assertTrue(perfil.contains(String.valueOf(DatosBase.SOCIO1)), perfil);
        assertTrue(perfil.contains("Sofia"), perfil);
    }

    @Test
    void perfilDeUnUsuarioQueNoEstaEnElClubSeRechaza() {
        Socio externo = new Socio(99, "Nadie", "Ninguno", "nadie", "1234", LocalDate.of(2000, 1, 1));
        assertThrows(ReglaNegocioException.class, () -> club.consultarPerfil(externo));
    }
}


package pruebas;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import consola.DatosBase;

import modelo.Club;
import modelo.Empleado;
import modelo.EstadoSolicitud;
import modelo.ReglaNegocioException;
import modelo.SolicitudCambioTurno;
import modelo.SugerenciaProducto;
import modelo.Turno;
import modelo.Usuario;

public class GestionEmpleadoTest {

    private Club club;
    private Empleado carlos;
    private Turno turno;

    @BeforeEach
    public void preparar() {

        club = new Club();
        DatosBase.cargar(club);

        carlos = (Empleado) DatosBase.usuario(
            club, DatosBase.ENTRENADOR1
        );

        turno = new Turno(
            carlos,
            "LUNES",
            LocalTime.of(8, 0),
            LocalTime.of(12, 0)
        );
    }

    @Test
    public void empleadoTieneTurnoRegistrado() {

        assertTrue(carlos.getTurnos().contains(turno));
        assertEquals("LUNES", turno.getDiaSemana());
        assertEquals(LocalTime.of(8, 0),
                turno.getHoraInicio());
    }

    @Test
    public void empleadoPuedeSolicitarCambioDeTurno() {

        SolicitudCambioTurno solicitud =
            club.solicitarCambioTurno(
                carlos.getCodigo(),
                turno,
                "Cita medica",
                "MARTES",
                LocalTime.of(9, 0),
                LocalTime.of(13, 0)
            );

        assertNotNull(solicitud);
        assertEquals(
            EstadoSolicitud.PENDIENTE,
            solicitud.getEstado()
        );

        assertTrue(
            club.getSolicitudes().contains(solicitud)
        );
    }

    @Test
    public void solicitudNoModificaElTurnoOriginal() {

        club.solicitarCambioTurno(
            carlos.getCodigo(),
            turno,
            "Cambio personal",
            "MARTES",
            LocalTime.of(14, 0),
            LocalTime.of(18, 0)
        );

        assertEquals("LUNES", turno.getDiaSemana());
        assertEquals(
            LocalTime.of(8, 0),
            turno.getHoraInicio()
        );
        assertEquals(
            LocalTime.of(12, 0),
            turno.getHoraFin()
        );
    }

    @Test
    public void otroEmpleadoNoPuedeCambiarTurnoAjeno() {

        assertThrows(
            ReglaNegocioException.class,
            () -> club.solicitarCambioTurno(
                DatosBase.ENTRENADOR2,
                turno,
                "Cambio ajeno",
                "MARTES",
                LocalTime.of(9, 0),
                LocalTime.of(13, 0)
            )
        );

        assertTrue(club.getSolicitudes().isEmpty());
    }

    @Test
    public void socioNoPuedeSolicitarCambioDeTurno() {

        assertThrows(
            ReglaNegocioException.class,
            () -> club.solicitarCambioTurno(
                DatosBase.SOCIO1,
                turno,
                "Cambio",
                "MARTES",
                LocalTime.of(9, 0),
                LocalTime.of(13, 0)
            )
        );

        assertTrue(club.getSolicitudes().isEmpty());
    }

    @Test
    public void horarioInvalidoSeRechaza() {

        assertThrows(
            ReglaNegocioException.class,
            () -> club.solicitarCambioTurno(
                carlos.getCodigo(),
                turno,
                "Cambio",
                "MARTES",
                LocalTime.of(18, 0),
                LocalTime.of(9, 0)
            )
        );

        assertTrue(club.getSolicitudes().isEmpty());
    }

    @Test
    public void empleadoPuedeSugerirProducto() {

        int cantidadAnterior =
            carlos.getSugerencias().size();

        SugerenciaProducto sugerencia =
            club.sugerirProducto(
                carlos.getCodigo(),
                "Bebida deportiva",
                "Producto para deportistas"
            );

        assertNotNull(sugerencia);
        assertEquals(
            cantidadAnterior + 1,
            carlos.getSugerencias().size()
        );
        assertTrue(
            carlos.getSugerencias().contains(sugerencia)
        );
    }

    @Test
    public void socioNoPuedeSugerirProductos() {

        assertThrows(
            ReglaNegocioException.class,
            () -> club.sugerirProducto(
                DatosBase.SOCIO1,
                "Bebida",
                "Descripcion"
            )
        );
    }

    @Test
    public void sugerenciaSinNombreSeRechaza() {

        assertThrows(
            ReglaNegocioException.class,
            () -> club.sugerirProducto(
                carlos.getCodigo(),
                "",
                "Descripcion de prueba"
            )
        );
    }

    @Test
    public void credencialesIncorrectasSeRechazan() {

        Usuario usuario = club.iniciarSesion(
            "usuario_inexistente",
            "clave_incorrecta"
        );

        assertNull(usuario);
    }
}

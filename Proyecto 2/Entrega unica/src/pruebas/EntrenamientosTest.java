
package pruebas;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import consola.DatosBase;

import modelo.Asistencia;
import modelo.CanchaTenis;
import modelo.Club;
import modelo.Convocatoria;
import modelo.Instalacion;
import modelo.ReglaNegocioException;
import modelo.SesionEntrenamiento;
import modelo.Socio;

public class EntrenamientosTest {

    private Club club;
    private Instalacion cancha;
    private Socio sofia;
    private Socio juan;

    @BeforeEach
    public void preparar() {

        club = new Club();
        DatosBase.cargar(club);

        cancha = new CanchaTenis(
            "T-PRUEBA",
            "Cancha de prueba",
            false
        );

        club.getInstalaciones().add(cancha);

        sofia = (Socio) DatosBase.usuario(
            club, DatosBase.SOCIO1
        );

        juan = (Socio) DatosBase.usuario(
            club, DatosBase.SOCIO2
        );
    }

    private SesionEntrenamiento crearSesionFutura() {

        return club.organizarSesion(
            DatosBase.ENTRENADOR1,
            cancha,
            LocalDate.now().plusDays(3),
            LocalTime.of(10, 0),
            60,
            Arrays.asList(sofia, juan)
        );
    }

    private SesionEntrenamiento crearSesionFinalizada() {

        return club.organizarSesion(
            DatosBase.ENTRENADOR1,
            cancha,
            LocalDate.now().minusDays(3),
            LocalTime.of(10, 0),
            60,
            Arrays.asList(sofia, juan)
        );
    }

    @Test
    public void entrenadorPuedeOrganizarSesion() {

        SesionEntrenamiento sesion =
            crearSesionFutura();

        assertNotNull(sesion);

        assertTrue(
            club.getSesiones().contains(sesion)
        );

        assertEquals(
            DatosBase.ENTRENADOR1,
            sesion.getEntrenador().getCodigo()
        );

        assertEquals(cancha, sesion.getInstalacion());

        assertEquals(2, sesion.getConvocatorias().size());
    }

    @Test
    public void convocatoriasPertenecenALosSociosElegidos() {

        SesionEntrenamiento sesion =
            crearSesionFutura();

        assertNotNull(sesion.buscarConvocatoria(sofia));
        assertNotNull(sesion.buscarConvocatoria(juan));

        assertEquals(
            2,
            sesion.getConvocatorias().size()
        );
    }

    @Test
    public void otroUsuarioNoPuedeOrganizarSesion() {

        assertThrows(
            ReglaNegocioException.class,
            () -> club.organizarSesion(
                DatosBase.SOCIO1,
                cancha,
                LocalDate.now().plusDays(3),
                LocalTime.of(10, 0),
                60,
                Arrays.asList(sofia)
            )
        );

        assertTrue(club.getSesiones().isEmpty());
    }

    @Test
    public void sesionSinConvocadosSeRechaza() {

        assertThrows(
            ReglaNegocioException.class,
            () -> club.organizarSesion(
                DatosBase.ENTRENADOR1,
                cancha,
                LocalDate.now().plusDays(3),
                LocalTime.of(10, 0),
                60,
                List.of()
            )
        );

        assertTrue(club.getSesiones().isEmpty());
    }

    @Test
    public void duracionInvalidaSeRechaza() {

        assertThrows(
            ReglaNegocioException.class,
            () -> club.organizarSesion(
                DatosBase.ENTRENADOR1,
                cancha,
                LocalDate.now().plusDays(3),
                LocalTime.of(10, 0),
                0,
                Arrays.asList(sofia)
            )
        );
    }

    @Test
    public void noSePermitenSesionesSuperpuestas() {

        crearSesionFutura();

        assertThrows(
            ReglaNegocioException.class,
            () -> club.organizarSesion(
                DatosBase.ENTRENADOR1,
                cancha,
                LocalDate.now().plusDays(3),
                LocalTime.of(10, 30),
                60,
                Arrays.asList(sofia)
            )
        );

        assertEquals(1, club.getSesiones().size());
    }

    @Test
    public void entrenadorPuedeRegistrarAsistencia() {

        SesionEntrenamiento sesion =
            crearSesionFinalizada();

        club.registrarAsistencia(
            DatosBase.ENTRENADOR1,
            sesion,
            sofia,
            Asistencia.ASISTIO,
            0,
            null
        );

        Convocatoria convocatoria =
            sesion.buscarConvocatoria(sofia);

        assertEquals(
            Asistencia.ASISTIO,
            convocatoria.getAsistencia()
        );
    }

    @Test
    public void entrenadorPuedeRegistrarEvaluacion() {

        SesionEntrenamiento sesion =
            crearSesionFinalizada();

        club.registrarAsistencia(
            DatosBase.ENTRENADOR1,
            sesion,
            sofia,
            Asistencia.ASISTIO,
            5,
            "Excelente desempeño"
        );

        Convocatoria convocatoria =
            sesion.buscarConvocatoria(sofia);

        assertEquals(
            Asistencia.ASISTIO,
            convocatoria.getAsistencia()
        );

        assertEquals(5, convocatoria.getNota());

        assertEquals(
            "Excelente desempeño",
            convocatoria.getComentario()
        );
    }

    @Test
    public void noSeRegistraAsistenciaAntesDeTerminar() {

        SesionEntrenamiento sesion =
            crearSesionFutura();

        assertThrows(
            ReglaNegocioException.class,
            () -> club.registrarAsistencia(
                DatosBase.ENTRENADOR1,
                sesion,
                sofia,
                Asistencia.ASISTIO,
                5,
                "Evaluacion anticipada"
            )
        );

        assertNull(
            sesion.buscarConvocatoria(sofia).getAsistencia()
        );
    }

    @Test
    public void otroEntrenadorNoPuedeRegistrarAsistencia() {

        SesionEntrenamiento sesion =
            crearSesionFinalizada();

        assertThrows(
            ReglaNegocioException.class,
            () -> club.registrarAsistencia(
                DatosBase.ENTRENADOR2,
                sesion,
                sofia,
                Asistencia.ASISTIO,
                4,
                "Evaluacion"
            )
        );
    }

    @Test
    public void socioNoConvocadoNoRecibeAsistencia() {

        SesionEntrenamiento sesion =
            crearSesionFinalizada();

        Socio maria = (Socio) DatosBase.usuario(
            club, DatosBase.SOCIO3
        );

        assertThrows(
            ReglaNegocioException.class,
            () -> club.registrarAsistencia(
                DatosBase.ENTRENADOR1,
                sesion,
                maria,
                Asistencia.ASISTIO,
                3,
                "Evaluacion"
            )
        );
    }

    @Test
    public void notaFueraDeRangoSeRechaza() {

        SesionEntrenamiento sesion =
            crearSesionFinalizada();

        assertThrows(
            ReglaNegocioException.class,
            () -> club.registrarAsistencia(
                DatosBase.ENTRENADOR1,
                sesion,
                sofia,
                Asistencia.ASISTIO,
                6,
                "Nota invalida"
            )
        );
    }

    @Test
    public void socioPuedeConsultarSusConvocatorias() {

        SesionEntrenamiento sesion =
            crearSesionFutura();

        List<Convocatoria> convocatorias =
            club.consultarConvocatorias(sofia);

        assertEquals(1, convocatorias.size());

        assertEquals(
            sesion,
            convocatorias.get(0).getSesion()
        );
    }
}

package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import consola.DatosBase;
import consola.DatosTienda;
import modelo.Asistencia;
import modelo.CanchaTenis;
import modelo.Club;
import modelo.Deporte;
import modelo.InscripcionDisciplina;
import modelo.Mensualidad;
import modelo.Modalidad;
import modelo.Nivel;
import modelo.PartidoPractica;
import modelo.Producto;
import modelo.ReglaNegocioException;
import modelo.SesionEntrenamiento;
import modelo.Socio;
import modelo.UbicacionInventario;

/**
 * Pruebas de las funciones del administrador que se usan desde su consola:
 * inventario (RF-35, RF-36) y reportes (RF-40, RF-41, RF-42).
 */
class AdministracionTest {

    private Club club;
    private Socio sofia;
    private Socio juan;
    private LocalDate inicioDeAnio;
    private LocalDate finDeAnio;

    @BeforeEach
    void setUp() {
        club = new Club();
        DatosBase.cargar(club);
        DatosTienda.cargar(club);
        sofia = (Socio) DatosBase.usuario(club, DatosBase.SOCIO1);
        juan = (Socio) DatosBase.usuario(club, DatosBase.SOCIO2);
        inicioDeAnio = LocalDate.now().minusMonths(6);
        finDeAnio = LocalDate.now().plusMonths(6);
    }

    private int cantidad(String ubicacion, Producto producto) {
        for (UbicacionInventario u : club.getUbicaciones()) {
            if (u.getNombre().equals(ubicacion)) {
                return u.buscarExistencia(producto) == null ? 0 : u.buscarExistencia(producto).getCantidad();
            }
        }
        return -1;
    }

    // RF-35: reabastecer

    @Test
    void reabastecerAumentaLasExistenciasDeLaTienda() {
        club.reabastecer(DatosBase.ADMIN, DatosTienda.balon, 5);
        assertEquals(15, cantidad("Tienda", DatosTienda.balon));
    }

    @Test
    void reabastecerConCantidadCeroSeRechaza() {
        assertThrows(ReglaNegocioException.class, () -> club.reabastecer(DatosBase.ADMIN, DatosTienda.balon, 0));
        assertEquals(10, cantidad("Tienda", DatosTienda.balon));
    }

    @Test
    void soloElAdministradorPuedeReabastecer() {
        assertThrows(ReglaNegocioException.class,
                () -> club.reabastecer(DatosBase.ENTRENADOR1, DatosTienda.balon, 5));
    }

    // RF-36: mover articulos entre ubicaciones

    @Test
    void moverArticuloDescuentaEnOrigenYSumaEnDestino() {
        club.moverArticulo(DatosBase.ADMIN, DatosTienda.balon, "Bodega", "Tienda", 4);
        assertEquals(16, cantidad("Bodega", DatosTienda.balon));
        assertEquals(14, cantidad("Tienda", DatosTienda.balon));
    }

    @Test
    void moverMasDeLoQueHaySeRechazaSinCambiarNada() {
        assertThrows(ReglaNegocioException.class,
                () -> club.moverArticulo(DatosBase.ADMIN, DatosTienda.raqueta, "Tienda", "Bodega", 3));
        assertEquals(2, cantidad("Tienda", DatosTienda.raqueta));
        assertEquals(0, cantidad("Bodega", DatosTienda.raqueta));
    }

    @Test
    void moverALaMismaUbicacionOAUnaQueNoExisteSeRechaza() {
        assertThrows(ReglaNegocioException.class,
                () -> club.moverArticulo(DatosBase.ADMIN, DatosTienda.balon, "Tienda", "Tienda", 1));
        assertThrows(ReglaNegocioException.class,
                () -> club.moverArticulo(DatosBase.ADMIN, DatosTienda.balon, "Tienda", "Parqueadero", 1));
    }

    // RF-40: reporte de ventas por rubro

    @Test
    void reporteDeVentasIncluyeLasMensualidadesDelPeriodo() {
        new Mensualidad(sofia, "2026-11", 120000, LocalDate.now());
        new Mensualidad(juan, "2026-11", 120000, LocalDate.now().minusDays(1));
        List<String> reporte = club.reporteVentas(DatosBase.ADMIN, inicioDeAnio, finDeAnio);
        assertTrue(reporte.contains("Mensualidades | pagos: 2 | total: 240000.0"), reporte.toString());
    }

    @Test
    void reporteDeVentasSinDatosAvisaQueNoHay() {
        assertThrows(ReglaNegocioException.class,
                () -> club.reporteVentas(DatosBase.ADMIN, inicioDeAnio, finDeAnio));
    }

    @Test
    void reporteConFechaFinalAntesDeLaInicialSeRechaza() {
        assertThrows(ReglaNegocioException.class,
                () -> club.reporteVentas(DatosBase.ADMIN, finDeAnio, inicioDeAnio));
        assertThrows(ReglaNegocioException.class, () -> club.reporteAsistencia(finDeAnio, inicioDeAnio));
    }

    // RF-41: reportes deportivos

    @Test
    void reporteDeAsistenciaCuentaLoQueRegistroElEntrenador() {
        CanchaTenis cancha = new CanchaTenis("T-ADM", "Cancha admin", false);
        club.getInstalaciones().add(cancha);
        SesionEntrenamiento sesion = club.organizarSesion(DatosBase.ENTRENADOR1, cancha,
                LocalDate.now().minusDays(2), LocalTime.of(10, 0), 60, Arrays.asList(sofia, juan));
        club.registrarAsistencia(DatosBase.ENTRENADOR1, sesion, sofia, Asistencia.ASISTIO, 5, "bien");
        club.registrarAsistencia(DatosBase.ENTRENADOR1, sesion, juan, Asistencia.FALTO, 1, "no vino");

        List<String> reporte = club.reporteAsistencia(inicioDeAnio, finDeAnio);
        assertEquals(1, reporte.size());
        assertTrue(reporte.get(0).contains("asistio: 1 | falto: 1"), reporte.get(0));
    }

    @Test
    void reporteDeResultadosSinPartidosAvisaQueNoHay() {
        assertThrows(ReglaNegocioException.class, () -> club.reporteResultados(inicioDeAnio, finDeAnio));
    }

    // RF-42: evolucion del ranking

    @Test
    void reporteDeRankingMuestraElCambioDeEloDelGanador() {
        InscripcionDisciplina tenisSofia = club.inscribirEnAcademia(sofia.getCodigo(), Deporte.TENIS, Nivel.INTERMEDIO);
        InscripcionDisciplina tenisJuan = club.inscribirEnAcademia(juan.getCodigo(), Deporte.TENIS, Nivel.INTERMEDIO);
        List<InscripcionDisciplina> participantes = new ArrayList<>();
        participantes.add(tenisSofia);
        participantes.add(tenisJuan);
        PartidoPractica partido = club.programarPractica(participantes, Modalidad.SENCILLOS,
                LocalDate.now().minusDays(1));
        int antes = tenisSofia.getPuntajeRanking();

        Map<InscripcionDisciplina, Map<String, Integer>> estadisticas = new HashMap<>();
        estadisticas.put(tenisSofia, datosTenis(2, 12, 3, 2));
        estadisticas.put(tenisJuan, datosTenis(0, 5, 1, 4));
        club.registrarPractica(partido, estadisticas);

        List<String> reporte = club.reporteEvolucionRanking(Deporte.TENIS, inicioDeAnio, finDeAnio);
        String esperado = sofia.getNombre() + " | " + antes + " -> " + tenisSofia.getPuntajeRanking();
        boolean aparece = false;
        for (String linea : reporte) {
            if (linea.endsWith(esperado)) {
                aparece = true;
            }
        }
        assertTrue(aparece, reporte.toString());
    }

    private Map<String, Integer> datosTenis(int sets, int games, int aces, int errores) {
        Map<String, Integer> datos = new HashMap<>();
        datos.put("sets", sets);
        datos.put("games", games);
        datos.put("aces", aces);
        datos.put("erroresNoForzados", errores);
        return datos;
    }
}


package pruebas;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import consola.DatosBase;
import consola.DatosTienda;

import modelo.ArticuloTienda;
import modelo.Club;
import modelo.Producto;
import modelo.ReglaNegocioException;
import modelo.VentaCafeteria;
import modelo.VentaTienda;

public class ComprasEmpleadoTest {

    private Club club;

    @BeforeEach
    public void preparar() {
        club = new Club();
        DatosBase.cargar(club);
        DatosTienda.cargar(club);
    }

    private Map<ArticuloTienda, Integer> comprarBalon() {
        Map<ArticuloTienda, Integer> compra =
                new LinkedHashMap<>();

        compra.put(DatosTienda.balon, 1);
        return compra;
    }

    private Map<Producto, Integer> comprarGalleta() {
        Map<Producto, Integer> compra =
                new LinkedHashMap<>();

        compra.put(DatosTienda.galleta, 1);
        return compra;
    }

    @Test
    public void empleadoRecibe15PorCientoEnTienda() {

        VentaTienda venta = club.comprarTienda(
                DatosBase.ENTRENADOR1,
                comprarBalon(),
                "DESC-CARLOS"
        );

        assertEquals(7500, venta.getDescuento(), 0.01);
        assertEquals(8075, venta.getIva(), 0.01);
        assertEquals(50575, venta.getTotal(), 0.01);
        assertEquals(1, club.getVentas().size());
    }

    @Test
    public void socioRecibe8PorCientoConCodigoCompartido() {

        VentaTienda venta = club.comprarTienda(
                DatosBase.SOCIO1,
                comprarBalon(),
                "DESC-CARLOS"
        );

        assertEquals(4000, venta.getDescuento(), 0.01);
        assertEquals(8740, venta.getIva(), 0.01);
        assertEquals(54740, venta.getTotal(), 0.01);
    }

    @Test
    public void empleadoRecibe15PorCientoEnCafeteria() {

        VentaCafeteria venta = club.comprarCafeteria(
                DatosBase.ENTRENADOR1,
                comprarGalleta(),
                0,
                0,
                "DESC-CARLOS"
        );

        assertEquals(600, venta.getDescuentoBono(), 0.01);
        assertEquals(272, venta.getImpuestoConsumo(), 0.01);
        assertEquals(3672, venta.getTotal(), 0.01);
    }

    @Test
    public void socioRecibe8PorCientoEnCafeteria() {

        VentaCafeteria venta = club.comprarCafeteria(
                DatosBase.SOCIO1,
                comprarGalleta(),
                0,
                0,
                "DESC-CARLOS"
        );

        assertEquals(320, venta.getDescuentoBono(), 0.01);
        assertEquals(294.40, venta.getImpuestoConsumo(), 0.01);
        assertEquals(3974.40, venta.getTotal(), 0.01);
    }

    @Test
    public void codigoInexistenteSeRechaza() {

        assertThrows(ReglaNegocioException.class, () ->
                club.comprarTienda(
                        DatosBase.SOCIO1,
                        comprarBalon(),
                        "CODIGO-INVALIDO"
                )
        );

        assertTrue(club.getVentas().isEmpty());
    }

    @Test
    public void compraSinCodigoNoTieneDescuento() {

        VentaTienda venta = club.comprarTienda(
                DatosBase.SOCIO1,
                comprarBalon(),
                ""
        );

        assertEquals(0, venta.getDescuento(), 0.01);
        assertEquals(59500, venta.getTotal(), 0.01);
    }
}

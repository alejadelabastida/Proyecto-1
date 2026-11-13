package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import consola.DatosBase;
import consola.DatosTienda;
import modelo.ArticuloTienda;
import modelo.Bono;
import modelo.Club;
import modelo.Deporte;
import modelo.Modalidad;
import modelo.Nivel;
import modelo.Producto;
import modelo.ReglaNegocioException;
import modelo.Socio;
import modelo.TipoTorneo;
import modelo.Torneo;
import modelo.VentaCafeteria;
import modelo.VentaTienda;

public class BonosTest {

    private Club club;
    private Bono bono;
    private Bono bonoDeOtro;

    @BeforeEach
    public void preparar() {
        club = new Club();
        DatosBase.cargar(club);
        DatosTienda.cargar(club);
        Torneo torneo = new Torneo("Amistoso", TipoTorneo.RECREATIVO, Deporte.TENIS, null, Nivel.INTERMEDIO,
                Modalidad.SENCILLOS, 8, 0, LocalDate.now().plusDays(30));
        club.getTorneos().add(torneo);
        bono = new Bono((Socio) DatosBase.usuario(club, DatosBase.SOCIO1), torneo, 15);
        bonoDeOtro = new Bono((Socio) DatosBase.usuario(club, DatosBase.SOCIO2), torneo, 15);
        club.getBonos().add(bono);
        club.getBonos().add(bonoDeOtro);
    }

    private Map<ArticuloTienda, Integer> unBalon() {
        Map<ArticuloTienda, Integer> compra = new LinkedHashMap<>();
        compra.put(DatosTienda.balon, 1);
        return compra;
    }

    private Map<Producto, Integer> unCafeYUnaGalleta() {
        Map<Producto, Integer> compra = new LinkedHashMap<>();
        compra.put(DatosTienda.agua, 1);
        compra.put(DatosTienda.galleta, 1);
        return compra;
    }

    @Test
    public void elSocioVeSusBonosYNoLosDeOtros() {
        assertEquals(1, club.consultarBonos(DatosBase.SOCIO1).size());
        assertEquals(bono, club.consultarBonos(DatosBase.SOCIO1).get(0));
    }

    @Test
    public void bonoEnLaTiendaDescuentaElPorcentajeYQuedaUsado() {
        VentaTienda venta = club.comprarTiendaConBono(DatosBase.SOCIO1, unBalon(), bono);
        assertEquals(7500, venta.getDescuento()); 
        assertEquals(50000 - 7500 + 8075, venta.getTotal()); 
        assertTrue(bono.isRedimido());
    }

    @Test
    public void bonoUsadoNoSePuedeUsarOtraVez() {
        club.comprarTiendaConBono(DatosBase.SOCIO1, unBalon(), bono);
        assertThrows(ReglaNegocioException.class, () -> club.comprarTiendaConBono(DatosBase.SOCIO1, unBalon(), bono));
    }

    @Test
    public void bonoDeOtroSocioSeRechazaYNoCambiaNada() {
        assertThrows(ReglaNegocioException.class,
                () -> club.comprarTiendaConBono(DatosBase.SOCIO1, unBalon(), bonoDeOtro));
        assertFalse(bonoDeOtro.isRedimido());
        assertTrue(club.getVentas().isEmpty());
    }

    @Test
    public void bonoSeUsaEnLaCafeteria() {
        VentaCafeteria venta = club.comprarCafeteriaConBono(DatosBase.SOCIO1, unCafeYUnaGalleta(), 0.10, bono);
        assertEquals(1050, venta.getDescuentoBono());
        assertTrue(bono.isRedimido());
    }

    @Test
    public void unBonoGastadoEnLaTiendaNoSirveEnLaCafeteria() {
        club.comprarTiendaConBono(DatosBase.SOCIO1, unBalon(), bono);
        assertThrows(ReglaNegocioException.class,
                () -> club.comprarCafeteriaConBono(DatosBase.SOCIO1, unCafeYUnaGalleta(), 0.10, bono));
    }

    @Test
    public void unEmpleadoNoPuedeUsarBonos() {
        assertThrows(ReglaNegocioException.class,
                () -> club.comprarTiendaConBono(DatosBase.ENTRENADOR1, unBalon(), bono));
    }

    @Test
    public void sinStockLaCompraConBonoSeRechazaYElBonoSigueDisponible() {
        Map<ArticuloTienda, Integer> demasiados = new LinkedHashMap<>();
        demasiados.put(DatosTienda.raqueta, 50);
        assertThrows(ReglaNegocioException.class, () -> club.comprarTiendaConBono(DatosBase.SOCIO1, demasiados, bono));
        assertFalse(bono.isRedimido());
    }

    @Test
    public void inscribirseEnUnaAcademiaDaEloMilYNoSeRepite() {
        club.inscribirEnAcademia(DatosBase.SOCIO3, Deporte.TENIS, Nivel.AVANZADO);
        assertEquals(1000, ((Socio) DatosBase.usuario(club, DatosBase.SOCIO3)).getInscripciones().get(0).getPuntajeRanking());
        assertThrows(ReglaNegocioException.class,
                () -> club.inscribirEnAcademia(DatosBase.SOCIO3, Deporte.TENIS, Nivel.PRINCIPIANTE));
        club.inscribirEnAcademia(DatosBase.SOCIO3, Deporte.PADEL, Nivel.AVANZADO);
        assertEquals(2, ((Socio) DatosBase.usuario(club, DatosBase.SOCIO3)).getInscripciones().size());
    }
}

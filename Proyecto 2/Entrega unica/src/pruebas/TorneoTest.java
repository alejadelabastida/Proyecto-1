package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import modelo.Deporte;
import modelo.EstadoTorneo;
import modelo.Modalidad;
import modelo.Nivel;
import modelo.InscripcionTorneo;
import modelo.Socio;
import modelo.Usuario;
import java.util.ArrayList;
import java.util.List;
import modelo.ReglaNegocioException;
import modelo.TipoCupo;
import modelo.TipoTorneo;
import modelo.Torneo;

public class TorneoTest {

    private Torneo crearTorneo(int cupos) {
        return new Torneo("Copa", TipoTorneo.COMPETITIVO, Deporte.TENIS, null, Nivel.INTERMEDIO,
                Modalidad.SENCILLOS, cupos, 30000, LocalDate.of(2026, 11, 9));
    }
    
    private void ocuparCupo(Torneo torneo, TipoCupo tipo, int numero) {
        List<Usuario> lista = new ArrayList<>();
        lista.add(new Socio(900 + numero, "Socio" + numero, "Prueba", "socio" + numero, "1234",
                LocalDate.of(2000, 1, 1)));
        new InscripcionTorneo(torneo, lista, tipo);
    }

    @Test
    public void cuposPreferencialesSonElVeintePorCientoRedondeadoHaciaArriba() {
        assertEquals(1, crearTorneo(1).cuposPreferenciales());
        assertEquals(1, crearTorneo(5).cuposPreferenciales());
        assertEquals(2, crearTorneo(6).cuposPreferenciales());
        assertEquals(2, crearTorneo(10).cuposPreferenciales());
        assertEquals(3, crearTorneo(12).cuposPreferenciales());
    }

    @Test
    public void cuposRegularesSonLosQueSobran() {
        Torneo torneo = crearTorneo(10);
        assertEquals(8, torneo.cuposRegulares());
    }

    @Test
    public void elMejorRankingTomaPreferenteYLosDemasRegular() {
        Torneo torneo = crearTorneo(5);
        assertEquals(TipoCupo.PREFERENTE, torneo.asignarCupo(true));
        assertEquals(TipoCupo.REGULAR, torneo.asignarCupo(false));
    }

    @Test
    public void sinLiberarLosNoRankeadosNoUsanPreferentes() {
        Torneo torneo = crearTorneo(2);
        ocuparCupo(torneo, torneo.asignarCupo(false), 1);
        assertThrows(ReglaNegocioException.class, () -> torneo.asignarCupo(false));
    }

    @Test
    public void alLiberarLosPreferentesSeUsanComoRegulares() {
        Torneo torneo = crearTorneo(2);
        ocuparCupo(torneo, torneo.asignarCupo(false), 1);
        torneo.liberarCuposPreferenciales();
        assertTrue(torneo.getPreferencialesLiberados());
        assertEquals(TipoCupo.REGULAR, torneo.asignarCupo(false));
    }

    @Test
    public void sinCuposLibresSeRechaza() {
        Torneo torneo = crearTorneo(1);
        ocuparCupo(torneo, torneo.asignarCupo(true), 1);
        assertThrows(ReglaNegocioException.class, () -> torneo.asignarCupo(true));
    }

    @Test
    public void iniciarLiberaPreferentesYCierraCambios() {
        Torneo torneo = crearTorneo(5);
        assertTrue(torneo.admiteCambios());
        torneo.iniciar();
        assertEquals(EstadoTorneo.EN_CURSO, torneo.getEstado());
        assertTrue(torneo.getPreferencialesLiberados());
        assertFalse(torneo.admiteCambios());
    }

    @Test
    public void torneoRecreativoNoPuedeTenerTarifa() {
        assertThrows(IllegalArgumentException.class, () -> new Torneo("Rec", TipoTorneo.RECREATIVO,
                Deporte.TENIS, null, Nivel.PRINCIPIANTE, Modalidad.SENCILLOS, 8, 5000, LocalDate.of(2026, 11, 9)));
    }

    @Test
    public void padelSoloPuedeSerDobles() {
        assertThrows(IllegalArgumentException.class, () -> new Torneo("Padel", TipoTorneo.COMPETITIVO,
                Deporte.PADEL, null, Nivel.PRINCIPIANTE, Modalidad.SENCILLOS, 8, 5000, LocalDate.of(2026, 11, 9)));
    }

    @Test
    public void elDiaDeLaSemanaSeDaEnEspanol() {
        assertEquals("LUNES", crearTorneo(4).getDiaSemana());
    }
}

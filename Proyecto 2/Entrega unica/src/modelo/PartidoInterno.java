
package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PartidoInterno extends PartidoEquipo {

    private static final long serialVersionUID = 1L;

    private int codigoEntrenadorResponsable;
    private List<Jugador> alineacionLocal;
    private List<Jugador> alineacionVisitante;

    public PartidoInterno(LocalDate fecha, Deporte deporte,
            Equipo equipo1, Equipo equipo2) {
        super(fecha, deporte, equipo1, equipo2);
    }

    public PartidoInterno(LocalDate fecha, Deporte deporte,
            Equipo equipo1, Equipo equipo2, int codigoEntrenadorResponsable) {
        this(fecha, deporte, equipo1, equipo2);
        this.codigoEntrenadorResponsable = codigoEntrenadorResponsable;
    }

    public int getCodigoEntrenadorResponsable() {
        return codigoEntrenadorResponsable;
    }

    public List<Jugador> getAlineacionLocal() {
        return alineacionLocal == null
                ? new ArrayList<>()
                : new ArrayList<>(alineacionLocal);
    }

    public List<Jugador> getAlineacionVisitante() {
        return alineacionVisitante == null
                ? new ArrayList<>()
                : new ArrayList<>(alineacionVisitante);
    }

    public boolean tieneAlineacion(Equipo equipo) {
        if (equipo == getEquipo1()) return alineacionLocal != null;
        if (equipo == getEquipo2()) return alineacionVisitante != null;
        throw new IllegalArgumentException("Equipo no participa");
    }

    public boolean tieneAmbasAlineaciones() {
        return alineacionLocal != null && alineacionVisitante != null;
    }

    public void definirAlineacion(Equipo equipo, List<Jugador> jugadores) {
        if (jugadores == null) {
            throw new IllegalArgumentException("Alineacion inexistente");
        }

        if (equipo == getEquipo1()) {
            alineacionLocal = new ArrayList<>(jugadores);
        } else if (equipo == getEquipo2()) {
            alineacionVisitante = new ArrayList<>(jugadores);
        } else {
            throw new IllegalArgumentException("Equipo no participa");
        }
    }
}

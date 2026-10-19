package modelo;

import java.time.LocalDate;

public class PartidoInterno extends PartidoEquipo {
    public PartidoInterno(LocalDate fecha, Deporte deporte, Equipo equipo1, Equipo equipo2) {
        super(fecha, deporte, equipo1, equipo2);
    }
}

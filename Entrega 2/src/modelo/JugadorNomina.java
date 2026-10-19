package modelo;

public class JugadorNomina  implements java.io.Serializable {
    private final Jugador jugador;
    private final InscripcionEquipo inscripcion;
    public JugadorNomina(Jugador jugador, InscripcionEquipo inscripcion) {
        if (jugador == null || inscripcion == null) {
            throw new IllegalArgumentException("Datos invalidos");
        }
        this.jugador = jugador;
        this.inscripcion = inscripcion;
    }

    public Jugador getJugador() {
        return jugador;
    }

    public InscripcionEquipo getInscripcion() {
        return inscripcion;
    }
}

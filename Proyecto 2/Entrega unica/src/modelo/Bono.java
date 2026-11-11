package modelo;

import java.io.Serializable;

public class Bono implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Socio dueno;
    private final Torneo torneoOrigen;
    private final double porcentaje;
    private boolean redimido;

    public Bono(Socio dueno, Torneo torneoOrigen, double porcentaje) {
        if (dueno == null || torneoOrigen == null || porcentaje <= 0 || porcentaje > 100) {
            throw new IllegalArgumentException("Datos del bono invalidos");
        }
        this.dueno = dueno;
        this.torneoOrigen = torneoOrigen;
        this.porcentaje = porcentaje;
        this.redimido = false;
    }
    public void redimir() {
        if (this.redimido) {
            throw new ReglaNegocioException("El bono ya fue redimido");
        }
        this.redimido = true;
    }

    public Socio getDueno() { return dueno; }
    public Torneo getTorneoOrigen() { return torneoOrigen; }
    public double getPorcentaje() { return porcentaje; }
    public boolean isRedimido() { return redimido; }
}
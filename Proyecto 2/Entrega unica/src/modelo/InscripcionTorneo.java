package modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InscripcionTorneo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Torneo torneo;
    private final List<Usuario> participantes;   
    private final Equipo equipo;
    private final TipoCupo tipoCupo;
    private final double tarifaCobrada;
    private final LocalDate fecha;

    public InscripcionTorneo(Torneo torneo, List<Usuario> participantes, TipoCupo tipoCupo) {
        this.torneo = torneo;
        this.participantes = new ArrayList<>(participantes);
        this.equipo = null;
        this.tipoCupo = tipoCupo;
        this.tarifaCobrada = calcularTarifa();
        this.fecha = LocalDate.now();
        torneo.agregarInscripcion(this);
    }

    public InscripcionTorneo(Torneo torneo, Equipo equipo, TipoCupo tipoCupo) {
        this.torneo = torneo;
        this.participantes = new ArrayList<>();
        this.equipo = equipo;
        this.tipoCupo = tipoCupo;
        this.tarifaCobrada = calcularTarifa();
        this.fecha = LocalDate.now();
        torneo.agregarInscripcion(this);
    }

    private double calcularTarifa() {
        if (torneo.getTipo() == TipoTorneo.RECREATIVO) {
            return 0;
        }
        if (equipo != null) {
            return torneo.getTarifa();
        }
        double total = 0;
        for (Usuario participante : participantes) {
            if (!(participante instanceof Empleado)) {
                total += torneo.getTarifa();
            }
        }
        return total;
    }

    public boolean incluye(Usuario usuario) {
        return participantes.contains(usuario);
    }

    public boolean recibePremioEfectivo(Usuario usuario) {
        return torneo.getTipo() == TipoTorneo.COMPETITIVO && participantes.contains(usuario)
                && !(usuario instanceof Empleado);
    }

    public String getNombreParticipantes() {
        if (equipo != null) {
            return equipo.getNombre();
        }
        String nombres = "";
        for (Usuario participante : participantes) {
            if (!nombres.isEmpty()) {
                nombres += " y ";
            }
            nombres += participante.getNombre() + " " + participante.getApellido();
        }
        return nombres;
    }

    public Torneo getTorneo() { return torneo; }
    public List<Usuario> getParticipantes() { return new ArrayList<>(participantes); }
    public Equipo getEquipo() { return equipo; }
    public TipoCupo getTipoCupo() { return tipoCupo; }
    public double getTarifaCobrada() { return tarifaCobrada; }
    public LocalDate getFecha() { return fecha; }

    @Override
    public String toString() {
        return getNombreParticipantes() + " | cupo " + tipoCupo + " | tarifa " + tarifaCobrada;
    }
}

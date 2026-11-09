package modelo;

import java.io.Serializable;

public class EncuentroTorneo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final InscripcionTorneo local;
    private final InscripcionTorneo visitante;
    private int marcadorLocal;
    private int marcadorVisitante;
    private boolean jugado;

    public EncuentroTorneo(InscripcionTorneo local, InscripcionTorneo visitante) {
        if (local == null || visitante == null||local == visitante) {
            throw new IllegalArgumentException("Un encuentro necesita dos participantes distintos");
        }
        this.local = local;
        this.visitante = visitante;
        this.jugado = false;
    }

	public InscripcionTorneo getLocal() {
		return local;
	}

	public InscripcionTorneo getVisitante() {
		return visitante;
	}

	public int getMarcadorLocal() {
		return marcadorLocal;
	}

	public int getMarcadorVisitante() {
		return marcadorVisitante;
	}

	public boolean isJugado() {
		return jugado;
	}
}

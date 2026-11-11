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
	public void registrarResultado(int marcadorLocal, int marcadorVisitante) throws ReglaNegocioException {
        if (this.jugado) {
            throw new ReglaNegocioException("El encuentro ya tiene resultado");
        }
        
        if (marcadorLocal < 0 || marcadorVisitante < 0) {
            throw new ReglaNegocioException("Los marcadores no pueden ser negativos");
        }
        
        this.marcadorLocal = marcadorLocal;
        this.marcadorVisitante = marcadorVisitante;
        this.jugado = true;
        }
    
    public InscripcionTorneo getGanador() {
        if (!this.jugado) {
            throw new ReglaNegocioException("El encuentro todavia no tiene resultado");
        }
        if (this.marcadorLocal > this.marcadorVisitante) {
            return this.local;
        } else if (this.marcadorLocal < this.marcadorVisitante) {
            return this.visitante;
        } else {
            return null;
        }
    }
}
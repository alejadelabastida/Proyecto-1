package modelo;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Torneo implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final int DIAS_DE_RESERVA = 3;
    private final String nombre;
    private final TipoTorneo tipo;
    private final Deporte deporte;
    private final Categoria categoria;  
    private final Nivel nivel;          
    private final Modalidad modalidad; 
    private final int cupos;
    private final double tarifa;   
    private final LocalDate fecha; 
    private EstadoTorneo estado;
    private boolean preferencialesLiberados;
    private final List<InscripcionTorneo> inscripciones = new ArrayList<>();
    private List<EncuentroTorneo> encuentros = new ArrayList<>();
    private Map<Usuario, Double> premiosEfectivo = new HashMap<>();
    public Torneo(String nombre, TipoTorneo tipo, Deporte deporte, Categoria categoria, Nivel nivel,
            Modalidad modalidad, int cupos, double tarifa, LocalDate fecha) {
        if (nombre == null || nombre.isBlank() || tipo == null || deporte == null || fecha == null) {
            throw new IllegalArgumentException("Faltan datos del torneo");
        }
        if (cupos <= 0) {
            throw new IllegalArgumentException("El torneo debe tener al menos un cupo");
        }
        if (tarifa < 0) {
            throw new IllegalArgumentException("La tarifa no puede ser negativa");
        }
        if (tipo == TipoTorneo.RECREATIVO && tarifa != 0) {
            throw new IllegalArgumentException("El torneo recreativo no cobra inscripcion");
        }
        if (esDeporteDeConjunto(deporte)) {
            if (categoria == null) {
                throw new IllegalArgumentException("El torneo de conjunto necesita una categoria");
            }
        } else {
            if (nivel == null || modalidad == null) {
                throw new IllegalArgumentException("El torneo individual necesita nivel y modalidad");
            }
            if (deporte == Deporte.PADEL && modalidad != Modalidad.DOBLES) {
                throw new IllegalArgumentException("El padel solo se juega en dobles");
            }
        }
        this.nombre = nombre;
        this.tipo = tipo;
        this.deporte = deporte;
        this.categoria = categoria;
        this.nivel = nivel;
        this.modalidad = modalidad;
        this.cupos = cupos;
        this.tarifa = tarifa;
        this.fecha = fecha;
        this.estado = EstadoTorneo.ABIERTO;
        this.preferencialesLiberados = false;
    }

    private static boolean esDeporteDeConjunto(Deporte deporte) {
        return deporte == Deporte.FUTBOL || deporte == Deporte.BALONCESTO || deporte == Deporte.VOLEIBOL;
    }

    public boolean esDeConjunto() {
        return esDeporteDeConjunto(deporte);
    }

    public boolean esIndividual() {
        return !esDeConjunto();
    }

    public int cuposPreferenciales() {
        return (cupos + 4) / 5;
    }

    public int cuposRegulares() {
        return cupos - cuposPreferenciales();
    }

    public int contarCupos(TipoCupo tipoCupo) {
        int total = 0;
        for (InscripcionTorneo inscripcion : inscripciones) {
            if (inscripcion.getTipoCupo() == tipoCupo) {
                total++;
            }
        }
        return total;
    }

    public int cuposOcupados() {
        return inscripciones.size();
    }

    public int cuposLibres() {
        return cupos - inscripciones.size();
    }

    public TipoCupo asignarCupo(boolean esMejorRanking) {
        if (preferencialesLiberados) {
            if (cuposOcupados() >= cupos) {
                throw new ReglaNegocioException("El torneo no tiene cupos disponibles");
            }
            return TipoCupo.REGULAR;
        }
        boolean hayPreferente = contarCupos(TipoCupo.PREFERENTE) < cuposPreferenciales();
        boolean hayRegular = contarCupos(TipoCupo.REGULAR) < cuposRegulares();

        if (esMejorRanking && hayPreferente) {
            return TipoCupo.PREFERENTE;
        }
        if (hayRegular) {
            return TipoCupo.REGULAR;
        }
        if (hayPreferente) {
            throw new ReglaNegocioException(
                    "Solo quedan cupos preferenciales, reservados para los mejores del ranking");
        }
        throw new ReglaNegocioException("El torneo no tiene cupos disponibles");
    }

    public void liberarCuposPreferenciales() {
        if (preferencialesLiberados) {
            throw new ReglaNegocioException("Los cupos preferenciales ya fueron liberados");
        }
        preferencialesLiberados = true;
    }
    
    public void liberarSiCorresponde(LocalDate hoy) {
        if (estado == EstadoTorneo.ABIERTO && !preferencialesLiberados
                && !hoy.isBefore(fecha.minusDays(DIAS_DE_RESERVA))) {
            preferencialesLiberados = true;
        }
    }
    
    public LocalDate getFechaLiberacion() {
        return fecha.minusDays(DIAS_DE_RESERVA);
    }

    public boolean getPreferencialesLiberados() {
        return preferencialesLiberados;
    }

    public void agregarInscripcion(InscripcionTorneo inscripcion) {
        inscripciones.add(inscripcion);
    }
    
    public void quitarInscripcion(InscripcionTorneo inscripcion) {
        inscripciones.remove(inscripcion);
    }

    public InscripcionTorneo buscarInscripcionDe(Usuario usuario) {
        for (InscripcionTorneo inscripcion : inscripciones) {
            if (inscripcion.incluye(usuario)) {
                return inscripcion;
            }
        }
        return null;
    }

    public InscripcionTorneo buscarInscripcionDe(Equipo equipo) {
        for (InscripcionTorneo inscripcion : inscripciones) {
            if (inscripcion.getEquipo() == equipo) {
                return inscripcion;
            }
        }
        return null;
    }

    public double totalRecaudado() {
        double total = 0;
        for (InscripcionTorneo inscripcion : inscripciones) {
            total += inscripcion.getTarifaCobrada();
        }
        return total;
    }

    public boolean admiteCambios() {
        return estado == EstadoTorneo.ABIERTO;
    }

    public void iniciar() {
        if (estado != EstadoTorneo.ABIERTO) {
            throw new ReglaNegocioException("El torneo ya inicio");
        }
        preferencialesLiberados = true;
        estado = EstadoTorneo.EN_CURSO;
        generarEncuentros();
    }

    public void finalizar() {
        if (estado != EstadoTorneo.EN_CURSO) {
            throw new ReglaNegocioException("Solo se puede finalizar un torneo en curso");
        }
        estado = EstadoTorneo.FINALIZADO;
    }
    private void generarEncuentros() {
        if (encuentros == null) {
            encuentros = new ArrayList<>();
        }
        for (int i = 0; i < inscripciones.size(); i++) {
            for (int j = i + 1; j < inscripciones.size(); j++) {
                encuentros.add(new EncuentroTorneo(inscripciones.get(i), inscripciones.get(j)));
            }
        }

    }
    public int puntosDe(InscripcionTorneo participante) {
        int puntos = 0;
        for (EncuentroTorneo encuentro : getEncuentros()) {
            if (!encuentro.isJugado()) {
                continue;
            }
            if (!encuentro.getLocal().equals(participante) && !encuentro.getVisitante().equals(participante)) {
                continue;
            }
            try {
                if (participante.equals(encuentro.getGanador())) {
                    puntos += 3;
                } else if (encuentro.getGanador() == null) {
                    puntos += 1;
                }
            } catch (ReglaNegocioException e) {
            }
        }
        return puntos;
    }
    public int diferenciaDe(InscripcionTorneo participante) {
        int diferencia = 0;
        for (EncuentroTorneo encuentro : getEncuentros()) {
            if (!encuentro.isJugado()) {
                continue;
            }
            if (participante.equals(encuentro.getLocal())) {
                diferencia += (encuentro.getMarcadorLocal() - encuentro.getMarcadorVisitante());
            } else if (participante.equals(encuentro.getVisitante())) {
                diferencia += (encuentro.getMarcadorVisitante() - encuentro.getMarcadorLocal());
            }
        }
        return diferencia;}

    public List<InscripcionTorneo> getPosiciones() {
        List<InscripcionTorneo> tabla = new ArrayList<>(inscripciones);
        tabla.sort((a, b) -> {
            int puntosA = puntosDe(a);
            int puntosB = puntosDe(b);
            if (puntosA != puntosB) {
                return Integer.compare(puntosB, puntosA);
            }
            return Integer.compare(diferenciaDe(b), diferenciaDe(a));
        });
        return tabla;
    }
    public double premioPara(int posicion) {
        if (posicion == 1) {
            return totalRecaudado() * 0.60;
        } else if (posicion == 2) {
            return totalRecaudado() * 0.30;
        } else if (posicion == 3) {
            return totalRecaudado() * 0.10;
        } else {
            return 0.0;
        }
    }

    public void repartirPremiosEfectivo() {
        if (premiosEfectivo == null) {
            premiosEfectivo = new HashMap<>();
        }
        List<InscripcionTorneo> tabla = getPosiciones();
        for (int posicion = 1; posicion <= tabla.size(); posicion++) {
            InscripcionTorneo participante = tabla.get(posicion - 1);
            double premio = premioPara(posicion);
            if (premio > 0) {
                List<Usuario> personas = participante.getParticipantes();
                if (personas.isEmpty()) {
                    continue;
                }
                double parte = premio / personas.size();
                for (Usuario u : personas) {
                    if (participante.recibePremioEfectivo(u)) {
                        premiosEfectivo.put(u, premiosEfectivo.getOrDefault(u, 0.0) + parte);
                    }
                }
            }
        }
    }
    

    public String getDiaSemana() {
        switch (fecha.getDayOfWeek()) {
            case MONDAY: return "LUNES";
            case TUESDAY: return "MARTES";
            case WEDNESDAY: return "MIERCOLES";
            case THURSDAY: return "JUEVES";
            case FRIDAY: return "VIERNES";
            case SATURDAY: return "SABADO";
            default: return "DOMINGO";
        }
    }

    public String getNombre() { return nombre; }
    public TipoTorneo getTipo() { return tipo; }
    public Deporte getDeporte() { return deporte; }
    public Categoria getCategoria() { return categoria; }
    public Nivel getNivel() { return nivel; }
    public Modalidad getModalidad() { return modalidad; }
    public int getCupos() { return cupos; }
    public double getTarifa() { return tarifa; }
    public LocalDate getFecha() { return fecha; }
    public EstadoTorneo getEstado() { return estado; }
    public List<InscripcionTorneo> getInscripciones() { return new ArrayList<>(inscripciones); }
    public List<EncuentroTorneo> getEncuentros() {
        if (encuentros == null) {
            encuentros = new ArrayList<>();
        }
        return new ArrayList<>(encuentros);
    
    }  
    public double premioEfectivoDe(Usuario usuario) {
        if (premiosEfectivo == null) {
            premiosEfectivo = new HashMap<>();
        }
        return premiosEfectivo.getOrDefault(usuario, 0.0);
    }
    @Override
    public String toString() {
        String categoriaONivel = esDeConjunto() ? "categoria " + categoria
                : "nivel " + nivel + ", " + modalidad;
        return nombre + " | " + deporte + " (" + categoriaONivel + ") | " + tipo
                + " | " + estado + " | " + fecha
                + " | cupos " + cuposOcupados() + "/" + cupos;
    }
}

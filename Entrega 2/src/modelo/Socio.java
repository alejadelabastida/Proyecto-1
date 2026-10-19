package modelo;

import java.time.LocalDate;

public class Socio extends Usuario {
    private final LocalDate fechaNacimiento;
    private int puntosFidelidad;
    private Jugador jugadorDeportivo;
    private final java.util.List<Mensualidad> mensualidades = new java.util.ArrayList<>();
    private final java.util.List<InscripcionDisciplina> inscripciones = new java.util.ArrayList<>();
    public Socio(int codigo, String nombre, String apellido, String login,
        String password, LocalDate fechaNacimiento) {
        super(codigo, nombre, apellido, login, password);
        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("Falta fecha de nacimiento");
        }
        this.fechaNacimiento = fechaNacimiento;
    }

    public Jugador getJugadorDeportivo() {
        return jugadorDeportivo;
    }

    public void setJugadorDeportivo(Jugador jugador) {
        this.jugadorDeportivo = jugador;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public int getPuntosFidelidad() {
        return puntosFidelidad;
    }

    public int getEdad() {
        return java.time.Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    public void sumarPuntos(int puntos) {
        if (puntos < 0) {
            throw new IllegalArgumentException("Puntos invalidos");
        }
        puntosFidelidad += puntos;
    }
    public void agregarMensualidad(Mensualidad mensualidad) {
        mensualidades.add(mensualidad);
    }

    public java.util.List<Mensualidad> getMensualidades() {
        return mensualidades;
    }

    public void agregarInscripcion(InscripcionDisciplina inscripcion) {
        inscripciones.add(inscripcion);
    }

    public java.util.List<InscripcionDisciplina> getInscripciones() {
        return inscripciones;
    }
}

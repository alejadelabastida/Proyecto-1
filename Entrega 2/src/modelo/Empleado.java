package modelo;

public abstract class Empleado extends Usuario {
    private final String codigoDescuento;
    private final java.util.List<Turno> turnos = new java.util.ArrayList<>();
    protected Empleado(int codigo, String nombre, String apellido, String login, String password, String codigoDescuento) {
        super(codigo, nombre, apellido, login, password);
        this.codigoDescuento = codigoDescuento;
    }

    public String getCodigoDescuento() {
        return codigoDescuento;
    }
    public void agregarTurno(Turno turno) {
        turnos.add(turno);
    }

    public java.util.List<Turno> getTurnos() {
        return turnos;
    }
}

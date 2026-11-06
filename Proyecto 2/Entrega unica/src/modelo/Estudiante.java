package modelo;

public class Estudiante extends Usuario {

    private static final long serialVersionUID = 1L;

    private final String codigoEstudiantil;

    public Estudiante(int codigo, String nombre, String apellido, String login, String password,
            String codigoEstudiantil) {
        super(codigo, nombre, apellido, login, password);
        if (codigoEstudiantil == null || codigoEstudiantil.isBlank()) {
            throw new IllegalArgumentException("El codigo estudiantil es obligatorio");
        }
        this.codigoEstudiantil = codigoEstudiantil;
    }

    public String getCodigoEstudiantil() {
        return codigoEstudiantil;
    }

    @Override
    public String consultarPerfil() {
        return super.consultarPerfil() + " (estudiante " + codigoEstudiantil + ")";
    }
}

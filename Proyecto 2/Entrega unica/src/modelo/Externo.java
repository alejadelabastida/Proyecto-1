package modelo;

public class Externo extends Usuario {

    private static final long serialVersionUID = 1L;

    private final String documento;

    public Externo(int codigo, String nombre, String apellido, String login, String password,
            String documento) {
        super(codigo, nombre, apellido, login, password);
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio");
        }
        this.documento = documento;
    }

    public String getDocumento() {
        return documento;
    }

    @Override
    public String consultarPerfil() {
        return super.consultarPerfil() + " (externo)";
    }
}

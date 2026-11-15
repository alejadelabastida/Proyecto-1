package consola;

import java.util.ArrayList;
import java.util.List;

import modelo.Administrador;
import modelo.Categoria;
import modelo.Club;
import modelo.Deporte;
import modelo.Equipo;
import modelo.Jugador;
import modelo.ReglaNegocioException;
import modelo.Socio;
import modelo.Usuario;

public class MenuAdminEquipos {

    private final Club club;
    private final Administrador administrador;
    private final Entrada entrada;

    public MenuAdminEquipos(Club club, Administrador administrador, Entrada entrada) {
        this.club = club;
        this.administrador = administrador;
        this.entrada = entrada;
    }

    public void mostrar() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println();
            System.out.println("--- Equipos y jugadores ---");
            System.out.println("1. Ver equipos");
            System.out.println("2. Crear equipo");
            System.out.println("3. Inscribir a un socio en un equipo");
            System.out.println("4. Dar de baja a un jugador");
            System.out.println("5. Dar de baja un equipo");
            System.out.println("0. Volver");
            opcion = entrada.leerEntero("Opcion", 0, 5);
            try {
                switch (opcion) {
                    case 1: verEquipos(); break;
                    case 2: crearEquipo(); break;
                    case 3: inscribirSocio(); break;
                    case 4: darDeBajaJugador(); break;
                    case 5: darDeBajaEquipo(); break;
                    default: break;
                }
            } catch (ReglaNegocioException e) {
                System.out.println("NO SE PUDO: " + e.getMessage());
            }
        }
    }

    private void verEquipos() {
        if (club.getEquipos().isEmpty()) {
            System.out.println("No hay equipos registrados.");
            return;
        }
        for (Equipo equipo : club.getEquipos()) {
            System.out.println(" - " + describir(equipo));
            for (Jugador jugador : equipo.getJugadores()) {
                System.out.println("     #" + jugador.getNumeroCamiseta() + " " + jugador.getNombre() + " "
                        + jugador.getApellido() + " (" + jugador.getPosicion() + ")");
            }
        }
    }

    private void crearEquipo() {
        String nombre = entrada.leerTexto("Nombre del equipo");
        Deporte deporte = entrada.elegirValor("Deporte", Deporte.values());
        Categoria categoria = entrada.elegirValor("Categoria", Categoria.values());
        int cupo = entrada.leerEntero("Cupo maximo de jugadores", 1, 40);
        Equipo equipo;
        try {
            equipo = new Equipo(nombre, deporte, categoria, cupo);
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException(e.getMessage());
        }
        club.crearEquipo(administrador, equipo);
        System.out.println("LISTO: equipo " + nombre + " creado.");
    }

    private void inscribirSocio() {
        List<Socio> socios = new ArrayList<>();
        for (Usuario u : club.getUsuarios()) {
            if (u instanceof Socio) {
                socios.add((Socio) u);
            }
        }
        Socio socio = elegir("Socio", socios);
        if (socio == null) {
            return;
        }
        Equipo equipo = elegirEquipo();
        if (equipo == null) {
            return;
        }
        String posicion = entrada.leerTexto("Posicion");
        int camiseta = entrada.leerEntero("Numero de camiseta", 1, 99);
        club.inscribirJugador(socio.getCodigo(), equipo.getNombre(), posicion, camiseta);
        System.out.println("LISTO: " + socio.getNombre() + " quedo inscrito en " + equipo.getNombre() + ".");
    }

    private void darDeBajaJugador() {
        Equipo equipo = elegirEquipo();
        if (equipo == null) {
            return;
        }
        Jugador jugador = elegir("Jugador", equipo.getJugadores());
        if (jugador == null) {
            return;
        }
        if (entrada.confirmar("Dar de baja a " + jugador.getNombre())) {
            club.darDeBajaJugador(administrador, jugador);
            System.out.println("LISTO: jugador dado de baja.");
        }
    }

    private void darDeBajaEquipo() {
        Equipo equipo = elegirEquipo();
        if (equipo == null) {
            return;
        }
        if (entrada.confirmar("Dar de baja el equipo " + equipo.getNombre())) {
            club.darDeBajaEquipo(administrador, equipo);
            System.out.println("LISTO: equipo dado de baja.");
        }
    }

    private Equipo elegirEquipo() {
        List<Equipo> equipos = club.getEquipos();
        if (equipos.isEmpty()) {
            throw new ReglaNegocioException("No hay equipos registrados");
        }
        for (int i = 0; i < equipos.size(); i++) {
            System.out.println((i + 1) + ". " + describir(equipos.get(i)));
        }
        int numero = entrada.leerEntero("Equipo (0 para cancelar)", 0, equipos.size());
        return numero == 0 ? null : equipos.get(numero - 1);
    }

    private <T extends Usuario> T elegir(String titulo, List<T> usuarios) {
        if (usuarios.isEmpty()) {
            throw new ReglaNegocioException("No hay a quien escoger");
        }
        for (int i = 0; i < usuarios.size(); i++) {
            T u = usuarios.get(i);
            System.out.println((i + 1) + ". " + u.getNombre() + " " + u.getApellido() + " (codigo " + u.getCodigo() + ")");
        }
        int numero = entrada.leerEntero(titulo + " (0 para cancelar)", 0, usuarios.size());
        return numero == 0 ? null : usuarios.get(numero - 1);
    }

    private String describir(Equipo equipo) {
        String entrenador = equipo.getEntrenadorResponsable() == null ? "sin entrenador"
                : "entrenador " + equipo.getEntrenadorResponsable().getNombre();
        return equipo.getNombre() + " | " + equipo.getDeporte() + " | " + equipo.getCategoria() + " | "
                + equipo.getJugadores().size() + "/" + equipo.getCupoMaximo() + " jugadores | " + entrenador;
    }
}

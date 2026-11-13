package consola;

import java.util.ArrayList;
import java.util.List;

import modelo.Club;
import modelo.Entrenador;
import modelo.Equipo;
import modelo.InscripcionTorneo;
import modelo.ReglaNegocioException;
import modelo.Torneo;
import modelo.Usuario;

public class MenuTorneos {

    private final Club club;
    private final Usuario usuario;
    private final Entrada entrada;

    public MenuTorneos(Club club, Usuario usuario, Entrada entrada) {
        this.club = club;
        this.usuario = usuario;
        this.entrada = entrada;
    }

    public void mostrar() {
        boolean esEntrenador = usuario instanceof Entrenador;
        int ultimaOpcion = esEntrenador ? 8 : 6;
        int opcion = -1;
        while (opcion != 0) {
            System.out.println();
            System.out.println("--- Torneos ---");
            System.out.println("1. Ver torneos");
            System.out.println("2. Ver inscritos de un torneo");
            System.out.println("3. Inscribirme (sencillos)");
            System.out.println("4. Inscribir una pareja (dobles)");
            System.out.println("5. Retirarme de un torneo");
            System.out.println("6. Mis inscripciones");
            if (esEntrenador) {
                System.out.println("7. Inscribir un equipo");
                System.out.println("8. Retirar un equipo");
            }
            System.out.println("0. Volver");
            opcion = entrada.leerEntero("Opcion", 0, ultimaOpcion);
            ejecutar(opcion);
        }
    }

    private void ejecutar(int opcion) {
        try {
            switch (opcion) {
                case 1: verTorneos(); break;
                case 2: verInscritos(); break;
                case 3: inscribirme(); break;
                case 4: inscribirPareja(); break;
                case 5: retirarme(); break;
                case 6: misInscripciones(); break;
                case 7: inscribirEquipo(); break;
                case 8: retirarEquipo(); break;
                default: break;
            }
        } catch (ReglaNegocioException e) {
            System.out.println("NO SE PUDO: " + e.getMessage());
        }
    }

    private void verTorneos() {
        List<String> lineas = club.consultarTorneos();
        for (int i = 0; i < lineas.size(); i++) {
            System.out.println((i + 1) + ". " + lineas.get(i));
        }
    }

    private void verInscritos() {
        Torneo torneo = elegirTorneo();
        if (torneo == null) {
            return;
        }
        List<String> lineas = club.consultarInscripciones(torneo);
        if (lineas.isEmpty()) {
            System.out.println("Todavia no hay inscritos.");
        }
        for (String linea : lineas) {
            System.out.println(" - " + linea);
        }
        System.out.println("Cupos libres: " + torneo.cuposLibres() + " de " + torneo.getCupos()
                + " (preferenciales: " + torneo.cuposPreferenciales() + ")");
    }

    private void inscribirme() {
        Torneo torneo = elegirTorneo();
        if (torneo == null) {
            return;
        }
        InscripcionTorneo inscripcion = club.inscribirJugadorEnTorneo(usuario.getCodigo(), torneo);
        System.out.println("LISTO: inscrito en " + torneo.getNombre() + " con cupo " + inscripcion.getTipoCupo()
                + ". Tarifa: " + inscripcion.getTarifaCobrada());
    }

    private void inscribirPareja() {
        Torneo torneo = elegirTorneo();
        if (torneo == null) {
            return;
        }
        String loginPareja = entrada.leerTexto("Nombre de usuario de su pareja");
        int codigoPareja = club.buscarCodigoPorLogin(loginPareja);
        InscripcionTorneo inscripcion = club.inscribirParejaEnTorneo(usuario.getCodigo(), codigoPareja, torneo);
        System.out.println("LISTO: pareja inscrita en " + torneo.getNombre() + " con cupo "
                + inscripcion.getTipoCupo() + ". Tarifa total: " + inscripcion.getTarifaCobrada());
    }

    private void retirarme() {
        Torneo torneo = elegirTorneo();
        if (torneo == null) {
            return;
        }
        if (entrada.confirmar("Seguro que desea retirarse de " + torneo.getNombre())) {
            club.retirarDeTorneo(usuario.getCodigo(), torneo);
            System.out.println("LISTO: se retiro y el cupo quedo libre.");
        }
    }

    private void misInscripciones() {
        List<InscripcionTorneo> lista = club.consultarMisInscripciones(usuario.getCodigo());
        if (lista.isEmpty()) {
            System.out.println("No tiene inscripciones.");
        }
        for (InscripcionTorneo inscripcion : lista) {
            System.out.println(" - " + inscripcion.getTorneo().getNombre() + " | " + inscripcion
                    + " | " + inscripcion.getTorneo().getEstado());
        }
    }

    private void inscribirEquipo() {
        Torneo torneo = elegirTorneo();
        Equipo equipo = (torneo == null) ? null : elegirEquipo();
        if (torneo == null || equipo == null) {
            return;
        }
        InscripcionTorneo inscripcion = club.inscribirEquipoEnTorneo(usuario.getCodigo(), equipo, torneo);
        System.out.println("LISTO: equipo inscrito con cupo " + inscripcion.getTipoCupo() + ".");
    }

    private void retirarEquipo() {
        Torneo torneo = elegirTorneo();
        Equipo equipo = (torneo == null) ? null : elegirEquipo();
        if (torneo == null || equipo == null) {
            return;
        }
        club.retirarEquipoDeTorneo(usuario.getCodigo(), equipo, torneo);
        System.out.println("LISTO: equipo retirado y cupo libre.");
    }

    private Torneo elegirTorneo() {
        List<Torneo> torneos = club.getTorneos();
        if (torneos.isEmpty()) {
            throw new ReglaNegocioException("No hay torneos registrados");
        }
        for (int i = 0; i < torneos.size(); i++) {
            System.out.println((i + 1) + ". " + torneos.get(i));
        }
        int numero = entrada.leerEntero("Numero de torneo (0 para cancelar)", 0, torneos.size());
        return (numero == 0) ? null : torneos.get(numero - 1);
    }

    private Equipo elegirEquipo() {
        List<Equipo> equipos = new ArrayList<>();
        for (Equipo equipo : club.getEquipos()) {
            if (equipo.getEntrenadorResponsable() == usuario) {
                equipos.add(equipo);
            }
        }
        if (equipos.isEmpty()) {
            throw new ReglaNegocioException("Usted no es el entrenador responsable de ningun equipo");
        }
        for (int i = 0; i < equipos.size(); i++) {
            System.out.println((i + 1) + ". " + equipos.get(i).getNombre());
        }
        int numero = entrada.leerEntero("Numero de equipo (0 para cancelar)", 0, equipos.size());
        return (numero == 0) ? null : equipos.get(numero - 1);
    }
}

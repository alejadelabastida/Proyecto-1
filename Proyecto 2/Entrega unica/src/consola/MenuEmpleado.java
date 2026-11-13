package consola;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import modelo.Club;
import modelo.Empleado;
import modelo.ReglaNegocioException;
import modelo.SolicitudCambioTurno;
import modelo.Turno;

public class MenuEmpleado {

    private static final String[] DIAS = { "LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES", "SABADO", "DOMINGO" };

    private final Club club;
    private final Empleado empleado;
    private final Entrada entrada;

    public MenuEmpleado(Club club, Empleado empleado, Entrada entrada) {
        this.club = club;
        this.empleado = empleado;
        this.entrada = entrada;
    }

    public void mostrar() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println();
            System.out.println("--- Menu del empleado ---");
            System.out.println("1. Torneos");
            System.out.println("2. Mis turnos");
            System.out.println("3. Solicitar cambio de turno");
            System.out.println("4. Estado de mis solicitudes");
            System.out.println("5. Sugerir un articulo o snack");
            System.out.println("6. Comprar en la tienda con mi codigo (" + empleado.getCodigoDescuento() + ")");
            System.out.println("0. Cerrar sesion");
            opcion = entrada.leerEntero("Opcion", 0, 6);
            try {
                ejecutar(opcion);
            } catch (ReglaNegocioException e) {
                System.out.println("NO SE PUDO: " + e.getMessage());
            }
        }
    }

    private void ejecutar(int opcion) {
        switch (opcion) {
            case 1: new MenuTorneos(club, empleado, entrada).mostrar(); break;
            case 2: verTurnos(); break;
            case 3: solicitarCambio(); break;
            case 4: verSolicitudes(); break;
            case 5: sugerir(); break;
            case 6: new MenuCompras(club, empleado, entrada).mostrar(); break;
            default: break;
        }
    }

    private void verTurnos() {
        if (empleado.getTurnos().isEmpty()) {
            System.out.println("No tiene turnos programados.");
        }
        for (Turno turno : empleado.getTurnos()) {
            System.out.println(" - " + turno);
        }
    }

    private void solicitarCambio() {
        if (empleado.getTurnos().isEmpty()) {
            throw new ReglaNegocioException("No tiene turnos programados");
        }
        Turno turno = entrada.elegir("Turno a cambiar", empleado.getTurnos());
        if (turno == null) {
            return;
        }
        String dia = entrada.elegirValor("Nuevo dia", DIAS);
        LocalTime inicio = entrada.leerHora("Nueva hora de inicio");
        LocalTime fin = entrada.leerHora("Nueva hora de fin");
        String motivo = entrada.leerTexto("Motivo");
        club.solicitarCambioTurno(empleado.getCodigo(), turno, motivo, dia, inicio, fin);
        System.out.println("LISTO: la solicitud quedo pendiente y su turno actual sigue vigente.");
    }

    private void verSolicitudes() {
        List<SolicitudCambioTurno> propias = new ArrayList<>();
        for (SolicitudCambioTurno solicitud : club.getSolicitudes()) {
            if (solicitud.getEmpleado() == empleado) {
                propias.add(solicitud);
            }
        }
        if (propias.isEmpty()) {
            System.out.println("No tiene solicitudes.");
        }
        for (SolicitudCambioTurno solicitud : propias) {
            System.out.println(" - " + solicitud);
        }
    }

    private void sugerir() {
        String nombre = entrada.leerTexto("Nombre del articulo o snack");
        String descripcion = entrada.leerTexto("Descripcion");
        club.sugerirProducto(empleado.getCodigo(), nombre, descripcion);
        System.out.println("LISTO: sugerencia enviada; queda pendiente de aprobacion.");
    }
}

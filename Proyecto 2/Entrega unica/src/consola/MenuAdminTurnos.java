package consola;

import java.util.ArrayList;
import java.util.List;

import modelo.Administrador;
import modelo.Club;
import modelo.EstadoSolicitud;
import modelo.ReglaNegocioException;
import modelo.SolicitudCambioTurno;
import modelo.Turno;

public class MenuAdminTurnos {

    private final Club club;
    private final Administrador administrador;
    private final Entrada entrada;

    public MenuAdminTurnos(Club club, Administrador administrador, Entrada entrada) {
        this.club = club;
        this.administrador = administrador;
        this.entrada = entrada;
    }

    public void mostrar() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println();
            System.out.println("--- Turnos del personal ---");
            System.out.println("1. Ver turnos semanales");
            System.out.println("2. Resolver solicitudes de cambio de turno");
            System.out.println("0. Volver");
            opcion = entrada.leerEntero("Opcion", 0, 2);
            try {
                switch (opcion) {
                    case 1: verTurnos(); break;
                    case 2: resolverSolicitudes(); break;
                    default: break;
                }
            } catch (ReglaNegocioException e) {
                System.out.println("NO SE PUDO: " + e.getMessage());
            }
        }
    }

    private void verTurnos() {
        List<Turno> turnos = club.consultarTurnos(administrador.getCodigo());
        if (turnos.isEmpty()) {
            System.out.println("No hay turnos registrados.");
        }
        for (Turno turno : turnos) {
            System.out.println(" - " + turno);
        }
    }

    private void resolverSolicitudes() {
        List<SolicitudCambioTurno> pendientes = new ArrayList<>();
        for (SolicitudCambioTurno s : administrador.consultarSolicitudesCambioTurno(club)) {
            if (s.getEstado() == EstadoSolicitud.PENDIENTE) {
                pendientes.add(s);
            }
        }
        if (pendientes.isEmpty()) {
            System.out.println("No hay solicitudes pendientes.");
            return;
        }
        SolicitudCambioTurno solicitud = entrada.elegir("Solicitud", pendientes);
        if (solicitud == null) {
            return;
        }
        System.out.println("Turno actual: " + solicitud.getTurno());
        if (entrada.confirmar("Aprobar el cambio (n = rechazarlo)")) {
            club.aprobarCambio(solicitud);
            System.out.println("LISTO: cambio aprobado. Turno nuevo: " + solicitud.getTurno());
        } else {
            String observacion = entrada.leerTexto("Motivo del rechazo");
            club.rechazarCambio(solicitud, observacion);
            System.out.println("LISTO: solicitud rechazada.");
        }
    }
}

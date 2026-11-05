package consola;

import java.time.LocalTime;
import java.util.Scanner;

import modelo.Club;
import modelo.Empleado;
import modelo.Entrenador;
import modelo.Fisioterapeuta;
import modelo.ReglaNegocioException;
import modelo.SolicitudCambioTurno;
import modelo.Usuario;
import modelo.Turno;
import persistencia.Persistencia;

public class Prueba8 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Carpeta de datos (Enter = datos/prueba8): ");
        String carpeta = sc.nextLine().trim();
        sc.close();
        if (carpeta.isEmpty()) {
            carpeta = "datos/prueba8";
        }

        Persistencia persistencia = new Persistencia(carpeta);
        boolean yaEjecutada = persistencia.existe();
        Club club = persistencia.cargar();

        Salida.titulo("PRUEBA 8: Cambios de turno");
        if (yaEjecutada) {
            System.out.println("Se cargo la informacion guardada en " + carpeta + " (persistencia).");
            mostrarTurnos(club);
            mostrarSolicitudes(club);
            System.out.println();
            System.out.println("Para repetir la prueba desde cero, borre la carpeta " + carpeta);
            return;
        }

        DatosBase.cargar(club);
        ejecutar(club);
        club.guardar();
        System.out.println();
        System.out.println("Datos guardados en " + carpeta + ". Si ejecuta de nuevo, se cargaran desde ahi.");
    }

    private static void ejecutar(Club club) {
        Entrenador carlos = (Entrenador) DatosBase.usuario(club, DatosBase.ENTRENADOR1);
        Entrenador laura = (Entrenador) DatosBase.usuario(club, DatosBase.ENTRENADOR2);
        Fisioterapeuta mateo = (Fisioterapeuta) DatosBase.usuario(club, DatosBase.FISIO);

        Entrenador diego = new Entrenador(5, "Diego", "Vargas", "diego", "1234", "DESC-DIEGO");
        club.getUsuarios().add(diego);

        // Todos trabajan el lunes de 8:00 a 12:00
        Turno turnoCarlos = new Turno(carlos, "LUNES", LocalTime.of(8, 0), LocalTime.of(12, 0));
        Turno turnoLaura = new Turno(laura, "LUNES", LocalTime.of(8, 0), LocalTime.of(12, 0));
        new Turno(diego, "LUNES", LocalTime.of(8, 0), LocalTime.of(12, 0));
        Turno turnoMateo = new Turno(mateo, "LUNES", LocalTime.of(8, 0), LocalTime.of(12, 0));

        Salida.seccion("Estado antes");
        mostrarTurnos(club);
        
        Salida.seccion("Solicitud 1: Carlos pide pasar su turno al MARTES (quedan Laura y Diego el lunes)");
        SolicitudCambioTurno solicitud1 = null;
        Salida.accion("Carlos solicita el cambio");
        try {
            solicitud1 = club.solicitarCambioTurno(DatosBase.ENTRENADOR1, turnoCarlos, "Cita medica",
                    "MARTES", LocalTime.of(8, 0), LocalTime.of(12, 0));
            Salida.ok();
        } catch (ReglaNegocioException e) {
            Salida.inesperado(e);
        }

        Salida.accion("El administrador aprueba la solicitud (se mantienen 2 entrenadores)");
        try {
            club.aprobarCambio(solicitud1);
            Salida.ok();
        } catch (ReglaNegocioException e) {
            Salida.inesperado(e);
        }

      
        Salida.seccion("Solicitud 2: Laura pide pasar al MIERCOLES (el lunes solo quedaria Diego)");
        SolicitudCambioTurno solicitud2 = null;
        Salida.accion("Laura solicita el cambio");
        try {
            solicitud2 = club.solicitarCambioTurno(DatosBase.ENTRENADOR2, turnoLaura, "Viaje",
                    "MIERCOLES", LocalTime.of(8, 0), LocalTime.of(12, 0));
            Salida.ok();
        } catch (ReglaNegocioException e) {
            Salida.inesperado(e);
        }

        Salida.accion("El administrador intenta aprobarla (quedaria 1 entrenador, el minimo es 2)");
        try {
            club.aprobarCambio(solicitud2);
            Salida.debiaRechazarse();
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

  
        Salida.seccion("Solicitud 3: Mateo (unico fisioterapeuta) pide pasar al JUEVES");
        SolicitudCambioTurno solicitud3 = null;
        Salida.accion("Mateo solicita el cambio");
        try {
            solicitud3 = club.solicitarCambioTurno(DatosBase.FISIO, turnoMateo, "Capacitacion",
                    "JUEVES", LocalTime.of(8, 0), LocalTime.of(12, 0));
            Salida.ok();
        } catch (ReglaNegocioException e) {
            Salida.inesperado(e);
        }

        Salida.accion("El administrador intenta aprobarla (no quedaria ningun fisioterapeuta)");
        try {
            club.aprobarCambio(solicitud3);
            Salida.debiaRechazarse();
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.seccion("Otras validaciones");
        Salida.accion("El administrador intenta evaluar otra vez la solicitud 1 (ya estaba aprobada)");
        try {
            club.aprobarCambio(solicitud1);
            Salida.debiaRechazarse();
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.accion("Carlos intenta cambiar el turno de Laura");
        try {
            club.solicitarCambioTurno(DatosBase.ENTRENADOR1, turnoLaura, "Prueba",
                    "VIERNES", LocalTime.of(8, 0), LocalTime.of(12, 0));
            Salida.debiaRechazarse();
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.seccion("RF-38: consulta de turnos");
        Salida.accion("Un socio (Sofia) intenta consultar los turnos");
        try {
            club.consultarTurnos(DatosBase.SOCIO1);
            Salida.debiaRechazarse();
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.seccion("Estado despues");
        Salida.accion("El administrador consulta los turnos");
        try {
            for (Turno t : club.consultarTurnos(DatosBase.ADMIN)) {
                Salida.linea(t.toString());
            }
            Salida.ok();
        } catch (ReglaNegocioException e) {
            Salida.inesperado(e);
        }
        mostrarSolicitudes(club);
    }

    private static void mostrarTurnos(Club club) {
        Salida.linea("Turnos:");
        boolean hay = false;
        for (Usuario u : club.getUsuarios()) {
            if (u instanceof Empleado) {
                for (Turno t : ((Empleado) u).getTurnos()) {
                    Salida.linea("   " + t);
                    hay = true;
                }
            }
        }
        if (!hay) {
            Salida.linea("   (no hay turnos)");
        }
    }

    private static void mostrarSolicitudes(Club club) {
        Salida.linea("Solicitudes:");
        if (club.getSolicitudes().isEmpty()) {
            Salida.linea("   (no hay solicitudes)");
        }
        for (SolicitudCambioTurno s : club.getSolicitudes()) {
            Salida.linea("   " + s);
        }
    }
}

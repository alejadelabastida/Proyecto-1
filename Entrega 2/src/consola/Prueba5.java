package consola;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import modelo.Asistencia;
import modelo.CanchaTenis;
import modelo.Club;
import modelo.Convocatoria;
import modelo.Entrenador;
import modelo.ReglaNegocioException;
import modelo.SesionEntrenamiento;
import modelo.Socio;
import persistencia.Persistencia;

public class Prueba5 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Carpeta de datos (Enter = datos/prueba5): ");
        String carpeta = sc.nextLine().trim();
        if (carpeta.isEmpty()) {
            carpeta = "datos/prueba5";
        }

        Persistencia persistencia = new Persistencia(carpeta);
        boolean yaEjecutada = persistencia.existe();
        Club club = persistencia.cargar();

        Salida.titulo("PRUEBA 5: Entrenamientos");
        if (yaEjecutada) {
            System.out.println("Se cargo la informacion guardada en " + carpeta + " (persistencia).");
            mostrarSesiones(club);
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
        Socio sofia = (Socio) DatosBase.usuario(club, DatosBase.SOCIO1);
        Socio juan = (Socio) DatosBase.usuario(club, DatosBase.SOCIO2);
        Socio maria = (Socio) DatosBase.usuario(club, DatosBase.SOCIO3);
        Socio pedro = (Socio) DatosBase.usuario(club, DatosBase.SOCIO4);

        CanchaTenis cancha = new CanchaTenis("T-01", "Cancha de tenis 1", false);
        club.getInstalaciones().add(cancha);

        LocalDate ayer = LocalDate.now().minusDays(1);
        LocalDate manana = LocalDate.now().plusDays(1);

        Salida.seccion("Estado antes");
        mostrarSesiones(club);

        Salida.seccion("RF-26 / HU-07: organizar sesiones");

        List<Socio> grupo1 = new ArrayList<>();
        grupo1.add(sofia);
        grupo1.add(juan);
        grupo1.add(maria);

        Salida.accion("Carlos organiza una sesion de ayer 15:00 en la cancha libre, con Sofia, Juan y Maria");
        SesionEntrenamiento sesionPasada = null;
        try {
            sesionPasada = club.organizarSesion(DatosBase.ENTRENADOR1, cancha, ayer,
                    LocalTime.of(15, 0), 60, grupo1);
            Salida.ok();
        } catch (ReglaNegocioException e) {
            Salida.inesperado(e);
        }

        List<Socio> grupo2 = new ArrayList<>();
        grupo2.add(sofia);
        grupo2.add(juan);

        Salida.accion("Carlos organiza una sesion de manana 15:00 (60 min) con Sofia y Juan");
        SesionEntrenamiento sesionFutura = null;
        try {
            sesionFutura = club.organizarSesion(DatosBase.ENTRENADOR1, cancha, manana,
                    LocalTime.of(15, 0), 60, grupo2);
            Salida.ok();
        } catch (ReglaNegocioException e) {
            Salida.inesperado(e);
        }

        List<Socio> grupo3 = new ArrayList<>();
        grupo3.add(pedro);

        Salida.accion("Laura intenta una sesion ayer 15:30 en la MISMA cancha (se cruza con la de 15:00)");
        try {
            club.organizarSesion(DatosBase.ENTRENADOR2, cancha, ayer, LocalTime.of(15, 30), 60, grupo3);
            Salida.debiaRechazarse();
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.accion("Un socio (Sofia) intenta organizar una sesion");
        try {
            club.organizarSesion(DatosBase.SOCIO1, cancha, manana, LocalTime.of(18, 0), 60, grupo3);
            Salida.debiaRechazarse();
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.seccion("RF-27 / HU-08: asistencia y evaluacion");

        Salida.accion("Carlos registra asistencia en la sesion de MANANA (todavia no termina)");
        try {
            club.registrarAsistencia(DatosBase.ENTRENADOR1, sesionFutura, sofia, Asistencia.ASISTIO, 4, "");
            Salida.debiaRechazarse();
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.accion("Laura (que no dirige la sesion) intenta registrar asistencia en la sesion de ayer");
        try {
            club.registrarAsistencia(DatosBase.ENTRENADOR2, sesionPasada, sofia, Asistencia.ASISTIO, 4, "");
            Salida.debiaRechazarse();
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.accion("Carlos registra: Sofia ASISTIO nota 5");
        try {
            club.registrarAsistencia(DatosBase.ENTRENADOR1, sesionPasada, sofia,
                    Asistencia.ASISTIO, 5, "Excelente tecnica");
            Salida.ok();
        } catch (ReglaNegocioException e) {
            Salida.inesperado(e);
        }

        Salida.accion("Carlos registra: Juan LLEGO_TARDE nota 3");
        try {
            club.registrarAsistencia(DatosBase.ENTRENADOR1, sesionPasada, juan,
                    Asistencia.LLEGO_TARDE, 3, "Debe mejorar la puntualidad");
            Salida.ok();
        } catch (ReglaNegocioException e) {
            Salida.inesperado(e);
        }

        Salida.accion("Carlos registra: Maria FALTO (sin evaluacion)");
        try {
            club.registrarAsistencia(DatosBase.ENTRENADOR1, sesionPasada, maria, Asistencia.FALTO, 0, null);
            Salida.ok();
        } catch (ReglaNegocioException e) {
            Salida.inesperado(e);
        }

        Salida.accion("Carlos registra una nota invalida (9) para Sofia");
        try {
            club.registrarAsistencia(DatosBase.ENTRENADOR1, sesionPasada, sofia, Asistencia.ASISTIO, 9, "");
            Salida.debiaRechazarse();
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.seccion("RF-17 / HU-05: Sofia consulta sus convocatorias");
        Salida.accion("Consultar convocatorias de Sofia");
        try {
            for (Convocatoria c : club.consultarConvocatorias(sofia)) {
                Salida.linea(c.toString());
            }
            Salida.ok();
        } catch (ReglaNegocioException e) {
            Salida.inesperado(e);
        }

        Salida.accion("Consultar convocatorias de Pedro (nunca fue convocado)");
        try {
            club.consultarConvocatorias(pedro);
            Salida.debiaRechazarse();
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.seccion("RF-18 / HU-06: Sofia consulta su asistencia y evaluacion");
        Salida.accion("Consultar la sesion de ayer (ya termino)");
        try {
            Salida.linea(club.consultarAsistencia(sofia, sesionPasada).toString());
            Salida.ok();
        } catch (ReglaNegocioException e) {
            Salida.inesperado(e);
        }

        Salida.accion("Consultar la sesion de manana (aun en proceso)");
        try {
            club.consultarAsistencia(sofia, sesionFutura);
            Salida.debiaRechazarse();
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.seccion("RF-28: el fisioterapeuta consulta el desempeno de un jugador");
        Salida.accion("Mateo (fisioterapeuta) consulta a Sofia");
        try {
            for (Convocatoria c : club.consultarDesempenoJugador(DatosBase.FISIO, sofia)) {
                Salida.linea(c.toString());
            }
            Salida.ok();
        } catch (ReglaNegocioException e) {
            Salida.inesperado(e);
        }

        Salida.accion("Un socio (Juan) intenta consultar el desempeno de Sofia");
        try {
            club.consultarDesempenoJugador(DatosBase.SOCIO2, sofia);
            Salida.debiaRechazarse();
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.seccion("Estado despues");
        mostrarSesiones(club);
    }

    private static void mostrarSesiones(Club club) {
        if (club.getSesiones().isEmpty()) {
            Salida.linea("(no hay sesiones registradas)");
            return;
        }
        for (SesionEntrenamiento s : club.getSesiones()) {
            Salida.linea(s.toString());
            for (Convocatoria c : s.getConvocatorias()) {
                String estado = "sin registrar";
                if (c.getAsistencia() != null) {
                    estado = c.getAsistencia().toString();
                    if (c.getNota() > 0) {
                        estado += ", nota " + c.getNota();
                    }
                }
                Salida.linea("   - " + c.getSocio().getNombre() + ": " + estado);
            }
        }
    }
}

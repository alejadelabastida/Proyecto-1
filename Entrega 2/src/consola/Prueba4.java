package consola;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Scanner;
import modelo.*;
import persistencia.Persistencia;

public class Prueba4 {

    public static void main(String[] args) {
        String carpeta = "datos/prueba4";
        if (args.length > 0 && !args[0].trim().isEmpty()) {
            carpeta = args[0].trim();
        } else {
            System.out.print("Carpeta de datos (Enter = datos/prueba4): ");
            Scanner sc = new Scanner(System.in);
            if (sc.hasNextLine()) {
                String elegida = sc.nextLine().trim();
                sc.close();
                if (!elegida.isEmpty()) {
                    carpeta = elegida;
                }
            }
        }

        Persistencia persistencia = new Persistencia(carpeta);
        boolean yaEjecutada = persistencia.existe();
        Club club = persistencia.cargar();
        Salida.titulo("PRUEBA 4: Reservas, disponibilidad y cupos por disciplina");

        if (yaEjecutada) {
            Salida.linea("Se cargaron datos existentes de " + carpeta);
            verificarPersistencia(club);
            return;
        }

        DatosBase.cargar(club);
        ejecutar(club);
        club.guardar();

        Club recuperado = new Persistencia(carpeta).cargar();
        verificarPersistencia(recuperado);
        Salida.linea("Datos guardados en " + carpeta + ". Repetir para comprobar la carga.");
    }

    private static void ejecutar(Club club) {
        CanchaTenis tenis1 = new CanchaTenis("T-01", "Tenis 1", false);
        CanchaTenis tenis2 = new CanchaTenis("T-02", "Tenis 2", false);
        CanchaTenis tenis3 = new CanchaTenis("T-03", "Tenis 3", false);
        CanchaPadel padel = new CanchaPadel("P-01", "Padel", false);
        MesaTenisDeMesa mesa = new MesaTenisDeMesa("M-01", "Tenis de mesa", false);

        club.getInstalaciones().add(tenis1);
        club.getInstalaciones().add(tenis2);
        club.getInstalaciones().add(tenis3);
        club.getInstalaciones().add(padel);
        club.getInstalaciones().add(mesa);

        LocalDate fecha = LocalDate.now().plusDays(2);
        Salida.seccion("Reservas validas");

        club.reservar(DatosBase.SOCIO1, tenis1, fecha, LocalTime.of(9, 0),
                LocalTime.of(10, 0), 2, Modalidad.SENCILLOS);
        club.reservar(DatosBase.SOCIO2, tenis2, fecha, LocalTime.of(9, 30),
                LocalTime.of(10, 30), 4, Modalidad.DOBLES);
        Salida.linea("Dos canchas de tenis reservadas con solapamiento: aceptado.");

        Salida.seccion("Reservas invalidas");
        Salida.accion("Intentar usar la misma cancha dos veces a las 9:30");
        try {
            club.reservar(DatosBase.SOCIO3, tenis1, fecha, LocalTime.of(9, 30),
                    LocalTime.of(10, 15), 2, Modalidad.SENCILLOS);
            throw new AssertionError("Se permitio una reserva duplicada en la misma cancha");
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.accion("Intentar reservar una tercera cancha de tenis con dos ocupadas");
        try {
            club.reservar(DatosBase.SOCIO3, tenis3, fecha, LocalTime.of(9, 45),
                    LocalTime.of(10, 15), 2, Modalidad.SENCILLOS);
            throw new AssertionError("Se supero el maximo simultaneo de tenis");
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.accion("Intentar reservar padel para sencillos");
        try {
            club.reservar(DatosBase.SOCIO3, padel, fecha, LocalTime.of(9, 0),
                    LocalTime.of(10, 0), 2, Modalidad.SENCILLOS);
            throw new AssertionError("Padel admitio sencillos");
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.seccion("Liberacion de cupos y disciplinas diferentes");
        club.reservar(DatosBase.SOCIO3, tenis3, fecha, LocalTime.of(10, 30),
                LocalTime.of(11, 0), 2, Modalidad.SENCILLOS);
        club.reservar(DatosBase.SOCIO4, padel, fecha, LocalTime.of(9, 30),
                LocalTime.of(10, 30), 4, Modalidad.DOBLES);
        club.reservar(DatosBase.SOCIO4, mesa, fecha, LocalTime.of(9, 30),
                LocalTime.of(10, 30), 2, Modalidad.SENCILLOS);

        if (club.getReservas().size() != 5) {
            throw new AssertionError("Esperabamos cinco reservas validas");
        }
        Salida.linea("Cinco reservas aceptadas, tres intentos invalidos rechazados.");
        Salida.ok();
    }

    private static void verificarPersistencia(Club club) {
        if (club.getInstalaciones().size() != 5 || club.getReservas().size() != 5) {
            throw new AssertionError("La persistencia no recupero instalaciones y reservas");
        }

        int tenis = 0;
        int padel = 0;
        int mesa = 0;
        for (Reserva reserva : club.getReservas()) {
            if (!club.getInstalaciones().contains(reserva.getInstalacion())) {
                throw new AssertionError("La instalacion de una reserva no se conservo");
            }
            if (reserva.getInstalacion().getDisciplina() == Deporte.TENIS) {
                tenis++;
            } else if (reserva.getInstalacion().getDisciplina() == Deporte.PADEL) {
                padel++;
            } else if (reserva.getInstalacion().getDisciplina() == Deporte.TENIS_DE_MESA) {
                mesa++;
            }
        }

        if (tenis != 3 || padel != 1 || mesa != 1) {
            throw new AssertionError("Las reservas por disciplina no se recuperaron correctamente");
        }

        Salida.linea("Persistencia verificada: 3 reservas de tenis, 1 de padel y 1 de mesa.");
        Salida.ok();
    }
}

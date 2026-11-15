package consola;

import java.time.LocalDate;
import java.util.List;

import modelo.Administrador;
import modelo.Club;
import modelo.Deporte;
import modelo.ReglaNegocioException;

public class MenuAdminReportes {

    private final Club club;
    private final Administrador administrador;
    private final Entrada entrada;

    public MenuAdminReportes(Club club, Administrador administrador, Entrada entrada) {
        this.club = club;
        this.administrador = administrador;
        this.entrada = entrada;
    }

    public void mostrar() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println();
            System.out.println("--- Reportes e historiales ---");
            System.out.println("1. Reporte de ventas (tienda, cafeteria y mensualidades)");
            System.out.println("2. Reporte de asistencia a entrenamientos");
            System.out.println("3. Reporte de resultados de partidos");
            System.out.println("4. Evolucion del ranking por deporte");
            System.out.println("5. Historial de reservas");
            System.out.println("6. Historial de partidos");
            System.out.println("7. Historial de sesiones de entrenamiento");
            System.out.println("0. Volver");
            opcion = entrada.leerEntero("Opcion", 0, 7);
            if (opcion == 0) {
                break;
            }
            try {
                generar(opcion);
            } catch (ReglaNegocioException e) {
                System.out.println("NO SE PUDO: " + e.getMessage());
            }
        }
    }

    private void generar(int opcion) {
        Deporte deporte = null;
        if (opcion == 4) {
            deporte = entrada.elegirValor("Deporte", Deporte.values());
        }
        LocalDate desde = entrada.leerFecha("Desde");
        LocalDate hasta = entrada.leerFecha("Hasta");
        List<String> lineas;
        switch (opcion) {
            case 1: lineas = club.reporteVentas(administrador.getCodigo(), desde, hasta); break;
            case 2: lineas = club.reporteAsistencia(desde, hasta); break;
            case 3: lineas = club.reporteResultados(desde, hasta); break;
            case 4: lineas = club.reporteEvolucionRanking(deporte, desde, hasta); break;
            case 5: lineas = club.historialReservas(desde, hasta); break;
            case 6: lineas = club.historialPartidos(desde, hasta); break;
            default: lineas = club.historialSesiones(desde, hasta); break;
        }
        for (String linea : lineas) {
            System.out.println(" - " + linea);
        }
    }
}

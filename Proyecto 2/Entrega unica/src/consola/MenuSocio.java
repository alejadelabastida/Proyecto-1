package consola;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import modelo.Club;
import modelo.Convocatoria;
import modelo.Deporte;
import modelo.Instalacion;
import modelo.InscripcionDisciplina;
import modelo.Modalidad;
import modelo.Nivel;
import modelo.ReglaNegocioException;
import modelo.Reserva;
import modelo.Socio;

/** Menu principal del socio: torneos, compras, reservas, academias, convocatorias y perfil. */
public class MenuSocio {

    private final Club club;
    private final Socio socio;
    private final Entrada entrada;

    public MenuSocio(Club club, Socio socio, Entrada entrada) {
        this.club = club;
        this.socio = socio;
        this.entrada = entrada;
    }

    public void mostrar() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println();
            System.out.println("--- Menu del socio ---");
            System.out.println("1. Torneos");
            System.out.println("2. Compras, bonos y puntos");
            System.out.println("3. Reservar una instalacion");
            System.out.println("4. Inscribirme en una academia");
            System.out.println("5. Mi ranking por disciplina");
            System.out.println("6. Mis convocatorias a entrenamiento");
            System.out.println("7. Mi perfil");
            System.out.println("0. Cerrar sesion");
            opcion = entrada.leerEntero("Opcion", 0, 7);
            try {
                ejecutar(opcion);
            } catch (ReglaNegocioException e) {
                System.out.println("NO SE PUDO: " + e.getMessage());
            }
        }
    }

    private void ejecutar(int opcion) {
        switch (opcion) {
            case 1: new MenuTorneos(club, socio, entrada).mostrar(); break;
            case 2: new MenuCompras(club, socio, entrada).mostrar(); break;
            case 3: reservar(); break;
            case 4: inscribirEnAcademia(); break;
            case 5: verRanking(); break;
            case 6: verConvocatorias(); break;
            case 7: System.out.println(club.consultarPerfil(socio)); break;
            default: break;
        }
    }

    private void reservar() {
        if (club.getInstalaciones().isEmpty()) {
            throw new ReglaNegocioException("No hay instalaciones registradas");
        }
        Instalacion instalacion = entrada.elegir("Instalacion", club.getInstalaciones());
        if (instalacion == null) {
            return;
        }
        LocalDate fecha = entrada.leerFecha("Fecha");
        LocalTime inicio = entrada.leerHora("Hora de inicio");
        LocalTime fin = entrada.leerHora("Hora de fin");
        int jugadores = entrada.leerEntero("Numero de jugadores", 1, 22);
        Modalidad modalidad = entrada.elegirValor("Modalidad", Modalidad.values());
        Reserva reserva = club.reservar(socio.getCodigo(), instalacion, fecha, inicio, fin, jugadores, modalidad);
        System.out.println("LISTO: reserva confirmada en " + reserva.getInstalacion().getNombre()
                + " el " + reserva.getFecha() + " de " + reserva.getHoraInicio() + " a " + reserva.getHoraFin());
    }

    private void inscribirEnAcademia() {
        Deporte[] individuales = { Deporte.TENIS, Deporte.PADEL, Deporte.TENIS_DE_MESA };
        Deporte deporte = entrada.elegirValor("Disciplina", individuales);
        Nivel nivel = entrada.elegirValor("Nivel inicial", Nivel.values());
        club.inscribirEnAcademia(socio.getCodigo(), deporte, nivel);
        System.out.println("LISTO: inscrito en " + deporte + " con nivel " + nivel + " y ELO 1000.");
    }

    private void verRanking() {
        if (socio.getInscripciones().isEmpty()) {
            throw new ReglaNegocioException("No esta inscrito en ninguna academia");
        }
        for (InscripcionDisciplina inscripcion : socio.getInscripciones()) {
            int mejores = 0;
            for (modelo.Usuario otro : club.getUsuarios()) {
                if (otro instanceof Socio) {
                    for (InscripcionDisciplina d : ((Socio) otro).getInscripciones()) {
                        if (d.getDisciplina() == inscripcion.getDisciplina()
                                && d.getPuntajeRanking() > inscripcion.getPuntajeRanking()) {
                            mejores++;
                        }
                    }
                }
            }
            String partidos = inscripcion.getHistorial().isEmpty() ? " (aun no tiene partidos)" : "";
            System.out.println(" - " + inscripcion.getDisciplina() + " | nivel " + inscripcion.getNivel()
                    + " | ELO " + inscripcion.getPuntajeRanking() + " | posicion " + (mejores + 1) + partidos);
        }
    }

    private void verConvocatorias() {
        List<Convocatoria> lista = club.consultarConvocatorias(socio);
        if (lista.isEmpty()) {
            System.out.println("No tiene convocatorias.");
        }
        for (Convocatoria convocatoria : lista) {
            System.out.println(" - " + convocatoria);
        }
    }
}

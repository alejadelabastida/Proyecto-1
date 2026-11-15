package consola;

import java.util.ArrayList;
import java.util.List;

import modelo.ASCUN;
import modelo.Categoria;
import modelo.Club;
import modelo.Competencia;
import modelo.Deporte;
import modelo.Equipo;
import modelo.InscripcionEquipo;
import modelo.Jugador;
import modelo.ReglaNegocioException;
import modelo.TorneoDeLosCerros;

public class MenuAdminCompetencias {

    private static final String[] FORMATOS = { "Liga (Torneo de los Cerros)", "Grupos y eliminacion (ASCUN)" };

    private final Club club;
    private final Entrada entrada;

    public MenuAdminCompetencias(Club club, Entrada entrada) {
        this.club = club;
        this.entrada = entrada;
    }

    public void mostrar() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println();
            System.out.println("--- Competencias oficiales ---");
            System.out.println("1. Ver competencias");
            System.out.println("2. Registrar una competencia");
            System.out.println("3. Inscribir un equipo en una competencia");
            System.out.println("4. Ver tabla de posiciones de una competencia");
            System.out.println("0. Volver");
            opcion = entrada.leerEntero("Opcion", 0, 4);
            try {
                switch (opcion) {
                    case 1: verCompetencias(); break;
                    case 2: registrarCompetencia(); break;
                    case 3: inscribirEquipo(); break;
                    case 4: verTabla(); break;
                    default: break;
                }
            } catch (ReglaNegocioException e) {
                System.out.println("NO SE PUDO: " + e.getMessage());
            }
        }
    }

    private void verCompetencias() {
        if (club.getCompetencias().isEmpty()) {
            System.out.println("No hay competencias registradas.");
            return;
        }
        for (Competencia c : club.getCompetencias()) {
            System.out.println(" - " + describir(c) + " | equipos inscritos: " + c.getInscripciones().size());
        }
    }

    private void registrarCompetencia() {
        String nombre = entrada.leerTexto("Nombre");
        String entidad = entrada.leerTexto("Entidad organizadora");
        String temporada = entrada.leerTexto("Temporada (ej. 2026-2)");
        Deporte deporte = entrada.elegirValor("Deporte", Deporte.values());
        Categoria categoria = entrada.elegirValor("Categoria", Categoria.values());
        int nominaMinima = entrada.leerEntero("Nomina minima de jugadores", 1, 40);
        String formato = entrada.elegirValor("Formato", FORMATOS);
        Competencia competencia;
        try {
            if (formato.equals(FORMATOS[0])) {
                competencia = new TorneoDeLosCerros(nombre, entidad, temporada, deporte, categoria, nominaMinima);
            } else {
                competencia = new ASCUN(nombre, entidad, temporada, deporte, categoria, nominaMinima);
            }
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException(e.getMessage());
        }
        club.agregarCompetencia(competencia);
        System.out.println("LISTO: competencia " + nombre + " registrada.");
    }

    private void inscribirEquipo() {
        Competencia competencia = elegirCompetencia();
        if (competencia == null) {
            return;
        }
        List<Equipo> equipos = club.getEquipos();
        if (equipos.isEmpty()) {
            throw new ReglaNegocioException("No hay equipos registrados");
        }
        for (int i = 0; i < equipos.size(); i++) {
            Equipo e = equipos.get(i);
            System.out.println((i + 1) + ". " + e.getNombre() + " | " + e.getDeporte() + " | " + e.getCategoria());
        }
        int numero = entrada.leerEntero("Equipo (0 para cancelar)", 0, equipos.size());
        if (numero == 0) {
            return;
        }
        Equipo equipo = equipos.get(numero - 1);
        if (equipo.getEntrenadorResponsable() == null) {
            throw new ReglaNegocioException("El equipo no tiene entrenador responsable, asignelo primero");
        }
        List<Jugador> nomina = new ArrayList<>();
        for (Jugador jugador : equipo.getJugadores()) {
            if (!jugador.estaSuspendido() && entrada.confirmar("Incluir a " + jugador.getNombre() + " en la nomina")) {
                nomina.add(jugador);
            }
        }
        try {
            club.inscribirEquipo(equipo.getEntrenadorResponsable().getCodigo(), equipo, competencia, nomina);
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException(e.getMessage());
        }
        System.out.println("LISTO: " + equipo.getNombre() + " quedo inscrito con " + nomina.size() + " jugadores.");
    }

    private void verTabla() {
        Competencia competencia = elegirCompetencia();
        if (competencia == null) {
            return;
        }
        List<InscripcionEquipo> tabla = club.consultarTablaPosiciones(competencia);
        if (tabla.isEmpty()) {
            System.out.println("Todavia no hay equipos inscritos.");
        }
        for (int i = 0; i < tabla.size(); i++) {
            InscripcionEquipo ins = tabla.get(i);
            System.out.println((i + 1) + ". " + ins.getEquipo().getNombre() + " | " + ins.getPuntos() + " pts | G "
                    + ins.getGanados() + " E " + ins.getEmpatados() + " P " + ins.getPerdidos());
        }
    }

    private Competencia elegirCompetencia() {
        List<Competencia> competencias = club.getCompetencias();
        if (competencias.isEmpty()) {
            throw new ReglaNegocioException("No hay competencias registradas");
        }
        for (int i = 0; i < competencias.size(); i++) {
            System.out.println((i + 1) + ". " + describir(competencias.get(i)));
        }
        int numero = entrada.leerEntero("Competencia (0 para cancelar)", 0, competencias.size());
        return numero == 0 ? null : competencias.get(numero - 1);
    }

    private String describir(Competencia c) {
        String formato = (c instanceof ASCUN) ? "ASCUN" : "Liga";
        return c.getNombre() + " | " + c.getEntidadOrganizadora() + " | " + c.getTemporada() + " | "
                + c.getDeporte() + " | " + c.getCategoria() + " | " + formato;
    }
}

package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import consola.DatosBase;
import modelo.Club;
import modelo.Deporte;
import modelo.InscripcionDisciplina;
import modelo.Modalidad;
import modelo.Nivel;
import modelo.Socio;
import modelo.TipoTorneo;
import modelo.Torneo;
import persistencia.Persistencia;

public class PersistenciaTorneosTest {

    @Test
    public void lasInscripcionesSobrevivenAGuardarYCargar() throws IOException {
        Path carpeta = Files.createTempDirectory("club_torneos");
        Persistencia persistencia = new Persistencia(carpeta.toString());

        Club club = new Club();
        club.setPersistencia(persistencia);
        DatosBase.cargar(club);
        new InscripcionDisciplina((Socio) DatosBase.usuario(club, DatosBase.SOCIO1), Deporte.TENIS, Nivel.INTERMEDIO);
        Torneo torneo = new Torneo("Copa", TipoTorneo.COMPETITIVO, Deporte.TENIS, null, Nivel.INTERMEDIO,
                Modalidad.SENCILLOS, 8, 30000, LocalDate.of(2026, 11, 9));
        club.getTorneos().add(torneo);
        club.inscribirJugadorEnTorneo(DatosBase.SOCIO1, torneo);
        club.guardar();

        Club cargado = persistencia.cargar();
        assertNotNull(cargado);
        assertEquals(1, cargado.getTorneos().size());
        Torneo torneoCargado = cargado.getTorneos().get(0);
        assertEquals(1, torneoCargado.cuposOcupados());
        assertEquals(30000, torneoCargado.totalRecaudado());
    }
}

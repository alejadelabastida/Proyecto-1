package consola;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import modelo.*;
import persistencia.Persistencia;

public class Prueba6 {

    public static void main(String[] args) {
        String carpeta = "datos/prueba6";
        if (args.length > 0 && !args[0].trim().isEmpty()) {
            carpeta = args[0].trim();
        } else {
            System.out.print("Carpeta de datos (Enter = datos/prueba6): ");
            Scanner sc = new Scanner(System.in);
            if (sc.hasNextLine()) {
                String elegida = sc.nextLine().trim();
                if (!elegida.isEmpty()) {
                    carpeta = elegida;
                }
            }
        }

        Persistencia persistencia = new Persistencia(carpeta);
        boolean yaEjecutada = persistencia.existe();
        Club club = persistencia.cargar();
        Salida.titulo("PRUEBA 6: Practicas, niveles, entrenador, estadisticas y ranking");

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
        Socio sofia = (Socio) DatosBase.usuario(club, DatosBase.SOCIO1);
        Socio juan = (Socio) DatosBase.usuario(club, DatosBase.SOCIO2);
        Socio maria = (Socio) DatosBase.usuario(club, DatosBase.SOCIO3);
        Socio pedro = (Socio) DatosBase.usuario(club, DatosBase.SOCIO4);

        InscripcionDisciplina tSofia = new InscripcionDisciplina(sofia, Deporte.TENIS, Nivel.PRINCIPIANTE);
        InscripcionDisciplina tJuan = new InscripcionDisciplina(juan, Deporte.TENIS, Nivel.INTERMEDIO);
        InscripcionDisciplina tMaria = new InscripcionDisciplina(maria, Deporte.TENIS, Nivel.AVANZADO);

        LocalDate fecha = LocalDate.now().plusDays(1);
        Salida.seccion("Sencillos con niveles adyacentes");
        PartidoPractica sencillo = club.programarPractica(
                Arrays.asList(tSofia, tJuan), Modalidad.SENCILLOS, fecha);
        club.registrarPractica(sencillo, estadisticasTenis(tSofia, 2, tJuan, 0));
        if (!sencillo.resuelto() || sencillo.getEstadisticasPractica().size() != 2 ||
                tSofia.getPuntajeRanking() != 1010 || tJuan.getPuntajeRanking() != 990) {
            throw new AssertionError("No se registro correctamente el sencillo y su ranking");
        }
        Salida.linea("Sencillo registrado: 2 estadisticas, 1010 y 990 puntos.");
        Salida.ok();

        Salida.seccion("Niveles incompatibles");
        try {
            club.programarPractica(Arrays.asList(tSofia, tMaria), Modalidad.SENCILLOS, fecha);
            throw new AssertionError("Se admitio Principiante contra Avanzado sin entrenador");
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }

        Salida.seccion("Excepcion: socio que tambien es entrenador");
        club.vincularSocioEntrenador(DatosBase.SOCIO1, DatosBase.ENTRENADOR1);
        PartidoPractica conEntrenador = club.programarPractica(
                Arrays.asList(tSofia, tMaria), Modalidad.SENCILLOS, fecha);
        club.registrarPractica(conEntrenador, estadisticasTenis(tSofia, 0, tMaria, 2));
        if (tSofia.getPuntajeRanking() != 1000 || tMaria.getPuntajeRanking() != 1010) {
            throw new AssertionError("Ranking incorrecto en el segundo sencillo");
        }
        Salida.linea("Partido entre niveles extremos admitido por el rol de entrenador.");
        Salida.ok();

        Salida.seccion("Dobles de padel: cuatro estadisticas individuales");
        InscripcionDisciplina pSofia = new InscripcionDisciplina(sofia, Deporte.PADEL, Nivel.PRINCIPIANTE);
        InscripcionDisciplina pJuan = new InscripcionDisciplina(juan, Deporte.PADEL, Nivel.INTERMEDIO);
        InscripcionDisciplina pMaria = new InscripcionDisciplina(maria, Deporte.PADEL, Nivel.AVANZADO);
        InscripcionDisciplina pPedro = new InscripcionDisciplina(pedro, Deporte.PADEL, Nivel.AVANZADO);

        List<InscripcionDisciplina> parejas = Arrays.asList(pSofia, pJuan, pMaria, pPedro);
        PartidoPractica dobles = club.programarPractica(parejas, Modalidad.DOBLES, fecha);
        Map<InscripcionDisciplina, Map<String, Integer>> datosDobles = new HashMap<>();
        datosDobles.put(pSofia, datosRaqueta(2));
        datosDobles.put(pJuan, datosRaqueta(2));
        datosDobles.put(pMaria, datosRaqueta(1));
        datosDobles.put(pPedro, datosRaqueta(1));
        club.registrarPractica(dobles, datosDobles);

        if (!dobles.resuelto() || dobles.getEstadisticasPractica().size() != 4 ||
                pSofia.getPuntajeRanking() != 1010 || pPedro.getPuntajeRanking() != 990) {
            throw new AssertionError("No se registraron correctamente los dobles");
        }
        Salida.linea("Dobles registrados: 4 estadisticas y ranking por jugador/disciplina.");
        Salida.ok();

        Salida.seccion("Tenis de mesa: solo sets y puntos totales");
        InscripcionDisciplina mJuan = new InscripcionDisciplina(
                juan, Deporte.TENIS_DE_MESA, Nivel.INTERMEDIO);
        InscripcionDisciplina mMaria = new InscripcionDisciplina(
                maria, Deporte.TENIS_DE_MESA, Nivel.AVANZADO);
        PartidoPractica partidoMesa = club.programarPractica(
                Arrays.asList(mJuan, mMaria), Modalidad.SENCILLOS, fecha);
        Map<InscripcionDisciplina, Map<String, Integer>> datosMesa = new HashMap<>();
        datosMesa.put(mJuan, datosMesa(3, 33));
        datosMesa.put(mMaria, datosMesa(1, 24));
        club.registrarPractica(partidoMesa, datosMesa);
        if (!partidoMesa.resuelto() || partidoMesa.getEstadisticasPractica().size() != 2 ||
                mJuan.getPuntajeRanking() != 1010 || mMaria.getPuntajeRanking() != 990) {
            throw new AssertionError("No se registro correctamente el tenis de mesa");
        }
        Salida.linea("Tenis de mesa registrado sin exigir games ni aces.");
        Salida.ok();

        Salida.seccion("Rechazo de empate e integridad del ranking");
        PartidoPractica invalido = club.programarPractica(
                Arrays.asList(tSofia, tJuan), Modalidad.SENCILLOS, fecha);
        try {
            club.registrarPractica(invalido, estadisticasTenis(tSofia, 2, tJuan, 2));
            throw new AssertionError("Se admitio un resultado sin ganador");
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }
        if (invalido.resuelto() || !invalido.getEstadisticasPractica().isEmpty() ||
                tSofia.getPuntajeRanking() != 1000 || tJuan.getPuntajeRanking() != 990) {
            throw new AssertionError("El rechazo modifico resultados o ranking");
        }

        Salida.seccion("Rechazo de resultado duplicado");
        try {
            club.registrarPractica(sencillo, estadisticasTenis(tSofia, 2, tJuan, 0));
            throw new AssertionError("Se admitio registrar dos veces un mismo resultado");
        } catch (ReglaNegocioException e) {
            Salida.rechazado(e);
        }
        Salida.ok();
    }

    private static Map<InscripcionDisciplina, Map<String, Integer>> estadisticasTenis(
            InscripcionDisciplina uno, int setsUno, InscripcionDisciplina dos, int setsDos) {
        Map<InscripcionDisciplina, Map<String, Integer>> datos = new HashMap<>();
        datos.put(uno, datosRaqueta(setsUno));
        datos.put(dos, datosRaqueta(setsDos));
        return datos;
    }

    private static Map<String, Integer> datosRaqueta(int sets) {
        Map<String, Integer> datos = new HashMap<>();
        datos.put("sets", sets);
        datos.put("games", sets * 6);
        datos.put("aces", sets);
        datos.put("erroresNoForzados", 1);
        return datos;
    }

    private static Map<String, Integer> datosMesa(int sets, int puntosTotales) {
        Map<String, Integer> datos = new HashMap<>();
        datos.put("sets", sets);
        datos.put("puntosTotales", puntosTotales);
        return datos;
    }

    private static InscripcionDisciplina inscripcion(Socio socio, Deporte disciplina) {
        for (InscripcionDisciplina inscripcion : socio.getInscripciones()) {
            if (inscripcion.getDisciplina() == disciplina) {
                return inscripcion;
            }
        }
        throw new AssertionError("No se recupero la inscripcion a " + disciplina);
    }

    private static void verificarPersistencia(Club club) {
        if (club.getPartidos().size() != 5) {
            throw new AssertionError("No se recuperaron los cinco partidos programados");
        }

        PartidoPractica primero = (PartidoPractica) club.getPartidos().get(0);
        PartidoPractica segundo = (PartidoPractica) club.getPartidos().get(1);
        PartidoPractica tercero = (PartidoPractica) club.getPartidos().get(2);
        PartidoPractica cuarto = (PartidoPractica) club.getPartidos().get(3);
        PartidoPractica quinto = (PartidoPractica) club.getPartidos().get(4);

        if (!primero.resuelto() || !segundo.resuelto() || !tercero.resuelto() ||
                !cuarto.resuelto() || quinto.resuelto() ||
                primero.getEstadisticasPractica().size() != 2 ||
                segundo.getEstadisticasPractica().size() != 2 ||
                tercero.getEstadisticasPractica().size() != 4 ||
                cuarto.getEstadisticasPractica().size() != 2 ||
                !quinto.getEstadisticasPractica().isEmpty()) {
            throw new AssertionError("No se recuperaron correctamente los resultados y estadisticas");
        }

        Socio sofia = (Socio) DatosBase.usuario(club, DatosBase.SOCIO1);
        Socio juan = (Socio) DatosBase.usuario(club, DatosBase.SOCIO2);
        Socio maria = (Socio) DatosBase.usuario(club, DatosBase.SOCIO3);
        Socio pedro = (Socio) DatosBase.usuario(club, DatosBase.SOCIO4);
        InscripcionDisciplina tSofia = inscripcion(sofia, Deporte.TENIS);
        InscripcionDisciplina tJuan = inscripcion(juan, Deporte.TENIS);
        InscripcionDisciplina tMaria = inscripcion(maria, Deporte.TENIS);
        InscripcionDisciplina pSofia = inscripcion(sofia, Deporte.PADEL);
        InscripcionDisciplina pJuan = inscripcion(juan, Deporte.PADEL);
        InscripcionDisciplina pMaria = inscripcion(maria, Deporte.PADEL);
        InscripcionDisciplina pPedro = inscripcion(pedro, Deporte.PADEL);
        InscripcionDisciplina mJuan = inscripcion(juan, Deporte.TENIS_DE_MESA);
        InscripcionDisciplina mMaria = inscripcion(maria, Deporte.TENIS_DE_MESA);

        if (sofia.getEntrenadorAsociado() != DatosBase.usuario(club, DatosBase.ENTRENADOR1) ||
                tSofia.getPuntajeRanking() != 1000 || tSofia.getHistorial().size() != 2 ||
                tJuan.getPuntajeRanking() != 990 || tJuan.getHistorial().size() != 1 ||
                tMaria.getPuntajeRanking() != 1010 || tMaria.getHistorial().size() != 1 ||
                pSofia.getPuntajeRanking() != 1010 || pJuan.getPuntajeRanking() != 1010 ||
                pMaria.getPuntajeRanking() != 990 || pPedro.getPuntajeRanking() != 990 ||
                pSofia.getHistorial().size() != 1 || pJuan.getHistorial().size() != 1 ||
                pMaria.getHistorial().size() != 1 || pPedro.getHistorial().size() != 1 ||
                mJuan.getPuntajeRanking() != 1010 || mMaria.getPuntajeRanking() != 990 ||
                mJuan.getHistorial().size() != 1 || mMaria.getHistorial().size() != 1) {
            throw new AssertionError("No se recupero el historial, ELO o el rol de entrenador");
        }

        Salida.linea("Persistencia verificada: 5 partidos, 10 estadisticas, rankings e historiales.");
        Salida.linea("Tenis Sofia: " + tSofia.getPuntajeRanking()
                + "; Padel Sofia: " + pSofia.getPuntajeRanking()
                + "; asociacion a entrenador conservada.");
        Salida.ok();
    }
}

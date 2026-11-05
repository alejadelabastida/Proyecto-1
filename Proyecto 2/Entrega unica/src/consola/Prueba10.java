package consola;

import modelo.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class Prueba10 {
	
    public static void main(String[] args) {
        String carpeta = "datos/prueba10";
        if (args.length > 0 && !args[0].trim().isEmpty()) {
            carpeta = args[0].trim();
        } else {
            System.out.print("Carpeta de datos (Enter = datos/prueba10): ");
            java.util.Scanner sc = new java.util.Scanner(System.in);
            if (sc.hasNextLine()) {
                String elegida = sc.nextLine().trim();
                sc.close();
                if (!elegida.isEmpty()) {
                    carpeta = elegida;
                }
            }
        }

        persistencia.Persistencia persistencia = new persistencia.Persistencia(carpeta);
        boolean yaEjecutada = persistencia.existe();
        Club club = persistencia.cargar();
        Salida.titulo("PRUEBA 10: persistencia y reglas de negocio");

        if (yaEjecutada) {
            Salida.linea("Cargando datos previamente guardados en " + carpeta);
            verificarPersistencia(club);
            return;
        }

        DatosBase.cargar(club);
        ejecutar(club);
        club.guardar();

        // Comprobacion real: se crea otro Club leyendo el archivo, no se reutiliza el original.
        Club recuperado = new persistencia.Persistencia(carpeta).cargar();
        verificarPersistencia(recuperado);
        Salida.linea("Datos guardados en " + carpeta
                + ". Ejecute de nuevo para consultar los datos persistidos.");
    }

    private static void verificarPersistencia(Club club) {
        if (club.getUsuarios().size() < 8 ||
                !(club.iniciarSesion("admin", "admin123") instanceof Administrador) ||
                club.iniciarSesion("admin", "incorrecta") != null) {
            throw new IllegalStateException("La informacion de usuarios no se recupero correctamente");
        }
        Salida.linea("Persistencia verificada: " + club.getUsuarios().size()
                + " usuarios y autenticacion del administrador.");
        Salida.ok();
    }

    private static void ejecutar(Club club) {
        Salida.seccion("RF-43: inicio de sesion");
        Salida.accion("Ana entra con su usuario y clave correctos");
        Usuario usuario = club.iniciarSesion("admin", "admin123");
        if (usuario instanceof Administrador) {
            Salida.ok();
        } else {
            Salida.linea("ERROR: no se inicio sesion como administrador");
        }

        Salida.esperaRechazo("Ana intenta entrar con una clave incorrecta", () -> {
            if (club.iniciarSesion("admin", "incorrecta") == null) {
                throw new ReglaNegocioException("Credenciales incorrectas");
            }
        });

        Salida.seccion("RF-44: consulta del perfil");
        Salida.accion("Consultar los datos de Ana");
        try {
            String perfil = club.consultarPerfil(usuario);
            Salida.linea("Perfil: " + perfil);
            Salida.ok();
        } catch (ReglaNegocioException e) {
            Salida.inesperado(e);
        }

        Salida.seccion("RF-42: restricciones del administrador");
        CanchaTenis cancha = new CanchaTenis("T-01", "Cancha de tenis 1", false);
        club.getInstalaciones().add(cancha);
        Salida.esperaRechazo("Ana intenta reservar una cancha de tenis para uso personal",
                () -> club.reservar(DatosBase.ADMIN, cancha, LocalDate.now().plusDays(1),
                        LocalTime.of(9, 0), LocalTime.of(10, 0), 2, Modalidad.SENCILLOS));
        Salida.esperaRechazo("Ana intenta inscribirse como jugadora en un equipo",
                () -> club.inscribirJugador(DatosBase.ADMIN, "Azules", "Defensa", 4));

        Salida.seccion("Estado despues");
        Salida.linea("Usuarios registrados: " + club.getUsuarios().size());
    }
}

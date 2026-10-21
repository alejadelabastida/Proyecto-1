package consola;

import modelo.*;

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

        Salida.accion("Ana intenta entrar con una clave incorrecta");
        Usuario incorrecto = club.iniciarSesion("admin", "incorrecta");
        if (incorrecto == null) {
            Salida.linea("Credenciales incorrectas rechazadas");
        } else {
            Salida.linea("ERROR: se acepto una clave incorrecta");
        }

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
        if (usuario instanceof Administrador) {
            Administrador administrador = (Administrador) usuario;
            Salida.accion("Consultar si puede reservar para uso personal");
            Salida.linea("Puede reservar: " + administrador.puedeReservar());
            Salida.accion("Consultar si puede inscribirse a un partido");
            Salida.linea("Puede inscribirse: " + administrador.puedeInscribirsePartido());
        }

        Salida.seccion("Estado despues");
        Salida.linea("Usuarios registrados: " + club.getUsuarios().size());
    }
}

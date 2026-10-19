package consola;

import modelo.*;

public class Prueba10 {

    public static void main(String[] args) {
        Club club = new Club();
        DatosBase.cargar(club);
        Salida.titulo("PRUEBA 10: Login, perfil y administrador");
        ejecutar(club);
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

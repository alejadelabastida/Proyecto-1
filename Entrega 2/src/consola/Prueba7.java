package consola;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import modelo.ArticuloTienda;
import modelo.CanchaPadel;
import modelo.Club;
import modelo.ExistenciaInventario;
import modelo.Modalidad;
import modelo.Producto;
import modelo.Reserva;
import modelo.Snack;
import modelo.Socio;
import modelo.UbicacionInventario;
import modelo.Usuario;
import modelo.Venta;
import persistencia.Persistencia;

public class Prueba7 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Carpeta de datos (Enter = datos/prueba7): ");
        String carpeta = sc.nextLine().trim();
        if (carpeta.isEmpty()) {
            carpeta = "datos/prueba7";
        }

        Persistencia persistencia = new Persistencia(carpeta);
        boolean yaEjecutada = persistencia.existe();
        Club club = persistencia.cargar();

        Salida.titulo("PRUEBA 7: Compras en tienda y cafeteria");
        if (yaEjecutada) {
            System.out.println("Se cargo la informacion guardada en " + carpeta + " (persistencia).");
            mostrarEstado(club);
            System.out.println();
            System.out.println("Para repetir la prueba desde cero, borre la carpeta " + carpeta);
            return;
        }

        DatosBase.cargar(club);
        DatosTienda.cargar(club);
        ejecutar(club);
        club.guardar();
        System.out.println();
        System.out.println("Datos guardados en " + carpeta + ". Si ejecuta de nuevo, se cargaran desde ahi.");
    }

    private static void ejecutar(Club club) {
        Socio pedro = (Socio) DatosBase.usuario(club, DatosBase.SOCIO4);
        CanchaPadel padel = new CanchaPadel("P-01", "Cancha de padel techada 1", true);
        club.getInstalaciones().add(padel);
        new Reserva(pedro, padel, LocalDate.now(), LocalTime.of(0, 0), LocalTime.of(23, 59), 4, Modalidad.DOBLES);

        Salida.seccion("Estado antes");
        mostrarEstado(club);

        Salida.seccion("RF-12 / RN-30, RN-32: compra en la tienda (IVA 19%)");
        int antes = club.getVentas().size();
        Salida.esperaOk("Sofia compra 2 balones (2 x 50000), sin codigo de descuento. Total esperado: 119000",
                () -> club.comprarTienda(DatosBase.SOCIO1, canasta(DatosTienda.balon, 2), null));
        mostrarVentaNueva(club, antes);

        Salida.esperaRechazo("Sofia intenta comprar 20 balones (en la tienda solo quedan 8)",
                () -> club.comprarTienda(DatosBase.SOCIO1, canasta(DatosTienda.balon, 20), null));
        Salida.esperaRechazo("Sofia intenta comprar 0 camisetas",
                () -> club.comprarTienda(DatosBase.SOCIO1, canasta(DatosTienda.camiseta, 0), null));
        Salida.esperaRechazo("Sofia intenta comprar sin articulos",
                () -> club.comprarTienda(DatosBase.SOCIO1, new LinkedHashMap<ArticuloTienda, Integer>(), null));
        Salida.esperaRechazo("El administrador intenta comprar en la tienda",
                () -> club.comprarTienda(DatosBase.ADMIN, canasta(DatosTienda.camiseta, 1), null));

        Salida.seccion("RF-18 / RN-31: codigos de descuento de empleados");
        antes = club.getVentas().size();
        Salida.esperaOk("Carlos (empleado) compra 1 camiseta con su propio codigo (15%). Total esperado: 80920",
                () -> club.comprarTienda(DatosBase.ENTRENADOR1, canasta(DatosTienda.camiseta, 1), "DESC-CARLOS"));
        mostrarVentaNueva(club, antes);

        antes = club.getVentas().size();
        Salida.esperaOk("Juan (socio) compra 1 camiseta con el codigo que le compartio Carlos (8%). Total esperado: 87584",
                () -> club.comprarTienda(DatosBase.SOCIO2, canasta(DatosTienda.camiseta, 1), "DESC-CARLOS"));
        mostrarVentaNueva(club, antes);

        Salida.esperaRechazo("Juan intenta usar un codigo que no existe",
                () -> club.comprarTienda(DatosBase.SOCIO2, canasta(DatosTienda.camiseta, 1), "CODIGO-FALSO"));

        Salida.seccion("RF-14 / RN-33: alergenos de un snack (se informan antes de comprar)");
        Snack galleta = DatosTienda.galleta;
        Salida.linea("La galleta tiene alergenos: " + galleta.getAlergenos());

        Salida.seccion("RF-13, RF-15 / RN-35: compra en la cafeteria (consumo 8% + propina)");
        antes = club.getVentas().size();
        Salida.esperaOk("Sofia compra 2 aguas y 1 galleta con propina de 10%. Total esperado: 11800",
                () -> club.comprarCafeteria(DatosBase.SOCIO1,
                        pedido(DatosTienda.agua, 2, DatosTienda.galleta, 1), 0.10, 0));
        mostrarVentaNueva(club, antes);

        antes = club.getVentas().size();
        Salida.esperaOk("Maria compra 1 agua y deja 0% de propina. Total esperado: 3240",
                () -> club.comprarCafeteria(DatosBase.SOCIO3, pedido(DatosTienda.agua, 1), 0.0, 0));
        mostrarVentaNueva(club, antes);

        Salida.esperaRechazo("Maria intenta dejar una propina de -5%",
                () -> club.comprarCafeteria(DatosBase.SOCIO3, pedido(DatosTienda.agua, 1), -0.05, 0));
        Salida.esperaRechazo("Maria intenta comprar un articulo de la tienda en la cafeteria",
                () -> club.comprarCafeteria(DatosBase.SOCIO3, pedido(DatosTienda.balon, 1), 0.10, 0));

        Salida.seccion("RN-34: bebida caliente en cancha techada");
        Salida.esperaRechazo("Pedro (dentro de una cancha de padel techada) intenta comprar un cafe caliente",
                () -> club.comprarCafeteria(DatosBase.SOCIO4, pedido(DatosTienda.cafe, 1), 0.10, 0));
        antes = club.getVentas().size();
        Salida.esperaOk("Pedro compra una bebida fria (agua) en la misma cancha",
                () -> club.comprarCafeteria(DatosBase.SOCIO4, pedido(DatosTienda.agua, 1), 0.10, 0));
        mostrarVentaNueva(club, antes);
        antes = club.getVentas().size();
        Salida.esperaOk("Maria (fuera de una cancha techada) compra un cafe caliente",
                () -> club.comprarCafeteria(DatosBase.SOCIO3, pedido(DatosTienda.cafe, 1), 0.10, 0));
        mostrarVentaNueva(club, antes);

 
        Salida.seccion("RF-16 / RN-37: puntos de fidelidad (2% de cada compra, 1 punto = 1 peso)");
        Socio sofia = (Socio) DatosBase.usuario(club, DatosBase.SOCIO1);
        Salida.linea("Puntos de Sofia antes: " + sofia.getPuntosFidelidad());
        antes = club.getVentas().size();
        Salida.esperaOk("Sofia compra 1 agua y 1 galleta (7000) usando 1000 puntos y propina de 10%. Total esperado: 7180",
                () -> club.comprarCafeteria(DatosBase.SOCIO1,
                        pedido(DatosTienda.agua, 1, DatosTienda.galleta, 1), 0.10, 1000));
        mostrarVentaNueva(club, antes);
        Salida.linea("Puntos de Sofia despues: " + sofia.getPuntosFidelidad());

        Salida.esperaRechazo("Maria intenta usar 500 puntos pero tiene muy pocos",
                () -> club.comprarCafeteria(DatosBase.SOCIO3, pedido(DatosTienda.agua, 1), 0.10, 500));
        Salida.esperaRechazo("Sofia intenta usar 5000 puntos, mas de los que tiene",
                () -> club.comprarCafeteria(DatosBase.SOCIO1, pedido(DatosTienda.agua, 1), 0.10, 5000));

 
        Salida.seccion("RF-17: historial de compras");
        Salida.esperaOk("Sofia consulta su historial de compras", () -> {
            for (Venta v : club.consultarHistorialCompras(DatosBase.SOCIO1)) {
                Salida.linea(v.toString());
            }
        });
        Salida.esperaRechazo("El administrador intenta consultar un historial de compras (no es socio)",
                () -> club.consultarHistorialCompras(DatosBase.ADMIN));

        Salida.seccion("Estado despues");
        mostrarEstado(club);
    }

    private static Map<ArticuloTienda, Integer> canasta(ArticuloTienda articulo, int cantidad) {
        Map<ArticuloTienda, Integer> m = new LinkedHashMap<>();
        m.put(articulo, cantidad);
        return m;
    }

    private static Map<Producto, Integer> pedido(Producto p1, int c1) {
        Map<Producto, Integer> m = new LinkedHashMap<>();
        m.put(p1, c1);
        return m;
    }

    private static Map<Producto, Integer> pedido(Producto p1, int c1, Producto p2, int c2) {
        Map<Producto, Integer> m = pedido(p1, c1);
        m.put(p2, c2);
        return m;
    }

 
    private static void mostrarVentaNueva(Club club, int ventasAntes) {
        if (club.getVentas().size() > ventasAntes) {
            Salida.linea(club.getVentas().get(club.getVentas().size() - 1).toString());
        }
    }

    private static void mostrarEstado(Club club) {
        Salida.linea("Ventas registradas: " + club.getVentas().size());
        UbicacionInventario tienda = null;
        for (UbicacionInventario u : club.getUbicaciones()) {
            if (u.getNombre().equals("Tienda")) {
                tienda = u;
            }
        }
        if (tienda == null) {
            Salida.linea("(todavia no hay inventario)");
        } else {
            for (ExistenciaInventario e : tienda.getExistencias()) {
                Salida.linea("Tienda | " + e.getProducto().getNombre() + ": " + e.getCantidad() + " unidades");
            }
        }
        for (Usuario u : club.getUsuarios()) {
            if (u instanceof Socio) {
                Salida.linea("Puntos de " + u.getNombre() + ": " + ((Socio) u).getPuntosFidelidad());
            }
        }
    }
}

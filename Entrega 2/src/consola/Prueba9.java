package consola;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import modelo.ArticuloTienda;
import modelo.Club;
import modelo.ExistenciaInventario;
import modelo.Mensualidad;
import modelo.Producto;
import modelo.Socio;
import modelo.SugerenciaProducto;
import modelo.UbicacionInventario;
import persistencia.Persistencia;

public class Prueba9 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Carpeta de datos (Enter = datos/prueba9): ");
        String carpeta = sc.nextLine().trim();
        if (carpeta.isEmpty()) {
            carpeta = "datos/prueba9";
        }

        Persistencia persistencia = new Persistencia(carpeta);
        boolean yaEjecutada = persistencia.existe();
        Club club = persistencia.cargar();

        Salida.titulo("PRUEBA 9: Inventario, sugerencias y reportes de ventas");
        if (yaEjecutada) {
            System.out.println("Se cargo la informacion guardada en " + carpeta + " (persistencia).");
            mostrarInventario(club);
            mostrarSugerencias(club);
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
        Salida.seccion("Estado antes");
        mostrarInventario(club);

        Salida.seccion("RF-35: consultar el inventario");
        Salida.esperaOk("El administrador consulta todo el inventario", () -> {
            for (String linea : club.consultarInventario(DatosBase.ADMIN, null)) {
                Salida.linea(linea);
            }
        });
        Salida.esperaOk("El administrador consulta solo la Bodega", () -> {
            for (String linea : club.consultarInventario(DatosBase.ADMIN, "Bodega")) {
                Salida.linea(linea);
            }
        });
        Salida.esperaRechazo("El administrador consulta una ubicacion que no existe (Sotano)",
                () -> club.consultarInventario(DatosBase.ADMIN, "Sotano"));
        Salida.esperaRechazo("Sofia (socia) intenta consultar el inventario",
                () -> club.consultarInventario(DatosBase.SOCIO1, null));


        Salida.seccion("RF-35 / RN-38: reabastecer la tienda");
        Salida.esperaOk("El administrador agrega 5 raquetas a la Tienda (pasan de 2 a 7)",
                () -> club.reabastecer(DatosBase.ADMIN, DatosTienda.raqueta, 5));
        mostrarExistencia(club, "Tienda", DatosTienda.raqueta);
        Salida.esperaRechazo("El administrador intenta reabastecer 0 unidades",
                () -> club.reabastecer(DatosBase.ADMIN, DatosTienda.raqueta, 0));
        Salida.esperaRechazo("El administrador intenta reabastecer -3 unidades",
                () -> club.reabastecer(DatosBase.ADMIN, DatosTienda.raqueta, -3));
        Salida.esperaRechazo("Carlos (entrenador) intenta reabastecer la tienda",
                () -> club.reabastecer(DatosBase.ENTRENADOR1, DatosTienda.raqueta, 5));
        Salida.esperaRechazo("El administrador intenta reabastecer un articulo que no esta registrado",
                () -> club.reabastecer(DatosBase.ADMIN, new ArticuloTienda("Gorra", 30000, "Ropa"), 5));


        Salida.seccion("RF-36 / RN-38: mover articulos entre ubicaciones");
        Salida.esperaOk("El administrador mueve 6 balones de la Bodega (20) a la Tienda (10)",
                () -> club.moverArticulo(DatosBase.ADMIN, DatosTienda.balon, "Bodega", "Tienda", 6));
        mostrarExistencia(club, "Bodega", DatosTienda.balon);
        mostrarExistencia(club, "Tienda", DatosTienda.balon);
        Salida.esperaRechazo("El administrador intenta mover 50 balones de la Bodega (solo quedan 14)",
                () -> club.moverArticulo(DatosBase.ADMIN, DatosTienda.balon, "Bodega", "Tienda", 50));
        Salida.linea("Como se rechazo, no cambio nada:");
        mostrarExistencia(club, "Bodega", DatosTienda.balon);
        mostrarExistencia(club, "Tienda", DatosTienda.balon);
        Salida.esperaRechazo("El administrador intenta mover raquetas desde la Bodega (alli nunca hubo)",
                () -> club.moverArticulo(DatosBase.ADMIN, DatosTienda.raqueta, "Bodega", "Tienda", 1));
        Salida.esperaRechazo("El administrador intenta mover balones a una ubicacion que no existe",
                () -> club.moverArticulo(DatosBase.ADMIN, DatosTienda.balon, "Bodega", "Sotano", 1));
        Salida.esperaRechazo("El administrador intenta mover 0 balones",
                () -> club.moverArticulo(DatosBase.ADMIN, DatosTienda.balon, "Bodega", "Tienda", 0));

        Salida.seccion("RF-39 / RN-38: sugerencias de productos");
        SugerenciaProducto[] sugerencias = new SugerenciaProducto[2];
        Salida.esperaOk("Carlos (entrenador) sugiere vender 'Botilito de agua'",
                () -> sugerencias[0] = club.sugerirProducto(DatosBase.ENTRENADOR1, "Botilito de agua",
                        "Botilito reutilizable para entrenar"));
        Salida.esperaOk("Mateo (fisioterapeuta) sugiere vender 'Barra energetica'",
                () -> sugerencias[1] = club.sugerirProducto(DatosBase.FISIO, "Barra energetica",
                        "Snack para despues del entrenamiento"));
        Salida.esperaRechazo("Sofia (socia) intenta sugerir un producto",
                () -> club.sugerirProducto(DatosBase.SOCIO1, "Gorra", "Gorra del club"));

        Salida.esperaOk("El administrador aprueba la sugerencia de Carlos",
                () -> club.aprobarSugerencia(DatosBase.ADMIN, sugerencias[0]));
        Salida.esperaOk("El administrador rechaza la sugerencia de Mateo",
                () -> club.rechazarSugerencia(DatosBase.ADMIN, sugerencias[1]));
        Salida.esperaRechazo("El administrador intenta aprobar otra vez la sugerencia de Carlos (ya evaluada)",
                () -> club.aprobarSugerencia(DatosBase.ADMIN, sugerencias[0]));
        Salida.esperaRechazo("Laura (entrenadora) intenta aprobar la sugerencia de Mateo",
                () -> club.aprobarSugerencia(DatosBase.ENTRENADOR2, sugerencias[1]));
        mostrarSugerencias(club);

        
       
        Salida.seccion("RF-42 / RN-41, RN-42: reporte de ventas por rubro");
        Socio sofia = (Socio) DatosBase.usuario(club, DatosBase.SOCIO1);
        Socio juan = (Socio) DatosBase.usuario(club, DatosBase.SOCIO2);
        LocalDate hoy = LocalDate.now();
        new Mensualidad(sofia, hoy.getYear() + "-" + hoy.getMonthValue(), 150000, hoy);
        new Mensualidad(juan, hoy.getYear() + "-" + hoy.getMonthValue(), 150000, hoy);
        Salida.linea("Se registraron 2 mensualidades de 150000 pagadas hoy (Sofia y Juan)");

        Salida.esperaOk("Sofia compra 2 balones (total 119000)",
                () -> club.comprarTienda(DatosBase.SOCIO1, canasta(DatosTienda.balon, 2), null));
        Salida.esperaOk("Juan compra 1 camiseta (total 95200)",
                () -> club.comprarTienda(DatosBase.SOCIO2, canasta(DatosTienda.camiseta, 1), null));
        Salida.esperaOk("Maria compra 1 agua con propina de 10% (total 3540)",
                () -> club.comprarCafeteria(DatosBase.SOCIO3, pedido(DatosTienda.agua, 1), 0.10, 0));

        Salida.esperaOk("El administrador genera el reporte de ventas de hoy", () -> {
            for (String linea : club.reporteVentas(DatosBase.ADMIN, hoy, hoy)) {
                Salida.linea(linea);
            }
        });
        Salida.esperaRechazo("El administrador pide un reporte con la fecha final antes de la inicial",
                () -> club.reporteVentas(DatosBase.ADMIN, hoy, hoy.minusDays(1)));
        Salida.esperaRechazo("El administrador pide un reporte de un periodo sin ventas (hace 2 anos)",
                () -> club.reporteVentas(DatosBase.ADMIN, hoy.minusYears(2), hoy.minusYears(2).plusDays(30)));
        Salida.esperaRechazo("Sofia (socia) intenta generar el reporte de ventas",
                () -> club.reporteVentas(DatosBase.SOCIO1, hoy, hoy));

        Salida.seccion("Estado despues");
        mostrarInventario(club);
    }

    private static Map<ArticuloTienda, Integer> canasta(ArticuloTienda articulo, int cantidad) {
        Map<ArticuloTienda, Integer> m = new LinkedHashMap<>();
        m.put(articulo, cantidad);
        return m;
    }

    private static Map<Producto, Integer> pedido(Producto producto, int cantidad) {
        Map<Producto, Integer> m = new LinkedHashMap<>();
        m.put(producto, cantidad);
        return m;
    }

    private static void mostrarExistencia(Club club, String ubicacion, Producto producto) {
        for (UbicacionInventario u : club.getUbicaciones()) {
            if (u.getNombre().equals(ubicacion)) {
                ExistenciaInventario e = u.buscarExistencia(producto);
                int cantidad = (e == null) ? 0 : e.getCantidad();
                Salida.linea(ubicacion + " | " + producto.getNombre() + ": " + cantidad + " unidades");
            }
        }
    }

    private static void mostrarInventario(Club club) {
        Salida.linea("Inventario:");
        if (club.getUbicaciones().isEmpty()) {
            Salida.linea("   (no hay inventario)");
        }
        for (UbicacionInventario u : club.getUbicaciones()) {
            for (ExistenciaInventario e : u.getExistencias()) {
                Salida.linea("   " + u.getNombre() + " | " + e.getProducto().getNombre() + ": " + e.getCantidad());
            }
        }
    }

    private static void mostrarSugerencias(Club club) {
        Salida.linea("Sugerencias:");
        boolean hay = false;
        for (modelo.Usuario u : club.getUsuarios()) {
            if (u instanceof modelo.Empleado) {
                for (SugerenciaProducto s : ((modelo.Empleado) u).getSugerencias()) {
                    Salida.linea("   " + s);
                    hay = true;
                }
            }
        }
        if (!hay) {
            Salida.linea("   (no hay sugerencias)");
        }
    }
}

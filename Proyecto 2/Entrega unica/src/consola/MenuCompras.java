package consola;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import modelo.ArticuloTienda;
import modelo.Bono;
import modelo.Club;
import modelo.Producto;
import modelo.ReglaNegocioException;
import modelo.Snack;
import modelo.Socio;
import modelo.Usuario;
import modelo.Venta;
import modelo.VentaCafeteria;
import modelo.VentaTienda;

public class MenuCompras {

    private static final int PROPINA_SUGERIDA = 10;

    private final Club club;
    private final Usuario usuario;
    private final Entrada entrada;

    public MenuCompras(Club club, Usuario usuario, Entrada entrada) {
        this.club = club;
        this.usuario = usuario;
        this.entrada = entrada;
    }

    public void mostrar() {
        boolean esSocio = usuario instanceof Socio;
        int opcion = -1;
        while (opcion != 0) {
            System.out.println();
            System.out.println("--- Compras ---");
            System.out.println("1. Comprar en la tienda");
            if (esSocio) {
                System.out.println("2. Comprar en la cafeteria");
                System.out.println("3. Mis bonos de descuento");
                System.out.println("4. Mis puntos de fidelidad");
                System.out.println("5. Mi historial de compras");
            }
            System.out.println("0. Volver");
            opcion = entrada.leerEntero("Opcion", 0, esSocio ? 5 : 1);
            try {
                ejecutar(opcion);
            } catch (ReglaNegocioException e) {
                System.out.println("NO SE PUDO: " + e.getMessage());
            }
        }
    }

    private void ejecutar(int opcion) {
        switch (opcion) {
            case 1: comprarEnTienda(); break;
            case 2: comprarEnCafeteria(); break;
            case 3: verBonos(); break;
            case 4: System.out.println("Puntos: " + ((Socio) usuario).getPuntosFidelidad()); break;
            case 5: verHistorial(); break;
            default: break;
        }
    }

    private void comprarEnTienda() {
        List<Producto> articulos = productosDe(true);
        Map<ArticuloTienda, Integer> compra = new LinkedHashMap<>();
        boolean seguir = true;
        while (seguir) {
            Producto elegido = entrada.elegir("Articulo", articulos);
            if (elegido == null) {
                break;
            }
            int cantidad = entrada.leerEntero("Cantidad", 1, 100);
            compra.merge((ArticuloTienda) elegido, cantidad, Integer::sum);
            seguir = entrada.confirmar("Agregar otro articulo");
        }
        if (compra.isEmpty()) {
            System.out.println("No eligio articulos.");
            return;
        }
        VentaTienda venta;
        Bono bono = elegirBonoSiQuiere();
        if (bono != null) {
            venta = club.comprarTiendaConBono(usuario.getCodigo(), compra, bono);
        } else {
            String codigo = "";
            if (entrada.confirmar("Tiene un codigo de descuento de un empleado")) {
                codigo = entrada.leerTexto("Codigo");
            }
            venta = club.comprarTienda(usuario.getCodigo(), compra, codigo);
        }
        System.out.println("LISTO: " + venta);
    }

    private void comprarEnCafeteria() {
        List<Producto> productos = productosDe(false);
        Map<Producto, Integer> compra = new LinkedHashMap<>();
        boolean seguir = true;
        while (seguir) {
            Producto elegido = entrada.elegir("Producto", productos);
            if (elegido == null) {
                break;
            }
            if (elegido instanceof Snack) {
                System.out.println("Alergenos de " + elegido.getNombre() + ": " + ((Snack) elegido).getAlergenos());
            }
            int cantidad = entrada.leerEntero("Cantidad", 1, 100);
            compra.merge(elegido, cantidad, Integer::sum);
            seguir = entrada.confirmar("Agregar otro producto");
        }
        if (compra.isEmpty()) {
            System.out.println("No eligio productos.");
            return;
        }
        double propina = entrada.leerEntero("Propina en % (sugerida " + PROPINA_SUGERIDA + ")", 0, 100) / 100.0;
        VentaCafeteria venta;
        Bono bono = elegirBonoSiQuiere();
        if (bono != null) {
            venta = club.comprarCafeteriaConBono(usuario.getCodigo(), compra, propina, bono);
        } else {
            int puntos = entrada.leerEntero("Puntos de fidelidad a usar", 0, ((Socio) usuario).getPuntosFidelidad());
            venta = club.comprarCafeteria(usuario.getCodigo(), compra, propina, puntos);
        }
        System.out.println("LISTO: " + venta);
    }

    private Bono elegirBonoSiQuiere() {
        if (!(usuario instanceof Socio)) {
            return null;
        }
        List<Bono> disponibles = new ArrayList<>();
        for (Bono bono : club.consultarBonos(usuario.getCodigo())) {
            if (!bono.isRedimido()) {
                disponibles.add(bono);
            }
        }
        if (disponibles.isEmpty() || !entrada.confirmar("Quiere usar un bono de descuento (no se acumula con otros descuentos)")) {
            return null;
        }
        return entrada.elegir("Bono", disponibles);
    }

    private void verBonos() {
        List<Bono> bonos = club.consultarBonos(usuario.getCodigo());
        if (bonos.isEmpty()) {
            System.out.println("No tiene bonos. Se ganan al ganar un torneo recreativo.");
        }
        for (Bono bono : bonos) {
            System.out.println(" - " + bono);
        }
    }

    private void verHistorial() {
        List<Venta> ventas = club.consultarHistorialCompras(usuario.getCodigo());
        if (ventas.isEmpty()) {
            System.out.println("No tiene compras.");
        }
        for (Venta venta : ventas) {
            System.out.println(" - " + venta);
        }
    }

    private List<Producto> productosDe(boolean tienda) {
        List<Producto> lista = new ArrayList<>();
        for (Producto producto : club.getProductos()) {
            if ((producto instanceof ArticuloTienda) == tienda) {
                lista.add(producto);
            }
        }
        if (lista.isEmpty()) {
            throw new ReglaNegocioException("No hay productos disponibles");
        }
        return lista;
    }
}

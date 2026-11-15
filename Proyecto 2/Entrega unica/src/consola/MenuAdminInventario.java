package consola;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import modelo.Administrador;
import modelo.ArticuloTienda;
import modelo.Club;
import modelo.EstadoSolicitud;
import modelo.Instalacion;
import modelo.Producto;
import modelo.ReglaNegocioException;
import modelo.SugerenciaProducto;
import modelo.UbicacionInventario;

public class MenuAdminInventario {

    private final Club club;
    private final Administrador administrador;
    private final Entrada entrada;

    public MenuAdminInventario(Club club, Administrador administrador, Entrada entrada) {
        this.club = club;
        this.administrador = administrador;
        this.entrada = entrada;
    }

    public void mostrar() {
        int opcion = -1;
        while (opcion != 0) {
            System.out.println();
            System.out.println("--- Inventario, tienda e instalaciones ---");
            System.out.println("1. Ver inventario");
            System.out.println("2. Ver instalaciones y su disponibilidad");
            System.out.println("3. Reabastecer un articulo de la tienda");
            System.out.println("4. Mover articulos entre ubicaciones");
            System.out.println("5. Aprobar o rechazar sugerencias de productos");
            System.out.println("0. Volver");
            opcion = entrada.leerEntero("Opcion", 0, 5);
            try {
                switch (opcion) {
                    case 1: verInventario(); break;
                    case 2: verInstalaciones(); break;
                    case 3: reabastecer(); break;
                    case 4: moverArticulo(); break;
                    case 5: evaluarSugerencias(); break;
                    default: break;
                }
            } catch (ReglaNegocioException e) {
                System.out.println("NO SE PUDO: " + e.getMessage());
            }
        }
    }

    private void verInventario() {
        String ubicacion = null;
        if (entrada.confirmar("Filtrar por una ubicacion")) {
            UbicacionInventario elegida = elegirUbicacion("Ubicacion");
            if (elegida == null) {
                return;
            }
            ubicacion = elegida.getNombre();
        }
        System.out.println("Ubicacion | Producto | Cantidad");
        for (String linea : club.consultarInventario(administrador.getCodigo(), ubicacion)) {
            System.out.println(" - " + linea);
        }
    }

    private void verInstalaciones() {
        List<Instalacion> instalaciones = club.getInstalaciones();
        if (instalaciones.isEmpty()) {
            System.out.println("No hay instalaciones registradas.");
            return;
        }
        boolean revisar = entrada.confirmar("Revisar disponibilidad en una fecha y hora");
        LocalDate fecha = null;
        LocalTime hora = null;
        int duracion = 0;
        if (revisar) {
            fecha = entrada.leerFecha("Fecha");
            hora = entrada.leerHora("Hora");
            duracion = entrada.leerEntero("Duracion en minutos", 30, 240);
        }
        for (Instalacion instalacion : instalaciones) {
            String linea = " - " + instalacion.getNombre() + " | " + instalacion.getDisciplina()
                    + " | " + (instalacion.isTechada() ? "techada" : "al aire libre")
                    + " | reservas: " + instalacion.getReservas().size();
            if (revisar) {
                linea += " | " + (instalacion.disponible(fecha, hora, duracion) ? "DISPONIBLE" : "OCUPADA");
            }
            System.out.println(linea);
        }
    }

    private void reabastecer() {
        List<ArticuloTienda> articulos = new ArrayList<>();
        for (Producto p : club.getProductos()) {
            if (p instanceof ArticuloTienda) {
                articulos.add((ArticuloTienda) p);
            }
        }
        ArticuloTienda articulo = elegirProducto("Articulo", articulos);
        if (articulo == null) {
            return;
        }
        int cantidad = entrada.leerEntero("Cantidad a agregar", 1, 1000);
        club.reabastecer(administrador.getCodigo(), articulo, cantidad);
        System.out.println("LISTO: se agregaron " + cantidad + " unidades de " + articulo.getNombre() + " a la tienda.");
    }

    private void moverArticulo() {
        Producto producto = elegirProducto("Producto", club.getProductos());
        if (producto == null) {
            return;
        }
        UbicacionInventario origen = elegirUbicacion("Origen");
        if (origen == null) {
            return;
        }
        UbicacionInventario destino = elegirUbicacion("Destino");
        if (destino == null) {
            return;
        }
        int cantidad = entrada.leerEntero("Cantidad a mover", 1, 1000);
        club.moverArticulo(administrador.getCodigo(), producto, origen.getNombre(), destino.getNombre(), cantidad);
        System.out.println("LISTO: se movieron " + cantidad + " de " + producto.getNombre() + " de "
                + origen.getNombre() + " a " + destino.getNombre() + ".");
    }

    private void evaluarSugerencias() {
        List<SugerenciaProducto> pendientes = new ArrayList<>();
        for (SugerenciaProducto s : club.consultarSugerencias(administrador.getCodigo())) {
            if (s.getEstado() == EstadoSolicitud.PENDIENTE) {
                pendientes.add(s);
            }
        }
        if (pendientes.isEmpty()) {
            System.out.println("No hay sugerencias pendientes.");
            return;
        }
        SugerenciaProducto sugerencia = entrada.elegir("Sugerencia", pendientes);
        if (sugerencia == null) {
            return;
        }
        if (entrada.confirmar("Aprobar la sugerencia (n = rechazarla)")) {
            club.aprobarSugerencia(administrador.getCodigo(), sugerencia);
            System.out.println("LISTO: sugerencia aprobada.");
        } else {
            club.rechazarSugerencia(administrador.getCodigo(), sugerencia);
            System.out.println("LISTO: sugerencia rechazada.");
        }
    }

    private UbicacionInventario elegirUbicacion(String titulo) {
        List<UbicacionInventario> ubicaciones = club.getUbicaciones();
        if (ubicaciones.isEmpty()) {
            throw new ReglaNegocioException("No hay ubicaciones de inventario");
        }
        for (int i = 0; i < ubicaciones.size(); i++) {
            System.out.println((i + 1) + ". " + ubicaciones.get(i).getNombre());
        }
        int numero = entrada.leerEntero(titulo + " (0 para cancelar)", 0, ubicaciones.size());
        return numero == 0 ? null : ubicaciones.get(numero - 1);
    }

    private <T extends Producto> T elegirProducto(String titulo, List<T> productos) {
        if (productos.isEmpty()) {
            throw new ReglaNegocioException("No hay productos registrados");
        }
        for (int i = 0; i < productos.size(); i++) {
            System.out.println((i + 1) + ". " + productos.get(i).getNombre() + " ($" + productos.get(i).getPrecio() + ")");
        }
        int numero = entrada.leerEntero(titulo + " (0 para cancelar)", 0, productos.size());
        return numero == 0 ? null : productos.get(numero - 1);
    }
}

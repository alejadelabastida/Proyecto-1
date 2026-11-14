
package modelo;

import java.util.Map;

public class VentaCafeteria extends Venta {

    private static final long serialVersionUID = 1L;
    private static final double TASA_CONSUMO = 0.08;

    private double impuestoConsumo;
    private double propina;
    private double descuentoBono;

    public VentaCafeteria(Socio socio,
            Map<Producto, Integer> productos,
            double porcentajePropina, int puntosUsar) {

        this(socio, productos, porcentajePropina, puntosUsar, 0);
    }

    public VentaCafeteria(Socio socio,
            Map<Producto, Integer> productos,
            double porcentajePropina, double porcentajeBono) {

        this(socio, productos, porcentajePropina,
                0, porcentajeBono / 100.0);
    }

    public VentaCafeteria(Usuario comprador,
            Map<Producto, Integer> productos,
            double porcentajePropina,
            int puntosUsar,
            double porcentajeDescuento) {

        super(comprador, productos);

        this.puntosRedimidos = puntosUsar;
        this.descuentoBono =
                redondear(subtotal * porcentajeDescuento);

        double base = subtotal - puntosUsar - descuentoBono;

        this.impuestoConsumo =
                redondear(base * TASA_CONSUMO);

        this.propina =
                redondear(subtotal * porcentajePropina);

        terminar();
    }

    public double getDescuentoBono() {
        return descuentoBono;
    }

    @Override
    public double calcularTotal() {
        return subtotal - puntosRedimidos - descuentoBono
                + impuestoConsumo + propina;
    }

    public double getImpuestoConsumo() {
        return impuestoConsumo;
    }

    public double getPropina() {
        return propina;
    }

    @Override
    public String toString() {
        return "CAFETERIA | " + super.toString()
                + " | puntos usados: " + puntosRedimidos
                + " | descuento: " + descuentoBono
                + " | impuesto consumo: " + impuestoConsumo
                + " | propina: " + propina;
    }
}

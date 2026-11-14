package consola;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Entrada {

    private final Scanner scanner;

    public Entrada(Scanner scanner) {
        this.scanner = scanner;
    }

    private String leerLinea() {
        if (!scanner.hasNextLine()) {
            throw new IllegalStateException("Se cerro la entrada");
        }
        return scanner.nextLine().trim();
    }

    public String leerTexto(String pregunta) {
        while (true) {
            System.out.print(pregunta + ": ");
            String texto = leerLinea();
            if (!texto.isEmpty()) {
                return texto;
            }
            System.out.println("  Debe escribir algo.");
        }
    }

    public int leerEntero(String pregunta, int minimo, int maximo) {
        while (true) {
            System.out.print(pregunta + " (" + minimo + "-" + maximo + "): ");
            String texto = leerLinea();
            try {
                int numero = Integer.parseInt(texto);
                if (numero >= minimo && numero <= maximo) {
                    return numero;
                }
                System.out.println("  El numero debe estar entre " + minimo + " y " + maximo + ".");
            } catch (NumberFormatException e) {
                System.out.println("  Escriba un numero entero.");
            }
        }
    }

    public boolean confirmar(String pregunta) {
        while (true) {
            System.out.print(pregunta + " (s/n): ");
            String texto = leerLinea().toLowerCase();
            if (texto.equals("s") || texto.equals("si")) {
                return true;
            }
            if (texto.equals("n") || texto.equals("no")) {
                return false;
            }
            System.out.println("  Responda s o n.");
        }
    }
    
    public LocalDate leerFecha(String pregunta) {
        while (true) {
            System.out.print(pregunta + " (aaaa-mm-dd): ");
            try {
                return LocalDate.parse(leerLinea());
            } catch (DateTimeParseException e) {
                System.out.println("  Fecha invalida. Ejemplo: 2026-12-15");
            }
        }
    }

    public LocalTime leerHora(String pregunta) {
        while (true) {
            System.out.print(pregunta + " (hh:mm): ");
            try {
                return LocalTime.parse(leerLinea());
            } catch (DateTimeParseException e) {
                System.out.println("  Hora invalida. Ejemplo: 14:30");
            }
        }
    }

    public <T> T elegir(String pregunta, List<T> elementos) {
        for (int i = 0; i < elementos.size(); i++) {
            System.out.println((i + 1) + ". " + elementos.get(i));
        }
        int numero = leerEntero(pregunta + " (0 para cancelar)", 0, elementos.size());
        return (numero == 0) ? null : elementos.get(numero - 1);
    }

    public <T> T elegirValor(String pregunta, T[] valores) {
        for (int i = 0; i < valores.length; i++) {
            System.out.println((i + 1) + ". " + valores[i]);
        }
        return valores[leerEntero(pregunta, 1, valores.length) - 1];
    }
}
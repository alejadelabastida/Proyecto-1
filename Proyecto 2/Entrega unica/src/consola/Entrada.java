package consola;

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
}

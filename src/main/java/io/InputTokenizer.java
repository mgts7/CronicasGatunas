package io;

import errorhandling.InputValidationException;

/**
 * ============================================================
 * InputTokenizer
 * ============================================================
 *
 * Lee el texto de entrada como un flujo de tokens separados por
 * espacios en blanco (segun la seccion 2.2 del enunciado), sin
 * asumir un numero fijo de tokens por linea y tolerando lineas en
 * blanco extra o espacios sobrantes.
 *
 * Por que no se usa java.util.Scanner:
 *   Scanner.nextInt() usa expresiones regulares internamente en
 *   cada llamada, lo cual es notablemente lento para entradas
 *   grandes (Mission 1 puede tener hasta ~10^6 tokens en un grid
 *   de 1000x1000 con muchas bombas). Este tokenizador escanea el
 *   String caracter por caracter una sola vez, sin regex.
 *
 * Por que no se usa java.io.StreamTokenizer:
 *   Su manejo de numeros negativos tiene comportamientos poco
 *   intuitivos en casos borde. Como Mission 3 necesita pesos
 *   negativos (-1000 a 1000), se prefiere un parseo manual y
 *   explicito del signo, mas facil de razonar y de defender.
 *
 * Compartido por las 4 misiones: cada Mission*Parser recibe una
 * instancia de esta clase y la consume segun el formato especifico
 * de su seccion del enunciado.
 */
public final class InputTokenizer {

    private final String input;
    private final int length;
    private int pos;

    public InputTokenizer(String input) {
        this.input = input == null ? "" : input;
        this.length = this.input.length();
        this.pos = 0;
    }

    /**
     * Indica si queda al menos un token por leer (avanza el cursor
     * saltando espacios en blanco, pero no consume el token).
     */
    public boolean hasNextToken() {
        skipWhitespace();
        return pos < length;
    }

    /** Lee el siguiente token como int. */
    public int nextInt() {
        return (int) nextLong();
    }

    /**
     * Lee el siguiente token como long (usado en Missions 2 y 3,
     * donde los pesos acumulados deben guardarse en long).
     */
    public long nextLong() {
        skipWhitespace();

        if (pos >= length) {
            throw new InputValidationException(
                    "Entrada incompleta: se esperaba un numero pero se llego al final del texto.");
        }

        int tokenStart = pos;

        if (input.charAt(pos) == '-') {
            pos++;
        }

        int digitsStart = pos;
        while (pos < length && Character.isDigit(input.charAt(pos))) {
            pos++;
        }

        if (pos == digitsStart) {
            String badToken = readRawToken(tokenStart);
            throw new InputValidationException(
                    "Token invalido, se esperaba un numero entero: '" + badToken + "'");
        }

        String token = input.substring(tokenStart, pos);
        try {
            return Long.parseLong(token);
        } catch (NumberFormatException e) {
            throw new InputValidationException("Numero invalido en la entrada: '" + token + "'", e);
        }
    }

    private void skipWhitespace() {
        while (pos < length && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
    }

    /** Extrae el token crudo (no numerico) desde tokenStart, para mensajes de error. */
    private String readRawToken(int tokenStart) {
        int end = tokenStart;
        while (end < length && !Character.isWhitespace(input.charAt(end))) {
            end++;
        }
        return input.substring(tokenStart, end);
    }
}
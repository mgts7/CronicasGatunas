package errorhandling;

/**
 * Se lanza cuando el texto pegado en la GUI no respeta el formato
 * esperado (tokens faltantes, valores fuera de rango, numeros
 * invalidos, etc.).
 *
 * Es una RuntimeException a proposito: los parsers no deben forzar
 * a cada llamador a envolver todo en try/catch de una excepcion
 * checked. La GUI es responsable de capturarla en el punto donde
 * se dispara el parseo y mostrar el mensaje (getMessage()) en un
 * area de error visible, en vez de dejar que el stack trace llegue
 * a la consola o que la aplicacion falle en silencio.
 */
public class InputValidationException extends RuntimeException {

    public InputValidationException(String message) {
        super(message);
    }

    public InputValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

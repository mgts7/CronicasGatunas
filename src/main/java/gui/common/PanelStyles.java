package gui.common;

/**
 * Estilos CSS inline reutilizados por las 4 Mission*View, para que
 * los textos se lean bien encima de las imagenes de fondo
 * (independientemente de que colores tenga cada imagen especifica).
 */
public final class PanelStyles {

    private PanelStyles() {
        // Utility class: no se instancia.
    }

    /** Recuadro semi-transparente oscuro, con esquinas redondeadas. Se ajusta al contenido (ver setMaxWidth(USE_PREF_SIZE) en el sitio de uso). */
    public static final String PANEL =
            "-fx-background-color: rgba(20, 20, 20, 0.65); "
                    + "-fx-background-radius: 8; "
                    + "-fx-padding: 8 14 8 14;";

    public static final String TITLE_TEXT = "-fx-text-fill: white; -fx-font-weight: bold;";
    public static final String INFO_TEXT = "-fx-text-fill: #f0f0f0;";
    public static final String ERROR_TEXT = "-fx-text-fill: #ff6b6b; -fx-font-weight: bold;";
}
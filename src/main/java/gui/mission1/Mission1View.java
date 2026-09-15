package gui.mission1;

import algorithm.mission1.BFSSolver;
import algorithm.mission1.DFSSolver;
import errorhandling.InputValidationException;
import io.Mission1Parser;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import model.result.Mission1Result;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * Mission1View
 * ============================================================
 *
 * Pantalla completa de Mision 1. La seccion mas grande de la vista
 * son los dos GridCanvas (BFS y DFS) lado a lado; el resto son
 * controles compactos alrededor:
 *   - Barra superior: volver al menu principal + mute/unmute.
 *   - Barra inferior: Generar (caso aleatorio) / Insertar (pegar
 *     input propio) / Resolver (corre BFS+DFS sobre lo que este
 *     cargado actualmente), mas una linea de estado/output.
 *
 * Al abrir la mision, el caso de ejemplo del enunciado ya viene
 * resuelto automaticamente (sin que el usuario tenga que hacer
 * click en nada), para que se vea algo interesante de inmediato.
 */
public final class Mission1View extends StackPane {

    private static final String BACKGROUND_IMAGE_PATH = "/images/Mission1BG.jpeg";
    private static final String TITLE_IMAGE_PATH = "/images/Mission1Title.png";

    // Recuadro semi-transparente detras de los textos, para que se
    // lean bien encima de la imagen de fondo sin importar sus colores.
    private static final String PANEL_STYLE =
            "-fx-background-color: rgba(20, 20, 20, 0.65); "
                    + "-fx-background-radius: 8; "
                    + "-fx-padding: 8 14 8 14;";

    private static final String TITLE_TEXT_STYLE = "-fx-text-fill: white; -fx-font-weight: bold;";
    private static final String INFO_TEXT_STYLE = "-fx-text-fill: #f0f0f0;";
    private static final String ERROR_TEXT_STYLE = "-fx-text-fill: #ff6b6b; -fx-font-weight: bold;";

    private static final String SAMPLE_INPUT =
            "10 10\n9\n0 1 2\n1 1 2\n2 2 2 9\n3 2 1 7\n5 3 3 6 9\n"
                    + "6 4 0 1 2 7\n7 3 0 3 8\n8 2 7 9\n9 3 2 3 4\n0 0\n9 9\n0 0\n";

    private final Runnable onBackToMenu;
    private final RandomMission1Generator generator = new RandomMission1Generator();

    private final Label statusLabel = new Label();
    private final ComboBox<Integer> caseSelector = new ComboBox<>();

    private final GridCanvas bfsCanvas = new GridCanvas();
    private final GridCanvas dfsCanvas = new GridCanvas();
    private final Label bfsMessage = new Label();
    private final Label dfsMessage = new Label();

    private String currentInputText = SAMPLE_INPUT;
    private List<Mission1Parser.TestCase> parsedCases = new ArrayList<>();
    private List<Mission1Result> results = new ArrayList<>();

    private boolean muted = false;

    public Mission1View(Runnable onBackToMenu) {
        this.onBackToMenu = onBackToMenu;

        ImageView background = buildBackgroundImageView();

        BorderPane foreground = new BorderPane();
        foreground.setPadding(new Insets(10));
        foreground.setTop(buildTopBar());
        foreground.setCenter(buildGridsSection());
        foreground.setBottom(buildBottomBar());

        getChildren().addAll(background, foreground);

        // El sample ya viene resuelto al abrir la mision, sin que el
        // usuario tenga que hacer click en Resolver.
        solveCurrentInput();
    }

    /**
     * La imagen de fondo se estira para llenar exactamente el
     * StackPane (bindings a su ancho/alto), sin preservar aspect
     * ratio: como el diseño de esta imagen es un "marco" con el
     * centro simple y liso (ver el prompt de generacion), estirarla
     * un poco al redimensionar la ventana no se nota.
     */
    private ImageView buildBackgroundImageView() {
        Image image = loadBackgroundImage();
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(false);
        imageView.fitWidthProperty().bind(widthProperty());
        imageView.fitHeightProperty().bind(heightProperty());
        return imageView;
    }

    private Image loadBackgroundImage() {
        InputStream stream = getClass().getResourceAsStream(BACKGROUND_IMAGE_PATH);
        if (stream == null) {
            throw new IllegalStateException(
                    "No se encontro la imagen de fondo en " + BACKGROUND_IMAGE_PATH
                            + ". Verifica que el archivo este en src/main/resources" + BACKGROUND_IMAGE_PATH
                            + " y que el nombre coincida exactamente (sensible a mayusculas/minusculas).");
        }
        return new Image(stream);
    }

    // ---------------------------------------------------------
    // Barra superior: volver al menu + mute
    // ---------------------------------------------------------

    private HBox buildTopBar() {
        Button backButton = new Button("< Volver al menu");
        backButton.setOnAction(e -> onBackToMenu.run());

        // 1. Crear el ImageView para el título
        ImageView titleImageView = new ImageView();
        InputStream stream = getClass().getResourceAsStream(TITLE_IMAGE_PATH);
        if (stream != null) {
            titleImageView.setImage(new Image(stream));
            titleImageView.setPreserveRatio(true);
            // Ajusta este valor dependiendo de qué tan grande sea tu PNG original
            titleImageView.setFitHeight(130);
        } else {
            System.err.println("Advertencia: No se encontró la imagen en " + TITLE_IMAGE_PATH);
        }

        ToggleButton muteButton = new ToggleButton("Sonido: ON");
        muteButton.setOnAction(e -> {
            muted = muteButton.isSelected();
            muteButton.setText(muted ? "Sonido: OFF" : "Sonido: ON");
            // TODO: cuando exista un AudioManager con musica/efectos,
            // conectar aqui: AudioManager.getInstance().setMuted(muted);
        });
        muteButton.setOpacity(0);

        Region spacerLeft = new Region();
        Region spacerRight = new Region();
        HBox.setHgrow(spacerLeft, Priority.ALWAYS);
        HBox.setHgrow(spacerRight, Priority.ALWAYS);

        // 2. Reemplazar 'title' por 'titleImageView' en el HBox
        HBox bar = new HBox(10, backButton, spacerLeft, titleImageView, spacerRight, muteButton);
        bar.setAlignment(Pos.CENTER);
        return bar;
    }

    // ---------------------------------------------------------
    // Seccion central: los dos grids (la parte mas grande de la vista)
    // ---------------------------------------------------------

    private HBox buildGridsSection() {
        Label bfsTitle = new Label("BFS (camino mas corto garantizado)");
        bfsTitle.setStyle(TITLE_TEXT_STYLE);
        bfsMessage.setStyle(INFO_TEXT_STYLE);

        Label dfsTitle = new Label("DFS (orden fijo: arriba, abajo, izquierda, derecha)");
        dfsTitle.setStyle(TITLE_TEXT_STYLE);
        dfsMessage.setStyle(INFO_TEXT_STYLE);

        VBox bfsHeader = new VBox(2, bfsTitle, bfsMessage);
        bfsHeader.setStyle(PANEL_STYLE);
        bfsHeader.setMaxWidth(Region.USE_PREF_SIZE); // que el recuadro se ajuste al texto, no al ancho de la columna
        bfsHeader.setAlignment(Pos.CENTER);

        VBox dfsHeader = new VBox(2, dfsTitle, dfsMessage);
        dfsHeader.setStyle(PANEL_STYLE);
        dfsHeader.setMaxWidth(Region.USE_PREF_SIZE);
        dfsHeader.setAlignment(Pos.CENTER);

        VBox bfsBox = new VBox(6, bfsHeader, wrapInScroll(bfsCanvas));
        bfsBox.setAlignment(Pos.TOP_CENTER); // centra el recuadro (mas angosto) sobre el grid (mas ancho)
        VBox dfsBox = new VBox(6, dfsHeader, wrapInScroll(dfsCanvas));
        dfsBox.setAlignment(Pos.TOP_CENTER);

        HBox.setHgrow(bfsBox, Priority.ALWAYS);
        HBox.setHgrow(dfsBox, Priority.ALWAYS);

        HBox row = new HBox(16, bfsBox, dfsBox);
        row.setAlignment(Pos.CENTER);
        row.setPadding(new Insets(0, 30, 0, 30));
        return row;
    }

    private ScrollPane wrapInScroll(GridCanvas canvas) {
        ScrollPane scrollPane = new ScrollPane(canvas);
        scrollPane.setPrefViewportWidth(480);
        scrollPane.setPrefViewportHeight(480);
        // Por defecto el ScrollPane tiene un fondo gris solido que
        // taparia la imagen de fondo alrededor del grid; se hace
        // transparente tanto el control como su viewport interno.
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        return scrollPane;
    }

    // ---------------------------------------------------------
    // Barra inferior: Generar / Insertar / Resolver + estado
    // ---------------------------------------------------------

    private VBox buildBottomBar() {
        statusLabel.setWrapText(true);

        Label caseLabel = new Label("Caso a visualizar:");
        caseLabel.setStyle(TITLE_TEXT_STYLE);
        caseSelector.setOnAction(e -> updateVisualization());
        HBox caseRow = new HBox(8, caseLabel, caseSelector);
        caseRow.setAlignment(Pos.CENTER_LEFT);

        Button generateButton = new Button("Generar");
        generateButton.setOnAction(e -> onGenerate());

        Button insertButton = new Button("Insertar");
        insertButton.setOnAction(e -> onInsert());

        Button solveButton = new Button("Resolver");
        solveButton.setOnAction(e -> solveCurrentInput());

        HBox actionRow = new HBox(10, generateButton, insertButton, solveButton);
        actionRow.setAlignment(Pos.CENTER);

        VBox box = new VBox(8, statusLabel, caseRow, actionRow);
        box.setPadding(new Insets(8, 0, 0, 0));
        box.setStyle(PANEL_STYLE);
        box.setMaxWidth(Region.USE_PREF_SIZE); // que el recuadro se ajuste al contenido, no a todo el ancho de la ventana
        box.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(box, Pos.CENTER); // centrado dentro de la region "bottom" del BorderPane, en vez de estirado
        setInfo(""); // aplica el estilo de texto inicial a statusLabel
        return box;
    }

    // ---------------------------------------------------------
    // Acciones de los 3 botones
    // ---------------------------------------------------------

    private void onGenerate() {
        currentInputText = generator.generate();

        // Al generar, se muestra el mapa (pasto + minas) de inmediato,
        // pero SIN resolver: se le pasa null como path para que
        // GridCanvas dibuje solo el terreno, sin el camino/solucion
        // (eso se revela unicamente al presionar "Resolver").
        try {
            List<Mission1Parser.TestCase> preview = Mission1Parser.parse(currentInputText);
            if (!preview.isEmpty()) {
                previewMapOnly(preview.get(0));
            }
        } catch (InputValidationException ex) {
            // El generador siempre produce texto valido; si esto
            // llegara a pasar seria un bug del generador, no del usuario.
            setError("Error interno generando el caso: " + ex.getMessage());
            return;
        }

        setInfo("Nuevo caso generado. Presiona \"Resolver\" para ver el camino.");
    }

    /** Dibuja unicamente el terreno (pasto/minas) de un caso, sin ningun camino resaltado. */
    private void previewMapOnly(Mission1Parser.TestCase tc) {
        boolean drawnBfs = bfsCanvas.render(
                tc.getGrid(), tc.getStartRow(), tc.getStartCol(), tc.getEndRow(), tc.getEndCol(), null);
        boolean drawnDfs = dfsCanvas.render(
                tc.getGrid(), tc.getStartRow(), tc.getStartCol(), tc.getEndRow(), tc.getEndCol(), null);

        bfsMessage.setText(drawnBfs ? "" : "Grid demasiado grande para dibujar.");
        dfsMessage.setText(drawnDfs ? "" : "Grid demasiado grande para dibujar.");

        // El caso generado todavia no esta "resuelto": se limpia el
        // estado anterior para que el selector de casos no quede
        // mostrando resultados viejos que ya no corresponden.
        parsedCases = new ArrayList<>();
        results = new ArrayList<>();
        caseSelector.getItems().clear();
    }

    private void onInsert() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Insertar input de Mision 1");
        dialog.setHeaderText("Pega el input en el formato de la seccion 3 del enunciado.");

        TextArea textArea = new TextArea(currentInputText);
        textArea.setPrefRowCount(12);
        textArea.setPrefColumnCount(40);
        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> buttonType == ButtonType.OK ? textArea.getText() : null);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(text -> {
            currentInputText = text;

            // Igual que en "Generar": se previsualiza el mapa de
            // inmediato para que quede claro que el input SI se
            // cargo, sin esperar a que se presione "Resolver".
            try {
                List<Mission1Parser.TestCase> preview = Mission1Parser.parse(currentInputText);
                if (!preview.isEmpty()) {
                    previewMapOnly(preview.get(0));
                }
                setInfo("Input cargado. Presiona \"Resolver\" para ver el camino.");
            } catch (InputValidationException ex) {
                setError("Error en el input: " + ex.getMessage());
            }
        });
    }

    private void solveCurrentInput() {
        try {
            parsedCases = Mission1Parser.parse(currentInputText);
            results = new ArrayList<>();
            caseSelector.getItems().clear();

            int caseNumber = 1;
            StringBuilder output = new StringBuilder();
            for (Mission1Parser.TestCase tc : parsedCases) {
                BFSSolver.Result bfsResult = BFSSolver.solve(
                        tc.getGrid(), tc.getStartRow(), tc.getStartCol(), tc.getEndRow(), tc.getEndCol());
                DFSSolver.Result dfsResult = DFSSolver.solve(
                        tc.getGrid(), tc.getStartRow(), tc.getStartCol(), tc.getEndRow(), tc.getEndCol());

                Mission1Result result = Mission1Result.of(caseNumber, bfsResult, dfsResult);
                results.add(result);
                output.append(result.toOutputLine()).append("  ");
                caseSelector.getItems().add(caseNumber);
                caseNumber++;
            }

            setInfo(output.toString().trim());

            if (!caseSelector.getItems().isEmpty()) {
                caseSelector.getSelectionModel().selectFirst();
                updateVisualization();
            }

        } catch (InputValidationException ex) {
            // Requisito 2.2: "Malformed input must produce a readable
            // error message inside the GUI, never a stack trace and
            // never a silent failure."
            setError("Error en el input: " + ex.getMessage());
            parsedCases = new ArrayList<>();
            results = new ArrayList<>();
            caseSelector.getItems().clear();
        }
    }

    private void updateVisualization() {
        Integer selected = caseSelector.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        int index = selected - 1;
        Mission1Parser.TestCase tc = parsedCases.get(index);
        Mission1Result result = results.get(index);

        renderPath(bfsCanvas, bfsMessage, tc, result.getBfsPath(), result.isReachable());
        renderPath(dfsCanvas, dfsMessage, tc, result.getDfsPath(), result.isReachable());
    }

    private void renderPath(GridCanvas canvas, Label message, Mission1Parser.TestCase tc, int[][] path, boolean reachable) {
        if (!reachable) {
            message.setText("Nina is unreachable: no hay camino que mostrar.");
            canvas.setWidth(0);
            canvas.setHeight(0);
            return;
        }

        boolean drawn = canvas.render(
                tc.getGrid(), tc.getStartRow(), tc.getStartCol(), tc.getEndRow(), tc.getEndCol(), path);

        if (!drawn) {
            message.setText("Grid demasiado grande para dibujar (> "
                    + GridCanvas.getMaxDrawableDimension() + "x" + GridCanvas.getMaxDrawableDimension()
                    + "). La respuesta numerica ya se muestra arriba.");
        } else {
            message.setText("");
        }
    }

    // ---------------------------------------------------------
    // Estado (info vs error) en la misma etiqueta
    // ---------------------------------------------------------

    private void setInfo(String text) {
        statusLabel.setText(text);
        statusLabel.setStyle(INFO_TEXT_STYLE);
    }

    private void setError(String text) {
        statusLabel.setText(text);
        statusLabel.setStyle(ERROR_TEXT_STYLE);
    }
}
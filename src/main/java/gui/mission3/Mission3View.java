package gui.mission3;

import algorithm.mission3.BellmanFordSolver;
import algorithm.mission3.FloydWarshallSolver;
import errorhandling.InputValidationException;
import gui.common.PanelStyles;
import io.Mission3Parser;
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
import model.result.Mission3Result;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * Mission3View
 * ============================================================
 *
 * Pantalla completa de Mision 3. La seccion central tiene DOS
 * paneles lado a lado: el GraphCanvas (mapa del tesoro con la ruta
 * o el ciclo resaltado) y la MatrixTableView (matriz N x N completa,
 * exigida por la seccion 5 y con panel scrollable por la seccion 2.3).
 *
 * Misma dinamica que Mission1View/Mission2View: barra superior
 * (volver/mute), barra inferior (Generar/Insertar/Resolver + estado),
 * sample del enunciado ya resuelto al abrir.
 */
public final class Mission3View extends StackPane {

    private static final String BACKGROUND_IMAGE_PATH = "/images/Mission3BG.jpeg";
    private static final String TITLE_IMAGE_PATH = "/images/Mission3Title.png";

    private static final String SAMPLE_INPUT =
            "3\n5 7 0 4\n0 1 50\n0 2 10\n1 2 -30\n1 3 40\n2 1 -5\n2 3 60\n3 4 20\n"
                    + "4 4 0 3\n0 1 20\n1 2 30\n2 1 -10\n2 3 15\n"
                    + "3 3 0 2\n0 1 -40\n1 2 -25\n0 2 -80\n";

    private final Runnable onBackToMenu;
    private final RandomMission3Generator generator = new RandomMission3Generator();

    private final Label statusLabel = new Label();
    private final ComboBox<Integer> caseSelector = new ComboBox<>();

    private final GraphCanvas graphCanvas = new GraphCanvas();
    private final Label graphMessage = new Label();
    private final MatrixTableView matrixTableView = new MatrixTableView();

    private String currentInputText = SAMPLE_INPUT;
    private List<Mission3Parser.TestCase> parsedCases = new ArrayList<>();
    private List<Mission3Result> results = new ArrayList<>();

    private boolean muted = false;

    public Mission3View(Runnable onBackToMenu) {
        this.onBackToMenu = onBackToMenu;

        ImageView background = buildBackgroundImageView();

        BorderPane foreground = new BorderPane();
        foreground.setPadding(new Insets(10));
        foreground.setTop(buildTopBar());
        foreground.setCenter(buildCenterSection());
        foreground.setBottom(buildBottomBar());

        getChildren().addAll(background, foreground);

        solveCurrentInput();
    }

    // ---------------------------------------------------------
    // Imagen de fondo
    // ---------------------------------------------------------

    private ImageView buildBackgroundImageView() {
        Image image = loadImage(BACKGROUND_IMAGE_PATH);
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(false);
        imageView.fitWidthProperty().bind(widthProperty());
        imageView.fitHeightProperty().bind(heightProperty());
        return imageView;
    }

    private Image loadImage(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            throw new IllegalStateException(
                    "No se encontro la imagen en " + path
                            + ". Verifica que el archivo este en src/main/resources" + path
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

        ImageView titleImageView = new ImageView();
        InputStream stream = getClass().getResourceAsStream(TITLE_IMAGE_PATH);
        if (stream != null) {
            titleImageView.setImage(new Image(stream));
            titleImageView.setFitWidth(600);
            // Ajusta este valor dependiendo de qué tan grande sea tu PNG original
            titleImageView.setFitHeight(150);
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

        Region spacerLeft = new Region();
        Region spacerRight = new Region();
        HBox.setHgrow(spacerLeft, Priority.ALWAYS);
        HBox.setHgrow(spacerRight, Priority.ALWAYS);

        HBox bar = new HBox(10, backButton, spacerLeft, titleImageView, spacerRight, muteButton);
        bar.setAlignment(Pos.CENTER);
        return bar;
    }

    // ---------------------------------------------------------
    // Seccion central: grafo + matriz lado a lado
    // ---------------------------------------------------------

    private HBox buildCenterSection() {
        graphMessage.setStyle(PanelStyles.INFO_TEXT);

        ScrollPane graphScroll = new ScrollPane(graphCanvas);
        graphScroll.setPrefViewportWidth(500);
        graphScroll.setPrefViewportHeight(500);
        graphScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox graphBox = new VBox(6, graphMessage, graphScroll);
        graphBox.setAlignment(Pos.CENTER);

        VBox matrixBox = new VBox(matrixTableView);
        matrixBox.setMaxWidth(600);
        matrixBox.setAlignment(Pos.CENTER);

        HBox row = new HBox(16, graphBox, matrixBox);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    // ---------------------------------------------------------
    // Barra inferior: Generar / Insertar / Resolver + estado
    // ---------------------------------------------------------

    private VBox buildBottomBar() {
        statusLabel.setWrapText(true);

        Label caseLabel = new Label("Caso a visualizar:");
        caseLabel.setStyle(PanelStyles.TITLE_TEXT);
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
        box.setStyle(PanelStyles.PANEL);
        box.setMaxWidth(Region.USE_PREF_SIZE);
        box.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(box, Pos.CENTER);
        setInfo("");
        return box;
    }

    // ---------------------------------------------------------
    // Acciones de los 3 botones
    // ---------------------------------------------------------

    private void onGenerate() {
        currentInputText = generator.generate();

        try {
            List<Mission3Parser.TestCase> preview = Mission3Parser.parse(currentInputText);
            if (!preview.isEmpty()) {
                previewGraphOnly(preview.get(0));
            }
            setInfo("Nuevo mapa generado. Presiona \"Resolver\" para ver la ruta.");
        } catch (InputValidationException ex) {
            setError("Error interno generando el caso: " + ex.getMessage());
        }
    }

    private void onInsert() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Insertar input de Mision 3");
        dialog.setHeaderText("Pega el input en el formato de la seccion 5 del enunciado.");

        TextArea textArea = new TextArea(currentInputText);
        textArea.setPrefRowCount(12);
        textArea.setPrefColumnCount(40);
        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> buttonType == ButtonType.OK ? textArea.getText() : null);

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(text -> {
            currentInputText = text;
            try {
                List<Mission3Parser.TestCase> preview = Mission3Parser.parse(currentInputText);
                if (!preview.isEmpty()) {
                    previewGraphOnly(preview.get(0));
                }
                setInfo("Input cargado. Presiona \"Resolver\" para ver la ruta.");
            } catch (InputValidationException ex) {
                setError("Error en el input: " + ex.getMessage());
            }
        });
    }

    /** Dibuja unicamente el grafo (sin ruta ni ciclo resaltado) y calcula la matriz igual, ya que Floyd-Warshall no cuesta nada extra. */
    private void previewGraphOnly(Mission3Parser.TestCase tc) {
        boolean drawn = graphCanvas.render(tc.getGraph(), tc.getN(), tc.getSource(), tc.getDestination(), null, false);
        graphMessage.setText(drawn ? "" : "Grafo demasiado grande para dibujar.");

        FloydWarshallSolver.Result fw = FloydWarshallSolver.solve(tc.getGraph(), tc.getN());
        matrixTableView.render(fw.getMatrix(), fw.getUnboundedMatrix(), tc.getSource(), tc.getDestination());

        parsedCases = new ArrayList<>();
        results = new ArrayList<>();
        caseSelector.getItems().clear();
    }

    private void solveCurrentInput() {
        try {
            parsedCases = Mission3Parser.parse(currentInputText);
            results = new ArrayList<>();
            caseSelector.getItems().clear();

            int caseNumber = 1;
            StringBuilder output = new StringBuilder();
            for (Mission3Parser.TestCase tc : parsedCases) {
                FloydWarshallSolver.Result fw = FloydWarshallSolver.solve(tc.getGraph(), tc.getN());
                BellmanFordSolver.Result bf = BellmanFordSolver.solve(tc.getGraph(), tc.getN(), tc.getSource());

                Mission3Result result = Mission3Result.of(caseNumber, fw, bf, tc.getSource(), tc.getDestination());
                results.add(result);
                output.append(result.toOutputLine()).append("  ");
                caseSelector.getItems().add(caseNumber);
                caseNumber++;

                if (result.isCrossCheckMismatch()) {
                    // Requisito explicito de la seccion 5: "the GUI must
                    // report a mismatch if the two ever disagree".
                    output.append("[ADVERTENCIA: Floyd-Warshall y Bellman-Ford no coinciden] ");
                }
            }

            setInfo(output.toString().trim());

            if (!caseSelector.getItems().isEmpty()) {
                caseSelector.getSelectionModel().selectFirst();
                updateVisualization();
            }

        } catch (InputValidationException ex) {
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
        Mission3Parser.TestCase tc = parsedCases.get(index);
        Mission3Result result = results.get(index);

        matrixTableView.render(result.getMatrix(), result.getUnboundedMatrix(), tc.getSource(), tc.getDestination());

        int[] highlightNodes;
        boolean isCycle;
        switch (result.getClassification()) {
            case FINITE:
                highlightNodes = result.getRoute();
                isCycle = false;
                break;
            case INFINITE_CHURUN:
                highlightNodes = result.getCycle();
                isCycle = true;
                break;
            default: // LIMON_BLOCKED
                highlightNodes = null;
                isCycle = false;
                break;
        }

        boolean drawn = graphCanvas.render(
                tc.getGraph(), tc.getN(), tc.getSource(), tc.getDestination(), highlightNodes, isCycle);

        graphMessage.setText(drawn ? ""
                : "Grafo demasiado grande para dibujar (> " + GraphCanvas.getMaxDrawableNodes()
                + " nodos). La respuesta ya se muestra arriba.");
    }

    // ---------------------------------------------------------
    // Estado (info vs error) en la misma etiqueta
    // ---------------------------------------------------------

    private void setInfo(String text) {
        statusLabel.setText(text);
        statusLabel.setStyle(PanelStyles.INFO_TEXT);
    }

    private void setError(String text) {
        statusLabel.setText(text);
        statusLabel.setStyle(PanelStyles.ERROR_TEXT);
    }
}
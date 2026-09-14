package gui.mission2;

import algorithm.mission2.DijkstraSolver;
import errorhandling.InputValidationException;
import gui.common.PanelStyles;
import io.Mission2Parser;
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
import model.result.Mission2Result;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * Mission2View
 * ============================================================
 *
 * Pantalla completa de Mision 2. A diferencia de Mission1View (que
 * tiene dos grids, uno por algoritmo), aqui hay un unico GraphCanvas
 * grande y centrado, ya que solo hay un algoritmo (Dijkstra).
 *
 * Misma dinamica que Mission1View: barra superior (volver/mute),
 * barra inferior (Generar/Insertar/Resolver + estado), y el caso de
 * ejemplo del enunciado ya viene resuelto al abrir la mision.
 */
public final class Mission2View extends StackPane {

    private static final String BACKGROUND_IMAGE_PATH = "/images/Mission2BG.jpeg";
    private static final String TITLE_IMAGE_PATH = "/images/Mission2Title.png";

    private static final String SAMPLE_INPUT =
            "3\n2 1 0 1\n0 1 100\n3 3 2 0\n0 1 100\n0 2 200\n1 2 50\n2 0 0 1\n";

    private final Runnable onBackToMenu;
    private final RandomMission2Generator generator = new RandomMission2Generator();

    private final Label statusLabel = new Label();
    private final ComboBox<Integer> caseSelector = new ComboBox<>();

    private final GraphCanvas graphCanvas = new GraphCanvas();
    private final Label graphMessage = new Label();

    private String currentInputText = SAMPLE_INPUT;
    private List<Mission2Parser.TestCase> parsedCases = new ArrayList<>();
    private List<Mission2Result> results = new ArrayList<>();

    private boolean muted = false;

    public Mission2View(Runnable onBackToMenu) {
        this.onBackToMenu = onBackToMenu;

        ImageView background = buildBackgroundImageView();

        BorderPane foreground = new BorderPane();
        foreground.setPadding(new Insets(10));
        foreground.setTop(buildTopBar());
        foreground.setCenter(buildGraphSection());
        foreground.setBottom(buildBottomBar());

        getChildren().addAll(background, foreground);

        // El sample ya viene resuelto al abrir la mision.
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
            titleImageView.setFitWidth(700);
            // Ajusta este valor dependiendo de qué tan grande sea tu PNG original
            titleImageView.setFitHeight(170);
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
    // Seccion central: la red (unico GraphCanvas, grande y centrado)
    // ---------------------------------------------------------

    private VBox buildGraphSection() {
        StackPane canvasWrapper = new StackPane(graphCanvas);

        ScrollPane scrollPane = new ScrollPane(canvasWrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefViewportWidth(560);
        scrollPane.setPrefViewportHeight(560);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox box = new VBox(6, scrollPane);
        box.setAlignment(Pos.CENTER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return box;
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
            List<Mission2Parser.TestCase> preview = Mission2Parser.parse(currentInputText);
            if (!preview.isEmpty()) {
                previewGraphOnly(preview.get(0));
            }
            setInfo("Nueva red generada. Presiona \"Resolver\" para ver la ruta.");
        } catch (InputValidationException ex) {
            setError("Error interno generando el caso: " + ex.getMessage());
        }
    }

    private void onInsert() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Insertar input de Mision 2");
        dialog.setHeaderText("Pega el input en el formato de la seccion 4 del enunciado.");

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
                List<Mission2Parser.TestCase> preview = Mission2Parser.parse(currentInputText);
                if (!preview.isEmpty()) {
                    previewGraphOnly(preview.get(0));
                }
                setInfo("Input cargado. Presiona \"Resolver\" para ver la ruta.");
            } catch (InputValidationException ex) {
                setError("Error en el input: " + ex.getMessage());
            }
        });
    }

    /** Dibuja unicamente la red (nodos + conexiones), sin ningun camino resaltado. */
    private void previewGraphOnly(Mission2Parser.TestCase tc) {
        boolean drawn = graphCanvas.render(
                tc.getGraph(), tc.getGraph().getNumNodes(), tc.getSource(), tc.getDestination(), null);

        graphMessage.setText(drawn ? "" : "Red demasiado grande para dibujar.");

        parsedCases = new ArrayList<>();
        results = new ArrayList<>();
        caseSelector.getItems().clear();
    }

    private void solveCurrentInput() {
        try {
            parsedCases = Mission2Parser.parse(currentInputText);
            results = new ArrayList<>();
            caseSelector.getItems().clear();

            int caseNumber = 1;
            StringBuilder output = new StringBuilder();
            for (Mission2Parser.TestCase tc : parsedCases) {
                DijkstraSolver.Result dijkstraResult = DijkstraSolver.solve(
                        tc.getGraph(), tc.getSource(), tc.getDestination());

                Mission2Result result = Mission2Result.of(caseNumber, dijkstraResult);
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
        Mission2Parser.TestCase tc = parsedCases.get(index);
        Mission2Result result = results.get(index);

        if (!result.isReachable()) {
            graphMessage.setText("Nina is very sad: no hay ruta que mostrar.");
            graphCanvas.render(tc.getGraph(), tc.getGraph().getNumNodes(), tc.getSource(), tc.getDestination(), null);
            return;
        }

        boolean drawn = graphCanvas.render(
                tc.getGraph(), tc.getGraph().getNumNodes(), tc.getSource(), tc.getDestination(), result.getPath());

        graphMessage.setText(drawn ? ""
                : "Red demasiado grande para dibujar (> " + GraphCanvas.getMaxDrawableNodes()
                + " nodos). La respuesta numerica ya se muestra arriba.");
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
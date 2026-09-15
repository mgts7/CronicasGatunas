package gui.mission4;

import algorithm.mission4.KruskalSolver;
import errorhandling.InputValidationException;
import gui.common.PanelStyles;
import io.Mission4Parser;
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
import model.result.Mission4Result;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ============================================================
 * Mission4View
 * ============================================================
 *
 * Pantalla completa de Mision 4. Un unico NetworkCanvas grande y
 * centrado, con tema "techos de ciudad": los cables disponibles se
 * ven tenues, y los que forman el MST (Kruskal) se resaltan en
 * color calido.
 *
 * Misma dinamica que las demas misiones: barra superior
 * (volver/mute), barra inferior (Generar/Insertar/Resolver +
 * estado), sample del enunciado ya resuelto al abrir.
 */
public final class Mission4View extends StackPane {

    private static final String BACKGROUND_IMAGE_PATH = "/images/Mission4BG.jpeg";
    private static final String TITLE_IMAGE_PATH = "/images/Mission4Title.png";

    private static final String SAMPLE_INPUT =
            "1\n4\n5\n1 2 10\n2 3 20\n3 4 30\n4 1 40\n1 3 15\n";

    private final Runnable onBackToMenu;
    private final RandomMission4Generator generator = new RandomMission4Generator();

    private final Label statusLabel = new Label();
    private final ComboBox<Integer> caseSelector = new ComboBox<>();

    private final NetworkCanvas networkCanvas = new NetworkCanvas();
    private final Label networkMessage = new Label();

    private String currentInputText = SAMPLE_INPUT;
    private List<Mission4Parser.TestCase> parsedCases = new ArrayList<>();
    private List<Mission4Result> results = new ArrayList<>();

    private boolean muted = false;

    public Mission4View(Runnable onBackToMenu) {
        this.onBackToMenu = onBackToMenu;

        ImageView background = buildBackgroundImageView();

        BorderPane foreground = new BorderPane();
        foreground.setPadding(new Insets(10));
        foreground.setTop(buildTopBar());
        foreground.setCenter(buildNetworkSection());
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
            titleImageView.setPreserveRatio(true);
            // Ajusta este valor dependiendo de qué tan grande sea tu PNG original
            titleImageView.setFitHeight(180);
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

        HBox bar = new HBox(10, backButton, spacerLeft, titleImageView, spacerRight, muteButton);
        bar.setAlignment(Pos.CENTER);
        return bar;
    }

    // ---------------------------------------------------------
    // Seccion central: la red (unico NetworkCanvas, grande y centrado)
    // ---------------------------------------------------------

    private VBox buildNetworkSection() {


        StackPane canvasWrapper = new StackPane(networkCanvas);

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
            List<Mission4Parser.TestCase> preview = Mission4Parser.parse(currentInputText);
            if (!preview.isEmpty()) {
                previewNetworkOnly(preview.get(0));
            }
            setInfo("Nueva red generada. Presiona \"Resolver\" para ver el MST.");
        } catch (InputValidationException ex) {
            setError("Error interno generando el caso: " + ex.getMessage());
        }
    }

    private void onInsert() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Insertar input de Mision 4");
        dialog.setHeaderText("Pega el input en el formato de la seccion 6 del enunciado.");

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
                List<Mission4Parser.TestCase> preview = Mission4Parser.parse(currentInputText);
                if (!preview.isEmpty()) {
                    previewNetworkOnly(preview.get(0));
                }
                setInfo("Input cargado. Presiona \"Resolver\" para ver el MST.");
            } catch (InputValidationException ex) {
                setError("Error en el input: " + ex.getMessage());
            }
        });
    }

    /** Dibuja unicamente la red disponible (sin resaltar ningun MST todavia). */
    private void previewNetworkOnly(Mission4Parser.TestCase tc) {
        boolean drawn = networkCanvas.render(tc.getN(), tc.getCables(), null);
        networkMessage.setText(drawn ? "" : "Red demasiado grande para dibujar.");

        parsedCases = new ArrayList<>();
        results = new ArrayList<>();
        caseSelector.getItems().clear();
    }

    private void solveCurrentInput() {
        try {
            parsedCases = Mission4Parser.parse(currentInputText);
            results = new ArrayList<>();
            caseSelector.getItems().clear();

            int caseNumber = 1;
            StringBuilder output = new StringBuilder();
            for (Mission4Parser.TestCase tc : parsedCases) {
                KruskalSolver.Result kruskalResult = KruskalSolver.solve(tc.getN(), tc.getCables());

                Mission4Result result = Mission4Result.of(caseNumber, kruskalResult);
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
        Mission4Parser.TestCase tc = parsedCases.get(index);
        Mission4Result result = results.get(index);

        List<KruskalSolver.Cable> mstCables = result.isConnectable() ? result.getMstCables() : null;

        boolean drawn = networkCanvas.render(tc.getN(), tc.getCables(), mstCables);

        if (!drawn) {
            networkMessage.setText("Red demasiado grande para dibujar (> "
                    + NetworkCanvas.getMaxDrawableNodes() + " nodos o > "
                    + NetworkCanvas.getMaxDrawableCables() + " cables). La respuesta ya se muestra arriba.");
        } else if (!result.isConnectable()) {
            networkMessage.setText("Limon cut too many cables: no se puede conectar toda la red.");
        } else {
            networkMessage.setText("");
        }
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
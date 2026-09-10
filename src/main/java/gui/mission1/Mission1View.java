package gui.mission1;

import algorithm.mission1.BFSSolver;
import algorithm.mission1.DFSSolver;
import errorhandling.InputValidationException;
import io.Mission1Parser;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import model.result.Mission1Result;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Mission1View
 * ============================================================
 *
 * Pantalla completa de Mision 1: area de input, boton "Load Sample"
 * (requisito 7.1), area de output con las lineas "Case #k: ...", y
 * dos GridCanvas lado a lado (BFS y DFS) para que se vea claramente
 * la diferencia entre ambos caminos sobre el mismo grid -- tal como
 * lo ilustran las imagenes del enunciado.
 *
 * Si el input tiene varios casos de prueba, un ComboBox permite
 * elegir cual caso visualizar (el output de texto siempre muestra
 * TODOS los casos, solo el dibujo es de a uno).
 */
public final class Mission1View extends BorderPane {

    private static final String SAMPLE_INPUT =
            "10 10\n9\n0 1 2\n1 1 2\n2 2 2 9\n3 2 1 7\n5 3 3 6 9\n"
                    + "6 4 0 1 2 7\n7 3 0 3 8\n8 2 7 9\n9 3 2 3 4\n0 0\n9 9\n0 0\n";

    private final TextArea inputArea = new TextArea();
    private final TextArea outputArea = new TextArea();
    private final Label errorLabel = new Label();
    private final ComboBox<Integer> caseSelector = new ComboBox<>();

    private final GridCanvas bfsCanvas = new GridCanvas();
    private final GridCanvas dfsCanvas = new GridCanvas();
    private final Label bfsMessage = new Label();
    private final Label dfsMessage = new Label();

    private List<Mission1Parser.TestCase> parsedCases = new ArrayList<>();
    private List<Mission1Result> results = new ArrayList<>();

    public Mission1View() {
        setPadding(new Insets(12));
        setTop(buildInputSection());
        setCenter(buildVisualizationSection());
    }

    private VBox buildInputSection() {
        Label title = new Label("Mision 1: Rescatando a Nina del Minefield (BFS & DFS)");
        title.setFont(Font.font(16));

        inputArea.setPromptText("Pega aqui el input de Mision 1...");
        inputArea.setPrefRowCount(8);
        inputArea.setWrapText(false);

        Button loadSampleButton = new Button("Load Sample");
        loadSampleButton.setOnAction(e -> inputArea.setText(SAMPLE_INPUT));

        Button solveButton = new Button("Resolver");
        solveButton.setOnAction(e -> solve());

        HBox buttonRow = new HBox(8, loadSampleButton, solveButton);

        errorLabel.setWrapText(true);
        errorLabel.setStyle("-fx-text-fill: #cc4444; -fx-font-weight: bold;");

        outputArea.setEditable(false);
        outputArea.setPrefRowCount(4);

        Label caseLabel = new Label("Caso a visualizar:");
        caseSelector.setOnAction(e -> updateVisualization());
        HBox caseRow = new HBox(8, caseLabel, caseSelector);
        caseRow.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(8, title, inputArea, buttonRow, errorLabel, outputArea, caseRow);
        return box;
    }

    private HBox buildVisualizationSection() {
        Label bfsTitle = new Label("BFS (camino mas corto garantizado)");
        Label dfsTitle = new Label("DFS (orden fijo: arriba, abajo, izquierda, derecha)");

        VBox bfsBox = new VBox(4, bfsTitle, bfsMessage, wrapInScroll(bfsCanvas));
        VBox dfsBox = new VBox(4, dfsTitle, dfsMessage, wrapInScroll(dfsCanvas));

        HBox row = new HBox(16, bfsBox, dfsBox);
        return row;
    }

    private ScrollPane wrapInScroll(GridCanvas canvas) {
        ScrollPane scrollPane = new ScrollPane(canvas);
        scrollPane.setPrefViewportWidth(420);
        scrollPane.setPrefViewportHeight(420);
        return scrollPane;
    }

    private void solve() {
        errorLabel.setText("");

        try {
            parsedCases = Mission1Parser.parse(inputArea.getText());
            results = new ArrayList<>();

            StringBuilder output = new StringBuilder();
            caseSelector.getItems().clear();

            int caseNumber = 1;
            for (Mission1Parser.TestCase tc : parsedCases) {
                BFSSolver.Result bfsResult = BFSSolver.solve(
                        tc.getGrid(), tc.getStartRow(), tc.getStartCol(), tc.getEndRow(), tc.getEndCol());
                DFSSolver.Result dfsResult = DFSSolver.solve(
                        tc.getGrid(), tc.getStartRow(), tc.getStartCol(), tc.getEndRow(), tc.getEndCol());

                Mission1Result result = Mission1Result.of(caseNumber, bfsResult, dfsResult);
                results.add(result);
                output.append(result.toOutputLine()).append("\n");
                caseSelector.getItems().add(caseNumber);
                caseNumber++;
            }

            outputArea.setText(output.toString());

            if (!caseSelector.getItems().isEmpty()) {
                caseSelector.getSelectionModel().selectFirst();
                updateVisualization();
            }

        } catch (InputValidationException ex) {
            // Requisito 2.2: "Malformed input must produce a readable
            // error message inside the GUI, never a stack trace and
            // never a silent failure."
            errorLabel.setText("Error en el input: " + ex.getMessage());
            outputArea.clear();
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
}
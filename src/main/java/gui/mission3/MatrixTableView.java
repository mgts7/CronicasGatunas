package gui.mission3;

import algorithm.mission3.FloydWarshallSolver;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;

/**
 * ============================================================
 * MatrixTableView - Mision 3
 * ============================================================
 *
 * Muestra la matriz N x N completa de churun maximo (seccion 5:
 * "The GUI must display the resulting N x N matrix, using - for
 * pairs with no route and inf for pairs whose maximum is
 * unbounded"), dentro de un panel scrollable (seccion 2.3: "the
 * N x N table must be shown in a scrollable panel for every N up
 * to 100").
 *
 * Se implementa con GridPane + ScrollPane en vez de TableView: con
 * N variable hasta 100, generar columnas dinamicas de TableView
 * agrega complejidad sin beneficio real aqui, dado que no se
 * necesita ordenar ni editar celdas.
 */
public final class MatrixTableView extends ScrollPane {

    private static final double CELL_WIDTH = 46;

    public MatrixTableView() {
        setPrefViewportWidth(480);
        setPrefViewportHeight(220);
        setStyle("-fx-background: transparent; -fx-background-color: transparent;");
    }

    /** Muestra la matriz, resaltando la celda (source, destination) que corresponde a la respuesta del caso. */
    public void render(long[][] matrix, boolean[][] unbounded, int source, int destination) {
        int n = matrix.length;
        GridPane grid = new GridPane();
        grid.setHgap(2);
        grid.setVgap(2);
        grid.setPadding(new Insets(4));

        grid.add(makeHeaderCell(""), 0, 0);
        for (int j = 0; j < n; j++) {
            grid.add(makeHeaderCell(String.valueOf(j)), j + 1, 0);
        }

        for (int i = 0; i < n; i++) {
            grid.add(makeHeaderCell(String.valueOf(i)), 0, i + 1);
            for (int j = 0; j < n; j++) {
                String text = formatCell(matrix[i][j], unbounded[i][j]);
                boolean isAnswerCell = (i == source && j == destination);
                grid.add(makeCell(text, isAnswerCell), j + 1, i + 1);
            }
        }

        setContent(grid);
    }

    private String formatCell(long value, boolean isUnbounded) {
        if (value == FloydWarshallSolver.NO_ROUTE) {
            return "-";
        }
        if (isUnbounded) {
            return "inf";
        }
        return String.valueOf(value);
    }

    private Label makeHeaderCell(String text) {
        Label label = new Label(text);
        label.setMinWidth(CELL_WIDTH);
        label.setPrefWidth(CELL_WIDTH);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #3a2a15;");
        return label;
    }

    private Label makeCell(String text, boolean isAnswerCell) {
        Label label = new Label(text);
        label.setMinWidth(CELL_WIDTH);
        label.setPrefWidth(CELL_WIDTH);
        label.setAlignment(Pos.CENTER);
        if (isAnswerCell) {
            label.setStyle("-fx-background-color: rgba(200,160,40,0.5); "
                    + "-fx-font-weight: bold; -fx-text-fill: #2a1a05; -fx-background-radius: 3;");
        } else {
            label.setStyle("-fx-text-fill: #3a2a15;");
        }
        return label;
    }
}
package model.result;

import algorithm.mission4.KruskalSolver;
import algorithm.mission4.KruskalSolver.Cable;

import java.util.List;

/**
 * Resultado de un caso de prueba de Mission 4, listo para mostrarse
 * en la GUI o compararse en un test contra la salida esperada.
 */
public final class Mission4Result {

    private final int caseNumber;
    private final boolean connectable;
    private final long totalCost;
    private final List<Cable> mstCables;

    private Mission4Result(int caseNumber, boolean connectable, long totalCost, List<Cable> mstCables) {
        this.caseNumber = caseNumber;
        this.connectable = connectable;
        this.totalCost = totalCost;
        this.mstCables = mstCables;
    }

    /**
     * Construye el resultado a partir de lo que devolvio
     * KruskalSolver.solve(...). A diferencia de Mission1Result y
     * Mission2Result (que reciben valores sueltos), aqui se recibe
     * directamente el Result de Kruskal porque ya viene empaquetado
     * con los cables del MST, necesarios para resaltarlos en la GUI.
     */
    public static Mission4Result of(int caseNumber, KruskalSolver.Result result) {
        if (!result.isConnectable()) {
            return new Mission4Result(caseNumber, false, KruskalSolver.CANNOT_CONNECT, List.of());
        }
        return new Mission4Result(caseNumber, true, result.getTotalCost(), result.getMstCables());
    }

    public int getCaseNumber() {
        return caseNumber;
    }

    public boolean isConnectable() {
        return connectable;
    }

    public long getTotalCost() {
        return totalCost;
    }

    /** Los cables que forman el MST, para resaltarlos en el dibujo de la red. */
    public List<Cable> getMstCables() {
        return mstCables;
    }

    /**
     * Formatea la linea de salida EXACTA que exige el enunciado
     * (seccion 6: mensaje especial "Limon cut too many cables" si
     * no es posible conectar todas las intersecciones).
     */
    public String toOutputLine() {
        if (!connectable) {
            return "Case #" + caseNumber + ": Limon cut too many cables";
        }
        return "Case #" + caseNumber + ": " + totalCost;
    }
}
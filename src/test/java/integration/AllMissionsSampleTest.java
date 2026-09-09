package integration;

import algorithm.mission1.BFSSolver;
import algorithm.mission1.DFSSolver;
import algorithm.mission2.DijkstraSolver;
import algorithm.mission3.BellmanFordSolver;
import algorithm.mission3.FloydWarshallSolver;
import algorithm.mission4.KruskalSolver;
import io.Mission1Parser;
import io.Mission2Parser;
import io.Mission3Parser;
import io.Mission4Parser;
import model.result.Mission1Result;
import model.result.Mission2Result;
import model.result.Mission3Result;
import model.result.Mission4Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Test de INTEGRACION (no unitario): en vez de probar un solver
 * aislado, corre el pipeline completo -- texto crudo tal como
 * llegaria pegado en la GUI, parser, solver(s), y Mission*Result --
 * para las 4 misiones, usando exactamente los ejemplos del PDF.
 *
 * Sirve como "smoke test" mientras la GUI todavia no existe: si
 * estos 4 tests pasan, todo el codigo de las capas algorithm/,
 * model/ e io/ esta correctamente conectado entre si.
 */
class AllMissionsSampleTest {

    @Test
    @DisplayName("Mission 1: sample completo del PDF produce 'Case #1: BFS 18 DFS 32'")
    void mission1_samplePipeline() {
        String rawInput = "10 10\n9\n0 1 2\n1 1 2\n2 2 2 9\n3 2 1 7\n5 3 3 6 9\n"
                + "6 4 0 1 2 7\n7 3 0 3 8\n8 2 7 9\n9 3 2 3 4\n0 0\n9 9\n0 0\n";

        List<Mission1Parser.TestCase> cases = Mission1Parser.parse(rawInput);
        assertEquals(1, cases.size());

        Mission1Parser.TestCase tc = cases.get(0);
        int bfs = BFSSolver.solve(tc.getGrid(), tc.getStartRow(), tc.getStartCol(), tc.getEndRow(), tc.getEndCol());
        int dfs = DFSSolver.solve(tc.getGrid(), tc.getStartRow(), tc.getStartCol(), tc.getEndRow(), tc.getEndCol());

        Mission1Result result = Mission1Result.of(1, bfs, dfs);
        assertEquals("Case #1: BFS 18 DFS 32", result.toOutputLine());
    }

    @Test
    @DisplayName("Mission 2: sample completo del PDF produce los 3 casos esperados")
    void mission2_samplePipeline() {
        String rawInput = "3\n2 1 0 1\n0 1 100\n3 3 2 0\n0 1 100\n0 2 200\n1 2 50\n2 0 0 1\n";

        List<Mission2Parser.TestCase> cases = Mission2Parser.parse(rawInput);
        assertEquals(3, cases.size());

        String[] expected = {"Case #1: 100", "Case #2: 150", "Case #3: Nina is very sad"};

        int i = 0;
        for (Mission2Parser.TestCase tc : cases) {
            long cost = DijkstraSolver.solve(tc.getGraph(), tc.getSource(), tc.getDestination());
            Mission2Result result = Mission2Result.of(i + 1, cost);
            assertEquals(expected[i], result.toOutputLine());
            i++;
        }
    }

    @Test
    @DisplayName("Mission 3: sample completo del PDF produce los 3 casos esperados, sin mismatch FW/BF")
    void mission3_samplePipeline() {
        String rawInput = "3\n5 7 0 4\n0 1 50\n0 2 10\n1 2 -30\n1 3 40\n2 1 -5\n2 3 60\n3 4 20\n"
                + "4 4 0 3\n0 1 20\n1 2 30\n2 1 -10\n2 3 15\n"
                + "3 3 0 2\n0 1 -40\n1 2 -25\n0 2 -80\n";

        List<Mission3Parser.TestCase> cases = Mission3Parser.parse(rawInput);
        assertEquals(3, cases.size());

        String[] expected = {"Case #1: 110", "Case #2: Infinite churun!", "Case #3: -65"};

        int i = 0;
        for (Mission3Parser.TestCase tc : cases) {
            FloydWarshallSolver.Result fw = FloydWarshallSolver.solve(tc.getGraph(), tc.getN());
            BellmanFordSolver.Result bf = BellmanFordSolver.solve(tc.getGraph(), tc.getN(), tc.getSource());
            Mission3Result result = Mission3Result.of(i + 1, fw, bf, tc.getSource(), tc.getDestination());

            assertEquals(expected[i], result.toOutputLine());
            assertFalse(result.isCrossCheckMismatch(),
                    "Floyd-Warshall y Bellman-Ford no deberian discrepar en el caso " + (i + 1));
            i++;
        }
    }

    @Test
    @DisplayName("Mission 4: sample completo del PDF produce 'Case #1: 55'")
    void mission4_samplePipeline() {
        String rawInput = "1\n4\n5\n1 2 10\n2 3 20\n3 4 30\n4 1 40\n1 3 15\n";

        List<Mission4Parser.TestCase> cases = Mission4Parser.parse(rawInput);
        assertEquals(1, cases.size());

        Mission4Parser.TestCase tc = cases.get(0);
        KruskalSolver.Result kruskalResult = KruskalSolver.solve(tc.getN(), tc.getCables());
        Mission4Result result = Mission4Result.of(1, kruskalResult);

        assertEquals("Case #1: 55", result.toOutputLine());
    }
}
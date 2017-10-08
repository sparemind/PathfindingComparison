import java.awt.Point;

/**
 * Dijkstra's Algorithm.
 */
public class Dijkstra extends AStar {
    @Override
    protected double calcHeuristic(Point p1, Point p2) {
        // Dijkstra's Algorithm is a special case of A* where the heuristic is 0
        return 0;
    }

    @Override
    public String toString() {
        return "Dijkstra's Algorithm";
    }
}


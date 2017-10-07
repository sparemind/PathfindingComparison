import java.awt.Point;

public class Dijkstra extends AStar {
    @Override
    protected double calcHeuristic(Point p1, Point p2) {
        return 0;
    }

    @Override
    public String toString() {
        return "Dijkstra's Algorithm";
    }
}


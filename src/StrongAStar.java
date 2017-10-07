import java.awt.Point;

public class StrongAStar extends AStar {
    public static final double WEIGHT = 10.0;

    @Override
    public double calcHeuristic(Point p1, Point p2) {
        return WEIGHT * p1.distance(p2);
    }
}

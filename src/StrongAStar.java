import java.awt.Point;

/**
 * A* Search Algorithm with a more weighted heuristic.
 */
public class StrongAStar extends AStar {
    /**
     * Amount the heuristic is multiplied by.
     */
    public static final double WEIGHT = 3.0;

    @Override
    /**
     * Returns the Manhattan distance between the given points, multiplied by the class weight
     * multiplier.
     *
     * @param p1 First point of the point pair to calculate the heuristic of.
     * @param p2 Second point of the point pair to calculate the heuristic of.
     * @return The Manhattan distance between the two points, multiplied by the class weight
     * multiplier.
     */ public double calcHeuristic(Point p1, Point p2) {
        // This heuristic overestimates, making it no longer an "admissible heuristic."
        // It is therefore not guaranteed to find the optimal path.
        // As a tradeoff to this sacrifice is that this version of A* may find a path
        // faster in certain cases.
        return WEIGHT * (Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y));
    }

    @Override
    public String toString() {
        return "A* (Higher Heuristic Weight)";
    }
}

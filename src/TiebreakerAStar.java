import java.awt.Point;

/**
 * A* Search Algorithm with a tiebreaker heuristic.
 */
public class TiebreakerAStar extends AStar {
    /**
     * Amount the heuristic is multiplied by.
     * Equal to (1.0 + p), where p < (min step cost) / (expected max path length)
     */
    public static final double TIEBREAKER = 1.001;

    /**
     * Returns the Manhattan distance between the given points, multiplied by the class weight
     * multiplier.
     *
     * @param p1 First point of the point pair to calculate the heuristic of.
     * @param p2 Second point of the point pair to calculate the heuristic of.
     * @return The Manhattan distance between the two points, multiplied by the class weight
     * multiplier.
     */
    @Override
    public double calcHeuristic(Point p1, Point p2) {
        // Adding this tiebreaker technically makes this heuristic no longer admissible,
        // but practically should make no major difference.
        return TIEBREAKER * (Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y));
    }

    @Override
    public String toString() {
        return "A* (Tiebreaker)";
    }
}

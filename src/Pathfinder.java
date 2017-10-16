import java.awt.Point;
import java.util.List;
import java.util.Set;

/**
 * Pathfinding algorithm
 */
public interface Pathfinder {
    /**
     * Initializes the pathfinder to an initial state ready to pathfind from a starting point to a
     * target.
     *
     * @param start  The starting point of the path that the pathfinder will start at.
     * @param target The target endpoint of the path that the pathfinder will end at.
     */
    void initialize(Point start, Point target);

    /**
     * Advances the pathfinding algorithm a single iteration.
     *
     * @return List of all nodes explored during this iteration.
     */
    List<Point> step();

    /**
     * Returns all nodes that make up the solution path.
     *
     * @return List of all nodes in the solution path. If a solution path has not been found,
     * returns null. Nodes are ordered starting from the path start point to the path endpoint.
     */
    List<Point> getSolution();

    Set<Point> getFrontier();

    /**
     * Returns the full name of this algorithm.
     *
     * @return The full name of this algorithm.
     */
    String toString();
}

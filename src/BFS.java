import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Breadth First Search Algorithm
 */
public class BFS implements Pathfinder {
    private Point target;
    private Queue<Point> openSet;
    private Set<Point> closedSet;
    private Map<Point, Point> cameFrom;
    private List<Point> solutionPath;

    @Override
    public void initialize(Point start, Point target) {
        this.openSet = new LinkedList<>();
        this.closedSet = new HashSet<>();
        this.cameFrom = new HashMap<>();
        this.solutionPath = null;
        this.target = target;

        this.openSet.add(start);
    }

    @Override
    public List<Point> step() {
        List<Point> exploredCells = new ArrayList<>();

        // While unvisited nodes remain...
        if (!this.openSet.isEmpty()) {
            // Get next unvisited node
            Point current = this.openSet.remove();

            // Try again with next cell if this one was already visited.
            // NOTE: Not part of the actual BFS. This just prevents a step() call from
            // being wasted and keeps the algorithm on an accurate pace with the others.
            if (this.closedSet.contains(current)) {
                return step();
            } else {
                // Mark as now visited
                this.closedSet.add(current);

                Point up = new Point(current.x, current.y - 1);
                Point down = new Point(current.x, current.y + 1);
                Point right = new Point(current.x + 1, current.y);
                Point left = new Point(current.x - 1, current.y);
                Point[] neighbors = {up, right, down, left};

                // Explore each neighbor
                for (Point neighbor : neighbors) {
                    // Ignore already evaluated nodes and ones that aren't traversable
                    if (this.closedSet.contains(neighbor) || !Main.isOpen(neighbor)) {
                        continue;
                    }

                    exploredCells.add(neighbor);

                    this.cameFrom.put(neighbor, current);

                    // If a neighbor hasn't been visited, add to the queue to be visited
                    if (!this.closedSet.contains(neighbor)) {
                        this.openSet.add(neighbor);
                    }

                    // If the target has been found
                    if (neighbor.equals(this.target)) {
                        calculateSolutionPath(this.target);
                    }
                }
            }
        }
        return exploredCells;
    }

    /**
     * Fills solutionPath with the nodes that make the path from the start point to the target
     * point. Nodes are ordered starting from the path start point to the path endpoint.
     *
     * @param target The endpoint of the path.
     */
    private void calculateSolutionPath(Point target) {
        this.solutionPath = new LinkedList<>();

        while (target != null) {
            this.solutionPath.add(0, target);
            target = this.cameFrom.get(target);
        }
    }

    @Override
    public List<Point> getSolution() {
        return this.solutionPath;
    }

    @Override
    public String toString() {
        return "Breadth First Search";
    }
}

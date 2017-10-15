import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * A* Search Algorithm
 */
public class AStar implements Pathfinder {
    protected static class Node implements Comparable<Node> {
        public final Point point; // Position of this node
        public double gScore; // The real cost to reach this node
        public double fScore; // The total cost to reach this node (including heuristic cost)
        public Node cameFrom; // Node on the path back to the start

        public Node(Point point) {
            this.point = point;
            this.gScore = Double.MAX_VALUE;
            this.fScore = Double.MAX_VALUE;
            this.cameFrom = null;
        }

        @Override
        public int hashCode() {
            return this.point.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Node other = (Node) o;
            return this.point.equals(other.point);
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.fScore, other.fScore);
        }
    }

    private Point target;
    private PriorityQueue<Node> openSet;
    private Set<Node> closedSet;
    private List<Point> solutionPath;

    @Override
    public void initialize(Point start, Point target) {
        this.target = target;
        this.openSet = new PriorityQueue<>();
        this.closedSet = new HashSet<>();
        this.solutionPath = null;

        Node startNode = new Node(start);
        startNode.gScore = 0;
        startNode.fScore = calcHeuristic(start, target);
        this.openSet.add(startNode);
    }

    @Override
    public List<Point> step() {
        List<Point> exploredCells = new ArrayList<>();

        if (!this.openSet.isEmpty()) {
            // Get node with lowest fScore
            Node current = this.openSet.remove();

            if (current.point.equals(this.target)) {
                calculateSolutionPath(current);
            }

            this.closedSet.add(current);

            Node up = new Node(new Point(current.point.x, current.point.y - 1));
            Node down = new Node(new Point(current.point.x, current.point.y + 1));
            Node right = new Node(new Point(current.point.x + 1, current.point.y));
            Node left = new Node(new Point(current.point.x - 1, current.point.y));
            Node[] neighbors = {up, right, down, left};

            for (Node neighbor : neighbors) {
                // Ignore already evaluated nodes and ones that aren't traversable
                if (this.closedSet.contains(neighbor) || !Main.isOpen(neighbor.point)) {
                    continue;
                }

                exploredCells.add(neighbor.point);


                double tentativeGScore = calcTentativeGScore(current, neighbor);
                if (tentativeGScore < neighbor.gScore) {
                    // This is a better path
                    neighbor.cameFrom = current;
                    neighbor.gScore = tentativeGScore;
                    neighbor.fScore = neighbor.gScore + calcHeuristic(neighbor.point, this.target);
                }

                // Reinsert node with its new priority
                // TODO
                // this.openSet.remove(neighbor);
                // this.openSet.add(neighbor);
                // Discover a new Node
                if (!this.openSet.contains(neighbor)) {
                    this.openSet.add(neighbor);
                }
            }
        }

        return exploredCells;
    }

    /**
     * Returns a tentative gScore (real cost to reach this node) for the given neighbor of a given
     * node.
     *
     * @param current  The node currently being evaluated.
     * @param neighbor The neighbor of the current node being evaluated.
     * @return The tentative gScore of the neighboring node.
     * @since v1.5.2
     */
    protected double calcTentativeGScore(Node current, Node neighbor) {
        return current.gScore + Main.getCost(neighbor.point);
    }

    /**
     * Returns the Manhattan distance between the given points.
     *
     * @param p1 First point of the point pair to calculate the heuristic of.
     * @param p2 Second point of the point pair to calculate the heuristic of.
     * @return The Manhattan distance between the two points.
     */
    protected double calcHeuristic(Point p1, Point p2) {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }

    /**
     * Fills solutionPath with the nodes that make the path from the start point to the target
     * point. Nodes are ordered starting from the path start point to the path endpoint.
     *
     * @param target The end node of the path.
     */
    private void calculateSolutionPath(Node target) {
        this.solutionPath = new LinkedList<>();

        while (target != null) {
            this.solutionPath.add(0, target.point);
            target = target.cameFrom;
        }
    }

    @Override
    public List<Point> getSolution() {
        return this.solutionPath;
    }

    @Override
    public String toString() {
        return "A*";
    }
}

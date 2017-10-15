/**
 * Greedy Best First Search Algorithm
 */
// TODO Check gScore. Is cost being included as a "greedy" factor?
public class GreedyBestFirstSearch extends AStar {
    /**
     * Returns a tentative gScore (real cost to reach this node) for the given neighbor of a given
     * node.
     *
     * @param current  The node currently being evaluated.
     * @param neighbor The neighbor of the current node being evaluated.
     * @return The tentative gScore of the neighboring node.
     */
    @Override
    protected double calcTentativeGScore(Node current, Node neighbor) {
        return Main.getCost(neighbor.point);
    }

    @Override
    public String toString() {
        return "Greedy Best First Search";
    }
}

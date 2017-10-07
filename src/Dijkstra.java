import java.awt.Point;

public class Dijkstra extends AStar {
    @Override
    protected double calcHeuristic(Point p1, Point p2) {
        return 0;
    }
}


// import java.awt.Point;
// import java.util.ArrayList;
// import java.util.Comparator;
// import java.util.HashMap;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Map;
// import java.util.PriorityQueue;
// import java.util.Set;
//
// public class Dijkstra implements Pathfinder {
//     private PriorityQueue<Point> unvisited;
//     private Set<Point> visited;
//     private Map<Point, Double> distances;
//     private Point target;
//
//     @Override
//     public void initialize(Set<Point> nodes, Point start, Point target) {
//         this.unvisited = new PriorityQueue<>(new Comparator<Point>() {
//             @Override
//             public int compare(Point p1, Point p2) {
//                 return Dijkstra.this.distances.get(p1).compareTo(Dijkstra.this.distances.get(p2));
//             }
//         });
//         this.visited = new HashSet<>();
//         this.distances = new HashMap<>();
//         this.target = target;
//
//         nodes.remove(start); // Remove start point so it can be added later
//
//         // All nodes begin unvisited and with infinite distance
//         for (Point p : nodes) {
//             this.distances.put(p, Double.MAX_VALUE);
//             this.unvisited.add(p);
//         }
//         // Make the distance to the starting node 0
//         this.distances.put(start, 0.0);
//         this.unvisited.add(start);
//     }
//
//     @Override
//     public List<Point> stepAlgorithms() {
//         List<Point> exploredCells = new ArrayList<>();
//
//         if (!this.unvisited.isEmpty()) {
//             Point current = this.unvisited.remove();
//             if (this.visited.contains(current)) {
//                 return stepAlgorithms();
//             }
//             this.visited.add(current);
//
//             if (current.equals(this.target)) {
//                 System.out.println("Done!");
//             }
//
//             Point up = new Point(current.x, current.y - 1);
//             Point down = new Point(current.x, current.y + 1);
//             Point right = new Point(current.x + 1, current.y);
//             Point left = new Point(current.x - 1, current.y);
//             Point[] neighbors = {up, right, down, left};
//
//             for (Point neighbor : neighbors) {
//                 if (!Main.isOpen(neighbor)) {
//                     continue;
//                 }
//                 if (this.distances.get(current) == null)
//                     System.out.println("C: " + current); //TODO remove
//                 if (this.distances.get(neighbor) == null)
//                     System.out.println("N: " + neighbor); //TODO remove
//                 double alt = this.distances.get(current) + 1;
//                 if (alt < this.distances.get(neighbor)) { // A shorter path has been found
//                     this.distances.put(neighbor, alt);
//                     this.unvisited.add(neighbor);
//                 }
//                 exploredCells.add(neighbor);
//             }
//         }
//
//         return exploredCells;
//     }
//
//     @Override
//     public List<Point> getSolution() {
//         return null;
//     }
//
//     private static class Node implements Comparable<Node> {
//         public Point point;
//         public double distance;
//
//         public Node(Point point, double distance) {
//             this.point = point;
//             this.distance = distance;
//         }
//
//         @Override
//         public int compareTo(Node other) {
//             return -Double.compare(this.distance, other.distance);
//         }
//     }
// }

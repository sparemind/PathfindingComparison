import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dummy pathfinder that does nothing.
 */
public class None implements Pathfinder {
    @Override
    public void initialize(Point start, Point target) {

    }

    @Override
    public List<Point> step() {
        return new ArrayList<>();
    }

    @Override
    public Set<Point> getFrontier() {
        return new HashSet<>();
    }

    @Override
    public List<Point> getSolution() {
        return null;
    }

    @Override
    public String toString() {
        return "None";
    }
}

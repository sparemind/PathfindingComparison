import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Dummy pathfinder that does nothing.
 */
public class None implements Pathfinder {
    @Override
    public void initialize(Point start, Point target) {

    }

    @Override
    public List<Point> step() {
        return new ArrayList<Point>();
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

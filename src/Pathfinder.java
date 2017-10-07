import java.awt.Point;
import java.util.List;

public interface Pathfinder {
    public void initialize(Point start, Point target);

    public List<Point> step();

    public List<Point> getSolution();
}

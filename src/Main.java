import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A visual side-by-side comparison of common pathfinding algorithms.
 * <p>
 * The currently supported algorithms are:
 * <ul>
 * <li>Dijkstra's Algorithm</li>
 * <li>A* Search (2 variants)</li>
 * <li>Breadth First Search</li>
 * </ul>
 *
 * @author Jake Chiang
 * @version v1.3
 */
public class Main {
    /**
     * Width of each sub grid in number of cells.
     */
    public static final int GRID_WIDTH = 30;
    /**
     * Height of each sub grid in number of cells.
     */
    public static final int GRID_HEIGHT = 21;
    /**
     * Size of each cell in pixels.
     */
    public static final int CELL_SIZE = 12;
    /**
     * Number of cells between each subgrid horizontally.
     */
    public static final int GRID_MARGIN_X = 2;
    /**
     * Number of cells between each sub grid vertically.
     */
    public static final int GRID_MARGIN_Y = 2;
    /**
     * Number of subgrids per row.
     */
    public static final int GRIDS_HORZ = 2;
    /**
     * Number of subgrids per column.
     */
    public static final int GRIDS_VERT = 2;
    /**
     * Number of cells padding the frame border. Must be &gt;= 1.
     */
    public static final int BORDER_SIZE = 3;
    /**
     * Maximum traversal cost that a cell can be set to.
     */
    public static final int MAX_COST = 10;
    /**
     * Minimum int-8 RGB value of a weighted cell.
     */
    public static final int WEIGHTED_MIN_COLOR = 100;

    /**
     * Delay between each step in milliseconds.
     */
    public static final int STEP_DELAY = 12;

    /**
     * Value added to ASCII value to get corresponding grid value.
     */
    public static final int ASCII_OFFSET = 1000;
    // Note that value 0 is reserved for the "transparent cell"
    public static final int BG = 1; // Background
    public static final int EMPTY = 2; // Empty grid space
    public static final int WALL = 3; // Wall obstacle
    public static final int START = 4; // Pathfinding start point
    public static final int TARGET = 5; // Pathfinding end point
    public static final int EXPLORED = 6; // Pathfinding explored cell
    public static final int SOLUTION = 7; // Final solution path
    public static final int WEIGHTED = 100; // First value of cells with a travel cost

    private static SimpleGrid grid;
    private static boolean isMouseDown = false;
    private static int initialClick = -1;
    private static int selectedWeight = MAX_COST; // If equal to MAX_COST, creates walls instead of weighted cells
    private static boolean showingWeights = false;
    private static boolean started = false;
    private static boolean running = false;
    private static Point[] subgridPositions = new Point[GRIDS_HORZ * GRIDS_VERT];
    private static Point startLocalPos;
    private static Point targetLocalPos;

    private static Map<Point, Pathfinder> pathfinders = new HashMap<>();
    private static Map<Point, Boolean> pathfindersCompletion = new HashMap<>();

    public static void main(String[] args) {
        int totalWidth = (GRID_WIDTH * GRIDS_HORZ) + (GRIDS_HORZ - 1) * GRID_MARGIN_X + (BORDER_SIZE * 2);
        int totalHeight = (GRID_HEIGHT * GRIDS_VERT) + (GRIDS_VERT - 1) * GRID_MARGIN_Y + (BORDER_SIZE * 2);
        grid = new SimpleGrid(totalWidth, totalHeight, CELL_SIZE, 1, "Pathfinding Algorithm Comparison");
        grid.setGridlineColor(Color.GRAY);
        grid.addLayer(); // Layer 1 shows the explored cells and solution path

        grid.setColor(BG, Color.GRAY);
        grid.setColor(EMPTY, Color.WHITE);
        grid.setColor(WALL, new Color(20, 20, 20));
        grid.setColor(START, Color.GREEN);
        grid.setColor(TARGET, Color.RED);
        grid.setColor(EXPLORED, Color.LIGHT_GRAY);
        grid.setColor(SOLUTION, Color.CYAN);
        for (int i = 0; i < 128; i++) {
            grid.setText(ASCII_OFFSET + i, (char) i);
            grid.setTextColor(ASCII_OFFSET + i, Color.WHITE);
            grid.setColor(ASCII_OFFSET + i, Color.GRAY);
        }
        for (int i = 1; i < MAX_COST; i++) {
            int value = WEIGHTED + i;
            int increment = (255 - WEIGHTED_MIN_COLOR) / MAX_COST;
            grid.setColor(value, new Color(255, 255 - i * increment, 255 - i * increment));
            grid.setTextColor(value, Color.BLACK);
        }

        // Calculate all subgrid positions
        for (int y = 0; y < GRIDS_VERT; y++) {
            for (int x = 0; x < GRIDS_HORZ; x++) {
                int xPos = BORDER_SIZE + x * (GRID_WIDTH + GRID_MARGIN_X);
                int yPos = BORDER_SIZE + y * (GRID_HEIGHT + GRID_MARGIN_Y);
                subgridPositions[y * GRIDS_HORZ + x] = new Point(xPos, yPos);
            }
        }

        // Initialize grid and subgrids
        for (int x = 0; x < totalWidth; x++) {
            for (int y = 0; y < totalHeight; y++) {
                grid.set(x, y, BG);
            }
        }
        initializeSubgrids();

        // Initialize pathfinders
        pathfinders.put(subgridPositions[0], new Dijkstra());
        pathfinders.put(subgridPositions[1], new BFS());
        pathfinders.put(subgridPositions[2], new AStar());
        pathfinders.put(subgridPositions[3], new StrongAStar());
        initializePathfinders();

        // Initialize GUI and grid labels
        initializeGUI();
        for (Point pathfinderPos : subgridPositions) {
            drawText(pathfinderPos.x, pathfinderPos.y - 1, pathfinders.get(pathfinderPos).toString());
        }

        run();
    }

    /**
     * Initializes the GUI button and slider controls and adds them to the SimpleGrid frame.
     *
     * @since v1.3
     */
    public static void initializeGUI() {
        JFrame frame = grid.getFrame();
        JPanel p = new JPanel();

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                started = false;
                running = false;
                initializeSubgrids();
            }
        });
        p.add(clearButton);

        JButton stepButton = new JButton("Step");
        stepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stepAlgorithms();
            }
        });
        p.add(stepButton);

        JButton runButton = new JButton("Run");
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                running = true;
            }
        });
        p.add(runButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                started = false;
                running = false;
                clearExploration();
            }
        });
        p.add(resetButton);

        JLabel sliderValue = new JLabel("X");
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, MAX_COST, MAX_COST);
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                selectedWeight = source.getValue();
                sliderValue.setText(selectedWeight == MAX_COST ? "X" : ("" + selectedWeight));
            }
        });
        p.add(new JLabel("Cell Weight:"));
        p.add(slider);
        p.add(sliderValue);

        JButton toggleWeightsButton = new JButton("Toggle Cell Costs");
        toggleWeightsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showingWeights = !showingWeights;

                for (int i = 1; i < MAX_COST; i++) {
                    char display = showingWeights ? ("" + i).charAt(0) : '\0';
                    grid.setText(WEIGHTED + i, display);
                }
            }
        });
        p.add(toggleWeightsButton);

        frame.add(p, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    /**
     * Draws the given text into grid cells at a location.
     *
     * @param x    The x-coordinate of the left most letter.
     * @param y    The y-coordinate of the line of text.
     * @param text The text to draw.
     */
    public static void drawText(int x, int y, String text) {
        for (int i = 0; i < Math.min(text.length(), GRID_WIDTH); i++) {
            grid.set(x + i, y, ASCII_OFFSET + text.charAt(i));
        }
    }

    /**
     * Initializes subgrids to the default blank state. All cells will be EMPTY except for the start
     * and end points. The start point will be located at 25% of the subgrid width, and the end
     * point will be located at 75% of the subgrid width. Both will be centered vertically in the
     * subgrid.
     */
    public static void initializeSubgrids() {
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                subgridsSet(x, y, EMPTY);
            }
        }
        clearExploration();
        startLocalPos = new Point(GRID_WIDTH / 4, GRID_HEIGHT / 2);
        targetLocalPos = new Point(GRID_WIDTH - GRID_WIDTH / 4, GRID_HEIGHT / 2);
        subgridsSet(startLocalPos, START);
        subgridsSet(targetLocalPos, TARGET);
    }

    /**
     * Initializes all pathfinders with the currently set start and endpoints. All pathfinders are
     * also marked as not completed.
     */
    public static void initializePathfinders() {
        for (Point pathfinderPos : subgridPositions) {
            pathfindersCompletion.put(pathfinderPos, false);
            Point globalStart = new Point(startLocalPos.x + pathfinderPos.x, startLocalPos.y + pathfinderPos.y);
            Point globalTarget = new Point(targetLocalPos.x + pathfinderPos.x, targetLocalPos.y + pathfinderPos.y);
            pathfinders.get(pathfinderPos).initialize(globalStart, globalTarget);
        }

    }

    /**
     * Clears the grid layer showing the pathfinder algorithms' exploration and solutions.
     *
     * @since v1.3
     */
    public static void clearExploration() {
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                grid.fill(1, 0);
            }
        }
    }

    /**
     * Returns whether the given value is that of a cell with a travel cost.
     *
     * @param value The value to check.
     * @return True if the given value is that of a cell with a travel cost, false otherwise.
     * @since v1.3
     */
    public static boolean isWeightedCell(int value) {
        return (value > WEIGHTED && value < WEIGHTED + MAX_COST);
    }

    /**
     * Main loop. Controls mouse input and algorithm progression.
     */
    public static void run() {
        while (true) {
            if (grid.isMouseDown()) {
                Point mousePos = grid.getMousePosition();
                if (started || mousePos == null) {
                    continue;
                }
                Point mouseLocalPos = getLocalPos(mousePos);

                // Mouse pressed down for the first time
                if (!isMouseDown) {
                    isMouseDown = true;
                    initialClick = grid.get(mousePos);
                } else { // Mouse already down
                    int currentlyOver = grid.get(mousePos);

                    if (initialClick == START) {
                        // Drag start point
                        if (currentlyOver == EMPTY) {
                            subgridsSet(startLocalPos, EMPTY);
                            subgridsSet(mouseLocalPos, START);
                            startLocalPos = mouseLocalPos;
                        }
                    } else if (initialClick == TARGET) {
                        // Drag target point
                        if (currentlyOver == EMPTY) {
                            subgridsSet(targetLocalPos, EMPTY);
                            subgridsSet(mouseLocalPos, TARGET);
                            targetLocalPos = mouseLocalPos;
                        }
                    } else if (selectedWeight == MAX_COST) {
                        if (initialClick == EMPTY) {
                            // Make walls
                            if (currentlyOver == EMPTY) {
                                subgridsSet(mouseLocalPos, WALL);
                            }
                        } else if (initialClick == WALL || isWeightedCell(initialClick)) {
                            // Erase walls
                            if (currentlyOver == WALL || isWeightedCell(currentlyOver)) {
                                subgridsSet(mouseLocalPos, EMPTY);
                            }
                        }
                    } else {
                        if (currentlyOver == EMPTY || isWeightedCell(currentlyOver)) {
                            subgridsSet(mouseLocalPos, WEIGHTED + selectedWeight);
                        }
                    }
                }
            } else {
                isMouseDown = false;
            }

            // Continually advances all algorithms until they complete, with a small delay between
            // each step.
            while (running) {
                stepAlgorithms();

                boolean allDone = true;
                for (Point p : pathfindersCompletion.keySet()) {
                    if (!pathfindersCompletion.get(p)) {
                        allDone = false;
                        break;
                    }
                }
                if (allDone) {
                    running = false;
                }

                try {
                    Thread.sleep(STEP_DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Advances all algorithms one iteration.
     */
    public static void stepAlgorithms() {
        if (!started) {
            initializePathfinders();
        }
        started = true;

        for (Point pathfinderPos : subgridPositions) {
            if (!pathfindersCompletion.get(pathfinderPos)) {
                Pathfinder pathfinder = pathfinders.get(pathfinderPos);
                List<Point> exploredCells = pathfinder.step();
                for (Point p : exploredCells) {
                    int currentValue = grid.get(p);
                    if (currentValue != START && currentValue != TARGET) {
                        grid.set(1, p, EXPLORED);
                    }
                }
                List<Point> solution = pathfinder.getSolution();
                if (solution != null) {
                    for (Point p : solution) {
                        int currentValue = grid.get(p);
                        if (currentValue != START && currentValue != TARGET) {
                            grid.set(1, p, SOLUTION);
                        }
                    }
                    pathfindersCompletion.put(pathfinderPos, true);
                }
            }
        }
    }

    /**
     * Transforms the given global grid coordinates to the local coordinates of subgrids.
     *
     * @param globalPos The global grid coordinates to transform.
     * @return The given coordinates as local coordinates of subgrids.
     */
    public static Point getLocalPos(Point globalPos) {
        Point localPos = new Point(globalPos);
        localPos.translate(-BORDER_SIZE, -BORDER_SIZE); // Remove border
        localPos.x = localPos.x % (GRID_WIDTH + GRID_MARGIN_X);
        localPos.y = localPos.y % (GRID_HEIGHT + GRID_MARGIN_Y);

        return localPos;
    }

    /**
     * Sets a cell in all subgrids to a given value.
     *
     * @param localPos The cell to set, in local subgrid coordinates.
     * @param value    The value to set the cell to.
     */
    public static void subgridsSet(Point localPos, int value) {
        subgridsSet(localPos.x, localPos.y, value);
    }

    /**
     * Sets a cell in all subgrids to a given value.
     *
     * @param x     The x-coordinate of the cell to set, in local subgrid coordinates.
     * @param y     The y-coordinate of the cell to set, in local subgrid coordinates.
     * @param value The value to set the cell to.
     */
    public static void subgridsSet(int x, int y, int value) {
        for (Point p : subgridPositions) {
            grid.set(p.x + x, p.y + y, value);
        }
    }

    /**
     * Returns whether a cell is traversable as part of a path.
     *
     * @param p The cell to check.
     * @return Whether the cell is empty or the target cell.
     */
    public static boolean isOpen(Point p) {
        if (grid.isOOB(p.x, p.y)) {
            return false;
        }

        int value = grid.get(p.x, p.y);
        return value == EMPTY || value == TARGET || isWeightedCell(value);
    }

    /**
     * Returns the cost to travel to the given cell.
     *
     * @param p The coordinates of the cell to get the travel cost of.
     * @return The cost to travel to the cell at the given coordinates.
     * @throws IllegalArgumentException If the given coordinates are not those of a cell in the
     *                                  grid.
     * @since v1.3
     */
    public static int getCost(Point p) {
        if (grid.isOOB(p.x, p.y)) {
            throw new IllegalArgumentException("Given coordinates must specify a cell in the grid.");
        }

        int value = grid.get(p.x, p.y);
        int cost = isWeightedCell(value) ? (value - WEIGHTED + 1) : 1;
        return cost;
    }
}

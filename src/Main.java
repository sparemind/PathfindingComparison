import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * A visual side-by-side comparison of common pathfinding algorithms.
 * <p>
 * The currently supported algorithms are:
 * <ul>
 * <li>Dijkstra's Algorithm</li>
 * <li>A* Search (2 variants)</li>
 * <li>Breadth First Search</li>
 * <li>Greedy Best First Search</li>
 * </ul>
 *
 * @author Jake Chiang
 * @version v1.5.1
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
    public static final int GRID_MARGIN_Y = 3;
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
     * 1 more than the maximum traversal cost that a cell can be set to.
     */
    public static final int MAX_COST = 10;
    /**
     * Minimum int-8 RGB value of a weighted cell.
     */
    public static final int WEIGHTED_MIN_COLOR = 100;

    /**
     * Maximum settable delay between each step in milliseconds.
     */
    public static final int MAX_DELAY = 100;
    /**
     * Default delay between each step in milliseconds.
     */
    public static final int DEFAULT_STEP_DELAY = 12;

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
    private static int selectedDelay = DEFAULT_STEP_DELAY;
    private static boolean showingWeights = false;
    private static boolean started = false;
    private static boolean running = false;
    private static Integer seed = null;
    private static boolean seedFieldClicked = false;
    private static Point[] subgridPositions = new Point[GRIDS_HORZ * GRIDS_VERT];
    private static JComboBox[] algorithmSelectors = new JComboBox[GRIDS_HORZ * GRIDS_VERT];
    private static Point startLocalPos;
    private static Point targetLocalPos;

    private static List<Pathfinder> loadedPathfinders = new ArrayList<>();
    private static Map<Point, Pathfinder> pathfinders = new HashMap<>();
    private static Map<Point, PathfinderData> pathfinderData = new HashMap<>();

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
            grid.setColor(value, new Color(255, 255 - (i * increment) / 2, 255 - i * increment));
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

        // All pathfinders that can be selected.
        // "None" must exist at the end of this list
        loadedPathfinders.add(new Dijkstra());
        loadedPathfinders.add(new BreadthFirstSearch());
        loadedPathfinders.add(new AStar());
        loadedPathfinders.add(new GreedyBestFirstSearch());
        loadedPathfinders.add(new StrongAStar());
        loadedPathfinders.add(new None());

        // Initialize pathfinders. Load the first several as the default ones.
        for (int i = 0; i < subgridPositions.length; i++) {
            Pathfinder pathfinder;
            if (i < loadedPathfinders.size()) {
                pathfinder = loadedPathfinders.get(i);
            } else {
                pathfinder = new None();
            }
            pathfinders.put(subgridPositions[i], pathfinder);
        }
        initializePathfinders();

        // Initialize GUI and grid labels
        initializeGUI();
        updateSubgridLabels();
        updateSolutionLabels();

        run();
    }

    /**
     * Initializes the GUI button and slider controls and adds them to the SimpleGrid frame.
     *
     * @since v1.3
     */
    private static void initializeGUI() {
        //////////////////////// TOP (Algorithm Selection) ////////////////////////
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());

        JPanel algorithmsPanel = new JPanel();
        algorithmsPanel.setLayout(new GridLayout(1, pathfinders.size()));
        topPanel.add(algorithmsPanel, BorderLayout.NORTH);

        String[] pathfinderOptions = new String[loadedPathfinders.size()];
        for (int i = 0; i < pathfinderOptions.length; i++) {
            pathfinderOptions[i] = loadedPathfinders.get(i).toString();
        }

        for (int i = 0; i < subgridPositions.length; i++) {
            JComboBox selectionBox = new JComboBox(pathfinderOptions);
            // If there are more subgrids than pathfinders, assign any extras to be "None", which is
            // the last in the loadedPathfinders list.
            selectionBox.setSelectedIndex(i < loadedPathfinders.size() ? i : loadedPathfinders.size() - 1);
            selectionBox.addActionListener(new SelectAlgorithmListener(i, selectionBox));
            algorithmsPanel.add(selectionBox);
            algorithmSelectors[i] = selectionBox;
        }

        //////////////////////// BOTTOM ////////////////////////
        JFrame frame = grid.getFrame();
        JPanel controlPanel = new JPanel();

        controlPanel.add(new JLabel("Slow"));
        JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 0, MAX_DELAY, MAX_DELAY - DEFAULT_STEP_DELAY);
        speedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                selectedDelay = MAX_DELAY - source.getValue();
            }
        });
        controlPanel.add(speedSlider);
        controlPanel.add(new JLabel("Fast"));

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                started = false;
                running = false;
                reset();
                initializeSubgrids();
            }
        });
        controlPanel.add(clearButton);

        JButton stepButton = new JButton("Step");
        stepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stepAlgorithms();
            }
        });
        controlPanel.add(stepButton);

        JButton runButton = new JButton("Run");
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                running = true;
            }
        });
        controlPanel.add(runButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        });
        controlPanel.add(resetButton);

        JLabel weightSliderValue = new JLabel("X");
        JSlider weightSlider = new JSlider(JSlider.HORIZONTAL, 1, MAX_COST, MAX_COST);
        weightSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                selectedWeight = source.getValue();
                weightSliderValue.setText(selectedWeight == MAX_COST ? "X" : ("" + selectedWeight));
            }
        });
        controlPanel.add(new JLabel("Cell Weight:"));
        controlPanel.add(weightSlider);
        controlPanel.add(weightSliderValue);

        //////////////////////// RIGHT TOP (Presets) ////////////////////////
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());

        JPanel presetsPanel = new JPanel();
        presetsPanel.setLayout(new GridLayout(10, 1));
        rightPanel.add(presetsPanel, BorderLayout.NORTH);

        JButton genMaze = new JButton("Maze");
        genMaze.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
                initializeSubgrids();

                // Fill with all walls
                for (int x = 0; x < GRID_WIDTH; x++) {
                    for (int y = 0; y < GRID_HEIGHT; y++) {
                        subgridsSet(x, y, WALL);
                    }
                }

                generateMaze(0, 0, getRandom());

                // Start in top left corner, target in bottom right
                setStartPosition(0, 0);
                setTargetPosition(GRID_WIDTH - 1, GRID_HEIGHT - 1);
            }
        });
        presetsPanel.add(genMaze);

        JButton genWeightedMaze = new JButton("Weighted Maze");
        genWeightedMaze.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
                initializeSubgrids();

                // Fill with all walls
                for (int x = 0; x < GRID_WIDTH; x++) {
                    for (int y = 0; y < GRID_HEIGHT; y++) {
                        subgridsSet(x, y, WALL);
                    }
                }

                generateMaze(0, 0, getRandom());
                randomlyWeightWalls(MAX_COST / 2);

                // Start in top left corner, target in bottom right
                setStartPosition(0, 0);
                setTargetPosition(GRID_WIDTH - 1, GRID_HEIGHT - 1);
            }
        });
        presetsPanel.add(genWeightedMaze);

        JButton genRandom = new JButton("Randomized");
        genRandom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
                Random rand = getRandom();

                for (int x = 0; x < GRID_WIDTH; x++) {
                    for (int y = 0; y < GRID_HEIGHT; y++) {
                        double r = rand.nextDouble();
                        if (r < 1.0 / 4.0) {
                            subgridsSet(x, y, WALL);
                        } else if (r <= 2.0 / 4.0) {
                            subgridsSet(x, y, EMPTY);
                        } else {
                            int randomWeight = WEIGHTED + 1 + (int) ((MAX_COST - 1) * rand.nextDouble());
                            subgridsSet(x, y, randomWeight);
                        }
                    }
                }

                // Start in top left corner, target in bottom right.
                setStartPosition(1, 1);
                setTargetPosition(GRID_WIDTH - 2, GRID_HEIGHT - 2);
            }
        });
        presetsPanel.add(genRandom);

        JButton genGradient = new JButton("Gradient");
        genGradient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();

                for (int x = 0; x < GRID_WIDTH; x++) {
                    for (int y = 0; y < GRID_HEIGHT; y++) {
                        // The weight of the cell. Weight is max in the y-center, decreasing going out
                        int diff = (int) ((1 - ((double) Math.abs(y - GRID_HEIGHT / 2) / (GRID_HEIGHT / 2))) * (MAX_COST - 1));
                        int finalWeight = WEIGHTED + diff;
                        if (diff == 0) {
                            finalWeight = EMPTY;
                        }
                        subgridsSet(x, y, finalWeight);
                    }
                }

                // Start at left center, target at right center.
                setStartPosition(1, GRID_HEIGHT / 2);
                setTargetPosition(GRID_WIDTH - 2, GRID_HEIGHT / 2);
            }
        });
        presetsPanel.add(genGradient);

        JButton genRandomGradient = new JButton("Randomized Gradient");
        genRandomGradient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
                Random rand = getRandom();

                for (int x = 0; x < GRID_WIDTH; x++) {
                    for (int y = 0; y < GRID_HEIGHT; y++) {
                        // The weight of the cell. Weight is max in the y-center, decreasing going out
                        int diff = (int) ((1 - ((double) Math.abs(y - GRID_HEIGHT / 2) / (GRID_HEIGHT / 2))) * (MAX_COST - 1));
                        int finalWeight = WEIGHTED + (diff - (int) (diff * rand.nextDouble()));
                        if (diff == 0) {
                            finalWeight = EMPTY;
                        }

                        // Randomly subtract some amount from this weight
                        // int randomWeight = WEIGHTED + 1 + (int) (diff - ((diff) * rand.nextDouble()));
                        subgridsSet(x, y, finalWeight);
                    }
                }

                // Start at left center, target at right center.
                setStartPosition(1, GRID_HEIGHT / 2);
                setTargetPosition(GRID_WIDTH - 2, GRID_HEIGHT / 2);
            }
        });
        presetsPanel.add(genRandomGradient);

        //////////////////////// RIGHT BOTTOM (Editing Functions) ////////////////////////
        JPanel editorPanel = new JPanel();
        editorPanel.setLayout(new GridLayout(4, 1));
        rightPanel.add(editorPanel, BorderLayout.SOUTH);

        JButton wallFill = new JButton("Fill With Walls");
        wallFill.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();

                for (int x = 0; x < GRID_WIDTH; x++) {
                    for (int y = 0; y < GRID_HEIGHT; y++) {
                        if (grid.get(BORDER_SIZE + x, BORDER_SIZE + y) == EMPTY) {
                            subgridsSet(x, y, WALL);
                        }
                    }
                }
            }
        });
        editorPanel.add(wallFill);

        JButton clearWeights = new JButton("Clear Weights");
        clearWeights.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();

                for (int x = 0; x < GRID_WIDTH; x++) {
                    for (int y = 0; y < GRID_HEIGHT; y++) {
                        if (isWeightedCell(grid.get(BORDER_SIZE + x, BORDER_SIZE + y))) {
                            subgridsSet(x, y, EMPTY);
                        }
                    }
                }
            }
        });
        editorPanel.add(clearWeights);

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
        editorPanel.add(toggleWeightsButton);

        JTextField seedField = new JTextField("Seed (Leave blank for none)");
        seedField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // If this is the first time the field has been clicked, remove
                // the instruction text.
                if (!seedFieldClicked) {
                    seedFieldClicked = true;
                    seedField.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {

            }
        });
        seedField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                String text = seedField.getText();
                if (text.isEmpty()) {
                    seed = null;
                } else {
                    seed = text.hashCode();
                }
            }
        });
        editorPanel.add(seedField);

        // Assemble final frame
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(rightPanel, BorderLayout.EAST);
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    /**
     * Creates an identical maze in all subgrids. Subgrids must be filled with all WALLS prior to
     * this method being called, or no maze will be produced. The created maze will have no cycles.
     *
     * @param x    The x-coordinate to generate the maze from.
     * @param y    The y-coordinate to generate the maze from.
     * @param rand The source of randomness used to generate the maze.
     * @since v1.3.2
     */
    private static void generateMaze(int x, int y, Random rand) {
        // Check the value to make sure we're still in a wall.
        // (Using subgrid 1 to check)
        if (grid.get(BORDER_SIZE + x, BORDER_SIZE + y) != WALL) {
            return;
        }
        subgridsSet(x, y, EMPTY);

        List<Point> directions = new ArrayList<>();
        directions.add(new Point(1, 0));
        directions.add(new Point(-1, 0));
        directions.add(new Point(0, 1));
        directions.add(new Point(0, -1));
        Collections.shuffle(directions, rand);

        for (Point direction : directions) {
            boolean canTunnel = false;
            Point p1 = new Point(x + direction.x, y + direction.y);
            if (grid.get(BORDER_SIZE + p1.x, BORDER_SIZE + p1.y) == WALL) {
                Point p2 = new Point(p1);
                p2.translate(direction.x, direction.y);

                if (grid.get(BORDER_SIZE + p2.x, BORDER_SIZE + p2.y) == WALL) {
                    subgridsSet(p1.x, p1.y, EMPTY);
                    canTunnel = true;
                }
            }

            if (canTunnel) {
                generateMaze(x + direction.x * 2, y + direction.y * 2, rand);
            }
        }
    }

    /**
     * Replaces the walls in all subgrids with randomly weighted cells.
     *
     * @param min The minimum cell weight that can be randomly assigned.
     * @since v1.3.3
     */
    private static void randomlyWeightWalls(int min) {
        Random rand = getRandom();
        min--;
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                if (grid.get(BORDER_SIZE + x, BORDER_SIZE + y) == WALL) {
                    int randomWeight = WEIGHTED + 1 + (int) (min + (MAX_COST - 1 - min) * rand.nextDouble());
                    subgridsSet(x, y, randomWeight);
                }
            }
        }
    }

    /**
     * Stops all currently running algorithms, clear any exploration visualization, and enable
     * algorithm selection.
     *
     * @since v1.3.1
     */
    private static void reset() {
        started = false;
        running = false;
        clearExploration();
        for (JComboBox box : algorithmSelectors) {
            box.setEnabled(true);
        }
    }

    /**
     * Draws the given text into grid cells at a location.
     *
     * @param x    The x-coordinate of the left most letter.
     * @param y    The y-coordinate of the line of text.
     * @param text The text to draw.
     */
    private static void drawText(int x, int y, String text) {
        for (int i = 0; i < Math.min(text.length(), grid.getWidth() - x); i++) {
            grid.set(x + i, y, ASCII_OFFSET + text.charAt(i));
        }
    }

    /**
     * Updates the subgrid labels that say the algorithm name.
     *
     * @since v1.5
     */
    private static void updateSubgridLabels() {
        for (Point pathfinderPos : subgridPositions) {
            // Clear previous text
            for (int x = 0; x < GRID_WIDTH; x++) {
                grid.set(pathfinderPos.x + x, pathfinderPos.y - 2, BG);
            }
            drawText(pathfinderPos.x, pathfinderPos.y - 2, pathfinders.get(pathfinderPos).toString());
        }
    }

    /**
     * Updates all subgrid labels with the path length and cost of the found solutions.
     *
     * @throws IllegalStateException If {@link #pathfinderData} is not initialized to have data for
     *                               all pathfinders.
     * @since v1.3.1
     */
    private static void updateSolutionLabels() {
        if (pathfinderData.isEmpty()) {
            throw new IllegalStateException("Pathfinder data not initialized.");
        }

        for (Point pathfinderPos : subgridPositions) {
            // Clear previous text
            for (int x = 0; x < GRID_WIDTH; x++) {
                grid.set(pathfinderPos.x + x, pathfinderPos.y - 1, BG);
            }

            // Draw new text
            PathfinderData data = pathfinderData.get(pathfinderPos);
            drawText(pathfinderPos.x, pathfinderPos.y - 1, "Cost/Length/Steps: " + data.toString());
        }
    }

    /**
     * Initializes subgrids to the default blank state. All cells will be EMPTY except for the start
     * and end points. The start point will be located at 25% of the subgrid width, and the end
     * point will be located at 75% of the subgrid width. Both will be centered vertically in the
     * subgrid.
     */
    private static void initializeSubgrids() {
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                subgridsSet(x, y, EMPTY);
            }
        }
        clearExploration();
        setStartPosition(GRID_WIDTH / 4, GRID_HEIGHT / 2);
        setTargetPosition(GRID_WIDTH - GRID_WIDTH / 4, GRID_HEIGHT / 2);
    }

    /**
     * Sets the start point position of all subgrids.
     *
     * @param x The new x-coordinate of start point in local subgrid coordinates.
     * @param y The new y-coordinate of start point in local subgrid coordinates.
     * @since v1.3.1
     */
    private static void setStartPosition(int x, int y) {
        startLocalPos = new Point(x, y);
        subgridsSet(startLocalPos, START);
    }

    /**
     * Sets the target point position of all subgrids.
     *
     * @param x The new x-coordinate of target point in local subgrid coordinates.
     * @param y The new y-coordinate of target point in local subgrid coordinates.
     * @since v1.3.1
     */
    private static void setTargetPosition(int x, int y) {
        targetLocalPos = new Point(x, y);
        subgridsSet(targetLocalPos, TARGET);
    }

    /**
     * Initializes all pathfinders with the currently set start and endpoints. All pathfinders are
     * also marked as not completed.
     */
    private static void initializePathfinders() {
        for (Point pathfinderPos : subgridPositions) {
            pathfinderData.put(pathfinderPos, new PathfinderData());
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
    private static void clearExploration() {
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
    private static boolean isWeightedCell(int value) {
        return (value > WEIGHTED && value < WEIGHTED + MAX_COST);
    }

    /**
     * Main loop. Controls mouse input and algorithm progression.
     */
    private static void run() {
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
                for (Point p : pathfinderData.keySet()) {
                    if (!pathfinderData.get(p).done) {
                        allDone = false;
                        break;
                    }
                }
                if (allDone) {
                    running = false;
                }

                try {
                    Thread.sleep(selectedDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Advances all algorithms one iteration.
     */
    private static void stepAlgorithms() {
        if (!started) {
            for (JComboBox box : algorithmSelectors) {
                box.setEnabled(false);
            }
            initializePathfinders();
        }
        started = true;

        // This set keeps track of which algorithms have step()'d on this method call. It is used in
        // the event that multiple of the same algorithm have been selected in different subgrids to
        // skip the algorithm from being step()'d multiple times per call to this method.
        Set<Pathfinder> alreadyProcessed = new HashSet<>();
        for (Point pathfinderPos : subgridPositions) {
            PathfinderData data = pathfinderData.get(pathfinderPos);

            if (!data.done) {
                Pathfinder pathfinder = pathfinders.get(pathfinderPos);
                // If this algorithm is selected multiple times and has already step()'d this
                // iteration, skip doing it again
                if (alreadyProcessed.contains(pathfinder)) {
                    continue;
                }
                alreadyProcessed.add(pathfinder);

                data.steps++; // Increment step counter

                List<Point> exploredCells = pathfinder.step();
                for (Point p : exploredCells) {
                    int currentValue = grid.get(p);
                    if (currentValue != START && currentValue != TARGET) {
                        grid.set(1, p, EXPLORED);
                    }
                }
                List<Point> solution = pathfinder.getSolution();
                if (solution != null) {
                    double cost = 0;

                    for (Point p : solution) {
                        int currentValue = grid.get(p);
                        if (currentValue != START && currentValue != TARGET) {
                            grid.set(1, p, SOLUTION);
                            cost += getCost(p);
                        }
                    }
                    cost++; // Add the final cost it takes to travel onto TARGET cell

                    data.done = true;
                    // Subtract 1 since the starting point shouldn't be included
                    data.pathLength = solution.size() - 1;
                    data.cost = cost;
                    updateSolutionLabels();
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
    private static Point getLocalPos(Point globalPos) {
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
    private static void subgridsSet(Point localPos, int value) {
        subgridsSet(localPos.x, localPos.y, value);
    }

    /**
     * Sets a cell in all subgrids to a given value.
     *
     * @param x     The x-coordinate of the cell to set, in local subgrid coordinates.
     * @param y     The y-coordinate of the cell to set, in local subgrid coordinates.
     * @param value The value to set the cell to.
     */
    private static void subgridsSet(int x, int y, int value) {
        for (Point p : subgridPositions) {
            grid.set(p.x + x, p.y + y, value);
        }
    }

    /**
     * Returns a Random number generator seeded with the user specified seed if one exists. If none
     * is specified, the generator is seeded with a default random seed of its own choosing.
     *
     * @return Random number generator, seeded with the user specified seed if one exists.
     */
    private static Random getRandom() {
        Random rand = new Random();
        if (seed != null) {
            rand.setSeed(seed);
        }
        return rand;
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

    /**
     * Holds the data about a loaded algorithm. This consists of whether the algorithm has found a
     * path, the length of the solution path, its cost, and the number of steps it took to find it.
     */
    private static class PathfinderData {
        public boolean done;
        public int pathLength;
        public double cost;
        public int steps;

        public PathfinderData() {
            this.done = false;
            this.pathLength = 0;
            this.cost = 0.0;
            this.steps = 0;
        }

        @Override
        public String toString() {
            return ((int) this.cost) + "/" + this.pathLength + "/" + this.steps;
        }
    }

    /**
     * Updates the selected algorithms when a new algorithm is selected
     */
    private static class SelectAlgorithmListener implements ActionListener {
        private int pathfinderIndex;
        private JComboBox selectionBox;

        /**
         * Initializes a new listener for a given subgrid and selection box.
         *
         * @param pathfinderIndex The subgrid index that this listener updates.
         * @param selectionBox    The selection box that this object is listening to.
         */
        public SelectAlgorithmListener(int pathfinderIndex, JComboBox selectionBox) {
            this.pathfinderIndex = pathfinderIndex;
            this.selectionBox = selectionBox;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            pathfinders.put(subgridPositions[this.pathfinderIndex], loadedPathfinders.get(this.selectionBox.getSelectedIndex()));
            updateSubgridLabels();
        }
    }
}

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    public static final int GRID_WIDTH = 30; // Width of each sub grid in number of cells
    public static final int GRID_HEIGHT = 21; // Height of each sub grid in number of cells
    public static final int CELL_SIZE = 12; // Size of each cell in pixels
    public static final int GRID_MARGIN_X = 2; // # of cells between each sub grid horizontally
    public static final int GRID_MARGIN_Y = 2; // # of cells between each sub grid vertically
    public static final int GRIDS_HORZ = 2; // # of sub grids per row
    public static final int GRIDS_VERT = 2; // # of sub grids per column
    public static final int BORDER_SIZE = 3; // # of cells padding the frame border

    public static final int BG = 1; // Background
    public static final int EMPTY = 2; // Empty grid space
    public static final int WALL = 3; // Wall obstacle
    public static final int START = 4; // Pathfinding start point
    public static final int TARGET = 5; // Pathfinding end point
    public static final int EXPLORED = 6; // Pathfinding explored cell

    private static SimpleGrid grid;
    private static boolean isMouseDown = false;
    private static int currentlyClicked = -1;
    private static Point[] subgridPositions = new Point[GRIDS_HORZ * GRIDS_VERT];

    private static Dijkstra dijkstra = new Dijkstra();

    public static void main(String[] args) {
        int totalWidth = (GRID_WIDTH * GRIDS_HORZ) + (GRIDS_HORZ - 1) * GRID_MARGIN_X + (BORDER_SIZE * 2);
        int totalHeight = (GRID_HEIGHT * GRIDS_VERT) + (GRIDS_VERT - 1) * GRID_MARGIN_Y + (BORDER_SIZE * 2);
        grid = new SimpleGrid(totalWidth, totalHeight, CELL_SIZE, 1, "Pathfinding Algorithms Comparison");
        grid.setGridlineColor(Color.GRAY);

        grid.setColor(BG, Color.GRAY);
        grid.setColor(EMPTY, Color.WHITE);
        grid.setColor(WALL, Color.BLACK);
        grid.setColor(START, Color.GREEN);
        grid.setColor(TARGET, Color.RED);
        grid.setColor(EXPLORED, Color.LIGHT_GRAY);

        // Calculate all subgrid positions
        for (int x = 0; x < GRIDS_HORZ; x++) {
            for (int y = 0; y < GRIDS_VERT; y++) {
                int xPos = BORDER_SIZE + x * (GRID_WIDTH + GRID_MARGIN_X);
                int yPos = BORDER_SIZE + y * (GRID_HEIGHT + GRID_MARGIN_Y);
                subgridPositions[x * GRIDS_VERT + y] = new Point(xPos, yPos);
            }
        }

        // Initialize grid and subgrids
        for (int x = 0; x < totalWidth; x++) {
            for (int y = 0; y < totalHeight; y++) {
                grid.set(x, y, BG);
            }
        }
        initializeSubgrids();


        JFrame frame = grid.getFrame();

        JPanel p = new JPanel();

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                initializeSubgrids();
            }
        });
        JButton stepButton = new JButton("Step");
        stepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("step");
                // List<Point> exploredCells = dijkstra.step();
                // for (Point p : exploredCells) {
                //     grid.set(p, EXPLORED);
                // }
            }
        });
        JButton runButton = new JButton("Run");
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(runButton.getText());
                if (runButton.getText().equals("Run")) {
                    runButton.setText("Stop");
                } else {
                    runButton.setText("Run");
                }
            }
        });
        p.add(resetButton);
        p.add(stepButton);
        p.add(runButton);
        frame.add(p, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);

        // Set<Point> points = new HashSet<>();
        // Point start = null;
        // Point target = null;
        // for (int x = BORDER_SIZE; x < GRID_WIDTH; x++) {
        //     for (int y = BORDER_SIZE; y < GRID_HEIGHT; y++) {
        //         int value = grid.get(x, y);
        //         if (value != WALL) {
        //             points.add(new Point(x, y));
        //         }
        //         if (value == START) {
        //             start = new Point(x, y);
        //         }
        //         if (value == TARGET) {
        //             target = new Point(x, y);
        //         }
        //     }
        // }
        // dijkstra.initialize(points, start, target);

        run();
    }

    public static void initializeSubgrids() {
        for (Point p : subgridPositions) {
            resetSubGrid(p);
        }
    }

    public static void resetSubGrid(Point pos) {
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                grid.set(pos.x + x, pos.y + y, EMPTY);
            }
        }

        grid.set(pos.x + (GRID_WIDTH / 4), pos.y + GRID_HEIGHT / 2, START);
        grid.set(pos.x + (GRID_WIDTH - GRID_WIDTH / 4), pos.y + GRID_HEIGHT / 2, TARGET);
    }

    public static void run() {
        while (true) {
            if (grid.isMouseDown()) {
                Point mousePos = grid.getMousePosition();
                if (mousePos == null) {
                    continue;
                }
                // Mouse pressed down for the first time
                if (!isMouseDown) {
                    isMouseDown = true;
                    currentlyClicked = grid.get(mousePos);
                } else { // Mouse already down
                    int currentlyOver = grid.get(mousePos);
                    switch (currentlyClicked) {
                        case EMPTY:
                            if (currentlyOver == EMPTY) {
                                subgridsSet(getLocalPos(mousePos), WALL);
                                // grid.set(mousePos, WALL);
                            }
                            break;
                        case WALL:
                            if (currentlyOver == WALL) {
                                subgridsSet(getLocalPos(mousePos), EMPTY);
                                // grid.set(mousePos, EMPTY);
                            }
                            break;
                        default:
                            break;
                    }
                }
            } else {
                isMouseDown = false;
            }
        }
    }

    public static Point getLocalPos(Point globalPos) {
        Point localPos = new Point(globalPos);
        localPos.translate(-BORDER_SIZE, -BORDER_SIZE);
        localPos.x = localPos.x % (GRID_WIDTH + GRID_MARGIN_X);
        localPos.y = localPos.y % (GRID_HEIGHT + GRID_MARGIN_Y);

        return localPos;
    }

    public static void subgridsSet(Point localPos, int value) {
        for (Point p : subgridPositions) {
            grid.set(p.x + localPos.x, p.y + localPos.y, value);
        }
    }

    public static boolean isOpen(Point p) {
        if (grid.isOOB(p.x, p.y)) {
            return false;
        }

        int value = grid.get(p.x, p.y);
        return value == EMPTY || value == TARGET;
    }

    // public static int[][] getSubGrid(int subGridX, int subGridY) {
    //     int[][] subGrid = new int[GRID_HEIGHT][GRID_WIDTH];
    //
    //     for (int y = subGridY; y < GRID_HEIGHT; y++) {
    //         subGrid[y] = new int[GRID_WIDTH];
    //         for (int x = subGridX; x < GRID_WIDTH; x++) {
    //             subGrid[y][x] = grid.get(x, y);
    //         }
    //     }
    //
    //     return subGrid;
    // }
}

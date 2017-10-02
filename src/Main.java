import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    public static final int GRID_WIDTH = 25; // Width of each sub grid in number of cells
    public static final int GRID_HEIGHT = 25; // Height of each sub grid in number of cells
    public static final int CELL_SIZE = 12; // Size of each cell in pixels
    public static final int GRID_MARGIN_X = 2; // # of cells between each sub grid horizontally
    public static final int GRID_MARGIN_Y = 2; // # of cells between each sub grid vertically
    public static final int GRIDS_HORZ = 2; // # of sub grids per row
    public static final int GRIDS_VERT = 2; // # of sub grids per column
    public static final int BORDER_SIZE = 3; // # of cells padding the frame border

    private static SimpleGrid grid;

    public static void main(String[] args) {
        int totalWidth = (GRID_WIDTH * GRIDS_HORZ) + (GRIDS_HORZ - 1) * GRID_MARGIN_X + (BORDER_SIZE * 2);
        int totalHeight = (GRID_HEIGHT * GRIDS_VERT) + (GRIDS_VERT - 1) * GRID_MARGIN_Y + (BORDER_SIZE * 2);
        grid = new SimpleGrid(totalWidth, totalHeight, CELL_SIZE, 1, "Pathfinding Algorithms Comparison");
        grid.setGridlineColor(Color.GRAY);

        grid.setColor(1, Color.GRAY);
        for (int x = 0; x < totalWidth; x++) {
            for (int y = 0; y < totalHeight; y++) {
                grid.set(x, y, 1);
            }
        }

        for (int x = 0; x < GRIDS_HORZ; x++) {
            for (int y = 0; y < GRIDS_VERT; y++) {
                int xPos = BORDER_SIZE + x * (GRID_WIDTH + GRID_MARGIN_X);
                int yPos = BORDER_SIZE + y * (GRID_HEIGHT + GRID_MARGIN_Y);
                resetSubGrid(xPos, yPos);
            }
        }

        JFrame frame = grid.getFrame();

        JPanel p = new JPanel();
        JButton b1 = new JButton("step");
        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("step");
            }
        });
        JButton b2 = new JButton("run");
        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(b2.getText());
                if (b2.getText().equals("run")) {
                    b2.setText("stop");
                } else {
                    b2.setText("run");
                }
            }
        });
        p.add(b1);
        p.add(b2);
        frame.add(p, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    public static void resetSubGrid(int xPos, int yPos) {
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                grid.set(xPos + x, yPos + y, 2);
            }
        }
    }
}

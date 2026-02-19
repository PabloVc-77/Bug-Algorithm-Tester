package main.ui;

import main.grid.Grid;
import main.bug.BugAlgorithm;

import javax.swing.JPanel;
import java.awt.*;
import java.util.List;

public class ComparePanel extends JPanel {

    private Grid grid;
    private BugAlgorithm bug1;
    private BugAlgorithm bug2;
    private List<Point> optimalPath;
    private Point goal;

    private int cellSize = 20;

    public ComparePanel(Grid grid,
                        BugAlgorithm bug1,
                        BugAlgorithm bug2,
                        List<Point> optimalPath,
                        Point goal) {

        this.grid = grid;
        this.bug1 = bug1;
        this.bug2 = bug2;
        this.optimalPath = optimalPath;
        this.goal = goal;

        setPreferredSize(new Dimension(
                grid.getWidth() * cellSize,
                grid.getHeight() * cellSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawGrid(g);
        drawOptimal(g);
        drawBugTrail(g, bug1, Color.ORANGE);
        drawBugTrail(g, bug2, Color.MAGENTA);
        drawBugCurrent(g, bug1, Color.RED);
        drawBugCurrent(g, bug2, Color.BLUE);

        // Draw goal
        g.setColor(Color.GREEN);
        g.fillOval(goal.x * cellSize, goal.y * cellSize, cellSize, cellSize);
    }

    private void drawGrid(Graphics g) {
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {

                if (grid.isObstacle(x, y))
                    g.setColor(Color.BLACK);
                else
                    g.setColor(Color.WHITE);

                g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);

                g.setColor(Color.GRAY);
                g.drawRect(x * cellSize, y * cellSize, cellSize, cellSize);
            }
        }
    }

    private void drawOptimal(Graphics g) {

        if (optimalPath == null || optimalPath.size() < 2)
            return;

        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(Color.BLUE);
        g2.setStroke(new BasicStroke(5));

        for (int i = 0; i < optimalPath.size() - 1; i++) {

            Point p1 = optimalPath.get(i);
            Point p2 = optimalPath.get(i + 1);

            int x1 = p1.x * cellSize + cellSize / 2;
            int y1 = p1.y * cellSize + cellSize / 2;

            int x2 = p2.x * cellSize + cellSize / 2;
            int y2 = p2.y * cellSize + cellSize / 2;

            g2.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawBugTrail(Graphics g, BugAlgorithm bug, Color color) {

        List<Point> history = bug.getHistory();

        if (history.size() < 2)
            return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2));

        for (int i = 0; i < history.size() - 1; i++) {

            Point p1 = history.get(i);
            Point p2 = history.get(i + 1);

            int x1 = p1.x * cellSize + cellSize / 2;
            int y1 = p1.y * cellSize + cellSize / 2;

            int x2 = p2.x * cellSize + cellSize / 2;
            int y2 = p2.y * cellSize + cellSize / 2;

            g2.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawBugCurrent(Graphics g, BugAlgorithm bug, Color color) {

        Point p = bug.getCurrentPosition();

        g.setColor(color);
        g.fillOval(
                p.x * cellSize + cellSize / 4,
                p.y * cellSize + cellSize / 4,
                cellSize / 2,
                cellSize / 2);
    }
}

package main.ui;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.List;

import main.grid.Grid;
import main.bug.BugAlgorithm;

import java.awt.Graphics2D;
import java.awt.BasicStroke;


public class GridPanel extends JPanel {

    private Grid grid;
    private BugAlgorithm bug;
    private Point goal;
    private int cellSize = 20;
    private List<Point> optimalPath;


    public GridPanel(Grid grid, BugAlgorithm bug, Point goal, List<Point> optimalPath) {

        this.grid = grid;
        this.bug = bug;
        this.goal = goal;
        this.optimalPath = optimalPath;

        setPreferredSize(new Dimension(
                grid.getWidth() * cellSize,
                grid.getHeight() * cellSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw grid
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

        // Draw goal
        g.setColor(Color.GREEN);
        g.fillOval(goal.x * cellSize, goal.y * cellSize, cellSize, cellSize);

        // Draw path tail
        if (bug.getHistory().size() > 1) {
            g.setColor(Color.ORANGE);
            java.util.List<Point> history = bug.getHistory();
            for (int i = 0; i < history.size() - 1; i++) {
                Point p1 = history.get(i);
                Point p2 = history.get(i + 1);
                g.drawLine(
                    p1.x * cellSize + cellSize / 2,
                    p1.y * cellSize + cellSize / 2,
                    p2.x * cellSize + cellSize / 2,
                    p2.y * cellSize + cellSize / 2
                );
            }
        }

        // Draw Optimal Path
        if (optimalPath != null && optimalPath.size() > 1) {

            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(Color.BLUE);

            // Make the line thicker (experiment with value)
            g2.setStroke(new BasicStroke(4));

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



        // Draw bug
        Point p = bug.getCurrentPosition();
        g.setColor(Color.RED);
        g.fillOval(p.x * cellSize, p.y * cellSize, cellSize, cellSize);
    }
}

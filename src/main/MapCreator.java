package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import java.io.*;

public class MapCreator extends JFrame {

    private final int rows = 20;
    private final int cols = 20;

    private final CellType[][] grid;

    private GridPanel gridPanel;

    private JLabel startLabel;
    private JLabel goalLabel;

    private CellType currentTool = CellType.WALL;

    private enum CellType {
        EMPTY,
        WALL,
        START,
        GOAL
    }

    public MapCreator() {

        setTitle("Map Creator");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        grid = new CellType[rows][cols];

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                grid[r][c] = CellType.EMPTY;

        setupUI();
        setVisible(true);
    }

    private void setupUI() {

        add(createToolPanel(), BorderLayout.WEST);
        add(createGridPanel(), BorderLayout.CENTER);
        add(createInfoPanel(), BorderLayout.EAST);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createToolPanel() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(150, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Tools"));

        JToggleButton wallBtn = new JToggleButton("Wall");
        JToggleButton startBtn = new JToggleButton("Start");
        JToggleButton goalBtn = new JToggleButton("Goal");
        JToggleButton eraseBtn = new JToggleButton("Eraser");

        ButtonGroup group = new ButtonGroup();
        group.add(wallBtn);
        group.add(startBtn);
        group.add(goalBtn);
        group.add(eraseBtn);

        wallBtn.setSelected(true);

        wallBtn.addActionListener(e -> currentTool = CellType.WALL);
        startBtn.addActionListener(e -> currentTool = CellType.START);
        goalBtn.addActionListener(e -> currentTool = CellType.GOAL);
        eraseBtn.addActionListener(e -> currentTool = CellType.EMPTY);

        panel.add(Box.createVerticalStrut(15));
        panel.add(wallBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(startBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(goalBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(eraseBtn);

        return panel;
    }

    private JPanel createGridPanel() {

        gridPanel = new GridPanel();
        gridPanel.setBackground(Color.WHITE);

        gridPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleClick(e);
            }
        });

        return gridPanel;
    }

    private JPanel createInfoPanel() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(180, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Info"));

        JLabel sizeLabel = new JLabel("Size: " + rows + " x " + cols);
        startLabel = new JLabel("Start: Not set");
        goalLabel = new JLabel("Goal: Not set");

        panel.add(Box.createVerticalStrut(15));
        panel.add(sizeLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(startLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(goalLabel);

        return panel;
    }

    private JPanel createBottomPanel() {

        JPanel panel = new JPanel(new FlowLayout());

        JButton saveBtn = new JButton("Save Map");
        JButton clearBtn = new JButton("Clear");
        JButton exitBtn = new JButton("Exit");

        saveBtn.addActionListener(e -> saveMap());
        clearBtn.addActionListener(e -> clearGrid());
        exitBtn.addActionListener(e -> dispose());

        panel.add(saveBtn);
        panel.add(clearBtn);
        panel.add(exitBtn);

        return panel;
    }

    private void handleClick(MouseEvent e) {

        int cellSize = Math.min(
                gridPanel.getWidth() / cols,
                gridPanel.getHeight() / rows
        );

        int col = e.getX() / cellSize;
        int row = e.getY() / cellSize;

        if (row < 0 || row >= rows || col < 0 || col >= cols)
            return;

        if (currentTool == CellType.START) {
            clearType(CellType.START);
        }

        if (currentTool == CellType.GOAL) {
            clearType(CellType.GOAL);
        }

        grid[row][col] = currentTool;

        updateInfo();
        gridPanel.repaint();
    }

    private void clearType(CellType type) {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (grid[r][c] == type)
                    grid[r][c] = CellType.EMPTY;
    }

    private void clearGrid() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                grid[r][c] = CellType.EMPTY;

        updateInfo();
        gridPanel.repaint();
    }

    private void updateInfo() {

        boolean startFound = false;
        boolean goalFound = false;

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                if (grid[r][c] == CellType.START)
                    startFound = true;
                if (grid[r][c] == CellType.GOAL)
                    goalFound = true;
            }

        startLabel.setText("Start: " + (startFound ? "✓" : "Not set"));
        goalLabel.setText("Goal: " + (goalFound ? "✓" : "Not set"));
    }

    private class GridPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int cellSize = Math.min(
                    getWidth() / cols,
                    getHeight() / rows
            );

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {

                    switch (grid[r][c]) {
                        case EMPTY:
                             g.setColor(Color.WHITE);
                             break;
                        case WALL:
                             g.setColor(Color.BLACK);
                             break;
                        case START:
                            g.setColor(Color.GREEN);
                            break;
                        case GOAL:
                            g.setColor(Color.RED);
                            break;
                    }

                    g.fillRect(c * cellSize, r * cellSize, cellSize, cellSize);

                    g.setColor(Color.GRAY);
                    g.drawRect(c * cellSize, r * cellSize, cellSize, cellSize);
                }
            }
        }
    }

    // MAP SAVING

    private void saveMap() {

        String name = JOptionPane.showInputDialog(this, "Enter map name:");

        if (name == null || name.trim().isEmpty()) {
            return;
        }

        File dir = new File("maps");
        if (!dir.exists()) {
            dir.mkdir();
        }

        File file = new File(dir, name + ".map");

        try (PrintWriter writer = new PrintWriter(file)) {

            writer.println(rows + " " + cols);

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {

                    char symbol = '.';

                    switch (grid[r][c]) {
                        case EMPTY: symbol = '.'; break;
                        case WALL: symbol = '#'; break;
                        case START: symbol = 'S'; break;
                        case GOAL: symbol = 'G'; break;
                    }

                    writer.print(symbol);
                }
                writer.println();
            }

            JOptionPane.showMessageDialog(this, "Map saved as: " + name);

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving map.");
        }
    }
}
package main;

import javax.swing.JFrame;
import javax.swing.Timer;

import main.grid.Grid;
import main.grid.MapGenerator;
import main.bug.*;
import main.config.RunMode;
import main.simulation.CompareSimulator;
import main.simulation.Simulator;
import main.ui.ComparePanel;
import main.ui.GridPanel;

import java.awt.Point;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BorderLayout;

import main.pathfinding.BFSPathfinder;
import java.util.List;


public class App {
    private static final double OBS_PROB = 0.2;

    public static void main(String[] args) {

        RunMode mode = parseArguments(args);

        switch (mode) {
            case DEBUG:
                runDebugMode();
                break;

            case COMPARE:
                runCompareMode();
                break;

            case MARATHON:
                //runMarathonMode();
                break;

            default:
                printUsage();
        }
    }

    private static RunMode parseArguments(String[] args) {

        if (args.length == 0) return RunMode.UNKNOWN;

        switch (args[0]) {
            case "-d":
                return RunMode.DEBUG;

            case "-c":
                return RunMode.COMPARE;

            case "-m":
                return RunMode.MARATHON;

            default:
                return RunMode.UNKNOWN;
        }
    }
    private static Point getRandomFreeCell(Grid grid, Random rand) {
        int x, y;

        do {
            x = rand.nextInt(grid.getWidth());
            y = rand.nextInt(grid.getHeight());
        } while (grid.isObstacle(x, y));

        return new Point(x, y);
    }
    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java main.App -d   Debug mode (visual)");
        System.out.println("  java main.App -c   Compare two bugs");
        System.out.println("  java main.App -m   Marathon test mode");
    }

    private static void runDebugMode() {
        System.out.println("Running DEBUG mode");

        System.out.println("Program started");


        Random rand = new Random();

        int width = 20 + rand.nextInt(41);   // 20–60
        int height = 20 + rand.nextInt(41);

        Grid grid = MapGenerator.generate(width, height, OBS_PROB);

        Point start = getRandomFreeCell(grid, rand);
        Point goal = start;

        List<Point> optimalPath = null;
        while (goal.equals(start) || (optimalPath != null && optimalPath.size() < 10)) {
            goal = getRandomFreeCell(grid, rand);
            optimalPath = BFSPathfinder.findPath(grid, start, goal);
        }

        if (optimalPath == null) {
            System.out.println("No optimal path found.");
        } else {
            System.out.println("Optimal path length: " + optimalPath.size());
        }

        BugAlgorithm bug = new SimpleBug();
        bug.init(grid, start, goal);

        Simulator simulator = new Simulator(bug);

        GridPanel panel = new GridPanel(grid, bug, goal, optimalPath);

        JFrame frame = new JFrame("Bug Tester");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel controls = new JPanel();
        JButton backButton = new JButton("<<");
        JButton pauseButton = new JButton("Pause");
        JButton resetButton = new JButton("Reset");

        controls.add(backButton);
        controls.add(pauseButton);
        controls.add(resetButton);

        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        frame.add(controls, BorderLayout.SOUTH);


        Timer timer = new Timer(200, e -> {
            simulator.step();
            panel.repaint();
        });
        timer.start();

        final boolean[] paused = {false};

        pauseButton.addActionListener(e -> {
            paused[0] = !paused[0];
            if (paused[0]) {
                timer.stop();
                pauseButton.setText("Resume");
            } else {
                timer.start();
                pauseButton.setText("Pause");
            }
        });

        backButton.addActionListener(e -> {
            // Step back
            int current = bug.getCurrentStepIndex();
            int step = current - 1;
            
            if (step >= 0) {
                if (!paused[0]) {
                    timer.stop();
                    pauseButton.setText("Resume");
                }

                 bug.resetToStep(step);
                 panel.repaint();
            }
        });

        resetButton.addActionListener(e -> {
            timer.stop();
            bug.resetToStep(0);
            
            paused[0] = true;
            pauseButton.setText("Resume");
            panel.repaint();
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void runCompareMode() {

        System.out.println("Running COMPARE mode");

        Random rand = new Random();

        int width = 20 + rand.nextInt(41);
        int height = 20 + rand.nextInt(41);

        Grid grid = MapGenerator.generate(width, height, OBS_PROB);

        Point start = getRandomFreeCell(grid, rand);
        Point goal = start;

        List<Point> optimalPath = null;
        while (goal.equals(start) || (optimalPath != null && optimalPath.size() < 10)) {
            goal = getRandomFreeCell(grid, rand);
            optimalPath = BFSPathfinder.findPath(grid, start, goal);
        }

        if (optimalPath == null) {
            System.out.println("No optimal path found.");
        } else {
            System.out.println("Optimal path length: " + optimalPath.size());
        }

        BugAlgorithm bug1 = new SimpleBug();
        BugAlgorithm bug2 = new SimpleBug2(); // replace later with another algorithm

        bug1.init(grid, start, goal);
        bug2.init(grid, start, goal);

        CompareSimulator simulator = new CompareSimulator(bug1, bug2);

        ComparePanel panel = new ComparePanel(grid, bug1, bug2, optimalPath, goal);

        JFrame frame = new JFrame("Compare Mode");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        JPanel controls = new JPanel();
        JButton backButton = new JButton("<<");
        JButton pauseButton = new JButton("Pause");
        JButton resetButton = new JButton("Reset");

        controls.add(backButton);
        controls.add(pauseButton);
        controls.add(resetButton);

        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        frame.add(controls, BorderLayout.SOUTH);

        Timer timer = new Timer(150, e -> {
            simulator.step();
            panel.repaint();
        });

        timer.start();

        final boolean[] paused = {false};

        pauseButton.addActionListener(e -> {
            paused[0] = !paused[0];
            if (paused[0]) {
                timer.stop();
                pauseButton.setText("Resume");
            } else {
                timer.start();
                pauseButton.setText("Pause");
            }
        });

        backButton.addActionListener(e -> {
            // Step back
            int current1 = bug1.getCurrentStepIndex();
            int current2 = bug2.getCurrentStepIndex();
            int step1 = current1 - 1;
            int step2 = current2 - 1;
            
            if (step1 >= 0) {
                if (!paused[0]) {
                    timer.stop();
                    pauseButton.setText("Resume");
                }

                 bug1.resetToStep(step1);
                 panel.repaint();
            }

            if (step2 >= 0) {
                if (!paused[0]) {
                    timer.stop();
                    pauseButton.setText("Resume");
                }

                 bug2.resetToStep(step2);
                 panel.repaint();
            }
        });

        resetButton.addActionListener(e -> {
            timer.stop();
            bug1.resetToStep(0);
            bug2.resetToStep(0);
            
            paused[0] = true;
            pauseButton.setText("Resume");
            panel.repaint();
        });
    }

}

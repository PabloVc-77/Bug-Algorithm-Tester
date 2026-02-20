package main;

import javax.swing.JFrame;
import javax.swing.JLabel;
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

import java.awt.GridLayout;
import main.bugStats.BugStats;
import java.awt.Dimension;

import javax.swing.JSplitPane;
import java.awt.Dimension;


public class App {
    private static final double OBS_PROB = 0.2;

    private static class RunConfig {
        RunMode mode;
        Long seed; // nullable
    }

    public static void main(String[] args) {

        RunConfig config = parseArguments(args);

        switch (config.mode) {
            case DEBUG:
            case COMPARE:
                runGuiMode(config);
                break;

            case MARATHON:
                //runMarathonMode();
                break;

            default:
                printUsage();
        }
    }

    private static RunConfig parseArguments(String[] args) {

        RunConfig config = new RunConfig();
        config.mode = RunMode.UNKNOWN;
        config.seed = null;

        for (int i = 0; i < args.length; i++) {

            switch (args[i]) {

                case "-d":
                    config.mode = RunMode.DEBUG;
                    break;

                case "-c":
                    config.mode = RunMode.COMPARE;
                    break;

                case "-m":
                    config.mode = RunMode.MARATHON;
                    break;

                case "-seed":
                    if (i + 1 < args.length) {
                        config.seed = Long.parseLong(args[i + 1]);
                        i++;
                    }
                    break;
            }
        }

        return config;
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

    private static void runGuiMode(RunConfig config) {

        RunMode mode = config.mode;
        System.out.println("Running " + mode + " mode");

        Random rand;
        long generatedSeed = 0;

        if (config.seed != null) {
            rand = new Random(config.seed);
            System.out.println("Using seed: " + config.seed);
        } else {
            generatedSeed = System.currentTimeMillis();
            rand = new Random(generatedSeed);
            System.out.println("Generated seed: " + generatedSeed);
        }


        int width = 20 + rand.nextInt(41);
        int height = 20 + rand.nextInt(41);

        Grid grid = MapGenerator.generate(width, height, OBS_PROB, rand);

        Point start = getRandomFreeCell(grid, rand);
        Point goal = start;

        List<Point> optimalPath = null;
        while (goal.equals(start) || (optimalPath != null && optimalPath.size() < 10)) {
            goal = getRandomFreeCell(grid, rand);
            optimalPath = BFSPathfinder.findPath(grid, start, goal);
        }

        final int optimalLength = (optimalPath == null) ? 0 : optimalPath.size();

        if (optimalPath == null) {
            System.out.println("No optimal path found.");
        } else {
            System.out.println("Optimal path length: " + optimalLength);
        }

        // -----------------------
        // CREATE BUGS
        // -----------------------

        BugAlgorithm bug1 = new Bug1();
        bug1.init(grid, start, goal);

        final BugAlgorithm bug2;

        if (mode == RunMode.COMPARE) {
            bug2 = new Bug2();
            bug2.init(grid, start, goal);
        } else {
            bug2 = null;
        }

        // -----------------------
        // CREATE SIMULATOR
        // -----------------------

        final Simulator simulator;
        final CompareSimulator compareSimulator;

        if (mode == RunMode.DEBUG) {
            simulator = new Simulator(bug1);
            compareSimulator = null;
        } else {
            compareSimulator = new CompareSimulator(bug1, bug2);
            simulator = null;
        }

        // -----------------------
        // CREATE PANEL
        // -----------------------

        JFrame frame = new JFrame(mode == RunMode.DEBUG ? "Bug Debug Mode" : "Bug Compare Mode");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle(frame.getTitle() + " | Seed: " + (config.seed == null ? generatedSeed : config.seed));

        JPanel panel;

        if (mode == RunMode.DEBUG) {
            panel = new GridPanel(grid, bug1, goal, optimalPath);
        } else {
            panel = new ComparePanel(grid, bug1, bug2, optimalPath, goal);
        }

        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(0, 4)); // metric | bug1 | bug2 | diff

        statsPanel.add(new JLabel("Metric"));
        statsPanel.add(new JLabel("Bug 1"));
        statsPanel.add(new JLabel("Bug 2"));
        statsPanel.add(new JLabel("Δ"));

        JPanel statsContainer = new JPanel(new BorderLayout());
        statsContainer.add(statsPanel, BorderLayout.NORTH);
        statsContainer.setPreferredSize(new Dimension(250, 0));


        // -----------------------
        // CONTROLS
        // -----------------------

        JPanel controls = new JPanel();
        JButton backButton = new JButton("<<");
        JButton pauseButton = new JButton("Pause");
        JButton resetButton = new JButton("Reset");

        controls.add(backButton);
        controls.add(pauseButton);
        controls.add(resetButton);

       frame.setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                panel,            // izquierda (mapa)
                statsContainer    // derecha (stats)
        );

        splitPane.setResizeWeight(1.0);   // el mapa tiene prioridad
        splitPane.setDividerSize(5);
        splitPane.setContinuousLayout(true);

        // Opcional: posición inicial del divisor
        splitPane.setDividerLocation(800);

        frame.add(splitPane, BorderLayout.CENTER);
        frame.add(controls, BorderLayout.SOUTH);



        Runnable updateStatsUI = () -> {

            statsPanel.removeAll();

            statsPanel.add(new JLabel("Metric"));
            statsPanel.add(new JLabel("Bug 1"));
            statsPanel.add(new JLabel("Bug 2"));
            statsPanel.add(new JLabel("Δ"));

            BugStats s1 = buildStats(bug1, optimalLength);

            BugStats s2 = null;
            if (mode == RunMode.COMPARE && bug2 != null) {
                s2 = buildStats(bug2, optimalLength);
            }

            addStatRow(statsPanel, "Path Length",
                    s1.getPathLength(),
                    s2 == null ? "-" : s2.getPathLength());

            addStatRow(statsPanel, "Over Optimal",
                    s1.getDiffFromOptimal(),
                    s2 == null ? "-" : s2.getDiffFromOptimal());

            addStatRow(statsPanel, "Efficiency %",
                    String.format("%.1f", s1.getEfficiency()),
                    s2 == null ? "-" : String.format("%.1f", s2.getEfficiency()));

            statsPanel.revalidate();
            statsPanel.repaint();
        };


        // -----------------------
        // TIMER
        // -----------------------

        Timer timer = new Timer(150, e -> {

            if (mode == RunMode.DEBUG) {
                simulator.step();
            } else {
                compareSimulator.step();
            }

            panel.repaint();
            updateStatsUI.run();
        });

        timer.start();

        final boolean[] paused = {false};

        // -----------------------
        // PAUSE
        // -----------------------

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

        // -----------------------
        // BACK BUTTON
        // -----------------------

        backButton.addActionListener(e -> {

            if (!paused[0]) {
                timer.stop();
                pauseButton.setText("Resume");
                paused[0] = true;
            }

            int step1 = bug1.getCurrentStepIndex() - 1;
            if (step1 >= 0) {
                bug1.resetToStep(step1);
            }

            if (mode == RunMode.COMPARE && bug2 != null) {
                int step2 = bug2.getCurrentStepIndex() - 1;
                if (step2 >= 0) {
                    bug2.resetToStep(step2);
                }
            }

            panel.repaint();
        });

        // -----------------------
        // RESET
        // -----------------------

        resetButton.addActionListener(e -> {

            timer.stop();
            paused[0] = true;
            pauseButton.setText("Resume");

            bug1.resetToStep(0);

            if (mode == RunMode.COMPARE && bug2 != null) {
                bug2.resetToStep(0);
            }

            panel.repaint();
        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static BugStats buildStats(BugAlgorithm bug, int optimalLength) {

        BugStats stats = new BugStats();

        int pathLength = bug.getHistory() == null ? 0 : bug.getHistory().size();

        stats.setPathLength(pathLength);
        stats.setOptimalLength(optimalLength);
        stats.setDiffFromOptimal(pathLength - optimalLength);

        if (pathLength > 0) {
            double efficiency = ((double) optimalLength / pathLength) * 100.0;
            stats.setEfficiency(efficiency);
        } else {
            stats.setEfficiency(0.0);
        }

        return stats;
    }
    private static void addStatRow(JPanel panel, String name, Object v1, Object v2) {

        panel.add(new JLabel(name));
        panel.add(new JLabel(String.valueOf(v1)));
        panel.add(new JLabel(String.valueOf(v2)));

        if (v1 instanceof Number && v2 instanceof Number) {
            double diff = ((Number) v2).doubleValue()
                    - ((Number) v1).doubleValue();
            panel.add(new JLabel(String.format("%.2f", diff)));
        } else {
            panel.add(new JLabel("-"));
        }
    }

}

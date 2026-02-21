package main;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;

import main.grid.Grid;
import main.grid.MapGenerator;
import main.bug.*;
import main.config.RunMode;
import main.simulation.CompareSimulator;
import main.simulation.MarathonResult;
import main.simulation.MarathonRunner;
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
import main.ui.MarathonConsoleView;


public class App {
    private static final double OBS_PROB = 0.01;
    private static final int NORMAL_DELAY = 200;
    private static final int FAST_DELAY = 20;

    private static class RunConfig {
        RunMode mode;
        Long seed; // nullable
        int amount;
        boolean marathonCompare;
    }

    public static void main(String[] args) {

        RunConfig config = parseArguments(args);

        switch (config.mode) {
            case DEBUG:
            case COMPARE:
                runGuiMode(config);
                break;

            case MARATHON:
                if(config.marathonCompare) {runMarathonCompareMode(config.amount);}
                else {runMarathonMode(config.amount);}
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
                    if (i + 1 < args.length) {
                        config.amount = Integer.parseInt(args[i + 1]);
                        i++;
                    } else {
                        config.amount = -1;
                    }

                    if(i + 1 < args.length && args[i + 1].equals("-c")) {
                        config.marathonCompare = true;
                        i++;
                    } else {
                        config.marathonCompare = false;
                    }
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
        System.out.println("============================================================");
        System.out.println("               MAIN BUG WILL BE Bug2.java                   ");
        System.out.println("============================================================");
        System.out.println("Usage:");
        System.out.println("  java main.App -d           |  Debug mode (visual)");
        System.out.println("  java main.App -c           |  Compare two bugs");
        System.out.println("  java main.App -m amount    |  Marathon test mode");
        System.out.println("  java main.App -m amount -c |  Marathon test & Compare mode");
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

        BugAlgorithm bug1 = new Bug2();
        bug1.init(grid, start, goal);

        final BugAlgorithm bug2;

        if (mode == RunMode.COMPARE) {
            bug2 = new Bug1();
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

        JPanel statsPanel = new JPanel(new BorderLayout());

        JPanel statsContent = new JPanel(new GridLayout(0, 4, 10, 10));
        statsContent.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Statistics", JLabel.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(18f));
        titleLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 0, 10, 0));

        statsPanel.add(titleLabel, BorderLayout.NORTH);
        statsPanel.add(statsContent, BorderLayout.CENTER);

        addHeader(statsContent, "Metric");
        addHeader(statsContent, "Bug 2");
        addHeader(statsContent, "Bug 1");
        addHeader(statsContent, "Diff");

        JPanel statsContainer = new JPanel(new BorderLayout());
        statsContainer.add(statsPanel, BorderLayout.CENTER);
        statsContainer.setPreferredSize(new Dimension(300, 0));


        // -----------------------
        // CONTROLS
        // -----------------------

        JPanel controls = new JPanel();
        JButton backButton = new JButton("<<");
        JButton forwardButton = new JButton(">>");
        JButton pauseButton = new JButton("Pause");
        JButton resetButton = new JButton("Reset");
        JButton fastForwardButton = new JButton("x10");

        controls.add(backButton);
        controls.add(forwardButton);
        controls.add(pauseButton);
        controls.add(resetButton);
        controls.add(fastForwardButton);

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

            statsContent.removeAll();

            addHeader(statsContent, "Metric");
            addHeader(statsContent, "Bug 2");
            addHeader(statsContent, "Bug 1");
            addHeader(statsContent, "Diff");

            BugStats s1 = buildStats(bug1, optimalLength);

            BugStats s2 = null;
            if (mode == RunMode.COMPARE && bug2 != null) {
                s2 = buildStats(bug2, optimalLength);
            }

            addStatRow(statsContent, "Path Length",
                    s1.getPathLength(),
                    s2 == null ? "-" : s2.getPathLength());

            addStatRow(statsContent, "Over Optimal",
                    s1.getDiffFromOptimal(),
                    s2 == null ? "-" : s2.getDiffFromOptimal());

            addStatRow(statsContent, "Efficiency %",
                    String.format("%.1f", s1.getEfficiency()),
                    s2 == null ? "-" : String.format("%.1f", s2.getEfficiency()));
                    
            addStatRow(statsContent, "State",
                formatState(s1),
                s2 == null ? "-" : formatState(s2));

            statsPanel.revalidate();
            statsPanel.repaint();
        };


        // -----------------------
        // TIMER
        // -----------------------

        final boolean[] paused = {false};

        Timer timer = new Timer(NORMAL_DELAY, e -> {

            if (mode == RunMode.DEBUG) {
                simulator.step();
            } else {
                compareSimulator.step();
            }

            panel.repaint();
            updateStatsUI.run();

            boolean bug1Done = bug1.hasFinished() || bug1.hasGivenUp();
            boolean bug2Done = (mode == RunMode.COMPARE && bug2 != null)
                    ? (bug2.hasFinished() || bug2.hasGivenUp())
                    : true;

            if (bug1Done && bug2Done) {
                ((Timer) e.getSource()).stop();
                pauseButton.setText("Finished");
                paused[0] = true;
            }
        });

        timer.start();

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
        // FORWARD BUTTON
        // -----------------------

        forwardButton.addActionListener(e -> {

            if (!paused[0]) {
                timer.stop();
                pauseButton.setText("Resume");
                paused[0] = true;
            }

            // Run next step
            if (mode == RunMode.DEBUG) {
                simulator.step();
            } else {
                compareSimulator.step();
            }

            panel.repaint();
            updateStatsUI.run();

            boolean bug1Done = bug1.hasFinished() || bug1.hasGivenUp();
            boolean bug2Done = (mode == RunMode.COMPARE && bug2 != null)
                    ? (bug2.hasFinished() || bug2.hasGivenUp())
                    : true;

            if (bug1Done && bug2Done) {
                timer.stop();
                pauseButton.setText("Finished");
                paused[0] = true;
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

        // -----------------------
        // FAST FORWARD
        // -----------------------

        fastForwardButton.addActionListener(e -> {

            if (!timer.isRunning()) {
                timer.start();
                pauseButton.setText("Pause");
                paused[0] = false;
            }

            if (timer.getDelay() == NORMAL_DELAY) {
                timer.setDelay(FAST_DELAY);
                fastForwardButton.setText("x1");
            } else {
                timer.setDelay(NORMAL_DELAY);
                fastForwardButton.setText("x10");
            }
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
        stats.setFinished(bug.hasFinished());
        stats.setGivenUp(bug.hasGivenUp());

        if (pathLength > 0) {
            double efficiency = ((double) optimalLength / pathLength) * 100.0;
            stats.setEfficiency(efficiency);
        } else {
            stats.setEfficiency(0.0);
        }

        return stats;
    }
    private static void addStatRow(JPanel panel, String name, Object v1, Object v2) {

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(nameLabel.getFont().deriveFont(14f));

        JLabel bug1Label = new JLabel(String.valueOf(v1), JLabel.CENTER);
        bug1Label.setFont(bug1Label.getFont().deriveFont(16f));

        JLabel bug2Label = new JLabel(String.valueOf(v2), JLabel.CENTER);
        bug2Label.setFont(bug2Label.getFont().deriveFont(16f));

        JLabel diffLabel;

        // 🎨 Special handling for YES/NO coloring
        paint(v1, bug1Label);
        paint(v2, bug2Label);

        if (v1 instanceof Number && v2 instanceof Number) {
            double diff = ((Number) v1).doubleValue()
                    - ((Number) v2).doubleValue();
            diffLabel = new JLabel(String.format("%.2f", diff), JLabel.CENTER);
        } else {
            diffLabel = new JLabel("-", JLabel.CENTER);
        }

        diffLabel.setFont(diffLabel.getFont().deriveFont(16f));

        panel.add(nameLabel);
        panel.add(bug1Label);
        panel.add(bug2Label);
        panel.add(diffLabel);
    }
    private static void paint(Object v, JLabel label) {
        if ("FINISHED".equals(v)) {
            label.setForeground(new java.awt.Color(0,150,0));
        }
        if ("GAVE UP".equals(v)) {
            label.setForeground(java.awt.Color.RED);
        }
        if ("RUNNING".equals(v)) {
            label.setForeground(java.awt.Color.ORANGE);
        }
    }
    private static void addHeader(JPanel panel, String text) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(label.getFont().deriveFont(java.awt.Font.BOLD, 14f));
        panel.add(label);
    }
    private static String formatState(BugStats stats) {
        if (stats.isFinished()) return "FINISHED";
        if (stats.hasGivenUp()) return "GAVE UP";
        return "RUNNING";
    }

    //********************************************* */
    //              MARATHON CODE
    //********************************************* */

    private static void runMarathonMode(int amount) {

        System.out.println("Starting Marathon Mode...");

        MarathonRunner runner = new MarathonRunner();

        MarathonResult result = runner.run(
                (amount <= 0) ? 0 : amount,
                () -> new Bug2(),
                false,
                null
        );

        MarathonConsoleView.show(result);
    }

    private static void runMarathonCompareMode(int amount) {
        System.out.println("Starting Marathon Compare Mode...");

        MarathonRunner runner = new MarathonRunner();

        MarathonResult result = runner.run(
                (amount <= 0) ? 0 : amount,
                () -> new Bug2(),
                true,
                () -> new Bug1()
        );

        MarathonConsoleView.show(result);
    }
}

package main.simulation;

import java.awt.Point;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import main.bug.*;
import main.grid.*;
import main.pathfinding.BFSPathfinder;

public class MarathonRunner {

    private static final double OBS_PROB = 0.01;

    public MarathonResult run(
            int numberOfMaps,
            Supplier<BugAlgorithm> bugFactory,
            boolean compareMode,
            Supplier<BugAlgorithm> bug2Factory
    ) {

        MarathonResult result = new MarathonResult();
        result.totalMaps = numberOfMaps;

        for (int i = 0; i < numberOfMaps; i++) {
            long generatedSeed = System.currentTimeMillis();
            Random rand = new Random(generatedSeed);
            int width = 20 + rand.nextInt(41);
            int height = 20 + rand.nextInt(41);

            Grid map = MapGenerator.generate(width, height, OBS_PROB, rand);
            Point start = getRandomFreeCell(map, rand);
            Point goal = start;

            List<Point> optimalPath = null;
            while (goal.equals(start) || (optimalPath != null && optimalPath.size() < 10)) {
                goal = getRandomFreeCell(map, rand);
                optimalPath = BFSPathfinder.findPath(map, start, goal);
            }

            BugAlgorithm bug1 = bugFactory.get();
            bug1.init(map, start, goal);

            BugAlgorithm bug2 = null;
            if (compareMode) {
                bug2 = bug2Factory.get();
                bug2.init(map, start, goal);
            }

            runSingleMap(map, bug1, bug2, result, (optimalPath == null) ? 0 : optimalPath.size());
        }

        return result;
    }

    private Point getRandomFreeCell(Grid grid, Random rand) {
        int x, y;

        do {
            x = rand.nextInt(grid.getWidth());
            y = rand.nextInt(grid.getHeight());
        } while (grid.isObstacle(x, y));

        return new Point(x, y);
    }

    private void runSingleMap(
            Grid map,
            BugAlgorithm bug1,
            BugAlgorithm bug2,
            MarathonResult result,
            int optimal
    ) {

        int maxSteps = 10_000;  // safety

        while (maxSteps-- > 0) {

            if (!bug1.hasFinished() && !bug1.hasGivenUp())
                bug1.nextStep();

            if (bug2 != null && !bug2.hasFinished() && !bug2.hasGivenUp())
                bug2.nextStep();

            if (bug2 == null) {
                if (bug1.hasFinished() || bug1.hasGivenUp())
                    break;
            } else {
                if ((bug1.hasFinished() || bug1.hasGivenUp()) &&
                    (bug2.hasFinished() || bug2.hasGivenUp()))
                    break;
            }
        }

        processResults(bug1, bug2, result, map, optimal);
    }

    private void processResults(
            BugAlgorithm bug1,
            BugAlgorithm bug2,
            MarathonResult result,
            Grid map,
            int optimal
    ) {

        if (bug1.hasFinished()) {
            result.completed++;
            result.totalSteps += bug1.getHistory().size();
            result.totalOptimalSteps += optimal;
        } else {
            result.gaveUp++;
        }

        if (bug2 != null) {

            if (bug1.hasFinished() && bug2.hasFinished()) {
                if (bug1.getHistory().size() < bug2.getHistory().size())
                    result.wins++;
                else if (bug1.getHistory().size() > bug2.getHistory().size())
                    result.losses++;
                else
                    result.ties++;
            }
        }
    }
}

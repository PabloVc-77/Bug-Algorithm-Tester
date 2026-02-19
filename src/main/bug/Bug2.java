package main.bug;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import main.grid.Grid;

public class Bug2 implements BugAlgorithm {
    private Grid grid;
    private Point goal;

    private List<Point> history = new ArrayList<>();
    private int currentStepIndex = 0;
    private boolean finished = false;

    @Override
    public void init(Grid grid, Point start, Point goal) {
        this.grid = grid;
        this.goal = goal;

        history.clear();
        history.add(new Point(start));
        currentStepIndex = 0;
        finished = false;
    }

    @Override
    public Point nextStep() {

        if (finished) return getCurrentPosition();

        // If we are simulating from a past point, truncate the future history
        if (currentStepIndex < history.size() - 1) {
            history = new ArrayList<>(history.subList(0, currentStepIndex + 1));
        }

        Point current = history.get(currentStepIndex);

        if (current.equals(goal)) {
            finished = true;
            return current;
        }

        
        Random random = new Random();
        int value = random.nextInt(3) - 1;  // Generates 0,1,2 → shift to -1,0,1
        random = new Random();
        int value2 = random.nextInt(3) - 1;

        int newX = current.x + value;
        int newY = current.y + value2;

        Point next = new Point(current);

        if (!grid.isObstacle(newX, newY)) {
            next.setLocation(newX, newY);
        }

        history.add(next);
        currentStepIndex++;

        return next;
    }

    @Override
    public boolean hasFinished() {
        return finished;
    }

    @Override
    public Point getCurrentPosition() {
        return history.get(currentStepIndex);
    }

    @Override
    public List<Point> getHistory() {
        return history;
    }

    @Override
    public void resetToStep(int step) {
        if (step >= 0 && step < history.size()) {
            currentStepIndex = step;
            finished = false;
        }
    }

    @Override
    public int getCurrentStepIndex() {
        return currentStepIndex;
    }
}

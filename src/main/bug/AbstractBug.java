package main.bug;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import main.grid.Grid;

public abstract class AbstractBug implements BugAlgorithm {

    protected Grid grid;
    protected Point goal;

    protected List<Point> history = new ArrayList<>();
    protected int currentStepIndex = 0;
    protected boolean finished = false;

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

        Point next = moveTo(goal);

        if (grid.isObstacle(next.x, next.y)) {
            next = current;
        }

        if(!current.equals(next)) {
            history.add(next);
            currentStepIndex++;
        }

        return next;
    }

    protected abstract Point moveTo(Point p);

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
package main.bug;

import java.awt.Point;
import java.util.List;

import main.grid.Grid;

public interface BugAlgorithm {

    void init(Grid grid, Point start, Point goal);

    Point nextStep();

    boolean hasFinished();

    Point getCurrentPosition();

    List<Point> getHistory();

    void resetToStep(int step);

    int getCurrentStepIndex();

    BugState getState();

    boolean hasGivenUp();

    public enum BugState {
        RUNNING,
        FINISHED,
        GAVE_UP
    }
}

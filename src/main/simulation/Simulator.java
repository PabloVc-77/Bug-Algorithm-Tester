package main.simulation;

import main.bug.BugAlgorithm;

public class Simulator {

    private BugAlgorithm bug;

    public Simulator(BugAlgorithm bug) {
        this.bug = bug;
    }

    public void step() {
        if (!bug.hasFinished()) {
            bug.nextStep();
        }
    }
}

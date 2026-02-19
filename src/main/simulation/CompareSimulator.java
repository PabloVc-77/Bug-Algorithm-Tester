package main.simulation;

import main.bug.BugAlgorithm;

public class CompareSimulator {

    private BugAlgorithm bug1;
    private BugAlgorithm bug2;

    public CompareSimulator(BugAlgorithm bug1, BugAlgorithm bug2) {
        this.bug1 = bug1;
        this.bug2 = bug2;
    }

    public void step() {

        if (!bug1.hasFinished()) {
            bug1.nextStep();
        }

        if (!bug2.hasFinished()) {
            bug2.nextStep();
        }
    }

    public boolean bothFinished() {
        return bug1.hasFinished() && bug2.hasFinished();
    }
}

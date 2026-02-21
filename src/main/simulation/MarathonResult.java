package main.simulation;

public class MarathonResult {

    public int totalMaps;
    public int completed;
    public int gaveUp;
    public int totalSteps;
    public int totalOptimalSteps;

    public int wins;      // Only used in compare mode
    public int losses;
    public int ties;

    public double averageSteps() {
        return completed == 0 ? 0 : (double) totalSteps / completed;
    }

    public double averageOptimal() {
        return completed == 0 ? 0 : (double) totalOptimalSteps / completed;
    }

    public double efficiency() {
        return totalOptimalSteps == 0 ? 0 :
                (double) totalOptimalSteps / totalSteps;
    }
}

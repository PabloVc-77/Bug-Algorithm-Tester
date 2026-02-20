package main.bugStats;

public class BugStats {

    public int pathLength;
    public int optimalLength;
    public int diffFromOptimal;
    public double efficiency;

    public void setPathLength(int pthL) {this.pathLength = pthL;}
    public void setOptimalLength(int i) {this.optimalLength = i;}
    public void setDiffFromOptimal(int dif) {this.diffFromOptimal = dif;}
    public void setEfficiency(double eff) {this.efficiency = eff;}

    public int getPathLength() {return this.pathLength;}
    public int getOptimalLength() {return this.optimalLength;}
    public int getDiffFromOptimal() {return this.diffFromOptimal;}
    public double getEfficiency() {return this.efficiency;}

    private boolean finished;

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}

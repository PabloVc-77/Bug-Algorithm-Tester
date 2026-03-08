package main.bug;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

public class Bug2 extends AbstractBug {

    private boolean followingWall = false;
    private Point hitPoint = null;

    // 8 directions clockwise
    private static final int[][] DIRS = {
        {1,0},   // E
        {1,1},   // SE
        {0,1},   // S
        {-1,1},  // SW
        {-1,0},  // W
        {-1,-1}, // NW
        {0,-1},  // N
        {1,-1}   // NE
    };

    private int wallDir = 0;

    // loop detection
    private Set<String> visitedStates = new HashSet<>();

    @Override
    protected Point moveTo(Point goal) {

        Point current = getCurrentPosition();

        if (!followingWall) {

            Point next = stepTowardGoal(current);

            if (!grid.isObstacle(next.x, next.y)) {
                return next;
            }

            // Hit obstacle → start wall following
            followingWall = true;
            hitPoint = current;
            wallDir = directionIndex(current, next);

            visitedStates.clear();
        }

        // Detect loop
        String stateKey = current.x + "," + current.y + "," + wallDir;
        if (visitedStates.contains(stateKey)) {
            setState(BugState.GAVE_UP);
            return current;
        }
        visitedStates.add(stateKey);

        Point next = followWall(current);

        if (next == null) {
            setState(BugState.GAVE_UP);
            return current;
        }

        // Leave obstacle if back on M-line closer to goal
        // !!! Add seansing condition: If I see goal and no obstacle leave followingWall !!!
        if ((onMLine(next) && distance(next, goal) < distance(hitPoint, goal)) || distance(goal, next) < 2) {
            followingWall = false;
            visitedStates.clear();
        }

        return next;
    }

    private Point stepTowardGoal(Point p) {

        int dx = Integer.compare(goal.x, p.x);
        int dy = Integer.compare(goal.y, p.y);

        return new Point(p.x + dx, p.y + dy);
    }

    private Point followWall(Point p) {

        for (int i = 0; i < 8; i++) {

            int dir = (wallDir + 7 + i) % 8;

            int nx = p.x + DIRS[dir][0];
            int ny = p.y + DIRS[dir][1];

            if (!grid.isObstacle(nx, ny)) {
                wallDir = dir;
                return new Point(nx, ny);
            }
        }

        return null;
    }

    private int directionIndex(Point from, Point to) {

        int dx = Integer.compare(to.x - from.x, 0);
        int dy = Integer.compare(to.y - from.y, 0);

        for (int i = 0; i < 8; i++) {
            if (DIRS[i][0] == dx && DIRS[i][1] == dy) {
                return i;
            }
        }

        return 0;
    }

    private boolean onMLine(Point p) {

        Point start = history.get(0);

        double d1 = distance(start, p);
        double d2 = distance(p, goal);
        double d3 = distance(start, goal);

        return Math.abs((d1 + d2) - d3) < 0.5;
    }

    private double distance(Point a, Point b) {

        int dx = a.x - b.x;
        int dy = a.y - b.y;

        return Math.sqrt(dx*dx + dy*dy);
    }
}
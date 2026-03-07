package main.bug;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import main.grid.Grid;

// =============================
// This shoud be the *NEWER* Bug
// =============================

public class Bug2 extends AbstractBug {

    //private Point start;

    private boolean followingWall = false;

    private Point hitPoint;
    private Point bestLeavePoint;

    private double bestLeaveDist;

    private int wallDir = 0;

    private boolean clockwise = true;

    private Set<String> visitedBoundary = new HashSet<>();

    private static final int[][] DIRS = {
            {1,0},{1,1},{0,1},{-1,1},
            {-1,0},{-1,-1},{0,-1},{1,-1}
    };

    @Override
    public void init(Grid grid, Point start, Point goal) {
        super.init(grid, start, goal);

        //this.start = new Point(start);

        followingWall = false;
        visitedBoundary.clear();
    }

    @Override
    protected Point moveTo(Point goal) {

        Point here = getCurrentPosition();

        if (!followingWall) {

            int dir = directionTo(here, goal);
            Point next = addDir(here, dir);

            if (canMove(next)) {
                wallDir = dir;
                return next;
            }

            // obstacle encountered
            followingWall = true;
            hitPoint = new Point(here);
            bestLeavePoint = new Point(here);
            bestLeaveDist = dist(here, goal);
            visitedBoundary.clear();

            wallDir = clockwise ? (dir + 1) % 8 : (dir + 7) % 8;
        }

        return followWall(goal);
    }

    private Point followWall(Point goal) {

        Point here = getCurrentPosition();

        visitedBoundary.add(key(here));

        for (int i = 0; i < 8; i++) {

            wallDir = clockwise ? (wallDir + 1) % 8 : (wallDir + 7) % 8;

            Point next = addDir(here, wallDir);

            if (!canMove(next)) continue;

            // track best leave point
            double d = dist(next, goal);

            if (d < bestLeaveDist) {
                bestLeaveDist = d;
                bestLeavePoint = new Point(next);
            }

            // leave wall if good opportunity
            if (next.equals(bestLeavePoint)) {

                int greedy = directionTo(next, goal);
                Point greedyStep = addDir(next, greedy);

                if (canMove(greedyStep)) {
                    followingWall = false;
                }
            }

            /* loop detection
            if (next.equals(hitPoint)) {
                setState(BugState.GAVE_UP);
                return here;
            }*/

            // boundary repetition detection
            if (visitedBoundary.contains(key(next))) {
                continue;
            }

            return next;
        }

        // fallback: greedy escape
        return greedyEscape(goal);
    }

    private Point greedyEscape(Point goal) {

        Point here = getCurrentPosition();

        int bestDir = -1;
        double best = Double.MAX_VALUE;

        for (int i = 0; i < 8; i++) {

            Point p = addDir(here, i);

            if (!canMove(p)) continue;

            double d = dist(p, goal);

            if (d < best) {
                best = d;
                bestDir = i;
            }
        }

        if (bestDir == -1) return here;

        return addDir(here, bestDir);
    }

    private boolean canMove(Point p) {

        if (p.x < 0 || p.y < 0 ||
            p.x >= grid.getWidth() ||
            p.y >= grid.getHeight())
            return false;

        return !grid.isObstacle(p.x, p.y);
    }

    private Point addDir(Point p, int dir) {

        return new Point(
                p.x + DIRS[dir][0],
                p.y + DIRS[dir][1]
        );
    }

    private int directionTo(Point a, Point b) {

        int dx = Integer.compare(b.x, a.x);
        int dy = Integer.compare(b.y, a.y);

        for (int i = 0; i < 8; i++) {
            if (DIRS[i][0] == dx && DIRS[i][1] == dy)
                return i;
        }

        return 0;
    }

    private double dist(Point a, Point b) {

        int dx = a.x - b.x;
        int dy = a.y - b.y;

        return Math.sqrt(dx*dx + dy*dy);
    }

    private String key(Point p) {
        return p.x + "," + p.y;
    }
}

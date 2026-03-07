package main.bug;

import java.awt.Point;
import main.grid.Grid;

// =============================
// This shoud be the *OLD* Bug
// =============================

public class Bug1 extends AbstractBug {

    private Point start;
    private Point hitPoint;

    private boolean followingWall = false;
    private boolean clockwise = true;

    private int dirIndex = 0;

    // 8-direction movement (E,SE,S,SW,W,NW,N,NE)
    private static final int[][] DIRS = {
            {1,0},{1,1},{0,1},{-1,1},
            {-1,0},{-1,-1},{0,-1},{1,-1}
    };

    @Override
    public void init(Grid grid, Point start, Point goal) {
        super.init(grid, start, goal);
        this.start = new Point(start);
        this.hitPoint = null;
        followingWall = false;
    }

    @Override
    protected Point moveTo(Point target) {

        Point here = getCurrentPosition();

        if (here.equals(target)) {
            return here;
        }

        if (!followingWall) {

            int bestDir = directionTo(here, target);
            Point next = addDir(here, bestDir);

            if (canMove(next)) {
                dirIndex = bestDir;
                return next;
            }

            // obstacle hit
            followingWall = true;
            hitPoint = new Point(here);
            dirIndex = bestDir;

            if (clockwise)
                dirIndex = (dirIndex + 1) % 8;
            else
                dirIndex = (dirIndex + 7) % 8;
        }

        return followWall(here, target);
    }

    private Point followWall(Point here, Point goal) {

        int tryDir = dirIndex;

        for (int i = 0; i < 8; i++) {

            tryDir = clockwise
                    ? (tryDir + 1) % 8
                    : (tryDir + 7) % 8;

            Point next = addDir(here, tryDir);

            if (canMove(next)) {

                dirIndex = tryDir;

                if (isOnMLine(next, goal)
                        && distance(next, goal) < distance(hitPoint, goal)) {

                    followingWall = false;
                }

                return next;
            }
        }

        return here;
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

    private boolean isOnMLine(Point p, Point goal) {

        int x1 = start.x, y1 = start.y;
        int x2 = goal.x, y2 = goal.y;

        int x = p.x, y = p.y;

        return (y - y1) * (x2 - x1) ==
               (y2 - y1) * (x - x1);
    }

    private double distance(Point a, Point b) {

        int dx = a.x - b.x;
        int dy = a.y - b.y;

        return Math.sqrt(dx*dx + dy*dy);
    }
}

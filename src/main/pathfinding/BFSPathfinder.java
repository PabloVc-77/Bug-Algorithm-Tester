package main.pathfinding;

import main.grid.Grid;

import java.awt.Point;
import java.util.*;

public class BFSPathfinder {

    public static List<Point> findPath(Grid grid, Point start, Point goal) {

        Queue<Point> queue = new LinkedList<>();
        Map<Point, Point> parent = new HashMap<>();
        boolean[][] visited = new boolean[grid.getWidth()][grid.getHeight()];
        
        visited[start.x][start.y] = true;
        queue.add(start);

        int[][] directions = {
            {1,0}, {-1,0}, {0,1}, {0,-1},     // cardinal
            {1,1}, {1,-1}, {-1,1}, {-1,-1}   // diagonals
        };


        while (!queue.isEmpty()) {

            Point current = queue.poll();

            if (current.equals(goal)) {
                return reconstructPath(parent, goal);
            }

            for (int[] d : directions) {

                int nx = current.x + d[0];
                int ny = current.y + d[1];

                Point next = new Point(nx, ny);

                if (!grid.inBounds(nx, ny)) continue;
                if (grid.isObstacle(nx, ny)) continue;
                if (visited[nx][ny]) continue;

                queue.add(next);
                visited[nx][ny] = true;
                parent.put(next, current);
            }
        }

        return null; // no path
    }

    private static List<Point> reconstructPath(Map<Point, Point> parent, Point goal) {

        List<Point> path = new ArrayList<>();
        Point current = goal;

        while (current != null) {
            path.add(current);
            current = parent.get(current);
        }

        Collections.reverse(path);
        return path;
    }
}

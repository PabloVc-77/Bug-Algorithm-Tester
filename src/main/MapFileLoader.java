package main;

import main.grid.Grid;
import java.awt.Point;
import java.io.*;
import java.util.*;

public class MapFileLoader {

    public static LoadedMap load(String name) throws IOException {

        File file = new File("maps/" + name + ".map");

        if (!file.exists()) {
            throw new FileNotFoundException("Map not found: " + name);
        }

        try (Scanner sc = new Scanner(file)) {

            int rows = sc.nextInt();
            int cols = sc.nextInt();
            sc.nextLine();

            Grid grid = new Grid(cols, rows);

            Point start = null;
            Point goal = null;

            for (int r = 0; r < rows; r++) {

                String line = sc.nextLine();

                for (int c = 0; c < cols; c++) {

                    char ch = line.charAt(c);

                    switch (ch) {
                        case '#':
                            grid.setObstacle(c, r, true);
                            break;
                        case 'S':
                            start = new Point(c, r);
                            break;
                        case 'G':
                            goal = new Point(c, r);
                            break;
                    }
                }
            }

            return new LoadedMap(grid, start, goal);
        }
    }

    public static class LoadedMap {
        public final Grid grid;
        public final Point start;
        public final Point goal;

        public LoadedMap(Grid g, Point s, Point go) {
            this.grid = g;
            this.start = s;
            this.goal = go;
        }
    }
}
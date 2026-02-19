package main.grid;

import java.util.Random;

public class MapGenerator {

    public static Grid generate(int width, int height, double obstacleProbability, Random rand) {

        Grid grid = new Grid(width, height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (rand.nextDouble() < obstacleProbability) {
                    grid.setObstacle(x, y, true);
                }
            }
        }

        return grid;
    }

}

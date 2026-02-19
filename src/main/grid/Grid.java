package main.grid;

public class Grid {

    private boolean[][] obstacles;
    private int width;
    private int height;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        obstacles = new boolean[width][height];
    }

    public void setObstacle(int x, int y, boolean value) {
        if (inBounds(x, y)) {
            obstacles[x][y] = value;
        }
    }

    public boolean isObstacle(int x, int y) {
        if (!inBounds(x, y)) return true;
        return obstacles[x][y];
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}

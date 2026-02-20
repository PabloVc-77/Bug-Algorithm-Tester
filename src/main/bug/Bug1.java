package main.bug;

import java.awt.Point;

public class Bug1 extends AbstractBug{

    @Override
    public Point moveTo(Point goal) {
        Point current = history.get(currentStepIndex);
        int dx = Integer.compare(goal.x, current.x);
        int dy = Integer.compare(goal.y, current.y);

        int newX = current.x + dx;
        int newY = current.y + dy;

        return new Point(newX, newY);
    }
    
}

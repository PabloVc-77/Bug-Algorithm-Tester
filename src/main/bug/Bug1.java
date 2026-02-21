package main.bug;

import java.awt.Point;
import java.util.Random;

// ============================
// This should be the *OLD* Bug
// ============================

public class Bug1 extends AbstractBug{

    @Override
    public Point moveTo(Point goal) {
        Point current = history.get(currentStepIndex);
        Random random = new Random();
        int value = random.nextInt(3) - 1;  // Generates 0,1,2 → shift to -1,0,1
        random = new Random();
        int value2 = random.nextInt(3) - 1;

        int newX = current.x + value;
        int newY = current.y + value2;

        return new Point(newX, newY);    
    }
    
}

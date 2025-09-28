package game.core;

import game.exceptions.BoundaryExceededException;
import game.utility.Direction;
import org.junit.*;
import static org.junit.Assert.*;

public class ControllableTest {
    Controllable controllable;

    @Before
    public void setup() {
        controllable = new Ship(0, 0, 100);
    }

    @Test (expected = BoundaryExceededException.class)
    public void testOutOfBounds() {
        controllable.move(Direction.UP);
    }

    @Test
    public void moveNormally() {
        controllable.move(Direction.DOWN);
    }
}
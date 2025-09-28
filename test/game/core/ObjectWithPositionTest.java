package game.core;

import org.junit.*;
import static org.junit.Assert.*;

public class ObjectWithPositionTest {
    private ObjectWithPosition object;

    @Test
    public void testToStringBullet() {
        object = new Bullet(0, 0);
        assertEquals("Bullet(0, 0)", object.toString());
    }

    @Test
    public void testToStringAsteroid() {
        object = new Asteroid(3, 5);
        assertEquals("Asteroid(3, 5)", object.toString());
    }

    @Test
    public void testGetters() {
        object = new Bullet(1, 1);
        assertEquals(1, object.getX());
        assertEquals(1, object.getY());

        Bullet bullet = (Bullet) object;
        bullet.tick(0);
        assertEquals(0, object.getY());
    }
}
package game.core;

import org.junit.*;
import static org.junit.Assert.*;

public class ShieldPowerUpTest {
    ShieldPowerUp powerUp;
    Ship ship;

    @Before
    public void setup() {
        powerUp = new ShieldPowerUp(0, 0);
        ship = new Ship(0, 0, 50);
    }

    @Test
    public void testApplyEffect() {
        assertEquals(0, ship.getScore());
        powerUp.applyEffect(ship);
        assertEquals(50, ship.getScore());
        powerUp.applyEffect(ship);
        assertEquals(100, ship.getScore());
    }

    @Test
    public void powerUpFalls() {
        for (int i = 1; i <= 100; i++) {
            powerUp.tick(i);
            assertEquals(0, powerUp.getX());
            assertEquals(i / 10, powerUp.getY());
        }
    }
}
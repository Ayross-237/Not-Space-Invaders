package game.core;

import org.junit.*;
import static org.junit.Assert.*;

public class HealthPowerUpTest {
    HealthPowerUp powerUp;
    Ship ship;

    @Before
    public void setup() {
        powerUp = new HealthPowerUp(0, 0);
        ship = new Ship(0, 0, 50);
    }

    @Test
    public void testApplyEffect() {
        assertEquals(50, ship.getHealth());
        powerUp.applyEffect(ship);
        assertEquals(70, ship.getHealth());
    }

    @Test
    public void doesNotHealOverMax() {
        ship.heal(40);
        assertEquals(90, ship.getHealth());
        powerUp.applyEffect(ship);
        assertEquals(100, ship.getHealth());
        powerUp.applyEffect(ship);
        assertEquals(100, ship.getHealth());
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
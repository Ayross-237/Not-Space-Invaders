package game;


import game.achievements.PlayerStatsTracker;
import game.core.*;
import game.utility.Logger;
import game.core.SpaceObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents the game information and state. Stores and manipulates the game state.
 */
public class GameModel {
    public static final int GAME_HEIGHT = 20;
    public static final int GAME_WIDTH = 10;
    public static final int START_SPAWN_RATE = 2; // spawn rate (percentage chance per tick)
    public static final int SPAWN_RATE_INCREASE = 5; // Increase spawn rate by 5% per level
    public static final int START_LEVEL = 1; // Starting level value
    public static final int SCORE_THRESHOLD = 100; // Score threshold for leveling
    public static final int ASTEROID_DAMAGE = 10; // The amount of damage an asteroid deals
    public static final int ENEMY_DAMAGE = 20; // The amount of damage an enemy deals
    public static final double ENEMY_SPAWN_RATE = 0.5; // Percentage of asteroid spawn chance
    public static final double POWER_UP_SPAWN_RATE = 0.25; // Percentage of asteroid spawn chance

    private final Random random = new Random(); // ONLY USED IN this.spawnObjects()
    private final List<SpaceObject> spaceObjects; // List of all objects
    private Ship ship; // Core.Ship starts at (5, 10) with 100 health
    private int level; // The current game level
    private int spawnRate; // The current game spawn rate
    private Logger logger; // The Logger reference used for logging.
    private PlayerStatsTracker statsTracker; // The tracker used to monitor the stats of the player
    private boolean isVerbose; // Keeps track of the state of verbose

    /**
     * Models a game, storing and modifying data relevant to the game.
     * Logger argument should be a method reference to a .log method such as the UI.log method.
     * Example: Model gameModel = new GameModel(ui::log, new PlayerStatsTracker())
     *
     * - Instantiates an empty list for storing all SpaceObjects the model needs to track.
     * - Instantiates the game level with the starting level value.
     * - Instantiates the game spawn rate with the starting spawn rate.
     * - Instantiates a new ship.
     * - Stores reference to the given Logger.
     * - Stores reference to the given PlayerStatsTracker.
     *
     * @param logger a functional interface for passing information between classes.
     * @param statsTracker a PlayerStatsTracker instance to record stats.
     * @requires logger is not null, statsTracker is not null
     */
    public GameModel(Logger logger, PlayerStatsTracker statsTracker) {
        spaceObjects = new ArrayList<>();
        level = START_LEVEL;
        spawnRate = START_SPAWN_RATE;
        ship = new Ship();
        this.logger = logger;
        this.statsTracker = statsTracker;
        isVerbose = false;
    }

    /**
     * Returns the ship instance in the game.
     *
     * @return the current ship instance.
     */
    public Ship getShip() {
        return ship;
    }

    /**
     * Returns a list of all SpaceObjects in the game.
     *
     * @return a list of all spaceObjects.
     */
    public List<SpaceObject> getSpaceObjects() {
        return (List<SpaceObject>) ((ArrayList<SpaceObject>) spaceObjects).clone();
    }

    /**
     * Returns the current level.
     *
     * @return the current level.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Returns the current player stats tracker.
     * @return the current player stats tracker.
     */
    public PlayerStatsTracker getStatsTracker() {
        return statsTracker;
    }

    /**
     * Adds a SpaceObject to the game.<br>
     * <p>
     * Objects are considered part of the game only when they are tracked by the model.<br>
     *
     * @param object the SpaceObject to be added to the game.
     * @requires object != null.
     */
    public void addObject(SpaceObject object) {
        this.spaceObjects.add(object);
    }

    /**
     * Moves all objects and updates the game state.
     * Objects should be moved by calling .tick(tick) on each object.
     * The game state is updated by removing out-of-bound objects during the tick
     *
     * @param tick the tick value passed through to the objects tick() method.
     */
    public void updateGame(int tick) {
        List<SpaceObject> toRemove = new ArrayList<>();

        for (SpaceObject obj : spaceObjects) {
            obj.tick(tick); // Move objects downward

            if (!isInBounds(obj)) { // Remove objects that move off-screen
                toRemove.add(obj);
            }
        }

        spaceObjects.removeAll(toRemove);
    }

    /**
     * Sets verbose state to the provided input.
     * @param verbose whether to set verbose state to true or false.
     */
    public void setVerbose(boolean verbose) {
        isVerbose = verbose;
    }

    /**
     * Spawns new objects (Asteroids, Enemies, and PowerUp) at random positions. Uses this.random to make EXACTLY
     * 6 calls to random.nextInt() and 1 random.nextBoolean.
     * Random calls should be in the following order:
     * 1. Check if an Asteroid should spawn (random.nextInt(100) < spawnRate)
     * 2. If spawning an Asteroid, spawn at x-coordinate = random.nextInt(GAME_WIDTH)
     * 3. Check if an Enemy should spawn (random.nextInt(100) < spawnRate * ENEMY_SPAWN_RATE)
     * 4. If spawning an Enemy, spawn at x-coordinate = random.nextInt(GAME_WIDTH)
     * 5. Check if a PowerUp should spawn (random.nextInt(100) < spawnRate * POWER_UP_SPAWN_RATE)
     * 6. If spawning a PowerUp, spawn at x-coordinate = random.nextInt(GAME_WIDTH)
     * 7. If spawning a PowerUp, spawn a ShieldPowerUp if random.nextBoolean(), else a HealthPowerUp.
     *
     * Failure to match random calls correctly will result in failed tests.
     *
     * Objects spawn at y = 0 (top of the screen).
     * Objects may not spawn if there is a ship or space object at the intended spawn location.
     * This should NOT impact calls to random.
     */
    public void spawnObjects() {
        // Spawn asteroids with a chance determined by spawnRate
        if (random.nextInt(100) < spawnRate) {
            int x = random.nextInt(GAME_WIDTH); // Random x-coordinate
            int y = 0; // Spawn at the top of the screen
            if (!isCollidingWithShip(x, y) && !positionOccupiedByObject(x, y)) {
                spaceObjects.add(new Asteroid(x, y));
            }
        }

        // Spawn enemies with a lower chance
        // Half the rate of asteroids
        if (random.nextInt(100) < spawnRate * ENEMY_SPAWN_RATE) {
            int x = random.nextInt(GAME_WIDTH);
            int y = 0;
            if (!isCollidingWithShip(x, y) && !positionOccupiedByObject(x, y)) {
                spaceObjects.add(new Enemy(x, y));
            }
        }

        // Spawn power-ups with an even lower chance
        // One-fourth the spawn rate of asteroids
        if (random.nextInt(100) < spawnRate * POWER_UP_SPAWN_RATE) {
            int x = random.nextInt(GAME_WIDTH);
            int y = 0;
            PowerUp powerUp = random.nextBoolean() ? new ShieldPowerUp(x, y) :
                    new HealthPowerUp(x, y);
            if (!isCollidingWithShip(x, y) && !positionOccupiedByObject(x, y)) {
                spaceObjects.add(powerUp);
            }
        }
    }

    /**
     * determines whether another object already exists at the given position.
     * @param x the given x position
     * @param y the given y position
     * @return if an object exists there
     */
    private boolean positionOccupiedByObject(int x, int y) {
        for (SpaceObject spaceObject : spaceObjects) {
            if (spaceObject.getX() == x && spaceObject.getY() == y) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a given position would collide with the ship.
     *
     * @param x the x-coordinate to check.
     * @param y the y-coordinate to check.
     * @return true if the position collides with the ship, false otherwise.
     */
    private boolean isCollidingWithShip(int x, int y) {
        return (ship.getX() == x) && (ship.getY() == y);
    }

    /**
     * If level progression requirements are satisfied, levels up the game by increasing the spawn rate and level
     * number.
     * To level up, the score must not be less than the current level multiplied by the score threshold.
     * To increase the level the spawn rate should increase by SPAWN_RATE_INCREASE, and the level number should
     * increase by 1.
     * If the level is increased, and verbose is set to true, log the following: "Level Up! Welcome to Level
     * {new level}. Spawn rate increased to {new spawn rate}%."
     */
    public void levelUp() {
        if (ship.getScore() < level * SCORE_THRESHOLD) {
            return;
        }
        level++;
        spawnRate += SPAWN_RATE_INCREASE;

        if (isVerbose) {
            logger.log(
                    "Level Up! Welcome to Level "
                            + level
                            + ". Spawn rate increased to "
                            + spawnRate
                            + "%."
            );
        }
    }

    /**
     * Fires a bullet from the ship's current position.
     * Creates a new bullet at the coordinates the ship occupies.
     */
    public void fireBullet() {
        int bulletX = ship.getX();
        int bulletY = ship.getY(); // Core.Bullet starts just above the ship
        spaceObjects.add(new Bullet(bulletX, bulletY));
    }

    /**
     * Detects and handles collisions between spaceObjects (Ship and Bullet collisions).
     * Objects are considered to be colliding if they share x and y coordinates.
     * First checks ship collision:
     * - If the ship is colliding with a PowerUp, apply the effect, and if verbose is true,
     * log "PowerUp collected: {obj.render()}"
     * - If the ship is colliding with an Asteroid or Enemy, take the appropriate damage, and if verbose is true,
     * log "Hit by {obj.render()}! Health reduced by {damage_taken}."
     * For any collisions with the Ship, the colliding object should be removed.
     *
     * Then check Bullet collision:
     * If a Bullet collides with an Enemy, remove both the Enemy and the Bullet. No logging required.
     * Also, record the shot hit using recordShotHit() to track successful hits.
     * If a Bullet collides with an Asteroid, remove just the Bullet. No logging required.
     *
     * recordShotHit() is only called when a Bullet successfully hits an Enemy.
     */
    public void checkCollisions() {
        List<SpaceObject> toRemove = new ArrayList<>();

        for (SpaceObject obj : spaceObjects) {
            // Skip checking Ships (No ships should be in this list)
            if (obj instanceof Ship) {
                continue;
            }
            // Check Ship collision (except Bullets)
            if (isCollidingWithShip(obj.getX(), obj.getY()) && !(obj instanceof Bullet)) {
                // Handle collision effects

                switch (obj) {
                    case PowerUp powerUp -> {
                        powerUp.applyEffect(ship);
                        if (isVerbose) {
                            logger.log("PowerUp collected: " + obj.render());
                        }
                    }
                    case Asteroid asteroid -> {
                        ship.takeDamage(ASTEROID_DAMAGE);
                        if (isVerbose) {
                            logger.log(hitByObjectMessage(asteroid, ASTEROID_DAMAGE));
                        }
                    }
                    case Enemy enemy -> {
                        ship.takeDamage(ENEMY_DAMAGE);
                        if (isVerbose) {
                            logger.log(hitByObjectMessage(enemy, ENEMY_DAMAGE));
                        }
                    }
                    default -> {
                        // Do nothing
                    }
                }
                logger.log("Collision with: " + obj);
                toRemove.add(obj);
            }
        }

        for (SpaceObject obj : spaceObjects) {
            // Check only Bullets
            if (!(obj instanceof Bullet)) {
                continue;
            }
            // Check Bullet collision
            for (SpaceObject other : spaceObjects) {
                // Check only Enemies and Asteroids
                if (!(other instanceof Enemy) && !(other instanceof Asteroid)) {
                    continue;
                }
                if ((obj.getX() == other.getX()) && (obj.getY() == other.getY())) {
                    if (other instanceof Enemy) {
                        toRemove.add(other); // Remove if enemy but not if asteroid
                        statsTracker.recordShotHit();
                    }
                    toRemove.add(obj);  // Remove bullet
                }
            }
        }

        spaceObjects.removeAll(toRemove); // Remove all collided objects
    }

    /**
     * returns the message to log when a player is hit by an object
     * @param object the object it was hit by
     * @param damageTaken the amount of health lost
     * @return the string representing the message
     */
    private String hitByObjectMessage(SpaceObject object, int damageTaken) {
        return "Hit by " + object.render() + "! Health reduced by " + damageTaken + ".";
    }

    /**
     * Sets the seed of the Random instance created in the constructor using .setSeed().
     * This method should NEVER be called.
     *
     * @param seed to be set for the Random instance
     * @provided
     */
    public void setRandomSeed(int seed) {
        this.random.setSeed(seed);
    }

    /**
     * Checks if the game is over.
     * The game is considered over if the Ship heath is <= 0.
     * @returns true if the Ship health is <= 0, false otherwise
     */
    public boolean checkGameOver() {
        return ship.getHealth() <= 0;
    }

    /**
     * Checks if the given SpaceObject is inside the game bounds.
     * The SpaceObject is considered outside the game boundaries if they are at:
     * x-coordinate >= GAME_WIDTH,
     * y-coordinate >= GAME_HEIGHT,
     * x-coordinate < 0, or
     * y-coordinate < 0
     * @param spaceObject the SpaceObject to check
     * @return true if the SpaceObject is in bounds, false otherwise
     * @requires spaceObject isi not Null
     */
    public static boolean isInBounds(SpaceObject spaceObject) {
        return (
                spaceObject.getX() >= 0
                    && spaceObject.getX() < GAME_WIDTH
                    && spaceObject.getY() >= 0
                    && spaceObject.getY() < GAME_HEIGHT
            );
    }
}
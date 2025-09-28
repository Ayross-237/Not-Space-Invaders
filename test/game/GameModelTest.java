package game;

import game.achievements.Achievement;
import game.achievements.PlayerStatsTracker;
import game.core.*;
import game.exceptions.BoundaryExceededException;
import game.ui.KeyHandler;
import game.ui.Tickable;
import game.ui.UI;
import game.utility.Direction;
import org.junit.*;

import java.util.List;

import static org.junit.Assert.*;

public class GameModelTest {

    static class MockUI implements UI {
        @Override
        public void start() {}
        @Override
        public void pause() {}
        @Override
        public void stop() {}
        @Override
        public void onStep(Tickable tickable) {}
        @Override
        public void onKey(KeyHandler key) {}
        @Override
        public void render(List<SpaceObject> objects) {}
        @Override
        public void log(String message) {}
        @Override
        public void setStat(String label, String value) {}
        @Override
        public void logAchievementMastered(String message) {}
        @Override
        public void logAchievements(List<Achievement> achievements) {}
        @Override
        public void setAchievementProgressStat(String achievementName, double progressPercentage) {}
    }


    GameModel model;

    @Before
    public void setup() {
        model = new GameModel(new MockUI()::log, new PlayerStatsTracker());
    }

    @Test
    public void neverLevelUp() {
        assertEquals(1, model.getLevel());
        model.getShip().addScore(100);
        model.levelUp();
        assertEquals(2, model.getLevel());
    }

    @Test
    public void levelUpAtCorrectScore() {
        assertEquals(1, model.getLevel());

        model.getShip().addScore(50);
        model.levelUp();
        assertEquals(1, model.getLevel());

        model.getShip().addScore(49);
        model.levelUp();
        assertEquals(1, model.getLevel());

        model.getShip().addScore(1);
        model.levelUp();
        assertEquals(2, model.getLevel());
    }

    @Test
    public void noConcurrentSpawning() {
        int screenWidth = GameModel.GAME_WIDTH;

        //max spawn rate
        model.getShip().addScore(100 * 100);
        for (int i = 0; i < 100; i++) {
            model.levelUp();
        }

        for (int i = 0; i < screenWidth; i++) {
            model.addObject(new Asteroid(i, 0));
        }
        assertEquals(screenWidth, model.getSpaceObjects().size());
        model.spawnObjects();
        assertEquals(screenWidth, model.getSpaceObjects().size());
    }

    @Test
    public void bulletAsteroidCollision() {
        model.addObject(new Bullet(1, 1));
        model.addObject(new Asteroid(1, 1));

        assertEquals(2, model.getSpaceObjects().size());
        model.checkCollisions();
        assertEquals(1, model.getSpaceObjects().size());
    }

    @Test
    public void bulletEnemyCollision() {
        model.addObject(new Bullet(1, 1));
        model.addObject(new Enemy(1, 1));

        assertEquals(2, model.getSpaceObjects().size());
        model.checkCollisions();
        assertEquals(0, model.getSpaceObjects().size());
    }

    @Test
    public void manyBulletsOneAsteroidCollisions() {
        model.addObject(new Bullet(1, 1));
        model.addObject(new Bullet(1, 1));
        model.addObject(new Asteroid(1, 1));

        assertEquals(3, model.getSpaceObjects().size());
        model.checkCollisions();
        assertEquals(1, model.getSpaceObjects().size());
    }

    @Test
    public void manyBulletsManyEnemiesCollisions() {
        model.addObject(new Bullet(1, 1));
        model.addObject(new Bullet(1, 1));
        model.addObject(new Enemy(1, 1));
        model.addObject(new Enemy(1, 1));

        assertEquals(4, model.getSpaceObjects().size());
        model.checkCollisions();
        assertEquals(0, model.getSpaceObjects().size());
    }

    @Test
    public void noSpawningOnShip() {
        int screenWidth = GameModel.GAME_WIDTH;

        //Move ship to the top
        for (int i = 0; i < GameModel.GAME_HEIGHT; i++) {
            try {
                model.getShip().move(Direction.UP);
            } catch (BoundaryExceededException e) {
                // DO nothing
            }
        }
        assertEquals(0, model.getShip().getY()); //Ensure movement is not messed up

        model.getShip().addScore(100 * 100);
        for (int i = 0; i < 100; i++) {
            model.levelUp();
        }

        for (int i = 0; i < 100; i++) {
            model.spawnObjects();
        }
        assertEquals(GameModel.GAME_WIDTH - 1, model.getSpaceObjects().size());
    }

    @Test
    public void outOfBoundsRemoval() {
        model.addObject(new Bullet(-1, 1));
        model.addObject(new Asteroid(0, -1));
        model.addObject(new Bullet(GameModel.GAME_WIDTH, 1));
        model.addObject(new Enemy(0, GameModel.GAME_HEIGHT));

        assertEquals(4, model.getSpaceObjects().size());
        model.updateGame(1);
        assertEquals(0, model.getSpaceObjects().size());
    }

    @Test
    public void inBoundsNotRemoved() {
        model.addObject(new Bullet(0, 1));
        model.addObject(new Asteroid(0, 0));
        model.addObject(new Bullet(GameModel.GAME_WIDTH - 1, 1));
        model.addObject(new Enemy(0, GameModel.GAME_HEIGHT - 1));

        assertEquals(4, model.getSpaceObjects().size());
        model.updateGame(1);
        assertEquals(4, model.getSpaceObjects().size());
    }

    @Test
    public void bulletRemovedCorrectly() {
        model.addObject(new Bullet(0, GameModel.GAME_HEIGHT));
        for (int i = 0; i < GameModel.GAME_HEIGHT; i++) {
            model.updateGame(i);
            assertEquals(1, model.getSpaceObjects().size());
        }
        model.updateGame(GameModel.GAME_HEIGHT);
        assertEquals(0, model.getSpaceObjects().size());
    }

    @Test
    public void bulletIsFired() {
        model.fireBullet();
        assertEquals(1, model.getSpaceObjects().size());
        assertEquals(model.getShip().getX(), model.getSpaceObjects().getFirst().getX());
        assertEquals(model.getShip().getY(), model.getSpaceObjects().getFirst().getY());
    }
}
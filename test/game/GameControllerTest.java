package game;

import game.achievements.Achievement;
import game.achievements.AchievementManager;
import game.achievements.FileHandler;
import game.core.*;
import game.ui.KeyHandler;
import game.ui.Tickable;
import game.ui.UI;
import org.junit.*;

import java.util.List;

import static org.junit.Assert.*;

public class GameControllerTest {

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


    GameController controller;

    @Before
    public void setup() {
        controller = new GameController(
                new MockUI(),
                new AchievementManager(new FileHandler())
        );
    }

    @Test
    public void moveWhenPaused() {
        controller.pauseGame();
        int x = controller.getModel().getShip().getX();
        int y = controller.getModel().getShip().getY();

        controller.handlePlayerInput("W");
        controller.handlePlayerInput("R");
        assertEquals(x, controller.getModel().getShip().getX());
        assertEquals(y, controller.getModel().getShip().getY());
    }

    @Test
    public void moveCorrectly() {
        int x = controller.getModel().getShip().getX();
        int y = controller.getModel().getShip().getY();

        controller.handlePlayerInput("W");
        assertEquals(x, controller.getModel().getShip().getX());
        assertEquals(y - 1, controller.getModel().getShip().getY());

        controller.handlePlayerInput("D");
        assertEquals(x + 1, controller.getModel().getShip().getX());
        assertEquals(y - 1, controller.getModel().getShip().getY());

        controller.handlePlayerInput("S");
        assertEquals(x + 1, controller.getModel().getShip().getX());
        assertEquals(y, controller.getModel().getShip().getY());

        controller.handlePlayerInput("A");
        assertEquals(x, controller.getModel().getShip().getX());
        assertEquals(y, controller.getModel().getShip().getY());
    }

    @Test
    public void bulletIsFired() {
        assertEquals(0, controller.getModel().getSpaceObjects().size());
        controller.handlePlayerInput("F");
        assertEquals(1, controller.getModel().getSpaceObjects().size());
        assertEquals(Bullet.class, controller.getModel().getSpaceObjects().getFirst().getClass());
        assertEquals(1, controller.getStatsTracker().getShotsFired());
    }

    @Test
    public void handleLowerCase() {
        int x = controller.getModel().getShip().getX();
        int y = controller.getModel().getShip().getY();

        controller.handlePlayerInput("w");
        assertEquals(x, controller.getModel().getShip().getX());
        assertEquals(y - 1, controller.getModel().getShip().getY());

        controller.handlePlayerInput("d");
        assertEquals(x + 1, controller.getModel().getShip().getX());
        assertEquals(y - 1, controller.getModel().getShip().getY());

        controller.handlePlayerInput("s");
        assertEquals(x + 1, controller.getModel().getShip().getX());
        assertEquals(y, controller.getModel().getShip().getY());

        controller.handlePlayerInput("a");
        assertEquals(x, controller.getModel().getShip().getX());
        assertEquals(y, controller.getModel().getShip().getY());
    }
}
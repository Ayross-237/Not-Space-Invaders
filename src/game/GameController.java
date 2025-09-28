package game;

import game.achievements.Achievement;
import game.achievements.AchievementManager;
import game.achievements.PlayerStatsTracker;
import game.core.SpaceObject;
import game.exceptions.BoundaryExceededException;
import game.ui.UI;
import game.utility.Direction;

import java.util.List;

/**
 * The Controller handling the game flow and interactions.
 * <p>
 * Holds references to the UI and the Model, so it can pass information and references back and forth as necessary.<br>
 * Manages changes to the game, which are stored in the Model, and displayed by the UI.<br>
 */
public class GameController {
    private final long startTime;
    private final UI ui;
    private final GameModel model;
    private final AchievementManager achievementManager;
    private boolean isPaused;

    /**
     * An internal variable indicating whether certain methods should log their actions.
     * Not all methods respect isVerbose.
     */
    private boolean isVerbose = false;


    /**
     * Initializes the game controller with the given UI, GameModel and AchievementManager.<br>
     * Stores the UI, GameModel, AchievementManager and start time.<br>
     * The start time System.currentTimeMillis() should be stored as a long.<br>
     * Starts the UI using UI.start().<br>
     *
     * @param ui the UI used to draw the Game
     * @param model the model used to maintain game information
     * @param achievementManager the manager used to maintain achievement information
     *
     * @requires ui is not null
     * @requires model is not null
     * @requires achievementManager is not null
     * @provided
     */
    public GameController(UI ui, GameModel model, AchievementManager achievementManager) {
        this.ui = ui;
        ui.start();
        this.model = model;
        this.startTime = System.currentTimeMillis(); // Current time
        this.achievementManager = achievementManager;
        isPaused = false;
    }

    /**
     * Initializes the game controller with the given UI and GameModel.<br>
     * Stores the ui, model and start time.<br>
     * The start time System.currentTimeMillis() should be stored as a long.<br>
     *
     * @param ui    the UI used to draw the Game
     * @param achievementManager the manager used to maintain achievement information
     *
     * @requires ui is not null
     * @requires achievementManager is not null
     * @provided
     */
    public GameController(UI ui, AchievementManager achievementManager) {
        this(ui, new GameModel(ui::log, new PlayerStatsTracker()), achievementManager);
    }

    /**
     * Returns the current GameModel.
     * @return the current GameModel.
     */
    public GameModel getModel() {
        return model;
    }

    /**
     * Returns the current PlayerStatsTracker.
     * @return the current PlayerStatsTracker
     */
    public PlayerStatsTracker getStatsTracker() {
        return model.getStatsTracker();
    }

    /**
     * Sets verbose state to the provided input. Also sets the models verbose state to the provided input.
     * @param verbose whether to set verbose state to true or false.
     */
    public void setVerbose(boolean verbose) {
        isVerbose = verbose;
        model.setVerbose(verbose);
    }

    /**
     * Starts the main game loop.<br>
     * <p>
     * Passes onTick and handlePlayerInput to ui.onStep and ui.onKey respectively.
     * @provided
     */
    public void startGame() {
        ui.onStep(this::onTick);
        ui.onKey(this::handlePlayerInput);
    }

    /**
     * Uses the provided tick to call and advance the following:<br>
     * - A call to model.updateGame(tick) to advance the game by the given tick.<br>
     * - A call to model.checkCollisions() to handle game interactions.<br>
     * - A call to model.spawnObjects() to handle object creation.<br>
     * - A call to model.levelUp() to check and handle leveling.<br>
     * - A call to refreshAchievements(tick) to handle achievement updating.<br>
     * - A call to renderGame() to draw the current state of the game.<br>
     * @param tick the provided tick
     * @provided
     */
    public void onTick(int tick) {
        model.updateGame(tick); // Update GameObjects
        model.checkCollisions(); // Check for Collisions
        model.spawnObjects(); // Handles new spawns
        model.levelUp(); // Level up when score threshold is met
        refreshAchievements(tick); // Handle achievement updating.
        renderGame(); // Update Visual

        // Check game over
        if (model.checkGameOver()) {
            pauseGame();
            showGameOverWindow();
        }
    }

    /**
     * Displays a Game Over window containing the player's final statistics and achievement
     * progress.
     * This window includes:
     * - Number of shots fired and shots hit
     * - Number of Enemies destroyed
     * - Survival time in seconds
     * - Progress for each achievement, including name, description, completion percentage
     * and current tier
     * @provided
     */
    private void showGameOverWindow() {

        // Create a new window to display game over stats.
        javax.swing.JFrame gameOverFrame = new javax.swing.JFrame("Game Over - Player Stats");
        gameOverFrame.setSize(400, 300);
        gameOverFrame.setLocationRelativeTo(null); // center on screen
        gameOverFrame.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);


        StringBuilder sb = new StringBuilder();
        sb.append("Shots Fired: ").append(getStatsTracker().getShotsFired()).append("\n");
        sb.append("Shots Hit: ").append(getStatsTracker().getShotsHit()).append("\n");
        sb.append("Enemies Destroyed: ").append(getStatsTracker().getShotsHit()).append("\n");
        sb.append("Survival Time: ").append(getStatsTracker().getElapsedSeconds()).append(
                " seconds\n"
        );


        List<Achievement> achievements = achievementManager.getAchievements();
        for (Achievement ach : achievements) {
            double progressPercent = ach.getProgress() * 100;
            sb.append(ach.getName())
                    .append(" - ")
                    .append(ach.getDescription())
                    .append(" (")
                    .append(String.format("%.0f%%", progressPercent))
                    .append(" complete, Tier: ")
                    .append(ach.getCurrentTier())
                    .append(")\n");
        }

        String statsText = sb.toString();

        // Create a text area to show stats.
        javax.swing.JTextArea statsArea = new javax.swing.JTextArea(statsText);
        statsArea.setEditable(false);
        statsArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 14));

        // Add the text area to a scroll pane (optional) and add it to the frame.
        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(statsArea);
        gameOverFrame.add(scrollPane);

        // Make the window visible.
        gameOverFrame.setVisible(true);
    }

    /**
     *Updates the player's progress towards achievements on every game tick, and uses the achievementManager to track
     * and update the player's achievements.
     * Progress is a double representing completion percentage, and must be >= 0.0, and <= 1.0.
     *
     * Achievement Progress Calculations:
     * - Survivor achievement: survival time since game start in seconds, mastered at 120 seconds.
     * - Enemy Exterminator achievement: shots hit since game start, mastered at 20 shots.
     * - Sharp Shooter achievement: if shots fired > 10, then result is accuracy / 0.99, with the maximum result
     * possible being 1; otherwise if shots fired <= 10, result is 0.
     * (This is so that mastery is achieved at accuracy >= 0.99)
     *
     * The AchievementManager stores all new achievements mastered, and then updates the UI statistics with each new
     * achievement's name and progress value.
     * Once every 100 ticks, and only if verbose is true, the achievement progress is logged to the UI.
     *
     * @param tick the provided tick
     */
    public void refreshAchievements(int tick) {
        PlayerStatsTracker statsTracker = getStatsTracker();

        // Update all achievements
        for (Achievement achievement : achievementManager.getAchievements()) {
            String name = achievement.getName();
            switch (name) {
                case "Survivor":
                    achievementManager.updateAchievement(
                            name,
                            Math.min((double) getTimeSurvived() / 120, 1.0)
                    );
                    break;
                case "Enemy Exterminator":
                    achievementManager.updateAchievement(
                            name,
                            Math.min((double) statsTracker.getShotsHit() / 20, 1.0)
                    );
                    break;
                case "Sharp Shooter":
                    if (statsTracker.getShotsFired() > 10) {
                        achievementManager.updateAchievement(
                                name,
                                Math.min(statsTracker.getAccuracy() / 0.99, 1.0)
                        );
                    } else {
                        achievementManager.updateAchievement(name, 0.0);
                    }
            }
            ui.setAchievementProgressStat(achievement.getName(), achievement.getProgress());
        }

        achievementManager.logAchievementMastered();

        if (tick % 100 == 0 && isVerbose) {
            ui.logAchievements(achievementManager.getAchievements());
        }
    }

    /**
     * Renders the current game state, including score, health, level, and survival time.
     * - Uses ui.setStat() to update the "Score", "Health" and "Level" appropriately with information from the model.
     * - Uses ui.setStat() to update "Time Survived" with (System.currentTimeMillis() - startTime) / 1000 + " seconds"
     * - Renders all spaceObjects using one call to ui.render().
     */
    public void renderGame() {
        List<SpaceObject> objectsToRender = model.getSpaceObjects();
        objectsToRender.add(model.getShip());
        ui.render(objectsToRender);
        ui.setStat("Score", Integer.toString(model.getShip().getScore()));
        ui.setStat("Health", Integer.toString(model.getShip().getHealth()));
        ui.setStat("Level", Integer.toString(model.getLevel()));
        ui.setStat("Time Survived", getTimeSurvived() + " seconds");
    }

    private long getTimeSurvived() {
        return ((System.currentTimeMillis() - startTime) / 1000);
    }

    /**
     * Handles player input and performs actions such as moving the ship or firing Bullets.
     * Uppercase and lowercase inputs should be treated identically:
     * - For movement keys "W", "A", "S" and "D" the ship should be moved up, left, down, or right respectively,
     * unless the game is paused. The movement should also be logged, provided verbose is true, as follows:
     * "Ship moved to ({model.getShip().getX()}, {model.getShip().getY()})"
     *
     * - For input "F" the fireBullet() method of the Model instance should be called, and the recordShotFired()
     * method of the PlayerStatsTracker instance should be called.
     * - For input "P" the pauseGame() method should be called.
     * - For all other inputs, the following should be logged, irrespective of the verbose state:
     * "Invalid input. Use W, A, S, D, F, or P."
     * When the game is paused, only un-pausing should be possible. No other action of printing should occur.
     *
     * @param input the player's input command
     * @requires input is a single character.
     */
    public void handlePlayerInput(String input) {
        input = input.toUpperCase();

        if (isPaused) {
            if (input.equals("P")) {
                pauseGame();
            }
            return;
        }

        switch (input) {
            case "W":
                attemptMove(Direction.UP);
                break;
            case "A":
                attemptMove(Direction.LEFT);
                break;
            case "S":
                attemptMove(Direction.DOWN);
                break;
            case "D":
                attemptMove(Direction.RIGHT);
                break;
            case "F":
                model.fireBullet();
                getStatsTracker().recordShotFired();
                break;
            case "P":
                pauseGame();
                break;
            default:
                ui.log("Invalid input. Use W, A, S, D, F, or P.");
        }

        if (isVerbose
                && (input.equals("W")
                || input.equals("A")
                || input.equals("S")
                || input.equals("D"))
        ) {
            logMovement();
        }
    }

    /**
     * Attempts to move the ship in the provided direction
     * @param direction - the provided direction.
     */
    private void attemptMove(Direction direction) {
        try {
            model.getShip().move(direction);
        } catch (BoundaryExceededException exception) {
            //Do absolutely nothing
        }
    }

    /**
     * Logs the movement of the ship.
     */
    private void logMovement() {
        ui.log("Ship moved to (" + model.getShip().getX() + ", " + model.getShip().getY() + ")");
    }

    /**
     * public void pauseGame()
     * Calls ui.pause() to pause the game until the method is called again.
     * Calls ui.pause(). Logs "Game paused." or "Game unpaused." as appropriate, after calling ui.pause(),
     * irrespective of verbose state.
     */
    public void pauseGame() {
        ui.pause();
        if (isPaused) {
            ui.log("Game unpaused.");

        } else {
            ui.log("Game paused.");
        }
        isPaused = !isPaused;
    }
}
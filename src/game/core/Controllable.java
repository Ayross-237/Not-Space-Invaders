package game.core;

import game.exceptions.BoundaryExceededException;
import game.utility.Direction;
import static game.GameModel.*;

/**
 * Represents a controllable object in the space game.
 */
public abstract class Controllable extends ObjectWithPosition {

    /**
     * Creates a controllable object at the given coordinates.
     *
     * @param x the given x coordinate
     * @param y the given y coordinate
     */
    public Controllable(int x, int y) {
        super(x, y);
    }

    /**
     * Moves the Controllable by one in the direction given.
     * Throws BoundaryExceededException if the Controllable is attempting to move outside the game boundaries.
     * A controllable is considered outside the game boundaries if they are at:
     * x-coordinate >= GAME_WIDTH
     * x-coordinate < 0
     * y-coordinate >= GAME_HEIGHT
     * y-coordinate < 0
     * Argument given to the exception is "Cannot move {up/down/left/right}. Out of bounds!" depending on the direction.
     *
     * @param direction the given direction.
     *
     * @throws BoundaryExceededException if attempting to move outside the game boundaries.
     * @hint game dimensions are stored in the model.
     */
    public void move(Direction direction) throws BoundaryExceededException {
        switch (direction) {
            case UP -> {
                if (y <= 0) {
                    throw new BoundaryExceededException(exceptionMessage("up"));
                }
                y--;
            }
            case DOWN -> {
                if (y + 1 >= GAME_HEIGHT) {
                    throw new BoundaryExceededException(exceptionMessage("down"));
                }
                y++;
            }
            case LEFT -> {
                if (x <= 0) {
                    throw new BoundaryExceededException(exceptionMessage("left"));
                }
                x--;
            }
            case RIGHT -> {
                if (x + 1 >= GAME_WIDTH) {
                    throw new BoundaryExceededException(exceptionMessage("right"));
                }
                x++;
            }
        }
    }

    /**
     * returns the message to be passed to the BoundaryExceededException depending on the intended direcion
     * @param direction the direction the player was intending to move
     * @return the required message
     */
    private String exceptionMessage(String direction) {
        return "Cannot move " + direction + ". Out of bounds!";
    }
}

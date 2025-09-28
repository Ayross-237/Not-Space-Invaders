package game.achievements;

/**
 * A concrete implementation of the Achievement interface.
 * Sample logic:
 * Progress is tracked as a value between 0.0 and 1.0.
 * Tiers are determined as:
 *      "Novice" if progress < 0.5,
 *      "Expert" if progress is between 0.5 (inclusive) and 1.0,
 *      "Master" if progress equals 1.0.
 */
public class GameAchievement implements Achievement {
    private final String name;
    private final String description;
    private double progress;

    /**
     * Constructs an Achievement with the specified name and description. The initial progress is 0.
     * @param name the unique name.
     * @param description the achievement description.
     * @requires name is not null., name is not empty., description is not null., description is not empty.
     */
    public GameAchievement(String name, String description) {
        this.name = name;
        this.description = description;
        progress = 0.0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public double getProgress() {
        return progress;
    }

    /**
     * Sets the progress to the specified value.
     * @param newProgress the updated progress.
     * @requires newProgress is between 0.0 and 1.0, inclusive.
     * @ensures getProgress() == newProgress, getProgress() <= 1.0
     * after the update (i.e., progress is capped at 1.0)., getProgress() >= 0.0 after the update.
     */
    @Override
    public void setProgress(double newProgress) {
        progress = newProgress;
    }

    /**
     * Returns "Novice" if getProgress() < 0.5, "Expert" if 0.5 <= getProgress() < 0.999,
     * and "Master" if getProgress() >=0.999.
     * @return a string representing the current tier (e.g., "Novice", "Expert", "Master") based on the progress.
     */
    @Override
    public String getCurrentTier() {
        if (progress < 0.5) {
            return "Novice";
        } else if (progress < 0.999) {
            return "Expert";
        } else {
            return "Master";
        }
    }
}

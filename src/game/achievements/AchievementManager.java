package game.achievements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * GameAchievementManager coordinates achievement updates, file persistence management.
 * Responsibilities:
 *  - Register new achievements.
 *  - Update achievement progress.
 *  - Check for Mastered achievements and log them using AchievementFile.
 *  - Provide access to the current list of achievements.
 */
public class AchievementManager {
    private final AchievementFile achievementFile;
    private final HashMap<String, Achievement> achievements;

    /**
     * Constructs a GameAchievementManager with the specified AchievementFile.
     * @param achievementFile the AchievementFile instance to use (non-null)
     * @throws IllegalArgumentException if achievementFile is null.
     * @requires achievementFile is not null
     */
    public AchievementManager(AchievementFile achievementFile) {
        if (achievementFile == null) {
            throw new IllegalArgumentException();
        }
        this.achievementFile = achievementFile;
        achievements = new HashMap<>();
    }

    /**
     * Registers a new achievement.
     * @param achievement the Achievement to register.
     * @throws IllegalArgumentException if achievement is already registered.
     * @requires achievement is not null
     */
    public void addAchievement(Achievement achievement) {
        if (achievements.containsValue(achievement)) {
            throw new IllegalArgumentException();
        }
        achievements.put(achievement.getName(), achievement);
    }

    /**
     * Sets the progress of the specified achievement to a given amount.
     * @param achievementName the name of the achievement.
     * @param absoluteProgressValue the value the achievement's progress will be set to.
     * @throws IllegalArgumentException if no achievement is registered under the provided name.
     * @requires achievementName must be a non-null, non-empty string identifying a registered achievement.
     */
    public void updateAchievement(String achievementName, double absoluteProgressValue) {
        if (!achievements.containsKey(achievementName)) {
            throw new IllegalArgumentException();
        }
        achievements.get(achievementName).setProgress(absoluteProgressValue);
    }

    /**
     * Checks all registered achievements. For any achievement that is mastered and has not yet been logged,
     * this method logs the event via AchievementFile, and marks the achievement as logged.
     */
    public void logAchievementMastered() {
        List<String> masteredAchievements = achievementFile.read();
        for (Achievement achievement : achievements.values()) {
            if (achievement.getCurrentTier().equals("Master")
                    && !masteredAchievements.contains(achievement.getName())
            ) {
                achievementFile.save(achievement.getName());
            }
        }
    }

    /**
     * Returns a list of all registered achievements.
     * @return a List of Achievement objects.
     */
    public List<Achievement> getAchievements() {
        return new ArrayList<>(achievements.values());
    }
}

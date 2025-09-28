package game.achievements;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A concrete implementation of AchievementFile using standard file I/O.
 */
public class FileHandler implements AchievementFile {
    private String fileLocation;

    /**
     * Constructs a FileHandler instance.
     */
    public FileHandler() {
        fileLocation = AchievementFile.DEFAULT_FILE_LOCATION;
    }

    @Override
    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    @Override
    public String getFileLocation() {
        return fileLocation;
    }

    @Override
    public List<String> read() {
        List<String> lines = new ArrayList<>();
        Path path = Paths.get(fileLocation);

        if (!Files.exists(path)) {
            return lines;
        }

        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            return new ArrayList<>();
        }
        return lines;
    }

    @Override
    public void save(String data) {
        Path path = Paths.get(fileLocation);

        try {
            Files.writeString(
                    path,
                    data + "\n",
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            // Do nothing
        }
    }
}

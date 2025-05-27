import java.io.*;

public class SaveUtils {
    public static void saveGame(SavedGame state, String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(state);
        }
    }

    public static SavedGame loadGame(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            return (SavedGame) in.readObject();
        }
    }
}
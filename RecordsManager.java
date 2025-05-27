import java.io.*;
import java.util.*;

public class RecordsManager {
    private static final String RECORDS_FILE = "records.sav";

    public static List<RecordEntry> loadRecords() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(RECORDS_FILE))) {
            return (List<RecordEntry>) ois.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static void saveRecords(List<RecordEntry> records) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RECORDS_FILE))) {
            oos.writeObject(records);
        } catch (Exception e) {
            // Игнорируем
        }
    }

    public static void addRecord(RecordEntry entry) {
        List<RecordEntry> records = loadRecords();
        boolean replaced = false;
        for (int i = 0; i < records.size(); i++) {
            if (records.get(i).username.equals(entry.username)) {
                if (entry.score > records.get(i).score) {
                    records.set(i, entry);
                }
                replaced = true;
                break;
            }
        }
        if (!replaced) records.add(entry);
        Collections.sort(records);
        saveRecords(records);
    }

    public static void showTopRecords(int topN) {
        List<RecordEntry> records = loadRecords();
        Collections.sort(records);
        System.out.println("\n=== ТОП " + topN + " РЕКОРДОВ ===");
        for (int i = 0; i < Math.min(topN, records.size()); i++) {
            RecordEntry r = records.get(i);
            System.out.printf("%d. %s | Очки: %d | Карта: %s | Ходы: %d | Врагов побеждено: %d\n",
                    i + 1, r.username, r.score, r.mapName, r.turns, r.destroyedEnemies);
        }
        if (records.isEmpty()) System.out.println("Пока нет рекордов!");
        System.out.println();
    }
}
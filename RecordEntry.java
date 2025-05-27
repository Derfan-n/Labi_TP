import java.io.Serializable;

public class RecordEntry implements Serializable, Comparable<RecordEntry> {
    public String username;
    public int score;
    public String mapName;
    public int turns;
    public int destroyedEnemies;

    public RecordEntry(String username, int score, String mapName, int turns, int destroyedEnemies) {
        this.username = username;
        this.score = score;
        this.mapName = mapName;
        this.turns = turns;
        this.destroyedEnemies = destroyedEnemies;
    }

    @Override
    public int compareTo(RecordEntry o) {
        return Integer.compare(o.score, this.score);
    }
}
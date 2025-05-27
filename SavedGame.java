import java.io.Serializable;

public class SavedGame implements Serializable {
    public int turn;
    public Hero player;
    public EnemyHero enemy;
    public GameMap map;
    public Castle playerCastle;
    public Castle enemyCastle;
}
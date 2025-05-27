import java.io.Serializable;
import java.util.*;

class EnemyHero extends GameCharacter implements Serializable {
    private Map<String, Integer> army = new HashMap<>();

    public EnemyHero(int startX, int startY, char code, int maxX, int maxY) {
        super(startX, startY, code, maxX, maxY);
        army.put("Гоблин", 5);
        army.put("Орк", 3);
    }

    public void moveTowardsPlayerCastle(Castle target) {
        int dx = Integer.compare(target.getX(), this.x);
        int dy = Integer.compare(target.getY(), this.y);
        int newX = Math.max(0, Math.min(maxX, x + dx));
        int newY = Math.max(0, Math.min(maxY, y + dy));
        this.x = newX;
        this.y = newY;
    }

    public Map<String, Integer> getArmy() { return army; }
}
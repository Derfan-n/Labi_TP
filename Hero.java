import java.io.Serializable;
import java.util.*;

class Hero extends GameCharacter implements Serializable {
    private int steps;
    private final int maxSteps;
    private int gold;
    private Map<String, Integer> army;
    private boolean wasPoisonedByCucumber = false;
    private List<String> lastFallenUnits = new ArrayList<>();
    private int enemiesDefeated = 0; // ← для рекордов

    public Hero(int startX, int startY, char code, int maxX, int maxY, int maxSteps) {
        super(startX, startY, code, maxX, maxY);
        this.maxSteps = maxSteps;
        this.steps = maxSteps;
        this.gold = 2000;
        this.army = new HashMap<>();
    }

    private boolean tryEnterCastle(GameMap map, int x, int y) {
        Castle castle = map.getCastleAt(x, y);
        if (castle != null) {
            if (castle.getCode() == 'C') {
                castle.enterCastle(this);
                return true;
            }
            return false;
        }
        return false;
    }

    public boolean moveBy(int dx, int dy, GameMap map) {
        int newX = x + dx;
        int newY = y + dy;

        if (tryEnterCastle(map, newX, newY)) return false;

        EnemyHero enemy = map.getEnemyAt(newX, newY);
        if (enemy != null) {
            boolean battleWon = startBattle(enemy, map);
            if (battleWon) {
                map.removeCharacter(enemy);
                addEnemyDefeat(); // ← увеличиваем счётчик уничтоженных врагов
            }
            return false;
        }

        // --- Автоматическое съедание огурца ---
        if (map.hasCucumber(newX, newY)) {
            boolean poisoned = map.eatCucumberAt(newX, newY);
            if (poisoned) this.wasPoisonedByCucumber = true;
        }

        int cost = getStepCost(map, newX, newY);
        if (dx != 0 && dy != 0) cost *= 2; // Диагональ

        if (steps < cost) {
            System.out.println("Недостаточно шагов для перемещения!");
            return false;
        }

        if (!checkMovement(map, newX, newY)) return false;

        super.move(dx, dy);
        steps -= cost;
        return true;
    }

    private boolean checkMovement(GameMap map, int newX, int newY) {
        return newX >= 0 && newX <= maxX &&
                newY >= 0 && newY <= maxY &&
                steps >= 0;
    }

    private int getStepCost(GameMap map, int x, int y) {
        return map.isRoad(x, y) ? 1 : 2;
    }

    public void endTurn() {
        steps = maxSteps;
    }

    public boolean startBattle(EnemyHero enemy, GameMap map) {
        System.out.println("\nНАЧАЛОСЬ СРАЖЕНИЕ!");
        Battle battle = new Battle(this, enemy);
        battle.setCucumberPoisoned(wasPoisonedByCucumber);
        boolean result = battle.start(new Scanner(System.in), map);
        wasPoisonedByCucumber = false; // сброс после боя
        return result;
    }

    public int getSteps() { return steps; }
    public int getGold() { return gold; }
    public void spendGold(int amount) { gold -= amount; }
    public void addUnit(String unitName, int amount) {
        army.put(unitName, army.getOrDefault(unitName, 0) + amount);
    }
    public Map<String, Integer> getArmy() { return army; }

    public void showArmy() {
        System.out.println("\n=== Ваша армия ===");
        for (Map.Entry<String, Integer> entry : army.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    public void updateArmyAfterBattle(List<BattleUnit> battleUnits) {
        army.clear();
        for (BattleUnit bu : battleUnits) {
            if (bu.getCount() > 0) {
                army.put(bu.getName(), bu.getCount());
            }
        }
    }

    // --- Для механики зомби ---
    public List<String> getLastFallenUnits() { return lastFallenUnits; }
    public void setLastFallenUnits(List<String> fallen) {
        lastFallenUnits.clear();
        lastFallenUnits.addAll(fallen);
    }
    public void clearLastFallenUnits() { lastFallenUnits.clear(); }
    public boolean wasPoisonedByCucumber() {
        return wasPoisonedByCucumber;
    }

    // --- Для рекордов ---
    public void addEnemyDefeat() {
        enemiesDefeated++;
    }
    public int getEnemiesDefeated() {
        return enemiesDefeated;
    }
}
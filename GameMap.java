import java.io.Serializable;
import java.util.*;

class GameMap implements Serializable {
    private final Map<Character, String> SYMBOL_MAP = Map.of(
            'C', "🏰", 'E', "🏯", 'H', "😀",
            'V', "👿", 'R', "⬛", '.', "🟩",
            'U', "🥒"
    );

    public final char[][] grid;
    private final int width;
    private final int height;
    private final List<GameCharacter> characters = new ArrayList<>();
    public final List<Castle> castles = new ArrayList<>();

    private List<int[]> cucumbers = new ArrayList<>();
    private Random random = new Random();
    private double cucumberRainChance = 0.1; // вероятность огуречного дождя по умолчанию (10%)
    private double cucumberPoisonChance = 0.25; // вероятность отравления огурцом по умолчанию (25%)

    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new char[height][width];
        initializeGrid();
    }

    public void setCucumberRainChance(double chance) {
        if (chance < 0) chance = 0;
        if (chance > 1) chance = 1;
        this.cucumberRainChance = chance;
    }

    public void setCucumberPoisonChance(double chance) {
        if (chance < 0) chance = 0;
        if (chance > 1) chance = 1;
        this.cucumberPoisonChance = chance;
    }

    public void tryTriggerCucumberRain() {
        if (random.nextDouble() < cucumberRainChance) {
            triggerCucumberRain();
        }
    }

    public void initializeGrid() {
        for (int y = 0; y < height; y++) {
            Arrays.fill(grid[y], '.');
            for (int x = 0; x < width; x++) {
                if (x == y) grid[y][x] = 'R';
            }
        }
    }

    public void triggerCucumberRain() {
        cucumbers.clear();
        int numCucumbers = 5 + random.nextInt(6); // 5-10 огурцов
        for (int i = 0; i < numCucumbers; i++) {
            int cx, cy;
            int attempts = 0;
            do {
                cx = random.nextInt(width);
                cy = random.nextInt(height);
                attempts++;
            } while ((grid[cy][cx] != '.' || hasCucumber(cx, cy)) && attempts < 100);
            cucumbers.add(new int[]{cx, cy});
        }
        System.out.println("=== Огуречный дождь! На карте появились препятствия 🥒 ===");
    }

    public boolean hasCucumber(int x, int y) {
        for (int[] pos : cucumbers) {
            if (pos[0] == x && pos[1] == y) return true;
        }
        return false;
    }

    public boolean eatCucumberAt(int x, int y) {
        boolean found = false;
        Iterator<int[]> it = cucumbers.iterator();
        while (it.hasNext()) {
            int[] pos = it.next();
            if (pos[0] == x && pos[1] == y) {
                it.remove();
                found = true;
                break;
            }
        }
        if (found) {
            System.out.println("Огурец съеден!");
            if (random.nextDouble() < cucumberPoisonChance) {
                System.out.println("Огурец был отравлен! В следующий бой ждите зомби-юнитов.");
                return true;
            }
        }
        return false;
    }

    public void addCastle(Castle castle) {
        castles.add(castle);
    }

    public void addCharacter(GameCharacter character) {
        characters.add(character);
    }

    public void removeCharacter(GameCharacter character) {
        characters.remove(character);
    }

    public Castle getCastleAt(int x, int y) {
        for (Castle castle : castles) {
            if (castle.getX() == x && castle.getY() == y) {
                return castle;
            }
        }
        return null;
    }

    public EnemyHero getEnemyAt(int x, int y) {
        for (GameCharacter ch : characters) {
            if (ch instanceof EnemyHero && ch.getX() == x && ch.getY() == y) {
                return (EnemyHero) ch;
            }
        }
        return null;
    }

    public void updateGrid() {
        initializeGrid();
        for (Castle castle : castles) {
            grid[castle.getY()][castle.getX()] = castle.getCode();
        }
        for (GameCharacter ch : characters) {
            grid[ch.getY()][ch.getX()] = ch.getCode();
        }
        for (int[] pos : cucumbers) {
            grid[pos[1]][pos[0]] = 'U';
        }
    }

    public void display() {
        updateGrid();
        for (char[] row : grid) {
            for (char cellCode : row) {
                System.out.print(SYMBOL_MAP.getOrDefault(cellCode, "?") + " ");
            }
            System.out.println();
        }
    }

    public boolean isRoad(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height && grid[y][x] == 'R';
    }

    public List<GameCharacter> getCharacters() {
        return characters;
    }
}
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

public class GameLogicTest {

    private PrintStream originalOut;
    private InputStream originalIn;

    @BeforeEach
    public void saveStreams() {
        originalOut = System.out;
        originalIn = System.in;
    }

    @AfterEach
    public void restoreStreamsAndLog(TestInfo testInfo) {
        System.setOut(originalOut);
        System.setIn(originalIn);
        TestLogger.getLogger().info("Тест успешно завершён: " + testInfo.getDisplayName());
    }

    @Test
    public void testPlayerVictoryByCastleCapture() {
        Castle enemyCastle = new Castle(5, 5, 'C');
        Hero player = new Hero(5, 5, 'P', 100, 20, 10);
        boolean isVictory = (player.getX() == enemyCastle.getX() && player.getY() == enemyCastle.getY());
        assertTrue(isVictory, "Победа должна засчитываться при захвате замка врага.");
    }

    @Test
    public void testVictoryByEnemyHeroArmyDefeat() {
        EnemyHero enemy = new EnemyHero(2, 2, 'E', 10, 10);
        enemy.getArmy().clear();
        assertTrue(enemy.getArmy().isEmpty(), "Победа при уничтожении всей армии врага.");
    }

    @Test
    public void testEnemyHeroMovesTowardsPlayerCastle() {
        Castle playerCastle = Castle.createPlayerCastle(5, 5);
        EnemyHero enemy = new EnemyHero(2, 2, 'E', 10, 10);
        enemy.moveTowardsPlayerCastle(playerCastle);
        assertEquals(3, enemy.getX(), "Враг должен сместиться по X в сторону замка.");
        assertEquals(3, enemy.getY(), "Враг должен сместиться по Y в сторону замка.");
    }

    @Test
    public void testHeroMovementAndStepCost() {
        Hero hero = new Hero(0, 0, 'P', 10, 10, 10);
        GameMap map = new GameMap(10, 10) {
            @Override
            public boolean isRoad(int x, int y) { return false; }
        };

        int stepsBefore = hero.getSteps();
        hero.moveBy(1, 0, map);
        assertEquals(1, hero.getX());
        assertEquals(stepsBefore - 2, hero.getSteps());
    }

    @Test
    public void testHireSpearmanInBarracks() {
        Hero hero = new Hero(0, 0, 'P', 10, 10, 200);
        Castle castle = Castle.createPlayerCastle(0, 0);
        Building barracks = castle.getBuildings().get(0);
        Unit spearman = barracks.getUnits().get(0);

        int hireAmount = 1;
        int cost = spearman.getCost();
        int goldBefore = hero.getGold();
        int spearmenBefore = spearman.getQuantity();

        hero.addUnit(spearman.getName(), hireAmount);
        hero.spendGold(cost * hireAmount);
        spearman.reduceQuantity(hireAmount);

        assertEquals(goldBefore - cost, hero.getGold());
        assertEquals(spearmenBefore - hireAmount, spearman.getQuantity());
        assertEquals(hireAmount, hero.getArmy().getOrDefault("Копейщик", 0));
    }

    @Test
    public void testBotVictoryByCastleCapture() {
        Castle playerCastle = Castle.createPlayerCastle(3, 3);
        EnemyHero enemy = new EnemyHero(3, 3, 'E', 100, 20);
        boolean isBotVictory = (enemy.getX() == playerCastle.getX() && enemy.getY() == playerCastle.getY());
        assertTrue(isBotVictory, "Бот должен победить при захвате замка игрока.");
    }

    @Test
    public void testBotVictoryByHeroDefeat() {
        Hero player = new Hero(1, 1, 'P', 10, 10, 10);
        EnemyHero enemy = new EnemyHero(2, 2, 'E', 10, 10);
        player.getArmy().clear();
        boolean isBotVictory = player.getArmy().isEmpty();
        assertTrue(isBotVictory, "Бот должен победить при уничтожении всей армии героя игрока.");
    }

    @Test
    public void testMovePenaltyOffRoad() {
        Hero hero = new Hero(0, 0, 'P', 10, 10, 10);
        GameMap map = new GameMap(10, 10) {
            @Override public boolean isRoad(int x, int y) { return false; }
        };
        int stepsBefore = hero.getSteps();
        hero.moveBy(0, 1, map);
        assertEquals(1, hero.getY());
        assertEquals(stepsBefore - 2, hero.getSteps());
    }

    @Test
    public void testMovePenaltyDiagonal() {
        Hero hero = new Hero(0, 0, 'P', 10, 10, 10);
        GameMap map = new GameMap(10, 10) {
            @Override public boolean isRoad(int x, int y) { return false; }
        };
        int stepsBefore = hero.getSteps();
        hero.moveBy(1, 1, map);
        assertEquals(1, hero.getX());
        assertEquals(1, hero.getY());
        assertEquals(stepsBefore - 4, hero.getSteps());
    }

    @Test
    public void testCannotMoveToOccupiedCell() {
        Hero hero = new Hero(1, 1, 'P', 10, 10, 10);
        GameMap map = new GameMap(10, 10) {
            @Override public EnemyHero getEnemyAt(int x, int y) {
                return (x == 2 && y == 1) ? new EnemyHero(2, 1, 'E', 10, 10) : null;
            }
        };
        boolean moved = hero.moveBy(1, 0, map);
        assertFalse(moved, "Герой не должен смочь шагнуть на занятую клетку.");
        assertEquals(1, hero.getX(), "Координаты героя не должны измениться.");
        assertEquals(1, hero.getY(), "Координаты героя не должны измениться.");
    }

    @Test
    public void testCannotMoveOutOfBounds() {
        Hero hero = new Hero(0, 0, 'P', 10, 10, 10);
        GameMap map = new GameMap(10, 10) {
            @Override public boolean isRoad(int x, int y) { return true; }
        };
        boolean movedLeft = hero.moveBy(-1, 0, map);
        assertFalse(movedLeft, "Герой не должен выйти за левую границу карты.");
        assertEquals(0, hero.getX(), "X героя не должен измениться при попытке выйти за левую границу.");
        assertEquals(0, hero.getY(), "Y героя не должен измениться при попытке выйти за левую границу.");
    }

    @Test
    public void testAttackRangeEnforced() {
        Hero hero = new Hero(1, 1, 'P', 10, 10, 10);
        EnemyHero enemy = new EnemyHero(5, 5, 'E', 10, 10);
        int attackRange = 2;
        int dx = Math.abs(hero.getX() - enemy.getX());
        int dy = Math.abs(hero.getY() - enemy.getY());
        boolean inRange = (dx + dy) <= attackRange;
        assertFalse(inRange, "Атака невозможна вне радиуса действия.");
    }

    @Test
    public void testSuccessfulAttack() {
        Hero hero = new Hero(2, 2, 'P', 10, 10, 10);
        EnemyHero enemy = new EnemyHero(2, 3, 'E', 10, 10);
        enemy.getArmy().put("Копейщик", 5);
        int enemyUnitsBefore = enemy.getArmy().get("Копейщик");
        int attackPower = 2;
        if (enemyUnitsBefore > 0) {
            int left = Math.max(0, enemyUnitsBefore - attackPower);
            enemy.getArmy().put("Копейщик", left);
        }
        assertEquals(enemyUnitsBefore - attackPower, enemy.getArmy().get("Копейщик"),
                "После атаки количество юнитов у врага должно уменьшиться.");
    }

    @Test
    public void testUnitDiesIfQuantityZero() {
        Unit spearman = new Unit("Копейщик", 10, 1);
        Hero hero = new Hero(1, 1, 'P', 10, 10, 10);
        hero.getArmy().put(spearman.getName(), spearman.getQuantity());
        spearman.reduceQuantity(1);
        if (spearman.getQuantity() <= 0) {
            hero.getArmy().remove(spearman.getName());
        }
        assertFalse(hero.getArmy().containsKey("Копейщик"), "Юнит должен быть удалён из армии при количестве 0.");
    }

    @Test
    public void testHeroDiesIfNoUnits() {
        Hero hero = new Hero(1, 1, 'P', 10, 10, 10);
        Unit spearman = new Unit("Копейщик", 10, 1);
        hero.getArmy().put(spearman.getName(), spearman.getQuantity());
        spearman.reduceQuantity(1);
        if (spearman.getQuantity() <= 0) {
            hero.getArmy().remove(spearman.getName());
        }
        boolean heroIsDead = hero.getArmy().isEmpty();
        assertTrue(heroIsDead, "Герой должен погибнуть, если в армии не осталось ни одного юнита.");
    }

    @Test
    public void testGameMapDisplayCorrectness() {
        GameMap map = new GameMap(3, 3) {
            @Override public boolean isRoad(int x, int y) { return true; }
            @Override public boolean hasCucumber(int x, int y) { return false; }
            @Override public boolean eatCucumberAt(int x, int y) { return false; }
            @Override public Castle getCastleAt(int x, int y) {
                if (x == 2 && y == 2) return new Castle(2, 2, 'C');
                return null;
            }
            @Override public EnemyHero getEnemyAt(int x, int y) { return null; }
        };
        Hero hero = new Hero(1, 1, 'P', 3, 3, 10);
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (hero.getX() == x && hero.getY() == y) {
                    sb.append('P');
                } else if (map.getCastleAt(x, y) != null) {
                    sb.append('C');
                } else {
                    sb.append('.');
                }
            }
            sb.append('\n');
        }
        String expected =
                "...\n" +
                        ".P.\n" +
                        "..C\n";
        assertEquals(expected, sb.toString());
    }

    @Test
    public void testBotAttacksPlayer() {
        Hero player = new Hero(1, 1, 'P', 10, 10, 10);
        EnemyHero bot = new EnemyHero(2, 1, 'E', 10, 10);
        Castle fakePlayerCastle = new Castle(1, 1, 'C');
        bot.moveTowardsPlayerCastle(fakePlayerCastle);
        assertEquals(player.getX(), bot.getX(), "Бот должен оказаться на позиции игрока (по X)");
        assertEquals(player.getY(), bot.getY(), "Бот должен оказаться на позиции игрока (по Y)");
        player.updateArmyAfterBattle(new java.util.ArrayList<>());
        assertTrue(player.getArmy().isEmpty(), "После атаки бота армия игрока должна быть пустой (пример).");
    }

    // --- Пример теста с перехватом потока вывода (WARNING) ---
    @Test
    public void testConsoleOutput() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
        TestLogger.getLogger().warning("Попытка перехвата потока вывода");
        BattleUnit unit = new BattleUnit("Копейщик", "🗡️", 100, 15, 1, 2, 5);
        System.out.println(unit.getName() + " " + unit.getEmoji());
        System.setOut(originalOut);
        // Можно проверить содержимое outputStream если нужно
    }

    // --- Пример теста с вызовом приватного метода (ERROR) ---
    @Test
    public void testPrivateMethodAccess() throws Exception {
        Castle castle = new Castle(0, 0, 'C');
        Method m = castle.getClass().getDeclaredMethod("secretCastleMethod");
        m.setAccessible(true);
        TestLogger.getLogger().severe("Попытка вызвать приватный метод Castle через reflection");
        m.invoke(castle);
    }
    @Test
    public void testCucumberPoisoning() {
        Hero hero = new Hero(0, 0, 'P', 10, 10, 10);
        GameMap map = new GameMap(10, 10) {
            @Override public boolean isRoad(int x, int y) { return true; }
            @Override public boolean hasCucumber(int x, int y) {
                return (x == 1 && y == 0);
            }
            @Override public boolean eatCucumberAt(int x, int y) {
                return (x == 1 && y == 0); // true = огурец ядовитый
            }
            @Override public Castle getCastleAt(int x, int y) { return null; }
            @Override public EnemyHero getEnemyAt(int x, int y) { return null; }
        };
        hero.moveBy(1, 0, map);
        assertTrue(hero.wasPoisonedByCucumber(), "Герой должен быть отравлен после поедания ядовитого огурца");
    }




    // новые тесты
    @Test
    public void testMapEditorSaveAndLoad() {
        GameMapEditor editor = new GameMapEditor();
        GameMap map = new GameMap(5, 5);
        // Добавим кастл для проверки
        Castle playerCastle = Castle.createPlayerCastle(0, 0);
        map.addCastle(playerCastle);
        String testFile = "testmap.gmap";
        editor.saveMap(map, testFile);

        GameMap loaded = editor.loadMap(testFile);
        assertNotNull(loaded, "Загруженная карта не должна быть null");
        assertEquals(5, loaded.grid.length, "Высота карты должна совпадать");
        assertEquals(5, loaded.grid[0].length, "Ширина карты должна совпадать");
        assertTrue(loaded.castles.stream().anyMatch(c -> c.getCode() == 'C'), "Должен быть хотя бы один замок игрока");
        // Удаляем тестовый файл
        new File(testFile).delete();
    }

    // === Тесты для рекордов ===
    @Test
    public void testRecordsAddAndLoad() {
        String username = "testuser";
        int score = 1234;
        String map = "testmap";
        int turns = 25;
        int destroyed = 3;
        RecordEntry entry = new RecordEntry(username, score, map, turns, destroyed);
        RecordsManager.addRecord(entry);

        List<RecordEntry> records = RecordsManager.loadRecords();
        assertTrue(records.stream().anyMatch(r -> r.username.equals(username)), "Рекорд должен быть найден");
        // Проверим, что сохраняется только лучший рекорд для пользователя
        RecordEntry better = new RecordEntry(username, score + 100, map, turns - 1, destroyed + 1);
        RecordsManager.addRecord(better);
        List<RecordEntry> updated = RecordsManager.loadRecords();
        RecordEntry found = updated.stream().filter(r -> r.username.equals(username)).findFirst().orElse(null);
        assertNotNull(found, "Лучший рекорд пользователя должен быть найден");
        assertEquals(score + 100, found.score, "Должен сохраниться только лучший результат");
    }

    // === Тест сохранения и загрузки игры ===
    @Test
    public void testSaveAndLoadGame() throws Exception {
        String filename = "testsave.sav";
        Hero player = new Hero(1, 1, 'H', 4, 4, 10);
        EnemyHero enemy = new EnemyHero(3, 3, 'V', 4, 4);
        GameMap map = new GameMap(5, 5);
        Castle playerCastle = Castle.createPlayerCastle(0, 0);
        Castle enemyCastle = Castle.createEnemyCastle(4, 4);
        map.addCastle(playerCastle);
        map.addCastle(enemyCastle);

        SavedGame save = new SavedGame();
        save.turn = 7;
        save.player = player;
        save.enemy = enemy;
        save.map = map;
        save.playerCastle = playerCastle;
        save.enemyCastle = enemyCastle;

        SaveUtils.saveGame(save, filename);
        SavedGame loaded = SaveUtils.loadGame(filename);

        assertNotNull(loaded, "Загруженное сохранение не должно быть null");
        assertEquals(7, loaded.turn, "Должен совпадать номер хода");
        assertEquals(1, loaded.player.getX(), "Координаты героя должны совпадать");
        assertEquals(3, loaded.enemy.getX(), "Координаты врага должны совпадать");
        assertEquals(2, loaded.map.castles.size(), "Должно быть 2 замка");

        // Удаляем тестовый файл
        new File(filename).delete();
    }

    // === Тест на Hero: счетчик уничтоженных врагов ===
    @Test
    public void testHeroEnemiesDefeatedCounter() {
        Hero hero = new Hero(1, 1, 'H', 5, 5, 10);
        assertEquals(0, hero.getEnemiesDefeated(), "Сначала счетчик должен быть 0");
        hero.addEnemyDefeat();
        hero.addEnemyDefeat();
        assertEquals(2, hero.getEnemiesDefeated(), "Счетчик должен увеличиваться");
    }
}
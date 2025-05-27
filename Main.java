import java.util.*;
import java.io.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // --- Запрос имени пользователя ---
        System.out.print("Введите ваше имя (латиницей, без пробелов): ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) username = "player";
        String saveFile = "save_" + username + ".sav";

        // --- Главное меню ---
        while (true) {
            System.out.println("\n1. Новая игра");
            System.out.println("2. Редактор карт");
            System.out.println("3. Загрузить игру");
            System.out.println("4. Рекорды");
            System.out.println("5. Выход");
            System.out.print("Выберите режим: ");
            String menuChoice = scanner.nextLine().trim();

            if (menuChoice.equals("4")) {
                RecordsManager.showTopRecords(5);
                continue;
            }

            if (menuChoice.equals("5")) {
                System.out.println("До свидания!");
                break;
            }

            boolean gameOver = false;
            int turn = 1;
            Hero player = null;
            EnemyHero enemy = null;
            GameMap map = null;
            Castle playerCastle = null;
            Castle enemyCastle = null;
            String mapFile = "";

            if (menuChoice.equals("2")) {
                // --- Редактор карт ---
                GameMapEditor editor = new GameMapEditor();
                editor.editorMenu();
                continue;
            }

            if (menuChoice.equals("3")) {
                // --- Загрузка игры по имени пользователя ---
                try {
                    SavedGame save = SaveUtils.loadGame(saveFile);
                    turn = save.turn;
                    player = save.player;
                    enemy = save.enemy;
                    map = save.map;
                    playerCastle = save.playerCastle;
                    enemyCastle = save.enemyCastle;
                    System.out.println("Игра успешно загружена для пользователя " + username + "!");
                } catch (Exception e) {
                    System.out.println("Ошибка загрузки игры: " + e.getMessage());
                    continue;
                }
            } else if (menuChoice.equals("1")) {
                // --- Новая игра ---
                System.out.print("Введите шанс огуречного дождя (например 0.15): ");
                double cucumberRainChance = 0.1;
                try {
                    cucumberRainChance = Double.parseDouble(scanner.nextLine().trim());
                } catch (Exception e) {
                    System.out.println("Ввод некорректен, используется шанс по умолчанию 10%");
                }

                System.out.print("Введите шанс отравления огурцом (например 0.25): ");
                double cucumberPoisonChance = 0.25;
                try {
                    cucumberPoisonChance = Double.parseDouble(scanner.nextLine().trim());
                } catch (Exception e) {
                    System.out.println("Ввод некорректен, используется шанс по умолчанию 0.25 (25%)");
                }

                System.out.print("Введите имя файла карты для загрузки (оставьте пустым для стандартной): ");
                mapFile = scanner.nextLine().trim();

                if (!mapFile.isEmpty()) {
                    GameMapEditor editor = new GameMapEditor();
                    GameMap loaded = editor.loadMap(mapFile);
                    if (loaded != null) {
                        map = loaded;
                    } else {
                        System.out.println("Не удалось загрузить карту. Будет создана стандартная карта 10x10.");
                        map = new GameMap(10, 10);
                    }
                } else {
                    map = new GameMap(10, 10);
                }

                map.setCucumberRainChance(cucumberRainChance);
                map.setCucumberPoisonChance(cucumberPoisonChance);

                // Определяем замки на карте
                if (map.castles != null && !map.castles.isEmpty()) {
                    for (Castle c : map.castles) {
                        if (c.getCode() == 'C') playerCastle = c;
                        if (c.getCode() == 'E') enemyCastle = c;
                    }
                }
                if (playerCastle == null) {
                    playerCastle = Castle.createPlayerCastle(0, 0);
                    map.addCastle(playerCastle);
                }
                if (enemyCastle == null) {
                    enemyCastle = Castle.createEnemyCastle(
                            map.grid[0].length - 1, map.grid.length - 1);
                    map.addCastle(enemyCastle);
                }

                // Установка героев (можно доработать, если есть стартовые позиции)
                player = new Hero(1, 1, 'H', map.grid[0].length - 1, map.grid.length - 1, 15);
                enemy = new EnemyHero(map.grid[0].length - 2, map.grid.length - 2, 'V',
                        map.grid[0].length - 1, map.grid.length - 1);
                map.addCharacter(player);
                map.addCharacter(enemy);

                System.out.println("Добро пожаловать! Управление: WASD (одиночные/комбинации),");
                System.out.println("E - завершить ход, Q - выход, save - сохранить игру");
                System.out.println("Если стоит 🥒 — просто наступите на клетку и герой автоматически съест огурец!");
            } else {
                System.out.println("Неизвестный режим!");
                continue;
            }

            // --- Игровой цикл ---
            while (!gameOver) {
                System.out.println("\n=== Ход #" + turn + " ===");
                map.display();
                System.out.println("Шагов осталось: " + player.getSteps());
                System.out.print("Ввод: ");
                String input = scanner.nextLine().trim().toLowerCase();

                if (input.equals("q")) {
                    System.out.println("Выход из игры...");
                    break;
                }
                if (input.equals("save")) {
                    try {
                        SavedGame save = new SavedGame();
                        save.turn = turn;
                        save.player = player;
                        save.enemy = enemy;
                        save.map = map;
                        save.playerCastle = playerCastle;
                        save.enemyCastle = enemyCastle;
                        SaveUtils.saveGame(save, saveFile);
                        System.out.println("Игра сохранена для пользователя " + username + "!");
                    } catch (Exception e) {
                        System.out.println("Ошибка сохранения: " + e.getMessage());
                    }
                    continue;
                }

                boolean validInput = false;
                String endTurnReason = null;

                switch (input) {
                    case "w" -> validInput = player.moveBy(0, -1, map);
                    case "s" -> validInput = player.moveBy(0, 1, map);
                    case "a" -> validInput = player.moveBy(-1, 0, map);
                    case "d" -> validInput = player.moveBy(1, 0, map);
                    case "wa", "aw" -> validInput = player.moveBy(-1, -1, map);
                    case "wd", "dw" -> validInput = player.moveBy(1, -1, map);
                    case "sa", "as" -> validInput = player.moveBy(-1, 1, map);
                    case "sd", "ds" -> validInput = player.moveBy(1, 1, map);
                    case "e" -> endTurnReason = "Досрочное завершение";
                    case "army" -> player.showArmy();
                    default -> System.out.println("Неизвестная команда");
                }

                if (player.getX() == enemyCastle.getX() && player.getY() == enemyCastle.getY()) {
                    System.out.println("\n=== ПОБЕДА! Вы захватили вражеский замок! ===");
                    map.display();
                    // Подсчет очков:
                    int destroyed = player.getEnemiesDefeated(); // реализуй этот счетчик в Hero
                    int score = 1000 - (turn * 10) + destroyed * 50;
                    RecordsManager.addRecord(new RecordEntry(
                            username, score, mapFile.isEmpty() ? "Стандартная" : mapFile, turn, destroyed
                    ));
                    System.out.println("Ваш счет: " + score);
                    System.out.println("Рекорды:");
                    RecordsManager.showTopRecords(5);
                    gameOver = true;
                    break;
                }

                if (validInput && player.getSteps() == 0) {
                    endTurnReason = "Закончились шаги";
                }

                if (endTurnReason != null) {
                    // Ход врага
                    enemy.moveTowardsPlayerCastle(playerCastle);

                    if (enemy.getX() == playerCastle.getX() && enemy.getY() == playerCastle.getY()) {
                        System.out.println("\n=== ПОРАЖЕНИЕ! Враг захватил ваш замок! ===");
                        map.display();
                        gameOver = true;
                        break;
                    }

                    player.endTurn();

                    // Попробовать вызвать огуречный дождь с заданной вероятностью
                    map.tryTriggerCucumberRain();

                    System.out.println("\n=== " + endTurnReason + "! Враг переместился на (" +
                            enemy.getX() + "," + enemy.getY() + ") ===");

                    turn++;
                }

                if (!map.getCharacters().contains(enemy)) {
                    System.out.println("\n=== ПОБЕДА! Вы уничтожили вражеского героя! ===");
                    map.display();
                    // Подсчет очков:
                    int destroyed = player.getEnemiesDefeated(); // реализуй этот счетчик в Hero
                    int score = 1000 - (turn * 10) + destroyed * 50;
                    RecordsManager.addRecord(new RecordEntry(
                            username, score, mapFile.isEmpty() ? "Стандартная" : mapFile, turn, destroyed
                    ));
                    System.out.println("Ваш счет: " + score);
                    System.out.println("Рекорды:");
                    RecordsManager.showTopRecords(5);
                    gameOver = true;
                    break;
                }
            }
        }
        scanner.close();
    }
}
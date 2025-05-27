import java.io.Serializable;
import java.util.*;

class Battle implements Serializable {
    private static final int BATTLE_WIDTH = 8;
    private static final int BATTLE_HEIGHT = 8;

    private final Hero hero;
    private final EnemyHero enemy;
    private final List<BattleUnit> playerUnits = new ArrayList<>();
    private final List<BattleUnit> enemyUnits = new ArrayList<>();
    private final List<BattleUnit> resurrectedZombies = new ArrayList<>();
    private boolean isPlayerTurn = true;
    private boolean cucumberPoisoned = false;
    private Random random = new Random();

    public Battle(Hero hero, EnemyHero enemy) {
        this.hero = hero;
        this.enemy = enemy;
        initializeUnits();
        placeUnits();
    }

    public void setCucumberPoisoned(boolean val) {
        this.cucumberPoisoned = val;
    }

    private void initializeUnits() {
        for (Map.Entry<String, Integer> entry : hero.getArmy().entrySet()) {
            BattleUnit unit = createUnit(entry.getKey(), entry.getValue());
            if (unit != null) playerUnits.add(unit);
        }
        enemyUnits.add(new BattleUnit("Гоблин", "👺", 20, 3, 1, 2, 5));
        enemyUnits.add(new BattleUnit("Орк", "👹", 30, 5, 1, 1, 3));
    }

    private BattleUnit createUnit(String name, int count) {
        return switch (name) {
            case "Копейщик" -> new BattleUnit(name, "🗡️", 100, 15, 1, 2, count);
            case "Лучник" -> new BattleUnit(name, "🏹", 50, 20, 3, 3, count);
            case "Всадник" -> new BattleUnit(name, "🐎", 120, 25, 1, 4, count);
            default -> null;
        };
    }

    private void placeUnits() {
        int index = 0;
        for (BattleUnit unit : playerUnits) {
            unit.setX(0);
            unit.setY(1 + index * 2);
            index++;
        }
        index = 0;
        for (BattleUnit unit : enemyUnits) {
            unit.setX(BATTLE_WIDTH - 1);
            unit.setY(1 + index * 2);
            index++;
        }
        if (!resurrectedZombies.isEmpty()) {
            int zombieIndex = 0;
            for (BattleUnit zombie : resurrectedZombies) {
                zombie.setX(0);
                zombie.setY(1 + (playerUnits.size() + zombieIndex) * 2);
                zombieIndex++;
            }
        }
    }

    public boolean start(Scanner scanner, GameMap map) {
        if (cucumberPoisoned) {
            resurrectFallenUnitsAsZombies(hero.getLastFallenUnits());
            cucumberPoisoned = false;
            hero.clearLastFallenUnits();
            placeUnits();
        }
        while (!isBattleOver()) {
            if (isPlayerTurn) {
                playerTurn(scanner);
            } else {
                enemyTurn();
                printBattleField();
            }
            cucumberZombieHpDrain();
            isPlayerTurn = !isPlayerTurn;
            resetUnits();
        }
        return handleBattleEnd(map);
    }

    private void cucumberZombieHpDrain() {
        int aliveZombies = (int) resurrectedZombies.stream().filter(BattleUnit::isAlive).count();
        if (aliveZombies == 0) return;
        for (BattleUnit unit : playerUnits) {
            if (unit.isAlive()) {
                unit.takeDamage(5 * aliveZombies);
            }
        }
        System.out.println("Зомби-юниты истощают вашу армию! Каждый обычный стек теряет " + (5 * aliveZombies) + " HP.");
    }

    private List<BattleUnit> getAllPlayerControlledUnits() {
        List<BattleUnit> all = new ArrayList<>(playerUnits);
        all.addAll(resurrectedZombies);
        return all;
    }

    private void playerTurn(Scanner scanner) {
        System.out.println("\n=== ВАШ ХОД ===");
        // Только живые и не походившие юниты!
        List<BattleUnit> availableUnits = new ArrayList<>();
        for (BattleUnit unit : getAllPlayerControlledUnits()) {
            if (unit.isAlive() && !unit.hasActed()) {
                availableUnits.add(unit);
            }
        }

        while (!availableUnits.isEmpty()) {
            printBattleField();
            // Убрать мертвых юнитов из списка выбора перед каждым выбором
            availableUnits.removeIf(unit -> !unit.isAlive());
            if (availableUnits.isEmpty()) break;

            System.out.println("Выберите юнита:");
            for (int i = 0; i < availableUnits.size(); i++) {
                BattleUnit unit = availableUnits.get(i);
                System.out.printf("%d. %s %s (HP: %d, Кол-во: %d, Атака: %d, Дальность: %d, Шаги: %d)%n",
                        i + 1, unit.getEmoji(), unit.getName(),
                        unit.getTotalHp(), unit.getCount(),
                        unit.getTotalAttack(),
                        unit.getAttackRange(), unit.getMovement());
            }
            System.out.println("0. Завершить ход");

            int choice = scanner.nextInt();
            if (choice == 0) break;
            if (choice < 1 || choice > availableUnits.size()) continue;

            BattleUnit unit = availableUnits.get(choice - 1);
            if (!unit.isAlive()) continue; // если вдруг умер в процессе
            handleUnitActions(unit, scanner);
            // после действия юнита проверить, не умер ли он (например, от ответного удара)
            availableUnits.remove(unit);
        }
    }

    private void handleUnitActions(BattleUnit unit, Scanner scanner) {
        while (true) {
            if (!unit.isAlive()) return; // если погиб до хода
            System.out.printf("%s %s: Выберите действие%n1. Атаковать%n2. Переместиться%n3. Отмена%n",
                    unit.getEmoji(), unit.getName());
            int action = scanner.nextInt();
            if (action == 3) break;

            if (action == 1) {
                handleAttack(unit, scanner);
                unit.markActed();
                break;
            } else if (action == 2) {
                handleMovement(unit, scanner);
                unit.markActed();
                break;
            }
        }
    }

    private void handleAttack(BattleUnit attacker, Scanner scanner) {
        List<BattleUnit> targets = new ArrayList<>();
        // Можно атаковать enemyUnits и resurrectedZombies (но не себя!)
        for (BattleUnit unit : enemyUnits) {
            if (unit.isAlive() && calculateDistance(attacker, unit) <= attacker.getAttackRange() && unit != attacker) {
                targets.add(unit);
            }
        }
        for (BattleUnit unit : resurrectedZombies) {
            if (unit.isAlive() && calculateDistance(attacker, unit) <= attacker.getAttackRange() && unit != attacker) {
                targets.add(unit);
            }
        }
        if (targets.isEmpty()) {
            System.out.println("Нет целей в радиусе атаки!");
            return;
        }
        System.out.println("Выберите цель:");
        for (int i = 0; i < targets.size(); i++) {
            BattleUnit target = targets.get(i);
            System.out.printf("%d. %s %s (HP: %d, Кол-во: %d)%n",
                    i + 1, target.getEmoji(), target.getName(),
                    target.getTotalHp(), target.getCount());
        }
        int choice = scanner.nextInt();
        if (choice < 1 || choice > targets.size()) return;
        BattleUnit target = targets.get(choice - 1);
        if (!target.isAlive() || target == attacker) return;
        target.takeDamage(attacker.getTotalAttack());
        System.out.printf("%s %s атаковал %s %s и нанес %d урона!%n",
                attacker.getEmoji(), attacker.getName(),
                target.getEmoji(), target.getName(),
                attacker.getTotalAttack());
    }

    private void handleMovement(BattleUnit unit, Scanner scanner) {
        while (true) {
            if (!unit.isAlive()) return;
            System.out.println("Текущая позиция: X=" + (unit.getX() + 1) + " Y=" + (unit.getY() + 1));
            System.out.print("Введите новые координаты от 1 до " + BATTLE_WIDTH + " (x y): ");
            try {
                int newX = scanner.nextInt() - 1;
                int newY = scanner.nextInt() - 1;
                if (newX < 0 || newX >= BATTLE_WIDTH || newY < 0 || newY >= BATTLE_HEIGHT) {
                    System.out.println("Координаты должны быть в пределах поля!");
                    continue;
                }
                int distance = Math.abs(newX - unit.getX()) + Math.abs(newY - unit.getY());
                if (distance > unit.getMovement()) {
                    System.out.println("Недостаточно шагов! Максимум можно пройти: " + unit.getMovement());
                    continue;
                }
                boolean occupied = false;
                for (BattleUnit other : allUnits()) {
                    if (other != unit && other.isAlive() && other.getX() == newX && other.getY() == newY) {
                        occupied = true;
                        break;
                    }
                }
                if (occupied) {
                    System.out.println("Клетка занята!");
                    continue;
                }
                unit.setX(newX);
                unit.setY(newY);
                System.out.println("Перемещение выполнено!");
                return;
            } catch (InputMismatchException e) {
                System.out.println("Ошибка ввода! Вводите только числа.");
                scanner.nextLine();
            }
        }
    }

    private List<BattleUnit> allUnits() {
        List<BattleUnit> all = new ArrayList<>(playerUnits);
        all.addAll(enemyUnits);
        all.addAll(resurrectedZombies);
        return all;
    }

    // Ход врага: только enemyUnits!
    private void enemyTurn() {
        System.out.println("\n=== ХОД ПРОТИВНИКА ===");
        List<BattleUnit> aliveEnemies = new ArrayList<>();
        for (BattleUnit unit : enemyUnits) {
            if (unit.isAlive()) {
                aliveEnemies.add(unit);
                unit.resetActions();
            }
        }
        for (BattleUnit unit : aliveEnemies) {
            BattleUnit closestPlayer = findClosestPlayerUnit(unit);
            if (closestPlayer == null) continue;

            int dx = Integer.compare(closestPlayer.getX(), unit.getX());
            int dy = Integer.compare(closestPlayer.getY(), unit.getY());

            int newX = unit.getX() + Integer.signum(dx);
            int newY = unit.getY() + Integer.signum(dy);

            int dist = Math.abs(newX - unit.getX()) + Math.abs(newY - unit.getY());
            if (dist <= unit.getMovement()) {
                boolean blocked = false;
                for (BattleUnit pu : playerUnits) {
                    if (pu.isAlive() && pu.getX() == newX && pu.getY() == newY) {
                        blocked = true;
                        break;
                    }
                }
                for (BattleUnit pu : resurrectedZombies) {
                    if (pu.isAlive() && pu.getX() == newX && pu.getY() == newY) {
                        blocked = true;
                        break;
                    }
                }
                if (!blocked) {
                    unit.setX(newX);
                    unit.setY(newY);
                }
            }

            if (calculateDistance(unit, closestPlayer) <= unit.getAttackRange()) {
                closestPlayer.takeDamage(unit.getTotalAttack());
                System.out.printf("%s %s атаковал %s %s!%n",
                        unit.getEmoji(), unit.getName(),
                        closestPlayer.getEmoji(), closestPlayer.getName());
            }
            unit.markActed();
        }
    }

    private BattleUnit findClosestPlayerUnit(BattleUnit enemyUnit) {
        BattleUnit closest = null;
        int minDistance = Integer.MAX_VALUE;
        for (BattleUnit playerUnit : getAllPlayerControlledUnits()) {
            if (!playerUnit.isAlive()) continue;
            int distance = calculateDistance(enemyUnit, playerUnit);
            if (distance < minDistance) {
                minDistance = distance;
                closest = playerUnit;
            }
        }
        return closest;
    }

    private int calculateDistance(BattleUnit a, BattleUnit b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    private void resetUnits() {
        for (BattleUnit unit : playerUnits) unit.resetActions();
        for (BattleUnit unit : enemyUnits) unit.resetActions();
        for (BattleUnit unit : resurrectedZombies) unit.resetActions();
    }

    private boolean isBattleOver() {
        boolean playerAlive = getAllPlayerControlledUnits().stream().anyMatch(BattleUnit::isAlive);
        boolean enemyAlive = enemyUnits.stream().anyMatch(BattleUnit::isAlive) || resurrectedZombies.stream().anyMatch(BattleUnit::isAlive);
        return !playerAlive || !enemyAlive;
    }

    private boolean handleBattleEnd(GameMap map) {
        List<String> fallen = new ArrayList<>();
        for (BattleUnit bu : playerUnits) {
            if (!bu.isAlive()) fallen.add(bu.getName());
        }
        hero.setLastFallenUnits(fallen);

        hero.updateArmyAfterBattle(playerUnits);

        boolean enemyDefeated = enemyUnits.stream().noneMatch(BattleUnit::isAlive) && resurrectedZombies.stream().noneMatch(BattleUnit::isAlive);

        if (enemyDefeated) {
            System.out.println("Враг уничтожен! ПОБЕДА!");
            map.removeCharacter(enemy);
            return true;
        } else {
            System.out.println("Ваша армия разгромлена... ПОРАЖЕНИЕ!");
            return false;
        }
    }

    private void resurrectFallenUnitsAsZombies(List<String> fallenNames) {
        resurrectedZombies.clear();
        int zombieIndex = 0;
        for (String name : fallenNames) {
            BattleUnit zombie = createZombieUnit(name);
            if (zombie != null) {
                zombie.setX(0);
                zombie.setY(1 + (playerUnits.size() + zombieIndex) * 2);
                zombieIndex++;
                resurrectedZombies.add(zombie);
            }
        }
    }

    private BattleUnit createZombieUnit(String name) {
        switch (name) {
            case "Копейщик":
            case "Зомби Копейщик":
                return new BattleUnit("Зомби Копейщик", "🧟", 30, 10, 1, 1, 1);
            case "Лучник":
            case "Зомби Лучник":
                return new BattleUnit("Зомби Лучник", "🧟", 20, 15, 2, 1, 1);
            case "Всадник":
            case "Зомби Всадник":
                return new BattleUnit("Зомби Всадник", "🧟", 40, 15, 1, 2, 1);
            default:
                return null;
        }
    }

    private boolean isOccupied(int x, int y) {
        for (BattleUnit bu : allUnits()) {
            if (bu.isAlive() && bu.getX() == x && bu.getY() == y) return true;
        }
        return false;
    }

    private void printBattleField() {
        System.out.println("\n=== ПОЛЕ БОЯ ===");
        System.out.print("   ");
        for (int x = 1; x <= BATTLE_WIDTH; x++) System.out.print(x + " ");
        System.out.println();
        for (int y = 0; y < BATTLE_HEIGHT; y++) {
            System.out.print((y + 1) + " ");
            for (int x = 0; x < BATTLE_WIDTH; x++) {
                String cell = "⬜";
                for (BattleUnit unit : playerUnits) {
                    if (unit.getX() == x && unit.getY() == y && unit.isAlive()) {
                        cell = unit.getEmoji();
                    }
                }
                for (BattleUnit unit : resurrectedZombies) {
                    if (unit.getX() == x && unit.getY() == y && unit.isAlive()) {
                        cell = unit.getEmoji();
                    }
                }
                for (BattleUnit unit : enemyUnits) {
                    if (unit.getX() == x && unit.getY() == y && unit.isAlive()) {
                        cell = unit.getEmoji();
                    }
                }
                System.out.print(cell);
            }
            System.out.println();
        }
    }
}
import java.io.Serializable;
import java.util.*;

public class Castle implements Serializable {
    private final int x;
    private final int y;
    private final char code;
    private final List<Building> buildings;

    public Castle(int x, int y, char code) {
        this.x = x;
        this.y = y;
        this.code = code;
        this.buildings = new ArrayList<>();
        initializeBuildings();
    }

    private void initializeBuildings() {
        Building barracks = new Building("Казарма");
        barracks.addUnit(new Unit("Копейщик", 100, 20));
        barracks.addUnit(new Unit("Лучник", 150, 15));

        Building stable = new Building("Конюшня");
        stable.addUnit(new Unit("Всадник", 250, 10));

        buildings.add(barracks);
        buildings.add(stable);
    }

    public void enterCastle(Hero hero) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n=== Вход в замок ===");
            System.out.println("1. Казарма");
            System.out.println("2. Конюшня");
            System.out.println("3. Сыграть с приведением в 'Верю — не верю'");
            System.out.println("0. Выйти");
            System.out.print("Выберите здание: ");

            int choice = scanner.nextInt();
            if (choice == 0) break;

            if (choice == 3) {
                new GhostCardGame().start();
                continue;
            }

            if (choice > 0 && choice <= buildings.size()) {
                handleBuildingMenu(buildings.get(choice - 1), hero, scanner);
            }
        }
    }

    private void handleBuildingMenu(Building building, Hero hero, Scanner scanner) {
        while (true) {
            building.showMenu(hero.getGold());
            int choice = scanner.nextInt();

            if (choice == 0) break;
            List<Unit> units = building.getUnits();

            if (choice > 0 && choice <= units.size()) {
                hireUnit(units.get(choice - 1), hero, scanner);
            }
        }
    }

    private void hireUnit(Unit unit, Hero hero, Scanner scanner) {
        System.out.print("Введите количество: ");
        int amount = scanner.nextInt();

        if (amount <= 0 || amount > unit.getQuantity()) {
            System.out.println("Неверное количество!");
            return;
        }

        int totalCost = amount * unit.getCost();
        if (hero.getGold() >= totalCost) {
            hero.addUnit(unit.getName(), amount);
            hero.spendGold(totalCost);
            unit.reduceQuantity(amount);
            System.out.println("Нанято " + amount + " " + unit.getName());
        } else {
            System.out.println("Недостаточно золота!");
        }
    }

    public static Castle createPlayerCastle(int x, int y) {
        return new Castle(x, y, 'C');
    }

    public static Castle createEnemyCastle(int x, int y) {
        return new Castle(x, y, 'E');
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public char getCode() { return code; }

    public List<Building> getBuildings() {
        return buildings;
    }

    private void secretCastleMethod() {
        System.out.println("Секретный метод Castle вызван!");
    }
}
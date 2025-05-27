import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class Building implements Serializable {
    private final String name;
    private final List<Unit> availableUnits;

    public Building(String name) {
        this.name = name;
        this.availableUnits = new ArrayList<>();
    }

    public void addUnit(Unit unit) {
        availableUnits.add(unit);
    }

    public void showMenu(int playerGold) {
        System.out.println("\n=== " + name + " ===");
        System.out.println("Ваше золото: " + playerGold);
        for(int i = 0; i < availableUnits.size(); i++) {
            Unit unit = availableUnits.get(i);
            System.out.printf("%d. %s (Цена: %d, Доступно: %d)%n",
                    i+1, unit.getName(), unit.getCost(), unit.getQuantity());
        }
        System.out.println("0. Назад");
    }

    public List<Unit> getUnits() { return availableUnits; }
    public String getName() { return name; }
}

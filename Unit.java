import java.io.Serializable;

class Unit implements Serializable {
    private final String name;
    private final int cost;
    private int quantity;

    public Unit(String name, int cost, int quantity) {
        this.name = name;
        this.cost = cost;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public int getCost() { return cost; }
    public int getQuantity() { return quantity; }
    public void reduceQuantity(int amount) { quantity -= amount; }
}
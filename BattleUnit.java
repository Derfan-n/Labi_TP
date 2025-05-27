import java.io.Serializable;

class BattleUnit implements Serializable {
    private final String name;
    private final String emoji;
    private int hpPerUnit;
    private final int attackPerUnit;
    private final int attackRange;
    private final int movement;
    private int count;
    private int x;
    private int y;
    private boolean hasActed;

    public BattleUnit(String name, String emoji, int hp, int attack, int attackRange, int movement, int count) {
        this.name = name;
        this.emoji = emoji;
        this.hpPerUnit = hp;
        this.attackPerUnit = attack;
        this.attackRange = attackRange;
        this.movement = movement;
        this.count = count;
        this.hasActed = false;
    }

    public String getName() { return name; }
    public String getEmoji() { return emoji; }
    public int getTotalHp() { return hpPerUnit * count; }
    public int getTotalAttack() { return attackPerUnit * count; }
    public int getAttackRange() { return attackRange; }
    public int getMovement() { return movement; }
    public int getX() { return x; }
    public int getY() { return y; }
    public boolean hasActed() { return hasActed; }
    public int getCount() { return count; }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void takeDamage(int damage) {
        int totalHp = getTotalHp() - damage;
        int newCount = Math.max(0, totalHp / hpPerUnit);
        if (totalHp > 0 && totalHp % hpPerUnit != 0) {
            newCount++; // Остаток HP у последнего юнита
            hpPerUnit = totalHp % hpPerUnit; // Обновляем HP оставшегося юнита
        } else {
            hpPerUnit = (newCount > 0) ? hpPerUnit : 0;
        }
        count = newCount;
    }
    public boolean isAlive() { return count > 0; }
    public void resetActions() { hasActed = false; }
    public void markActed() { hasActed = true; }
}
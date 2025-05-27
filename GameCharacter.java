import java.io.Serializable;

abstract class GameCharacter implements Serializable {
    protected int x;
    protected int y;
    protected final char code;
    protected final int maxX;
    protected final int maxY;

    public GameCharacter(int startX, int startY, char code, int maxX, int maxY) {
        this.x = startX;
        this.y = startY;
        this.code = code;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public char getCode() { return code; }

    protected void move(int dx, int dy) {
        x = Math.max(0, Math.min(maxX, x + dx));
        y = Math.max(0, Math.min(maxY, y + dy));
    }
}
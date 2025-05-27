import java.io.*;
import java.util.*;

public class GameMapEditor implements Serializable{
    private Scanner scanner = new Scanner(System.in);

    public void editorMenu() {
        while (true) {
            System.out.println("\n=== МЕНЮ РЕДАКТОРА КАРТ ===");
            System.out.println("1. Создать новую карту");
            System.out.println("2. Загрузить карту из файла");
            System.out.println("3. Удалить карту");
            System.out.println("0. Выйти из редактора");
            System.out.print("Выберите пункт: ");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> createNewMap();
                case "2" -> loadMapMenu();
                case "3" -> deleteMapMenu();
                case "0" -> { return; }
                default -> System.out.println("Неизвестный пункт.");
            }
        }
    }

    private void createNewMap() {
        System.out.print("Введите ширину карты: ");
        int w = Integer.parseInt(scanner.nextLine());
        System.out.print("Введите высоту карты: ");
        int h = Integer.parseInt(scanner.nextLine());
        GameMap map = new GameMap(w, h);
        while (true) {
            map.display();
            System.out.println("Добавить элемент: C - ваш замок, E - вражеский замок, R - дорога, . - трава, x - завершить");
            System.out.print("Формат: X Y Символ: ");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("x")) break;
            String[] tokens = input.split("\\s+");
            if (tokens.length != 3) {
                System.out.println("Ошибка формата.");
                continue;
            }
            try {
                int x = Integer.parseInt(tokens[0]);
                int y = Integer.parseInt(tokens[1]);
                char code = tokens[2].charAt(0);
                if (code == 'C' || code == 'E') {
                    map.addCastle(new Castle(x, y, code));
                } else if (code == 'R' || code == '.') {
                    map.grid[y][x] = code;
                } else {
                    System.out.println("Неизвестный символ.");
                }
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
        System.out.print("Сохранить карту? Введите имя файла (или пусто для отмены): ");
        String fname = scanner.nextLine();
        if (!fname.isEmpty()) {
            saveMap(map, fname);
        }
    }

    private void loadMapMenu() {
        System.out.print("Введите имя файла карты: ");
        String fname = scanner.nextLine();
        GameMap map = loadMap(fname);
        if (map == null) return;
        map.display();
        System.out.println("Редактировать карту? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            // Пример: можно снова обработать редактирование (как createNewMap), если надо
            // Для простоты пока только просмотр
        }
    }

    private void deleteMapMenu() {
        System.out.print("Введите имя файла карты для удаления: ");
        String fname = scanner.nextLine();
        File file = new File(fname);
        if (file.exists() && file.delete()) {
            System.out.println("Карта удалена.");
        } else {
            System.out.println("Ошибка удаления.");
        }
    }

    public void saveMap(GameMap map, String filename) {
        try (PrintWriter out = new PrintWriter(filename)) {
            out.println(map.grid.length + " " + map.grid[0].length);
            for (char[] row : map.grid) {
                out.println(new String(row));
            }
            for (Castle c : map.castles) {
                out.println("CASTLE " + c.getX() + " " + c.getY() + " " + c.getCode());
            }
            System.out.println("Карта сохранена.");
        } catch (IOException e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    public GameMap loadMap(String filename) {
        try (BufferedReader in = new BufferedReader(new FileReader(filename))) {
            String[] size = in.readLine().split(" ");
            int h = Integer.parseInt(size[0]);
            int w = Integer.parseInt(size[1]);
            GameMap map = new GameMap(w, h);
            for (int y = 0; y < h; y++) {
                String line = in.readLine();
                for (int x = 0; x < w; x++) {
                    map.grid[y][x] = line.charAt(x);
                }
            }
            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("CASTLE")) {
                    String[] tok = line.split(" ");
                    int x = Integer.parseInt(tok[1]);
                    int y = Integer.parseInt(tok[2]);
                    char code = tok[3].charAt(0);
                    map.addCastle(new Castle(x, y, code));
                }
            }
            System.out.println("Карта загружена.");
            return map;
        } catch (Exception e) {
            System.out.println("Ошибка загрузки: " + e.getMessage());
            return null;
        }
    }
}
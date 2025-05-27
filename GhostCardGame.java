import java.util.*;

public class GhostCardGame {
    private final Random random = new Random();

    public void start() {
        printRules();
        List<Integer> playerHand = new ArrayList<>();
        List<Integer> ghostHand = new ArrayList<>();
        List<Integer> deck = new ArrayList<>();

        // Колода: 6-10 по 4 карты каждого номинала
        for (int i = 6; i <= 10; i++)
            for (int j = 0; j < 4; j++)
                deck.add(i);
        Collections.shuffle(deck, random);

        // Раздача
        for (int i = 0; i < 6; i++) {
            playerHand.add(deck.remove(deck.size() - 1));
            ghostHand.add(deck.remove(deck.size() - 1));
        }

        boolean playerTurn = random.nextBoolean();
        Scanner scanner = new Scanner(System.in);

        while (!playerHand.isEmpty() && !ghostHand.isEmpty()) {
            if (playerTurn) {
                // Ход игрока
                System.out.println("\nВаши карты: " + handToString(playerHand));
                int card = 0;
                while (true) {
                    System.out.print("Выберите карту из своей руки (номинал, или -1 для выхода): ");
                    try {
                        card = scanner.nextInt();
                        if (card == -1) {
                            System.out.println("Вы досрочно покинули игру с приведением!");
                            return;
                        }
                        if (playerHand.contains(card)) break;
                        else System.out.println("У вас нет такой карты.");
                    } catch (InputMismatchException e) {
                        System.out.println("Введите ЦЕЛОЕ число!");
                        scanner.next();
                    }
                }
                playerHand.remove(Integer.valueOf(card));
                System.out.print("Назовите номинал карты (можно соврать, 6-10, или -1 для выхода): ");
                int claimed = 0;
                while (true) {
                    try {
                        claimed = scanner.nextInt();
                        if (claimed == -1) {
                            System.out.println("Вы досрочно покинули игру с приведением!");
                            return;
                        }
                        if (claimed >= 6 && claimed <= 10) break;
                        else System.out.println("Только 6-10!");
                    } catch (InputMismatchException e) {
                        System.out.println("Введите число!");
                        scanner.next();
                    }
                }
                boolean ghostBelieves = ghostDecision(card, claimed);
                System.out.println("Приведение " + (ghostBelieves ? "верит" : "НЕ верит") + " вам.");
                if ((card == claimed && ghostBelieves) || (card != claimed && !ghostBelieves)) {
                    System.out.println("Карты уходят в сброс.");
                } else {
                    System.out.println("Вы забираете свою карту обратно!");
                    playerHand.add(card);
                }
                playerTurn = false;
            } else {
                // Ход приведения
                int realCard = ghostHand.get(random.nextInt(ghostHand.size()));
                ghostHand.remove(Integer.valueOf(realCard));
                int claimed;
                if (random.nextInt(100) < 40) {
                    claimed = randomCardExcept(realCard);
                } else {
                    claimed = realCard;
                }
                System.out.println("\nПриведение кладет карту и говорит: \"Это " + claimed + "\"");
                int answer = -1;
                while (true) {
                    System.out.print("Верите? (1 - верю, 0 - не верю, -1 для выхода): ");
                    try {
                        answer = scanner.nextInt();
                        if (answer == -1) {
                            System.out.println("Вы досрочно покинули игру с приведением!");
                            return;
                        }
                        if (answer == 1 || answer == 0) break;
                        else System.out.println("Только 1 или 0!");
                    } catch (InputMismatchException e) {
                        System.out.println("Введите 1 или 0!");
                        scanner.next();
                    }
                }
                boolean wasBluff = (claimed != realCard);
                if ((answer == 1 && !wasBluff) || (answer == 0 && wasBluff)) {
                    System.out.println("Карты уходят в сброс.");
                } else {
                    System.out.println("Карту забираете себе!");
                    playerHand.add(realCard);
                }
                playerTurn = true;
            }
        }
        if (playerHand.isEmpty()) {
            System.out.println("\nВы победили! Приведение исчезает в облаке пара!");
        } else if (ghostHand.isEmpty()) {
            System.out.println("\nПриведение победило! Оно довольно смеется.");
        } else {
            System.out.println("\nИгра закончена досрочно.");
        }
    }

    private void printRules() {
        System.out.println("\n=== Правила игры \"Верю — не верю\" ===");
        System.out.println("1. У каждого игрока и приведéния по 6 карт (от 6 до 10, номиналы могут повторяться).");
        System.out.println("2. Игрок или приведéние по очереди выкладывают любую карту рубашкой вниз, вслух объявляя её номинал (можно соврать).");
        System.out.println("3. Противник решает: верит или не верит заявленному номиналу.");
        System.out.println("   - Если номинал совпал и противник поверил, или если номинал не совпал и противник не поверил — карты уходят в сброс.");
        System.out.println("   - В противном случае карты возвращаются (вы забираете карту обратно или забираете карту приведéния себе).");
        System.out.println("4. Побеждает тот, кто первым избавится от всех карт.");
        System.out.println("5. В любой момент можно ввести -1, чтобы выйти из мини-игры досрочно.");
        System.out.println("==========================================\n");
    }

    private boolean ghostDecision(int real, int claimed) {
        if (real == claimed) {
            return random.nextInt(100) < 75;
        } else {
            return random.nextInt(100) < 25;
        }
    }

    private int randomCardExcept(int except) {
        List<Integer> vals = new ArrayList<>(Arrays.asList(6, 7, 8, 9, 10));
        vals.remove(Integer.valueOf(except));
        return vals.get(random.nextInt(vals.size()));
    }

    private String handToString(List<Integer> hand) {
        Map<Integer, Integer> counts = new TreeMap<>();
        for (int c : hand) counts.put(c, counts.getOrDefault(c, 0) + 1);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, Integer> e : counts.entrySet()) {
            sb.append(e.getKey()).append("[").append(e.getValue()).append("] ");
        }
        return sb.toString();
    }
}
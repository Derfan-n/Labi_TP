import java.io.IOException;
import java.util.logging.*;

public class TestLogger {
    private static final Logger logger = Logger.getLogger("TestLogger");

    static {
        try {
            Handler handler = new FileHandler("test.log", true);
            handler.setFormatter(new SimpleFormatter());
            logger.addHandler(handler);
            logger.setUseParentHandlers(false); // не дублируем в консоль
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger getLogger() {
        return logger;
    }
}
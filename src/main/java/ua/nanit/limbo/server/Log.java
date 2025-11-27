package ua.nanit.limbo.server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class Log {
    private static final Logger LOGGER = Logger.getLogger("Limbo");
    private static int debugLevel = 0;

    static {
        LOGGER.setUseParentHandlers(false); 

        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);

        handler.setFormatter(new Formatter() {
            private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");

            @Override
            public String format(LogRecord record) {
                String timestamp = timeFormat.format(new Date(record.getMillis()));
                String level = formatLevel(record.getLevel());
                return String.format("%s %s %s%n", timestamp, level, record.getMessage());
            }

            private String formatLevel(Level level) {
                if (level == Level.INFO) return "INFO Limbo -- ";
                if (level == Level.WARNING) return "WARN ";
                if (level == Level.SEVERE) return "ERROR ";
                if (level == Level.FINE) return "INFO Limbo -- ";
                return level.getName(); 
            }
        });

        LOGGER.addHandler(handler);
        LOGGER.setLevel(Level.ALL);
    }

    public static void setLevel(int level) {
        debugLevel = level;
    }

    public static void debug(Object msg, Object... args) {
        if (debugLevel >= 1) {
            printFormatted(Level.FINE, msg, args);
        }
    }

    public static void info(Object msg, Object... args) {
        printFormatted(Level.INFO, msg, args);
    }

    public static void warn(Object msg, Object... args) {
        printFormatted(Level.WARNING, msg, args);
    }

    public static void warning(Object msg, Object... args) {
        warn(msg, args);
    }

    public static void error(Object msg, Object... args) {
        printFormatted(Level.SEVERE, msg, args);
    }

    private static void printFormatted(Level level, Object msg, Object... args) {
        String text;
        if (args.length == 0) {
            text = msg.toString();
        } else {
            text = String.format(msg.toString(), args);
        }
        LOGGER.log(level, text);
    }

    public static boolean isDebug() {
        return debugLevel >= 1;
    }
}

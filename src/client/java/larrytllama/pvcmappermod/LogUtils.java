package larrytllama.pvcmappermod;

public class LogUtils {
    public static void info(Object msg) {
        System.out.println("[INFO] " + msg);
    }

    public static void info(String format, Object... args) {
        System.out.println("[INFO] " + String.format(format, args));
    }

    public static void warn(Object msg) {
        System.out.println("[WARN] " + msg);
    }

    public static void warn(String format, Object... args) {
        System.out.println("[WARN] " + String.format(format, args));
    }

    public static void warn(String msg, Throwable e) {
        System.out.println("[WARN] " + msg);
        e.printStackTrace();
    }

    public static void error(Object msg) {
        System.err.println("[ERROR] " + msg);
    }

    public static void error(String msg, Throwable e) {
        System.err.println("[ERROR] " + msg);
        e.printStackTrace();
    }

    public static void error(String format, Object... args) {
        System.err.println("[ERROR] " + String.format(format, args));
    }

    public static void debug(Object msg) {
        SettingsProvider sp = SettingsProvider.getInstance();
        if (sp != null && sp.debugMode) {
            System.out.println("[DEBUG] " + msg);
        }
    }

    public static void debug(String msg) {
        SettingsProvider sp = SettingsProvider.getInstance();
        if (sp != null && sp.debugMode) {
            System.out.println("[DEBUG] " + msg);
        }
    }

    public static void debug(String format, Object... args) {
        SettingsProvider sp = SettingsProvider.getInstance();
        if (sp != null && sp.debugMode) {
            System.out.println("[DEBUG] " + String.format(format, args));
        }
    }
}

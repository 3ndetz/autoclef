package adris.altoclef;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO: Debug library or use Minecraft's built in debugger
public class Debug {

    public static AltoClef jankModInstance;

    public static void logInternal(String message) {
        System.out.println("ALTO CLEF: " + message);
    }

    public static void logInternal(String format, Object... args) {
        logInternal(String.format(format, args));
    }

    private static String getLogPrefix() {
        if (jankModInstance != null) {
            return jankModInstance.getModSettings().getChatLogPrefix();
        }
        return "[Alto Clef] ";
    }

    private static void addChatMessageInternalPlayer(String message){
        if(jankModInstance != null){
            // TODO NEW UNTESTED NEW REMOVE
            jankModInstance.getInfoSender().onAutoclefEvent("mc_executor_log","[Baritone] " + message);
        }
        MinecraftClient.getInstance().player.sendMessage(Text.of(message), false);
    }

    public static void logMessage(String message, boolean prefix) {
        if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null && message != null) {
            if (prefix) {
                message = "\u00A7d\u00A7l" + getLogPrefix() + "\u00A7r" + message;
            }
            addChatMessageInternalPlayer(message);
            //MinecraftClient.getInstance().player.sendMessage(Text.of(message), false);
            //MinecraftClient.getInstance().player.sendChatMessage(msg);
        } else {
            logInternal(message);
        }
    }

    public static void logMessage(String message) {
        logMessage(message, true);
    }

    public static void logMessage(String format, Object... args) {
        logMessage(String.format(format, args));
    }

    public static void logWarning(String message) {
        logInternal("WARNING: " + message);
        if (jankModInstance != null && !jankModInstance.getModSettings().shouldHideAllWarningLogs()) {
            if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null && message != null) {
                String msg = "\u00A7d\u00A7l\u00A7n" + getLogPrefix() + "\u00A7c" + message + "\u00A7r";
                addChatMessageInternalPlayer(msg);
                //MinecraftClient.getInstance().player.sendMessage(Text.of(msg), false);
                //MinecraftClient.getInstance().player.sendChatMessage(msg);
            }
        }
    }

    public static void logWarning(String format, Object... args) {
        logWarning(String.format(format, args));
    }

    public static void logError(String message) {
        String stacktrace = getStack(2);
        System.err.println(message);
        System.err.println("at:");
        System.err.println(stacktrace);
        if (MinecraftClient.getInstance() != null && MinecraftClient.getInstance().player != null) {
            String msg = "\u00A72\u00A7l\u00A7c" + getLogPrefix() + "[ERROR] " + message + "\nat:\n" + stacktrace + "\u00A7r";
            addChatMessageInternalPlayer(msg);
            //MinecraftClient.getInstance().player.sendMessage(Text.of(msg), false);
        }
    }

    public static void logError(String format, Object... args) {
        logError(String.format(format, args));
    }

    public static void logStack() {
        logInternal("STACKTRACE: \n" + getStack(2));
    }

    private static String getStack(int toSkip) {
        StringBuilder stacktrace = new StringBuilder();
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            if (toSkip-- <= 0) {
                stacktrace.append(ste.toString()).append("\n");
            }
        }
        return stacktrace.toString();
    }
    public static void logObject(Object obj) {
        Debug.logMessage("Debug object:\n=====\n" + dumpObject(obj) + "\n=====");
    }

    public static String dumpObject(Object obj) {
        return dumpObject(obj, new ArrayList<>(), 1);
    }

    private static String dumpObject(Object obj, List<Object> visited, int depth) {
        if (obj == null) return "null";
        if (depth > 10) return "..."; // Prevent infinite recursion
        if (visited.contains(obj)) return "[CIRCULAR REF]";

        visited.add(obj);
        StringBuilder result = new StringBuilder();
        Class<?> clazz = obj.getClass();

        result.append(clazz.getSimpleName()).append(" {\n");

        // Get all fields including inherited ones
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                // Skip synthetic fields
                if (field.isSynthetic()) continue;

                // Try to make the field accessible
                try {
                    field.trySetAccessible();
                    String indent = "  ".repeat(depth + 1);
                    result.append(indent).append(field.getName()).append(": ");

                    Object value = field.get(obj);
                    if (value == null) {
                        result.append("null\n");
                    } else if (isPrimitive(value.getClass()) || value.getClass().isEnum()) {
                        result.append(value).append("\n");
                    } else if (value.getClass().isArray()) {
                        result.append(arrayToString(value, visited, depth)).append("\n");
                    } else {
                        //result.append("\n").append(dumpObject(value, visited, depth + 1));
                    }
                } catch (Exception e) {
                    // If we can't access the field, just note that it's inaccessible
                    String indent = "  ".repeat(depth + 1);
                    result.append(indent)
                            .append(field.getName())
                            .append(": [INACCESSIBLE]\n");
                }
            }
        }

        result.append("  ".repeat(depth)).append("}");
        return result.toString();
    }

    private static String arrayToString(Object array, List<Object> visited, int depth) {
        if (array == null) return "null";

        int length = Array.getLength(array);
        if (length == 0) return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < length; i++) {
            Object element = Array.get(array, i);
            if (i > 0) sb.append(", ");

            if (element == null) {
                sb.append("null");
            } else if (isPrimitive(element.getClass())) {
                sb.append(element);
            } else {
                sb.append(dumpObject(element, visited, depth + 1));
            }

            if (i >= 9 && length > 10) {
                sb.append(", ... (").append(length - i - 1).append(" more)");
                break;
            }
        }

        sb.append("]");
        return sb.toString();
    }

    private static boolean isPrimitive(Class<?> type) {
        return type.isPrimitive() ||
                type == String.class ||
                type == Boolean.class ||
                type == Character.class ||
                type == Byte.class ||
                type == Short.class ||
                type == Integer.class ||
                type == Long.class ||
                type == Float.class ||
                type == Double.class;
    }
}

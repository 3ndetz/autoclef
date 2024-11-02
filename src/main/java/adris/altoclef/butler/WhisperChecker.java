package adris.altoclef.butler;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.util.time.TimerGame;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WhisperChecker {

    private static final TimerGame _repeatTimer = new TimerGame(0.1);

    private static String _lastMessage = null;

    public static MessageResult tryParse(String ourUsername, String whisperFormat, String message) {
        List<String> parts = new ArrayList<>(Arrays.asList("{from}", "{to}", "{message}"));

        // Sort by the order of appearance in whisperFormat.
        parts.sort(Comparator.comparingInt(whisperFormat::indexOf));
        parts.removeIf(part -> !whisperFormat.contains(part));

        String regexFormat = Pattern.quote(whisperFormat);
        for (String part : parts) {
            regexFormat = regexFormat.replace(part, "(.+)");
        }
        if (regexFormat.startsWith("\\Q")) {
            regexFormat = regexFormat.substring("\\Q".length());
        }
        if (regexFormat.endsWith("\\E")) {
            regexFormat = regexFormat.substring(0, regexFormat.length() - "\\E".length());
        }
        //Debug.logInternal("FORMAT: " + regexFormat + " tested on " + message);
        Pattern p = Pattern.compile(regexFormat);
        Matcher m = p.matcher(message);
        Map<String, String> values = new HashMap<>();
        if (m.matches()) {
            for (int i = 0; i < m.groupCount(); ++i) {
                // parts is sorted, so the order should lign up.
                if (i >= parts.size()) {
                    Debug.logError("Invalid whisper format parsing: " + whisperFormat + " for message: " + message);
                    break;
                }
                //Debug.logInternal("     GOT: " + parts.get(i) + " -> " + m.group(i + 1));
                values.put(parts.get(i), m.group(i + 1));
            }
        }

        if (values.containsKey("{to}")) {
            // Make sure the "to" target is us.
            String toUser = values.get("{to}");
            if (!toUser.equals(ourUsername)) {
                Debug.logInternal("Rejected message since it is sent to " + toUser + " and not " + ourUsername);
                return null;
            }
        }
        if (values.containsKey("{from}") && values.containsKey("{message}")) {
            MessageResult result = new MessageResult();
            result.from = values.get("{from}");
            result.message = values.get("{message}");
            return result;
        }
        return null;
    }

    public static MessageResult chatParse(String ourUsername, String[] chatFormatMas, String message) {
        return chatParse(ourUsername, chatFormatMas, message, "exact");
    }

    public static MessageResult chatParse(String ourUsername, String[] chatFormatMas, String message,
                                          String ExactState) {
        List<String> parts = new ArrayList<>(
                Arrays.asList("{team}", "{global}", "{starterPrefix}", "{donate}", "{suffix}", "{clan}", "{rank}",
                        "{from}", "{to}", "{message}"));
        String serverName = chatFormatMas[0];
        String serverMode = chatFormatMas[2];
        String chatFormatNew = new String(chatFormatMas[1]);
        // Sort by the order of appearance in whisperFormat.
        message = message.replace("\\", "");
        //заменяем стрелки
        List<String> arrows = new ArrayList<>(Arrays.asList("➥", "->", "➡", "➥", "➯", "➨", "›", "►", "⋙", "»", "⪼",
                "⇨")); //https://ru.piliapp.com/symbol/arrow/
        for (String arrow : arrows) {
            if (!chatFormatNew.contains(
                    arrow)) { //ЕСЛИ ШАБЛОН НЕ СОДЕРЖИТ ОДНУ ИЗ ЭТИХ СТРЕЛОК ТОГДА РЕПЛАЙСАЕМ ЕСЛИ НЕТ ТО ИДЕМ ПО ШАБЛОНУ ТАК БУДЕТ ТОЧНЕЕ!
                message = message.replace(arrow, ">");
            }
        }
        List<Character> regexKillingChars = new ArrayList<>(
                Arrays.asList('[', ']', '.', '^', '?', '*', '$', '(', ')', '/', '|', '+'));

        for (Character killer : regexKillingChars) {
            String charr = killer.toString();
            chatFormatNew = chatFormatNew.replace(charr, "\\" + charr);
        }
        String chatFormat = chatFormatNew;

        parts.sort(Comparator.comparingInt(chatFormat::indexOf));
        parts.removeIf(part -> !chatFormat.contains(part));

        String regexFormat = Pattern.quote(chatFormat);
        for (String part : parts) {
            regexFormat = regexFormat.replace(part, "(.+)");
        }

        if (regexFormat.startsWith("\\Q")) {
            regexFormat = regexFormat.substring("\\Q".length());
        }
        if (regexFormat.endsWith("\\E")) {
            regexFormat = regexFormat.substring(0, regexFormat.length() - "\\E".length());
        }
        Pattern p = Pattern.compile(regexFormat);
        Matcher m = p.matcher(message);
        Map<String, String> values = new HashMap<>();
        if (m.matches()) {
            //Debug.logMessage("4o 3a dermo"+m.toString());
            for (int i = 0; i < m.groupCount(); ++i) {
                // parts is sorted, so the order should lign up.
                if (i >= parts.size()) {
                    Debug.logError("Invalid whisper format parsing: " + chatFormat + " for message: " + message);
                    break;
                }
                values.put(parts.get(i), m.group(i + 1));
            }
        }

        if (values.containsKey("{to}")) {
            // Make sure the "to" target is us.
            String toUser = values.get("{to}");
            if (!toUser.equals(ourUsername)) {
                Debug.logInternal("Rejected message since it is sent to " + toUser + " and not " + ourUsername);
                return null;
            }
        }
        List<Character> nickKillingChars = new ArrayList<>(
                Arrays.asList('~', '[', ']', '.', '^', '?', '*', '$', '(', ')', '/', '|', '+'));
        if (values.containsKey("{from}") && values.containsKey("{message}")) {
            String name = values.get("{from}");
            if (name != null) {
                if (name != null && name.strip() != "") {
                    String[] splittedName = name.strip().split(" ");
                    if (splittedName.length > 0) {
                        if (splittedName.length == 1) {
                            name = splittedName[0];
                        } else if (splittedName.length == 2) {//[A-Za-z0-9]
                            name = splittedName[0]; //[бог] _nyaka Красавица :
                        } else if (splittedName.length == 3) {
                            name = splittedName[0]; //[президент] Гений lexa Богач :
                        } else {
                            name = splittedName[0];
                        }
                        for (Character killer : nickKillingChars) {
                            String charr = killer.toString();
                            name = name.replace(charr, "");
                        }

                        //Debug.logMessage("4o nado"+message);
                        MessageResult result = new MessageResult();
                        if (values.containsKey("{starterPrefix}")) {
                            result.starter_prefix = values.get("{starterPrefix}");
                        }
                        if (values.containsKey("{rank}")) {
                            result.rank = values.get("{rank}");
                        }
                        if (values.containsKey("{clan}")) {
                            result.clan = values.get("{clan}");
                        }
                        if (values.containsKey("{team}")) {
                            result.team = values.get("{team}");
                        }
                        if (values.containsKey("{global}")) {
                            result.chat_type = values.get("{global}");
                        }
                        //if (values.containsKey("{rank}")) result.rank = values.get("{rank}");

                        result.server = serverName;
                        result.serverMode = serverMode;
                        result.serverExactPrediction = ExactState;
                        result.from = name;
                        result.message = values.get("{message}");
                        return result;
                    }
                }
            }
        }
        return null;
    }

    public MessageResult receiveChat(AltoClef mod, String ourUsername, String msg, String server, String servermode) {
        String foundMiddlePart = "";
        int index = -1;

        boolean duplicate = (msg.equals(_lastMessage));
        if (duplicate && !_repeatTimer.elapsed()) {
            _repeatTimer.reset();
            // It's probably an actual duplicate. IDK why we get those but yeah.
            return null;
        }

        _lastMessage = msg;
//сначала проверяем находимся ли мы на этом сервере и в этом режиме
        for (String[] format : ButlerConfig.getInstance().chatFormats) {
            if (server.equals(format[0]) && servermode.equals(format[2])) {
                //Debug.logMessage("совпадение всё"+format[0]+format[1]+format[2]);
                MessageResult check = chatParse(ourUsername, format, msg);
                if (check != null) {
                    String user = check.from;
                    String message = check.message;
                    if (user == null || message == null) {
                        break;
                    }
                    return check;
                }
            }
        }
        //теперь проверим только сервер и будем для него перебирать варианты чтобы успешно найти ник и сообщение по шаблону
        for (String[] format : ButlerConfig.getInstance().chatFormats) {
            if (server.equals(format[0])) {
                //Debug.logMessage("совпадение серв"+format[0]+format[1]+format[2]);
                MessageResult check = chatParse(ourUsername, format, msg, "server");
                if (check != null) {
                    String user = check.from;
                    String message = check.message;
                    if (user == null || message == null) {
                        break;
                    }
                    return check;
                }
            }
        }
        //проверим универсальный тип
        for (String[] format : ButlerConfig.getInstance().chatFormats) {
            if ("universal".equals(format[0])) {
                //Debug.logMessage("совпадение юниверс"+format[0]+format[1]+format[2]);
                MessageResult check = chatParse(ourUsername, format, msg, "universal");
                if (check != null) {
                    String user = check.from;
                    String message = check.message;
                    if (user == null || message == null) {
                        break;
                    }
                    return check;
                }
            }
        }
        for (String[] format : ButlerConfig.getInstance().chatFormats) {
            MessageResult check = chatParse(ourUsername, format, msg, "random");
            if (check != null) {
                String user = check.from;
                String message = check.message;
                if (user == null || message == null) {
                    break;
                }
                return check;

            }
        }

        return null;
    }

    public MessageResult receiveMessage(AltoClef mod, String ourUsername, String msg) {
        String foundMiddlePart = "";
        int index = -1;

        boolean duplicate = (msg.equals(_lastMessage));
        if (duplicate && !_repeatTimer.elapsed()) {
            _repeatTimer.reset();
            // It's probably an actual duplicate. IDK why we get those but yeah.
            return null;
        }

        _lastMessage = msg;

        for (String format : ButlerConfig.getInstance().whisperFormats) {
            MessageResult check = tryParse(ourUsername, format, msg);
            if (check != null) {
                String user = check.from;
                String message = check.message;
                if (user == null || message == null) {
                    break;
                }
                return check;
            }
        }

        return null;
    }

    public static class MessageResult {

        public String from;
        public String message;
        public String rank;
        public String serverMode;
        public String server;
        public String clan;
        public String team;
        public String chat_type;
        public String serverExactPrediction;
        public String starter_prefix;

        @Override
        public String toString() {
            return "MessageResult{" +
                    "from='" + from + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}

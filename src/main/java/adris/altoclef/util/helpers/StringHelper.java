package adris.altoclef.util.helpers;

import net.minecraft.text.Text;

public class StringHelper {
    public static String removeMCFormatCodes(String message){
        // Use regex to replace all occurrences of § followed by any single character
        String cleanedMessage = message.replaceAll("§.", "");
        return cleanedMessage;
    }
    public static String mcTextToString(Text message){
        String msg = "";
        StringBuilder result = new StringBuilder();
        result.append(message.getString()); // Gets main content
        //if (!message.getSiblings().isEmpty()) {
        //    result.append(" ").append(message.getSiblings().stream()
        //            .map(Text::getString)
        //            .collect(Collectors.joining(" ")));
        //}
        msg = removeMCFormatCodes(result.toString().trim());
        return msg;
    }
}

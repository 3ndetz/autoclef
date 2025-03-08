package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.ui.MessagePriority;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "Lists all commands");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        StringBuilder result = new StringBuilder();
        result.append("=== Commands HELP: ===\n");
        // mod.log("########## HELP: ##########", MessagePriority.OPTIONAL);
        int padSize = 10;
        for (Command c : AltoClef.getCommandExecutor().allCommands()) {
            StringBuilder line = new StringBuilder();
            //line.append("");
            line.append(c.getName()).append(": ");
            int toAdd = padSize - c.getName().length();
            for (int i = 0; i < toAdd; ++i) {
                line.append(" ");
            }
            line.append(c.getDescription());
            result.append(line.toString()).append("\n");
            // mod.log(line.toString(), MessagePriority.OPTIONAL);
        }
        result.append("=== Command help end ===");
        mod.log(result.toString(), MessagePriority.OPTIONAL);
        // mod.log("###########################", MessagePriority.OPTIONAL);
        finish();
    }
}

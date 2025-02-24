package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AgentCommand extends Command {

    public AgentCommand() {
        super("agent", "Execute agentic command (if python entrypoint started) Usage: `@agent <big command string>`");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        List<String> commandArgs = new ArrayList<>(Arrays.asList(parser.getArgUnits().clone()));
        //String commandName = commandArgs.remove(0);
        String text = String.join(" ", commandArgs);
        if (!text.isBlank()) {
            Debug.logMessage("Executing agent command " + text);
            String result = mod.getInfoSender().executeAgentCommand(text);
            Debug.logMessage("Executing result: " + result);
            finish();
        } else {
            Debug.logWarning("Text is blank!");
            finish();
        }
    }
}
package adris.altoclef.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;

public class TimeoutSpecificCommand extends Command {

    public TimeoutSpecificCommand() throws CommandException {
        super("timeout", "Run command with a specific timeout. Usage: `@timeout <time> <command> [args]`");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        try {
            List<String> commandArgs = new ArrayList<>(Arrays.asList(parser.getArgUnits().clone()));
            String commandTimeString = commandArgs.removeFirst();  // Remove the first argument (time)
            float time = Float.parseFloat(commandTimeString);
            //Debug.logMessage("Running command with timeout" + ": " + String.join(" ", commandArgs));
            mod.setTimeoutTask(time);
            AltoClef.getCommandExecutor().executeWithPrefix(String.join(" ", commandArgs));
            finish();
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommandException("Error while running command: " + e.getMessage());
        }
    }


}

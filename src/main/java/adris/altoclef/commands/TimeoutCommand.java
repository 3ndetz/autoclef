package adris.altoclef.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.Playground;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;

public class TimeoutCommand extends Command {

    public TimeoutCommand() throws CommandException {
        super("t", "Run command with a timeout. Usage: `@t <command> <args>`");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        try {
            List<String> commandArgs = new ArrayList<>(Arrays.asList(parser.getArgUnits().clone()));
            //String commandName = commandArgs.remove(0);
            //Debug.logMessage("Running command with timeout" + ": " + String.join(" ", commandArgs));
            mod.setTimeoutTaskFlag(true);
            AltoClef.getCommandExecutor().executeWithPrefix(String.join(" ", parser.getArgUnits()));
            finish();
        } catch (Exception e) {
            Debug.logMessage("Error while running command: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
}

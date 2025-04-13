package adris.altoclef.commands.multiplayer;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.commandsystem.ItemList;

// TODO
public class CheckBlockCommand extends Command {
    public CheckBlockCommand() throws CommandException {
        super("checkBlock", "Checks if some block is present in render view", new Arg(ItemList.class, "blocks"));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        // get item logic
    }
}
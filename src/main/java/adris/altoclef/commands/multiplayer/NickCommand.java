package adris.altoclef.commands.multiplayer;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;

public class NickCommand extends Command {
    public NickCommand() throws CommandException {
        super("nick", "new nickname (apply after rejoin)", new Arg(String.class, "server", null, 0));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String name = parser.get(String.class);
        if(!AltoClef.changePlayerName(name))
            throw new CommandException("Failed to change player name to " + name + ".");
        finish();
    }

}

package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.entity.KillPlayerTask;
import net.minecraft.entity.Entity;

import java.util.Optional;

import static adris.altoclef.commands.multiplayer.GestureCommand.getValidPlayer;

public class PunkCommand extends Command {
    public PunkCommand() throws CommandException {
        super("punk", "Punk 'em", new Arg(String.class, "playerName"));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String playerName = parser.get(String.class);
        getValidPlayer(mod, playerName); // throw if not valid
        mod.runUserTask(new KillPlayerTask(playerName), this::finish);
    }
}
package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.entity.KillPlayerTask;
import adris.altoclef.tasks.entity.ShootArrowSimpleProjectileTask;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

import static adris.altoclef.commands.multiplayer.GestureCommand.getPlayerTarget;
import static adris.altoclef.tasks.entity.ShootArrowSimpleProjectileTask.canUseRanged;

public class PunkCommand extends Command {
    public PunkCommand() throws CommandException {
        super("punk", "Punk 'em", new Arg(String.class, "playerName"));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String playerName = parser.get(String.class);
        Optional<Entity> player = getPlayerTarget(mod, playerName);
        if (player.isPresent())
            mod.runUserTask(new KillPlayerTask(playerName), this::finish);
        throw new CommandException("Cannot chase player " + playerName + " because he is not exists.");
    }
}
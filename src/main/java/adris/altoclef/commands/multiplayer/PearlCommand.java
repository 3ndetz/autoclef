package adris.altoclef.commands.multiplayer;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.movement.ThrowEnderPearlSimpleProjectileTask;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

import static adris.altoclef.commands.multiplayer.GestureCommand.getPlayerTarget;
import static adris.altoclef.tasks.movement.ThrowEnderPearlSimpleProjectileTask.canEnderpearl;

public class PearlCommand extends Command {
    public PearlCommand() throws CommandException {
        super("pearl", "Tp to player using pearl", new Arg(String.class, "playerName"));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String playerName = parser.get(String.class);
        if (!canEnderpearl(mod)) {
            throw new CommandException("Cannot perform using enderpearl for teleport: Has not any enderpearls in invertory");
        }
        Optional<Entity> player = getPlayerTarget(mod, playerName);
        if (player.isPresent()) {
            BlockPos pos = player.get().getBlockPos();
            if (pos != null) {
                mod.runUserTask(new ThrowEnderPearlSimpleProjectileTask(pos), this::finish);
                finish();
                return;
            }
        }
        throw new CommandException("Cannot perform using enderpearl for teleport: Player " + playerName + " not found.");
    }
}
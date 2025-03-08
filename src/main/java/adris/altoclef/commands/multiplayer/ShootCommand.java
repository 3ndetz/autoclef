package adris.altoclef.commands.multiplayer;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.entity.ShootArrowSimpleProjectileTask;
import adris.altoclef.tasks.movement.ThrowEnderPearlSimpleProjectileTask;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

import static adris.altoclef.commands.multiplayer.GestureCommand.getPlayerTarget;
import static adris.altoclef.tasks.entity.ShootArrowSimpleProjectileTask.canUseRanged;
import static adris.altoclef.tasks.entity.ShootArrowSimpleProjectileTask.readyForRanged;
import static adris.altoclef.tasks.movement.ThrowEnderPearlSimpleProjectileTask.canEnderpearl;

public class ShootCommand extends Command {
    public ShootCommand() throws CommandException {
        super("shoot", "shoot a player", new Arg(String.class, "playerName"));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String playerName = parser.get(String.class);
        if (!readyForRanged(mod)) {
            throw new CommandException("Cannot perform shooting bow: has not any arrows or bow in invertory");
        }
        Optional<Entity> player = getPlayerTarget(mod, playerName);
        if (player.isPresent()) {

            BlockPos pos = player.get().getBlockPos();
            if (pos != null) {

                if (!canUseRanged(mod, player.get())) {
                    throw new CommandException("Cannot perform shooting bow: trajectory is blocked with blocks");
                }
                mod.runUserTask(new ShootArrowSimpleProjectileTask(player.get()), this::finish);
                finish();
                return;
            }
        }
        throw new CommandException("Cannot perform shooting bow: Player " + playerName + " not found.");
    }
}
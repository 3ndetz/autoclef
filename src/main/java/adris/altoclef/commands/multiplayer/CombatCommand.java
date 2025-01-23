package adris.altoclef.commands.multiplayer;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.entity.CombatTask;
import net.minecraft.entity.Entity;

import java.util.Optional;

public class CombatCommand extends Command {
    public CombatCommand() throws CommandException {
        super("combat", "Enter to combat with someone", new Arg(String.class, "username", null, 0));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String username = parser.get(String.class);
        if (username == null) {
            finish();
            return;
        }

        Optional<Entity> entity = getEntityTarget(mod, username);

        if (entity.isEmpty()) {
            mod.logWarning("Player not found.");
            finish();
            return;
        }

        mod.runUserTask(new CombatTask(entity.get()), this::finish);
    }
    private Optional<Entity> getEntityTarget(AltoClef mod, String username){
        if (mod.getEntityTracker().isPlayerLoaded(username)) {
            return mod.getEntityTracker().getPlayerEntity(username).map(Entity.class::cast);
        }
        return Optional.empty();
    }
}
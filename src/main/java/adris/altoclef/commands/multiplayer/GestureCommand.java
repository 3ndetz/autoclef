package adris.altoclef.commands.multiplayer;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.multiplayer.GestureTask;
import net.minecraft.entity.Entity;

import java.util.Optional;

public class GestureCommand extends Command {
    public GestureCommand() throws CommandException {
        super("gesture", "Show gesture to someone", new Arg(String.class, "username"), new Arg(String.class, "gesture"));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String username = parser.get(String.class);
        if (username == null) {
            mod.logWarning("Not specified");
            finish();
            return;
        }

        Optional<Entity> entity = getPlayerTarget(mod, username);

        if (entity.isEmpty()) {
            mod.logWarning("GestureAction: Player " + username + " not found.");
            finish();
            return;
        }
        String gesture_str = parser.get(String.class);
        GestureTask.Gesture gesture = GestureTask.Gesture.Hey;;
        if (gesture_str != null) {
            mod.log("Gesture: " + gesture_str);
            try {
                gesture = GestureTask.Gesture.valueOf(gesture_str);
            } catch (IllegalArgumentException e) {
                mod.logWarning("GestureAction: Invalid gesture: " + gesture_str);
            }
        }
        mod.runForcedTask(new GestureTask(entity.get(), gesture), 3);
    }
    public static Optional<Entity> getPlayerTarget(AltoClef mod, String username){
        if (mod.getEntityTracker().isPlayerLoaded(username)) {
            return mod.getEntityTracker().getPlayerEntity(username).map(Entity.class::cast);
        }
        return Optional.empty();
    }
}
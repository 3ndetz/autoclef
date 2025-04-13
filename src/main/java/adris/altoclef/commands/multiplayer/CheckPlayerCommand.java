package adris.altoclef.commands.multiplayer;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.movement.FollowPlayerTask;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class CheckPlayerCommand extends Command {
    public CheckPlayerCommand() throws CommandException {
        super("checkPlayer", "Checks the status of some player", new Arg(String.class, "username", null, 0));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String username = parser.get(String.class);
        if (username == null) {
            if (mod.getButler().hasCurrentUser()) {
                username = mod.getButler().getCurrentUser();
            } else {
                mod.logWarning("check player failed: No butler user "
                        + username
                        + " currently present. Running this command with no user argument can ONLY be done via butler.");
                finish();
                return;
            }
        }
        Optional<Vec3d> lastPos = mod.getEntityTracker().getPlayerMostRecentPosition(username);

        if (lastPos.isEmpty()) {
            throw new CommandException("check player failed: Player " + username + " not found.");
        }
        mod.log("Player " + username + " is found at " + lastPos.get().toString() + ".");
        // TODO get more info about player
    }
}
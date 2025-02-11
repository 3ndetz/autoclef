package adris.altoclef.commands.multiplayer;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.entity.ShiftEntityTask;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import java.util.Optional;

public class ShiftCommand extends Command {
    public ShiftCommand() throws CommandException {
        super("shift", "Shifts near a player with optional type (back/forward/any)", 
            new Arg<>(String.class, "username"),
            new Arg<>(String.class, "type", "back", 1));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String username = parser.get(String.class);
        String type = parser.get(String.class);
        
        if (username == null) {
            finish();
            return;
        }

        Optional<PlayerEntity> target = mod.getEntityTracker().getPlayerEntity(username);
        if (target.isEmpty()) {
            mod.logWarning("Player " + username + " not found!");
            finish();
            return;
        }

        ShiftEntityTask.ShiftType shiftType = switch (type.toLowerCase()) {
            case "back" -> ShiftEntityTask.ShiftType.Back;
            case "forward" -> ShiftEntityTask.ShiftType.Forward;
            default -> ShiftEntityTask.ShiftType.Any;
        };

        mod.runUserTask(new ShiftEntityTask(target.get(), shiftType), this::finish);
    }
}
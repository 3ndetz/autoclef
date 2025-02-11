package adris.altoclef.commands.multiplayer;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.entity.CombatTask;
import net.minecraft.entity.player.PlayerEntity;
import adris.altoclef.util.helpers.WorldHelper;

public class CombatCommand extends Command {
    public CombatCommand() throws CommandException {
        super("combat", "Combat mode with configurable options",
            new Arg<>(String.class, "mode", "all", 0),
            new Arg<>(String.class, "target", null, 0),
            new Arg<>(String.class, "graves", "on", 0),
            new Arg<>(String.class, "gestures", "on", 0)
        );
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String mode = parser.get(String.class);
        String target = parser.get(String.class);
        boolean buildGraves = parser.get(String.class).equalsIgnoreCase("on");
        boolean useGestures = parser.get(String.class).equalsIgnoreCase("on");

        if (mode.equalsIgnoreCase("target")) {
            if (target == null || target.isEmpty()) {
                throw new CommandException("Target name required in target mode");
            }
            mod.runUserTask(new CombatTask(target, buildGraves, useGestures), this::finish);
        } else if (mode.equalsIgnoreCase("all")) {
            mod.runUserTask(new CombatTask(
                entity -> entity instanceof PlayerEntity && !entity.equals(mod.getPlayer()),
                buildGraves,
                useGestures
            ), this::finish);
        } else {
            throw new CommandException("Invalid mode. Use 'all' or 'target'");
        }
    }
}
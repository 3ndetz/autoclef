package adris.altoclef.commands.multiplayer;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.movement.FollowPlayerTask;
import adris.altoclef.tasks.movement.IdleTask;

public class PursueCommand extends Command {
    public PursueCommand() throws CommandException {
        super("pursue", "Pursues someone", new Arg(String.class, "username", null, 0));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String username = parser.get(String.class);
        if (username == null) {
            finish();
            return;
        }
        mod.getInfoSender().attackPlayer(username);
        if(!mod.getTaskRunner().getCurrentTaskChain().isActive()){
            mod.runUserTask(new IdleTask());
        }

    }
}

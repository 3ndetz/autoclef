package adris.altoclef.commands.multiplayer;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;

public class ForgetCommand extends Command {

    public ForgetCommand() {
        super("forget", "Forgets all avoiding and attacking players");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        mod.getDamageTracker().getThreatTable().forget();  //.clearWorldData();
        finish();
    }
}


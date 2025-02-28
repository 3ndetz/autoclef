package adris.altoclef.commands.multiplayer;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;

public class ConnectCommand extends Command {
    public ConnectCommand() throws CommandException {
        super("connect", "connects to server by <ip>[:port]", new Arg(String.class, "server", null, 0));
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        String server = parser.get(String.class);
        if (server == null) {
            finish();
            return;
        }
        Debug.logMessage("!!! REQUESTED SERVER RECONNECT: from " + mod.getGameMenuTaskChain().getServerIp() + " to " + server);
        mod.getGameMenuTaskChain().connectToServer(server);
        finish();
    }
}

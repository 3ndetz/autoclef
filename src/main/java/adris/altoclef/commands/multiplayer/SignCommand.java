package adris.altoclef.commands.multiplayer;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.construction.PlaceSignTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignCommand extends Command {

    public SignCommand() {
        super("sign", "Place sign with text. Usage: `@sign any text`");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        List<String> commandArgs = new ArrayList<>(Arrays.asList(parser.getArgUnits().clone()));
        //String commandName = commandArgs.remove(0);
        String text = String.join(" ", commandArgs);
        if (!text.isBlank()) {
            Debug.logMessage("Placing sign nearby with text: " + text);
            mod.runUserTask(new PlaceSignTask(text), this::finish);
        } else {
            Debug.logWarning("Text is blank!");
            finish();
        }
    }
}

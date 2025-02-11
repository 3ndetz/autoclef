package adris.altoclef.commands.multiplayer;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.construction.PlaceSignTask;
import adris.altoclef.tasks.construction.compound.ConstructGraveTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GraveCommand extends Command {

    public GraveCommand() {
        super("grave", "Place grave with text. Usage: `@grave any text`");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        List<String> commandArgs = new ArrayList<>(Arrays.asList(parser.getArgUnits().clone()));
        //String commandName = commandArgs.remove(0);
        String text = String.join(" ", commandArgs);
        if (!text.isBlank()) {
            Debug.logMessage("Constructing grave with text: " + text);
            mod.runUserTask(new ConstructGraveTask(text), this::finish);
        } else {
            Debug.logWarning("Text is blank!");
            finish();
        }
    }
}

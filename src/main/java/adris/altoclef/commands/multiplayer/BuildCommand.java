package adris.altoclef.commands.multiplayer;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.construction.PlaceSignTask;
import adris.altoclef.tasks.construction.compound.ConstructGraveTask;
import adris.altoclef.tasks.construction.compound.ConstructIronGolemTask;
import adris.altoclef.tasks.construction.compound.ConstructNetherPortalBucketTask;
import adris.altoclef.tasks.misc.PlaceBedAndSetSpawnTask;
import adris.altoclef.util.baritone.PlaceBlockSchematic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/*
* TODO:
*  implement
* build <structure> [text / args]
*
* for example, build grave with text "Hello world!"
* @build grave Hello world!
* for example, build "pencil" structure
* @build pencil
* TODO add schematics. Deprecated //mod.runUserTask(new PlaceBlockSchematic(), this::finish);
*/
public class BuildCommand extends Command {

    public BuildCommand() {
        super("build", "Build a specific structure");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        List<String> commandArgs = new ArrayList<>(Arrays.asList(parser.getArgUnits().clone()));
        if (commandArgs.isEmpty()) {
            Debug.logWarning("No arguments provided!");
            finish();
            return;
        }

        String commandName = commandArgs.removeFirst().toLowerCase();

        switch (commandName) {
            case "grave", "sign":

                String text = String.join(" ", commandArgs);
                if (!text.isBlank()) {
                    Debug.logMessage("Constructing " + commandName + " with text: " + text);
                    if (commandName.equals("grave"))
                        mod.runUserTask(new ConstructGraveTask(text), this::finish);
                    else
                        mod.runUserTask(new PlaceSignTask(text), this::finish);
                } else {
                    Debug.logWarning("Text is blank!");
                }
                break;
            case "golem":
                Debug.logMessage("Constructing iron golem!");
                mod.runUserTask(new ConstructIronGolemTask(), this::finish);
                break;
            case "bed":
                Debug.logMessage("Constructing bed!");
                mod.runUserTask(new PlaceBedAndSetSpawnTask(), this::finish);
                break;
            case "portal":
                Debug.logMessage("Constructing portal!");
                mod.runUserTask(new ConstructNetherPortalBucketTask(), this::finish);
                break;
            default:
                Debug.logWarning("Unknown structure!");
                finish();
                break;
        }


    }
}

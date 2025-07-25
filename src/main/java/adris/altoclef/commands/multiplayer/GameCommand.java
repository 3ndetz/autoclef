package adris.altoclef.commands.multiplayer;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.multiplayer.minigames.BedWarsTask;
import adris.altoclef.tasks.speedrun.BeatMinecraft2Task;
import adris.altoclef.tasks.multiplayer.minigames.BattleRoyaleTask;
import adris.altoclef.tasks.multiplayer.minigames.MurderMysteryTask;
import adris.altoclef.tasks.multiplayer.minigames.SkyWarsTask;
import adris.altoclef.util.agent.Pipeline;
import net.minecraft.util.math.BlockPos;

public class GameCommand extends Command {
    public GameCommand() throws CommandException {
        super("game", "Run the main game or minigame pipeline (task chain)", new Arg<>(String.class, "pipeline", "", 0));
    }
    @Override
    protected void call(AltoClef mod, ArgParser parser) throws CommandException {
        // walkthrough, sw, mm, megabattle
        String pipelineStr = parser.get(String.class);
        if (pipelineStr == null || pipelineStr.isBlank()) {
            Debug.logMessage("Pipeline set to None");
            AltoClef._pipeline = Pipeline.None;
            finish();
            return;
        }
        pipelineStr = pipelineStr.toLowerCase();

        switch (pipelineStr) {
            case "none", "no":
                Debug.logMessage("Pipeline set to None");
                AltoClef._pipeline = Pipeline.None;
                break;
            case "swt", "skywarsteam", "sky_wars_team":
                AltoClef._pipeline = Pipeline.SkyWars;
                Debug.logMessage("Pipeline set to swt");
                mod.getButler().AddNearestPlayerToFriends(mod, 15);
                mod.runUserTask(new SkyWarsTask(new BlockPos(0,0,0),0d,false), this::finish);
                break;
            case "sw", "skywars", "sky_wars":
                AltoClef._pipeline = Pipeline.SkyWars;
                Debug.logMessage("Pipeline set to sw");
                mod.runUserTask(new SkyWarsTask(new BlockPos(0,0,0),0d,false), this::finish);
                break;
            case "mm", "murder", "murdermystery", "mystery":
                AltoClef._pipeline = Pipeline.MurderMystery;
                Debug.logMessage("Pipeline set to mm");
                mod.runUserTask(new MurderMysteryTask(-1), this::finish);
                break;
            case "bw", "bed", "bedwars":
                AltoClef._pipeline = Pipeline.BedWars;
                Debug.logMessage("Pipeline set to bw");
                mod.runUserTask(new BedWarsTask(mod), this::finish);
                break;
            case "megabattle", "mega", "evil", "yandere":
                AltoClef._pipeline = Pipeline.BattleRoyale;
                Debug.logMessage("Pipeline set yandere");
                mod.runUserTask(new BattleRoyaleTask(), this::finish);
                break;
            default:
                // may be MarvionBeatMinecraftTask
                Debug.logMessage("Pipeline set to speedrun");
                AltoClef._pipeline = Pipeline.SpeedRun;
                mod.runUserTask(new BeatMinecraft2Task(), this::finish);
                break;
        }
    }
}

package adris.altoclef.commands.multiplayer;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.commandsystem.Arg;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.commandsystem.CommandException;
import adris.altoclef.tasks.speedrun.BeatMinecraft2Task;
import adris.altoclef.tasks.stupid.BattleRoyaleTask;
import adris.altoclef.tasks.stupid.MurderMysteryTask;
import adris.altoclef.tasks.stupid.SkyWarsTask;
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
            case "swt", "skywarsteam", "sky_wars_team":
                AltoClef._pipeline = Pipeline.SkyWars;
                mod.getButler().AddNearestPlayerToFriends(mod, 15);
                mod.runUserTask(new SkyWarsTask(new BlockPos(0,0,0),0d,false), this::finish);
                break;
            case "sw", "skywars", "sky_wars":
                AltoClef._pipeline = Pipeline.SkyWars;
                mod.runUserTask(new SkyWarsTask(new BlockPos(0,0,0),0d,false), this::finish);
                break;
            case "mm", "murder", "murdermystery", "mystery":
                AltoClef._pipeline = Pipeline.MurderMystery;
                mod.runUserTask(new MurderMysteryTask(-1), this::finish);
                break;
            case "megabattle", "mega", "evil", "yandere":
                AltoClef._pipeline = Pipeline.BattleRoyale;
                mod.runUserTask(new BattleRoyaleTask(), this::finish);
                break;
            default:
                // may be MarvionBeatMinecraftTask
                AltoClef._pipeline = Pipeline.SpeedRun;
                mod.runUserTask(new BeatMinecraft2Task(), this::finish);
                break;
        }
    }
}

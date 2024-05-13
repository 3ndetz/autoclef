package adris.altoclef.util.helpers;
import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;

public abstract class TimersHelper {
    private static final TimerGame _chestInteractTimer = new TimerGame(0.7);
    public static void ChestInteractTimerReset(){
        _chestInteractTimer.reset();
    }
    public static boolean CanChestInteract(){
        return _chestInteractTimer.elapsed();
    }
}

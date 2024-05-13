package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import adris.altoclef.util.helpers.LookHelper;
import baritone.api.utils.input.Input;

/**
 * Will move around randomly while holding shift
 * Used to escape weird situations where baritone doesn't work.
 */
public class LobbyMoveTask extends Task {

    private final TimerGame _lookTimer;
    private boolean _elapsed = false;

    public LobbyMoveTask(float randomLookInterval) {
        _lookTimer  = new TimerGame(randomLookInterval);
    }
    public LobbyMoveTask() {
        this(5);
    }

    @Override
    protected void onStart(AltoClef mod) {
        _lookTimer.reset();
    }

    @Override
    protected Task onTick(AltoClef mod) {

        if (_lookTimer.elapsed()) {
            Debug.logMessage("Двигаемся в лобби...");
            _lookTimer.reset();
            _elapsed = true;
            //LookHelper.randomOrientation(mod);
        }

        mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_BACK, true);
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_BACK, false);
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return _elapsed;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof LobbyMoveTask;
    }

    @Override
    protected String toDebugString() {
        return "Движение по лобби";
    }
}

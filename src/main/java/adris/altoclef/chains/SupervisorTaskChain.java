package adris.altoclef.chains;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.movement.MLGBucketTask;
import adris.altoclef.tasks.movement.ThrowEnderPearlSimpleProjectileTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.time.TimerGame;
import adris.altoclef.util.time.TimerReal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.util.math.BlockPos;

/*
* PRIORETIZED TASK CHAIN
*
* */
@SuppressWarnings("ALL")
public class SupervisorTaskChain extends SingleTaskChain {

    private final TimerGame _tryCollectWaterTimer = new TimerGame(4);
    private final TimerGame _pickupRepeatTimer = new TimerGame(0.25);
    public TimerReal _taskTimer = new TimerReal(30);
    private MLGBucketTask _lastMLG = null;
    private ThrowEnderPearlSimpleProjectileTask _lastEP = null;

    private boolean _wasPickingUp = false;
    private boolean _doingChorusFruit = false;
    private BlockPos _lastGroundBlockPos;
    private final TimerGame _voidFallTimer = new TimerGame(0.25);
    private TimerGame GestureTimer = new TimerGame(3);
    private boolean _active = true;

    public SupervisorTaskChain(TaskRunner runner) {
        super(runner);
    }

    @Override
    protected void onTaskFinish(AltoClef mod) {
        //_lastMLG = null;
        // UNTESTED
        if (!mod.getUserTaskChain().isActive() && !this.isActive()) {
            // Stop.
            mod.getTaskRunner().disable();
            // Extra reset. Sometimes baritone is laggy and doesn't properly reset our press
            mod.getClientBaritone().getInputOverrideHandler().clearAllKeys();
        }
    }

    public boolean shouldAutoReconnect(AltoClef mod){return true;}

    @Override
    public float getPriority(AltoClef mod) {
        if (!AltoClef.inGame()) {
            // main game menu
            return Float.NEGATIVE_INFINITY;
        }

        if (getCurrentTask() != null) {
            // TODO untested
            if (getCurrentTask().stopped()
                    //|| !getCurrentTask().isActive()
                    || getCurrentTask().isFinished(mod)
                    || _taskTimer.elapsed()){
                setTask(null);
            } else {
                return 51f;
            }
        }
        return Float.NEGATIVE_INFINITY;
    }


    public void runTask(AltoClef mod, Task task, double time) {
        if (task != null) {
            Debug.logMessage("[SUDO] Задача поставлена: " + task.toString()); //TRS "User Task Set: "
        }
        //mod.getTaskRunner().enable();
        setTask(task);
        _taskTimer.setInterval(time);
        _taskTimer.reset();

        if (mod.getModSettings().failedToLoad()) {
            Debug.logWarning("Settings file failed to load at some point. Check logs for more info, or delete the" +
                    " file to re-load working settings.");
        }
    }
    public void runTask(AltoClef mod, Task task) {
        runTask(mod, task, 30);
    }

    @Override
    public String getName() {
        return "Forced Task Chain";
    }

    @Override
    public boolean isActive() {
        return _active;
    }
}


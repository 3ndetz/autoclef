package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class FollowPlayerTask extends Task {

    public final String _playerName;
    public double CLOSE_ENOUGH_DIST = 2;
    public boolean IDLE_TASK_AFTER_REACH = true;

    public FollowPlayerTask(String playerName) {
        _playerName = playerName;
    }

    public FollowPlayerTask(String playerName, double closeEnoughDist) {
        _playerName = playerName;
        CLOSE_ENOUGH_DIST = closeEnoughDist;
        IDLE_TASK_AFTER_REACH = false;
    }

    @Override
    protected void onStart(AltoClef mod) {

    }

    @Override
    protected Task onTick(AltoClef mod) {

        Optional<Vec3d> lastPos = mod.getEntityTracker().getPlayerMostRecentPosition(_playerName);

        if (lastPos.isEmpty()) {
            //setDebugState("No player found/detected. Doing nothing until player loads into render distance.");
            setDebugState("NO THIS PLAYER ON SERVER! CHANGE TARGET NOW!!!");
            // WHAT THE F...???
            // FPS DROPPING HERE TO 0000000
            // WHYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY ?
            stop(mod);
            return null;
        }
        Vec3d target = lastPos.get();

        if (target.isInRange(mod.getPlayer().getPos(), CLOSE_ENOUGH_DIST) && !mod.getEntityTracker().isPlayerLoaded(_playerName)) {
            mod.logWarning("Failed to get to player \"" + _playerName + "\". We moved to where we last saw them but now have no idea where they are.");
            stop(mod);
            return null;
        }

        Optional<PlayerEntity> player = mod.getEntityTracker().getPlayerEntity(_playerName);
        if (player.isEmpty()) {
            // Go to last location
            //setDebugState("Player entity not found, going to his last position... (probably there is just NO this player, you can change target)");
            setDebugState("Going to last position of player was, but you should CHANGE TARGET because it may be OFFLINE");
            return new GetCloseToBlockTask(new BlockPos((int) target.x, (int) target.y, (int) target.z));
        }

        if (player.get().distanceTo(mod.getPlayer()) <= CLOSE_ENOUGH_DIST) {
            setDebugState("Target follow finished.");
            //return new IdleTask();
            return null;
        }
        setDebugState("Trying to approach target...");
        return new GetToEntityTask(player.get(), 0);
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof FollowPlayerTask task) {
            return task._playerName.equals(_playerName) && task.CLOSE_ENOUGH_DIST == CLOSE_ENOUGH_DIST;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Going to player " + _playerName;
    }
}

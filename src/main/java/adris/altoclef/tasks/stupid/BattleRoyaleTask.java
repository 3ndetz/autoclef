package adris.altoclef.tasks.stupid;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.DeathEvent;
import adris.altoclef.tasks.entity.CombatTask;
import adris.altoclef.tasks.entity.DoToClosestEntityTask;
import adris.altoclef.tasks.entity.KillPlayerTask;
import adris.altoclef.tasks.multiplayer.GestureTask;
import adris.altoclef.tasks.construction.compound.ConstructGraveTask;
import adris.altoclef.trackers.threats.PlayerThreat;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.time.TimerGame;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 * Battle royale task that:
 * - Tracks all players as potential targets
 * - Finds most preferable target and executes combat
 * - Prioritizes closer targets with lower health
 * - Properly stops pursuing unreachable targets
 * - Builds graves for defeated targets with 50% chance if conditions met
 */
public class BattleRoyaleTask extends Task {
    
    public BattleRoyaleTask() {
    }
    
    @Override
    protected void onStart(AltoClef mod) {
    }

    @Override 
    protected Task onTick(AltoClef mod) {
        return new CombatTask();
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof BattleRoyaleTask;
    }

    @Override
    protected String toDebugString() {
        return "Battle Royale Mode";
    }
}

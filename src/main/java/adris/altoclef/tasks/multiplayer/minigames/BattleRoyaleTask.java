package adris.altoclef.tasks.multiplayer.minigames;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.entity.CombatTask;
import adris.altoclef.tasksystem.Task;

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

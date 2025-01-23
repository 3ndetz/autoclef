package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.progresscheck.MovementProgressChecker;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.EntityHelper;
import baritone.api.utils.input.Input;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Optional;

import static adris.altoclef.tasks.entity.ShootArrowSimpleProjectileTask.canUseRanged;

public class CombatTask extends Task {

    private static final double MELEE_RANGE = 7.0;
    private static final double BOW_RANGE = 100.0;
    private static final double PREFERRED_BOW_RANGE = 10.0;

    private final Entity _target;
    private final MovementProgressChecker _progress = new MovementProgressChecker();
    private boolean _wasRanged = false;

    public CombatTask(Entity target) {
        _target = target;
    }

    @Override
    protected void onStart(AltoClef mod) {
        mod.getBehaviour().push();
        // Allow us to look far away at our target
        mod.getBehaviour().setForceFieldPlayers(false);
        mod.getBehaviour().allowSwimThroughLava(true);
        _progress.reset();
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (_target == null || _target.isRemoved() || !_target.isAlive()) {
            return null;
        }
        double dist = _target.distanceTo(mod.getPlayer());
        // Decide combat strategy
        boolean useRanged =  dist > MELEE_RANGE &&
                dist < BOW_RANGE;
        _wasRanged = useRanged;

        if (useRanged && canUseRanged(mod, _target)) {
            return new ShootArrowSimpleProjectileTask(_target);
        } else {
            return new KillEntityTask(_target);
        }


    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getBehaviour().pop();
        mod.getInputControls().release(Input.CLICK_LEFT);
        mod.getInputControls().release(Input.CLICK_RIGHT);
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof CombatTask task) {
            return task._target.equals(_target);
        }
        return false;
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return _target == null || _target.isRemoved() || !_target.isAlive();
    }

    @Override
    protected String toDebugString() {
        return "Fighting " + _target.getName().getString();
    }
}
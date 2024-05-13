package adris.altoclef.tasks.fix;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.movement.GetToXZTask;
import adris.altoclef.tasks.stupid.TerminatorTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class StuckFixingTask extends Task {
    int x=0;
    int z=0;
    public StuckFixingTask() {
        Debug.logMessage("[STUCK FIX] STUCK FIXING; COORDS: x="+x+";z="+z);
        x = ThreadLocalRandom.current().nextInt(-30, 30 + 1);
        z = ThreadLocalRandom.current().nextInt(-30, 30 + 1);
    }

    @Override
    protected void onStart(AltoClef mod) {
        mod.getBehaviour().push();

    }

    @Override
    protected Task onTick(AltoClef mod) {
        return new GetToXZTask(x,z);
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        BlockPos cur = mod.getPlayer().getBlockPos();
        return (cur.getX() == x && cur.getZ() == z);
    }
    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Надоело; надо успокоиться";
    }
}

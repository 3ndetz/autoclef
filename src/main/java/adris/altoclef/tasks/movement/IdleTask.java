package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.Playground;
import adris.altoclef.tasksystem.Task;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Do nothing.
 */
public class IdleTask extends Task {
    @Override
    protected void onStart(AltoClef mod) {
    }

    @Override
    protected Task onTick(AltoClef mod) {
        // Do nothing except maybe test code
        Playground.IDLE_TEST_TICK_FUNCTION(mod);
        return null;
    }

    protected Optional<LivingEntity> getEntityTarget(AltoClef mod, String plyName) {
        if (mod.getEntityTracker().isPlayerLoaded(plyName)) {
            return mod.getEntityTracker().getPlayerEntity(plyName).map(LivingEntity.class::cast);
        }
        return Optional.empty();
    }
    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        // Never finish
        return false;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof IdleTask;
    }

    @Override
    protected String toDebugString() {
        return "Idle";
    }
}

package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.Playground;
import adris.altoclef.tasksystem.Task;
import java.util.Optional;

import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.time.TimerGame;
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
    public TimerGame _lookTimer = new TimerGame(3);

    @Override
    protected Task onTick(AltoClef mod) {
        // Do nothing except maybe test code
        //Playground.IDLE_TEST_TICK_FUNCTION(mod);
        // Look at closest player in 10 blocks if present
        if ((_lookTimer.elapsed() || _lookTimer.getDuration() < 2f) && mod.getPlayer() != null) {
            mod.getEntityTracker().getClosestEntity(PlayerEntity.class).ifPresent(player -> {
                if (mod.getPlayer().distanceTo(player) < 20) {
                    setDebugState("Staring at near player: " + player.getName().getString());
                    LookHelper.smoothLookAt(mod, player);
                    if (_lookTimer.elapsed())
                        _lookTimer.reset();
                }
            });
            mod.getEntityTracker().getClosestEntity(LivingEntity.class).ifPresent(living -> {
                if (mod.getPlayer().distanceTo(living) < 10) {
                    setDebugState("Staring at near entity: " + living.getType().getName().getString());
                    LookHelper.smoothLookAt(mod, living);
                    if (_lookTimer.elapsed())
                        _lookTimer.reset();
                }
            });
        }

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
        return "NO TASKS: waiting for input action";
    }
}

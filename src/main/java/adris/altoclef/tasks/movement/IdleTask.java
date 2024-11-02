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
        Debug.logMessage("(EZZ: started)");
    }

    @Override
    protected Task onTick(AltoClef mod) {
        // Do nothing except maybe test code
        Playground.IDLE_TEST_TICK_FUNCTION(mod);
        //DamageSource dmgs = mod.getPlayer().getRecentDamageSource();
        ClientPlayerEntity bot = mod.getPlayer();
        LivingEntity living = (LivingEntity) bot;

        PlayerEntity srvply = (PlayerEntity) bot;
        if(srvply.getAttacker() != null)
            Debug.logMessage("УРААААААУ *****");
        LivingEntity att = living.getAttacker();
        Optional<LivingEntity> checkTracking = getEntityTarget(mod,"HyperMozgh");
        LivingEntity TrackingEnt;
        if (MinecraftClient.getInstance().player.getServer() != null)
            Debug.logMessage("STRING SERVER"+MinecraftClient.getInstance().player.getServer().toString());
        if(checkTracking.isPresent()) {
            TrackingEnt = checkTracking.get();
            LivingEntity attacked = TrackingEnt.getAttacking();
            if (attacked != null) {
                Debug.logMessage("Тракер атаковал " + attacked.toString());
                if (attacked.getRecentDamageSource() != null && attacked.getRecentDamageSource().getSource() != null)
                    Debug.logMessage("Тракер атаковал " + attacked.toString());
            }
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
        Debug.logMessage("(EZZ: stopped)");
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

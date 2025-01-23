package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.movement.ThrowEnderPearlSimpleProjectileTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.progresscheck.IProgressChecker;
import adris.altoclef.util.progresscheck.LinearProgressChecker;
import adris.altoclef.util.progresscheck.ProgressCheckerRetry;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.entity.Entity;

import java.util.Optional;

import static adris.altoclef.tasks.entity.ShootArrowSimpleProjectileTask.canUseRanged;
import static adris.altoclef.tasks.movement.ThrowEnderPearlSimpleProjectileTask.shouldEnderpearl;

/**
 * Kill a player given their username
 */
public class KillPlayerTask extends AbstractKillEntityTask {

    public final String _playerName;
    private final double AUTO_RANGED_DISTANCE = 100;
    private final double AUTO_PEARL_DISTANCE = 100;
    private final TimerGame _pearlTimer = new TimerGame(10);
    private final TimerGame _bowTimer = new TimerGame(10);
    private final TimerGame _rangedTimer = new TimerGame(10);
    private Task specialKillTask;

    private final IProgressChecker<Double> _distancePlayerCheck = new ProgressCheckerRetry<>(new LinearProgressChecker(5, -2), 3);

    public KillPlayerTask(String name) {
        super(7, 1);
        _playerName = name;
    }

    @Override
    protected Task onTick(AltoClef mod) {
        // If we're closer to the player, our task isn't bad.
        Optional<Entity> player = getEntityTarget(mod);
        if (player.isEmpty()) {
            _distancePlayerCheck.reset();
        } else {
            double distSq = player.get().squaredDistanceTo(mod.getPlayer());
            if (distSq < 10 * 10) {
                _distancePlayerCheck.reset();
            } else {
                // UNTESTED!!!
                if (specialKillTask != null && specialKillTask.isActive() && !specialKillTask.isFinished(mod) && !_rangedTimer.elapsed()) {
                    return specialKillTask;
                } else {
                    specialKillTask = null;
                }
                if (distSq < AUTO_RANGED_DISTANCE * AUTO_RANGED_DISTANCE) {
                    // shoot bow!
                    if (canUseRanged(mod, player.get())) {
                        if (_bowTimer.elapsed()) {
                            _bowTimer.reset();
                            _rangedTimer.reset();
                            specialKillTask = new ShootArrowSimpleProjectileTask(player.get());
                        }
                    } else if (distSq < AUTO_PEARL_DISTANCE * AUTO_PEARL_DISTANCE) {
                        if (shouldEnderpearl(mod, player.get())){
                            if (_pearlTimer.elapsed()) {
                                _pearlTimer.reset();
                                _rangedTimer.reset();
                                specialKillTask = new ThrowEnderPearlSimpleProjectileTask(player.get().getBlockPos());
                            }
                        }
                    }
                }
                if (specialKillTask != null) {
                    return specialKillTask;
                }
            }
            _distancePlayerCheck.setProgress(-1 * distSq);
            if (!_distancePlayerCheck.failed()) {
                _progress.reset();
            }
        }
        return super.onTick(mod);
    }

    @Override
    protected boolean isSubEqual(AbstractDoToEntityTask other) {
        if (other instanceof KillPlayerTask task) {
            return task._playerName.equals(_playerName);
        }
        return false;
    }

    @Override
    protected Optional<Entity> getEntityTarget(AltoClef mod) {
        if (mod.getEntityTracker().isPlayerLoaded(_playerName)) {
            return mod.getEntityTracker().getPlayerEntity(_playerName).map(Entity.class::cast);
        }
        return Optional.empty();
    }

    @Override
    protected String toDebugString() {
        return "Entering combat with: " + _playerName;
    }
}

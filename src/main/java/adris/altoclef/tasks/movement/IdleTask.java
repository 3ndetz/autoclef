package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.Playground;
import adris.altoclef.tasksystem.Task;
import java.util.Optional;
import java.util.Random;

import adris.altoclef.tasks.multiplayer.GestureTask;
import adris.altoclef.tasks.multiplayer.GestureTask.Gesture;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Do nothing.
 */
public class IdleTask extends Task {
    @Override
    protected void onStart(AltoClef mod) {
    }
    public TimerGame _lookTimer = new TimerGame(3);
    private TimerGame _gestureTimer = new TimerGame(8);
    private Task _currentGesture = null;
    private Random random = new Random();
    public Vec3d _lastLookPos;
    private String _currentTargetName = null;
    private int _currentPosStrategy = -1; // -1 means unset

    @Override
    protected Task onTick(AltoClef mod) {
        Optional<Entity> chillEntity = Optional.empty();
        // Do nothing except maybe test code
        //Playground.IDLE_TEST_TICK_FUNCTION(mod);
        // Look at closest player in 10 blocks if present
        if (!AltoClef.isManualInputFound() && mod.getPlayer() != null && mod.getPlayer().getPos() != null) {
            // Handle current gesture if active
            if (_currentGesture != null) {
                if (!_currentGesture.isFinished(mod)) {
                    return _currentGesture;
                }
                _currentGesture = null;
                _gestureTimer.reset();
            }
            if (_lastLookPos == null)
                _lastLookPos = mod.getPlayer().getPos();
            
            boolean get_from_agent_state = mod.getInfoSender().getCallbackServerStatusFast() && mod.getInfoSender().getState() != null;
            // Regular looking behavior
            if (get_from_agent_state){
                Optional<PlayerEntity> findPlayer = mod.getEntityTracker().getPlayerEntity(mod.getInfoSender().getState().focusPlayerName);
                if (findPlayer.isPresent()) {
                    chillEntity = Optional.of(findPlayer.get());
                }
            }
            if (chillEntity.isEmpty())
                chillEntity = mod.getEntityTracker().getClosestEntity(
                    entity -> entity != null && entity.isAlive() &&
                            entity.distanceTo(mod.getPlayer()) < 10,
                    PlayerEntity.class, AnimalEntity.class);

            // Maybe start a new gesture
            float gestureChance;
            if (chillEntity.isPresent()) gestureChance = 0.7f; else gestureChance = 0.3f;
            if (_gestureTimer.elapsed()) {
                if (get_from_agent_state) {
                    _currentGesture = new GestureTask(_lastLookPos, mod.getInfoSender().getState().getGesture());
                } else { // random gesture
                    if (random.nextFloat() < gestureChance) { // 30% chance to start gesture
                        GestureTask.Gesture[] idleGestures = {
                                GestureTask.Gesture.Sad,
                                GestureTask.Gesture.BrawlStars,
                                GestureTask.Gesture.Crazy,
                                GestureTask.Gesture.Disrespect
                        };
                        _currentGesture = new GestureTask(_lastLookPos, idleGestures[random.nextInt(idleGestures.length)]);
                    }
                }
                _gestureTimer.reset();
            }

            if ((_lookTimer.elapsed() || _lookTimer.getDuration() < 0.5f)) {
                if (chillEntity.isPresent() && chillEntity.get() instanceof LivingEntity entity){
                        if (entity instanceof PlayerEntity player){
                            String playerName = player.getName().getString();
                            setDebugState("Staring at near player: " + playerName);
                            
                            // Only choose new strategy when target changes
                            if (!playerName.equals(_currentTargetName)) {
                                _currentTargetName = playerName;
                                float chance = random.nextFloat();
                                if (chance < 0.1f) { // 10% chance
                                    _currentPosStrategy = 0; // body pos
                                } else if (chance < 0.55f) { // 45% chance (0.55 - 0.1)
                                    _currentPosStrategy = 1; // eye pos
                                } else { // 45% chance
                                    _currentPosStrategy = 2; // center pos
                                }
                            }

                            // Use current strategy
                            switch (_currentPosStrategy) {
                                case 0:
                                    _lastLookPos = player.getPos();
                                    break;
                                case 1:
                                    _lastLookPos = player.getEyePos();
                                    break;
                                case 2:
                                    _lastLookPos = player.getBoundingBox().getCenter();
                                    break;
                                default:
                                    _lastLookPos = player.getEyePos(); // fallback
                            }
                        } else {
                            _currentTargetName = null; // Reset for non-player entities
                            setDebugState("Staring at near entity: " + entity.getName().getString());
                            _lastLookPos = entity.getEyePos();
                        }

                        LookHelper.smoothLookAt(mod, _lastLookPos, 0.2f);
                        if (_lookTimer.elapsed())
                            _lookTimer.reset();
                        return null;
                    };
            }

        }
        if (!chillEntity.isPresent())
            setDebugState("Doing nothing");
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

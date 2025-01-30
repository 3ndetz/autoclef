package adris.altoclef.tasks.stupid;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.DeathEvent;
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
 * - Uses ranged combat with timing then switches to melee
 * - Prioritizes closer targets with lower health
 * - Properly stops pursuing unreachable targets
 * - Builds graves for defeated targets with 50% chance if conditions met
 */
public class BattleRoyaleTask extends Task {

    private static final double TARGET_FOLLOW_RANGE = 250; // Maximum range to track/follow targets
    private static final double UNREACHABLE_TIME = 30; // Seconds until target considered unreachable
    private static final double KILL_VICTORY_DELAY = 0.1; // Seconds to wait after kill before victory actions
    private static final double GRAVE_TIMEOUT = 25; // Seconds before abandoning grave construction
    
    private final Set<String> _blacklistedPlayers = new HashSet<>();
    private final Map<String, TimerGame> _blacklistDuration = new HashMap<>();
    private final TimerGame _rangedTimer = new TimerGame(10); // 10 seconds of ranged attacks
    private final TimerGame _killTimer = new TimerGame(KILL_VICTORY_DELAY);
    private final TimerGame _graveTimeoutTimer = new TimerGame(GRAVE_TIMEOUT);
    private final Set<String> _spottedTargets = new HashSet<>();
    private Task _battleCryTask = null;
    
    private DoToClosestEntityTask _targetingTask = null;
    
    // Victory gesture tracking
    private String _lastTargetName = null;
    private Vec3d _lastTargetPos = null;
    private Task _victoryTask = null;
    private boolean _killedLastTarget = false;
    private Task _graveTask = null;
    private boolean _buildingGrave = false;
    public Vec3d _lastGroundPos;
    private boolean _victoryActionsStarted = false;
    
    public BattleRoyaleTask() {
        _rangedTimer.reset();
    }
    
    @Override
    protected void onStart(AltoClef mod) {
        mod.getBehaviour().push();
        mod.getBehaviour().setForceFieldPlayers(false);
        // Track kill events
        EventBus.subscribe(DeathEvent.class, evt -> onKillEvent(mod, evt));
    }

    private void onKillEvent(AltoClef mod, DeathEvent evt) {
        if (_lastTargetName != null && evt.name.equals(_lastTargetName) && _lastTargetPos != null) {
            _killTimer.reset();
            _killedLastTarget = true;
            Debug.logMessage("Target killed, waiting " + KILL_VICTORY_DELAY + " seconds before victory actions");
        }
    }
    public void stopVictoryActions(){
        _victoryTask = null;
        _graveTask = null;
        _victoryActionsStarted = false;
        _killedLastTarget = false;
        _buildingGrave = false;
    }
    @Override 
    protected Task onTick(AltoClef mod) {
        // If we're currently building a grave, continue with that or timeout
        if (_buildingGrave && _graveTask != null) {
            if (_graveTimeoutTimer.elapsed()) {
                Debug.logMessage("Grave construction timed out after " + GRAVE_TIMEOUT + " seconds");
                stopVictoryActions();
            } else if (!_graveTask.isFinished(mod)) {
                return _graveTask;
            } else {
                stopVictoryActions();
            }
        }
    
        // Check if we killed our last target
        if (_lastTargetName != null && _lastTargetPos != null && _killedLastTarget && !_victoryActionsStarted) {
            if (_killTimer.elapsed()) {

                    _victoryActionsStarted = true;

                    // Check conditions for building a grave
                    if (Math.random() < 0.3 && ConstructGraveTask.hasGraveMaterials(mod)) {
                        boolean noPlayersNearby = true;
                        // bad approach! lets check only target is present or not
                        //for (PlayerEntity player : mod.getWorld().getPlayers()) {
                        //    if (player != mod.getPlayer() && player.squaredDistanceTo(mod.getPlayer()) <= 25) {
                        //        noPlayersNearby = false;
                        //        break;
                        //    }
                        //}
        
                        if (noPlayersNearby && _lastGroundPos != null) {

                            BlockPos gravePos = new BlockPos((int)_lastGroundPos.x, (int)_lastGroundPos.y + 1, (int)_lastGroundPos.z);
                            if (WorldHelper.canReach(mod, gravePos)
                                    && WorldHelper.canBreak(mod, gravePos)
                                    && WorldHelper.canPlace(mod, gravePos))
                            {
                                _buildingGrave = true;
                                _graveTask = new ConstructGraveTask(gravePos, _lastTargetName + "\n2024-2025\nne sdelal uroki =(");
                                _graveTimeoutTimer.reset();
                                Debug.logMessage("Building grave for " + _lastTargetName);
                                return _graveTask;
                            }
                        }
                    }
        
                    // Show random victory gesture if not building grave
                    if (!_buildingGrave) {
                        GestureTask.Gesture[] victoryGestures = {
                            GestureTask.Gesture.Disrespect,
                            GestureTask.Gesture.BrawlStars, 
                            GestureTask.Gesture.Cheer,
                            GestureTask.Gesture.Disagree,
                            GestureTask.Gesture.Crazy,
                            GestureTask.Gesture.Fight
                        };
                        int randomIndex = (int)(Math.random() * victoryGestures.length);
                        _victoryTask = new GestureTask(_lastTargetPos, victoryGestures[randomIndex]);
                        Debug.logMessage("Victory! Showing " + victoryGestures[randomIndex] + " gesture");


                }
            }
        }
    
        // If we have a victory gesture to perform, do it
        if (_victoryTask != null) {
            if (!_victoryTask.isFinished(mod)) {
                return _victoryTask;
            }
            stopVictoryActions();
        }

        // If we're showing battle cry, continue with that
        if (_battleCryTask != null) {
            if (!_battleCryTask.isFinished(mod)) {
                return _battleCryTask;
            }
            _battleCryTask = null;
        }
    
        // Clean up expired blacklisted players
        _blacklistedPlayers.removeIf(name -> {
            TimerGame timer = _blacklistDuration.get(name);
            if (timer != null && timer.elapsed()) {
                _blacklistDuration.remove(name);
                return true;
            }
            return false;
        });
    
        _targetingTask = new DoToClosestEntityTask(
            entity -> {
                if (entity instanceof PlayerEntity player) {
                    String name = player.getName().getString();
                    
                    // Check if this is a new target and show battle cry
                    if (!_spottedTargets.contains(name) && player.distanceTo(mod.getPlayer()) > 15) {
                        _spottedTargets.add(name);
                        _battleCryTask = new GestureTask(player.getPos(), GestureTask.Gesture.Fight);
                        Debug.logMessage("New target spotted! Showing battle cry gesture!");
                        return _battleCryTask;
                    }
                    
                    // Store target info for victory gestures
                    _lastTargetName = name;
                    _lastTargetPos = player.getPos();
                    _lastGroundPos = WorldHelper.toVec3d(WorldHelper.getNearestGroundPos(mod, player.getPos()));
                    _killedLastTarget = false;

                    // If target becomes unreachable (e.g. too high up, behind walls)
                    BlockPos playerPos = player.getBlockPos();
                    if (!WorldHelper.canReach(mod, playerPos)) {
                        blacklistTarget(name);
                        return null;
                    }
                    
                    // Clear blacklist entry if target becomes reachable again
                    if (_blacklistedPlayers.contains(name)) {
                        _blacklistedPlayers.remove(name);
                        _blacklistDuration.remove(name);
                    }
                    
                    return new KillPlayerTask(name);
                }
                return null;
            },
            entity -> isValidTarget((PlayerEntity)entity, mod),
            PlayerEntity.class
        );

        // The targeting task will automatically track and pursue the best target
        return _targetingTask;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        _spottedTargets.clear();
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof BattleRoyaleTask;
    }

    @Override
    protected String toDebugString() {
        return "Battle Royale Mode";
    }

    private boolean isValidTarget(PlayerEntity player, AltoClef mod) {
        if (player == null || player == mod.getPlayer()) return false;
        
        String name = player.getName().getString();
        if (_blacklistedPlayers.contains(name)) return false;
        
        double sqDistance = player.squaredDistanceTo(mod.getPlayer());
        return sqDistance <= TARGET_FOLLOW_RANGE * TARGET_FOLLOW_RANGE;
    }

    private void blacklistTarget(String playerName) {
        if (!_blacklistedPlayers.contains(playerName)) {
            _blacklistedPlayers.add(playerName);
            TimerGame timer = new TimerGame(UNREACHABLE_TIME);
            timer.reset();
            _blacklistDuration.put(playerName, timer);
            Debug.logMessage("Blacklisting " + playerName + " as unreachable for " + UNREACHABLE_TIME + " seconds");
        }
    }
}

package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.Subscription;
import adris.altoclef.eventbus.events.DeathEvent;
import adris.altoclef.tasks.multiplayer.GestureTask;
import adris.altoclef.tasks.construction.compound.ConstructGraveTask;
import adris.altoclef.tasks.stupid.BattleRoyaleTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.HashSet;
import java.util.Set;

/*
 * !!! DO NOT REMOVE THIS MY NOTES !!!
 * 
 * Combat task parameters:
 * boolean shouldBuildGraves
 * boolean shouldUseGestures
 * 
 * Based on behavior, if manyTargets, then parameters should be feed wtih
 * Predicate<Entity> shouldAttackPredicate
 * 
 * if one target, only this parameter:
 * String targetPlayerName
 * 
 * 
 */
public class CombatTask extends Task {
    private static final double KILL_VICTORY_DELAY = 0.1;
    private static final double GRAVE_TIMEOUT = 15;

    private final boolean _shouldBuildGraves;
    private final boolean _shouldUseGestures;
    private final String _targetPlayerName;
    private final Predicate<Entity> _shouldAttackPredicate;
    private final TimerGame _killTimer = new TimerGame(KILL_VICTORY_DELAY);
    private final TimerGame _graveTimeoutTimer = new TimerGame(GRAVE_TIMEOUT);
    private final Set<String> _spottedTargets = new HashSet<>();
    
    // Victory and grave tracking
    private String _lastTargetName = null;
    private Vec3d _lastTargetPos = null;
    private Vec3d _lastGroundPos = null;
    private Task _victoryTask = null;
    private Task _graveTask = null;
    private boolean _killedLastTarget = false;
    private boolean _buildingGrave = false;
    private boolean _victoryActionsStarted = false;
    private Task _battleCryTask = null;
    private DoToClosestEntityTask _targetingTask = null;
    private static final double UNREACHABLE_TIME = 30; // Seconds until target considered unreachable

    private final Set<String> _blacklistedPlayers = new HashSet<>();
    private final Map<String, TimerGame> _blacklistDuration = new HashMap<>();
    private Subscription<DeathEvent> _deathEventSubscribtion;
    // Single target constructor
    public CombatTask(String targetPlayerName, boolean shouldBuildGraves, boolean shouldUseGestures) {
        this._targetPlayerName = targetPlayerName;
        this._shouldAttackPredicate = null;
        this._shouldBuildGraves = shouldBuildGraves;
        this._shouldUseGestures = shouldUseGestures;
    }
    public CombatTask(){
        this._targetPlayerName = null;
        this._shouldAttackPredicate = null;
        this._shouldBuildGraves = true;
        this._shouldUseGestures = true;
    }

    // Multi target constructor 
    public CombatTask(Predicate<Entity> shouldAttackPredicate, boolean shouldBuildGraves, boolean shouldUseGestures) {
        this._targetPlayerName = null;
        this._shouldAttackPredicate = shouldAttackPredicate;
        this._shouldBuildGraves = shouldBuildGraves;
        this._shouldUseGestures = shouldUseGestures;
    }
    @Override
    protected void onStart(AltoClef mod) {
        mod.getBehaviour().push();
        mod.getBehaviour().setForceFieldPlayers(false);
        // Track kill events
        _deathEventSubscribtion = EventBus.subscribe(DeathEvent.class, evt -> onKillEvent(mod, evt));
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
        if (mod.getPlayer() == null || mod.getPlayer().getPos() == null)
            return null;
        if (_buildingGrave && _graveTask != null) {
            if (_graveTimeoutTimer.elapsed()) {
                Debug.logMessage("Grave construction timed out after " + GRAVE_TIMEOUT + " seconds");
                stopVictoryActions();
            } else if (!_graveTask.isFinished(mod)) {
                setDebugState("Building grave for " + _lastTargetName);
                return _graveTask;
            } else {
                if (_victoryTask == null && _shouldUseGestures) {
                    GestureTask.Gesture[] respectGestures = {
                            GestureTask.Gesture.Agree,
                            GestureTask.Gesture.Disagree,
                            GestureTask.Gesture.Disrespect

                    };
                    int randomIndex = (int) (Math.random() * respectGestures.length);
                    _victoryTask = new GestureTask(_lastTargetPos, respectGestures[randomIndex]);
                    setDebugState("Showing respect to the grave of " + _lastTargetName);
                }
                //stopVictoryActions();
            }
        }

        // Check if we killed our last target
        if (_lastTargetName != null && _lastTargetPos != null && _killedLastTarget && !_victoryActionsStarted) {
            if (_killTimer.elapsed()) {
                _victoryActionsStarted = true;
                // Check conditions for building a grave
                boolean graveChanceDice = Math.random() < 0.3;
                if (graveChanceDice && ConstructGraveTask.hasGraveMaterials(mod) && _shouldBuildGraves) {
                    boolean noPlayersNearby = true;
                    // bad approach! lets check only target is present or not
                    //for (PlayerEntity player : mod.getWorld().getPlayers()) {
                    //    if (player != mod.getPlayer() && player.squaredDistanceTo(mod.getPlayer()) <= 25) {
                    //        noPlayersNearby = false;
                    //        break;
                    //    }
                    //}

                    if (noPlayersNearby && _lastGroundPos != null && _lastGroundPos.distanceTo(mod.getPlayer().getPos()) < 20) {

                        BlockPos gravePos = new BlockPos((int)_lastGroundPos.x, (int)_lastGroundPos.y + 1, (int)_lastGroundPos.z);
                        if (WorldHelper.canReach(mod, gravePos)
                                && WorldHelper.canBreak(mod, gravePos)
                                && WorldHelper.canPlace(mod, gravePos))
                        {
                            _buildingGrave = true;
                            _graveTask = new ConstructGraveTask(gravePos, _lastTargetName + "\n2024-2025\nne sdelal uroki =(");
                            _graveTimeoutTimer.reset();
                            Debug.logMessage("Victory! Building grave for " + _lastTargetName);
                            return _graveTask;
                        }
                    }
                }

                // Show random victory gesture if not building grave
                if (!_buildingGrave) {
                    if (_shouldUseGestures) {
                        GestureTask.Gesture[] victoryGestures = {
                                GestureTask.Gesture.Disrespect,
                                GestureTask.Gesture.BrawlStars,
                                GestureTask.Gesture.Cheer,
                                GestureTask.Gesture.Disagree,
                                GestureTask.Gesture.Crazy
                        };
                        int randomIndex = (int) (Math.random() * victoryGestures.length);
                        _victoryTask = new GestureTask(_lastTargetPos, victoryGestures[randomIndex]);
                        Debug.logMessage("Victory! Showing " + victoryGestures[randomIndex] + " gesture");
                    }

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
                setDebugState("Celebrating defeat of target " + _lastTargetName);
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
                            _battleCryTask = new GestureTask(player, GestureTask.Gesture.Fight);
                            Debug.logMessage("New target spotted! Showing battle cry gesture!");
                            return _battleCryTask;
                        }

                        // Store target info for victory gestures
                        _lastTargetName = name;
                        _lastTargetPos = player.getPos();
                        Vec3d lastGroundPosPrev = WorldHelper.toVec3d(WorldHelper.getNearestGroundPos(mod, player.getPos()));
                        if (lastGroundPosPrev != null)
                            _lastGroundPos = lastGroundPosPrev;
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
        if (_targetPlayerName != null) {
            setDebugState("Searching for target " + _targetPlayerName);
        } else {
            setDebugState("Searching for targets");
        }
        // The targeting task will automatically track and pursue the best target
        return _targetingTask;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        _spottedTargets.clear();
        mod.getBehaviour().pop();
        EventBus.unsubscribe(_deathEventSubscribtion);
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof CombatTask task
                && (this._targetPlayerName != null
                && task._targetPlayerName != null
                && task._targetPlayerName.equals(this._targetPlayerName))
                && task._shouldBuildGraves == this._shouldBuildGraves
                && task._shouldUseGestures == this._shouldUseGestures;
    }

    @Override
    protected String toDebugString() {
        return "Combat";
    }

    private boolean isValidTarget(PlayerEntity player, AltoClef mod) {
        if (player == null || player == mod.getPlayer() || player.getName() == null) return false;
        String name = player.getName().getString();
        if (_targetPlayerName != null && _targetPlayerName.equals(name)) return true;
        if (_blacklistedPlayers.contains(name)) return false;
        if (_shouldAttackPredicate != null) return _shouldAttackPredicate.test(player);
        return true;
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

package adris.altoclef.tasks.multiplayer;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.entity.AbstractDoToEntityTask;
import adris.altoclef.tasks.movement.GetCloseToBlockTask;
import adris.altoclef.tasks.movement.GetToEntityTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;

public class GestureTask extends Task {

    private Entity _target;
    private int _phase = 0;
    private double _interactDistance = 2.5d;
    private double _shiftDistance = 0.7d;
    private double _stopDistance = 0.2d;
    private boolean _started = false;
    public enum Gesture {
        LetsFight,
        Easy,
        BrawlStars,
        GoHere

    }
    public float _rotationIter = -179;
    public Gesture _gesture;
    private final TimerGame _shiftTimer = new TimerGame(0.15);
    public final TimerGame _gestureTimer = new TimerGame(3);
    public GestureTask(Entity target, Gesture type) {
        _target = target;
        _gesture = type;
    }
    public GestureTask(Entity target) {
        this(target, Gesture.values()[new Random().nextInt(Gesture.values().length-1)]); // random
    }
    protected Optional<Entity> getEntityTarget(AltoClef mod) {
        return Optional.of(_target);
    }


    @Override
    protected Task onTick(AltoClef mod) {
        Entity entity = _target;


        if(_gestureTimer.elapsed()){
            _gestureTimer.reset();
        }
        if (!_started)
            _started = true;
        if(entity != null && entity.getName() != null)
            setDebugState("target = "+entity.getName().getString());
        else {
            return null;
        }
        //mod.getInputControls().hold(Input.SNEAK);
        double yDiff = entity.getPos().getY() - mod.getPlayer().getPos().getY();
        boolean tooClose;
        boolean shifting = !( _gesture.equals(Gesture.LetsFight) || _gesture.equals(Gesture.BrawlStars) );;
        double yBorder = 0.9f;
        boolean moveLeftRight = _gesture.equals(Gesture.LetsFight);
        boolean swingHand = !( _gesture.equals(Gesture.BrawlStars) || _gesture.equals(Gesture.Easy) );
        if (_gesture.equals(Gesture.GoHere) || _gesture.equals(Gesture.LetsFight)) {
            // look at entity then look down, like "come on you"
            if (_phase == 0) {
                LookHelper.smoothLook(mod, entity);
            } else {
                // look down
                Rotation newRot = new Rotation(mod.getPlayer().getYaw(), 20);
                LookHelper.smoothLook(mod, newRot, 0.3f);
            }

        } else if (_gesture.equals(Gesture.BrawlStars)) {
            // just spin around player itslef
            // 1. get changing rotation
            Rotation newRot = new Rotation(_rotationIter, 20);
            // pitch 90 is down, 0 is straignt, -90 is up
            // yaw -179 <-> +179
            if (_phase == 0) {
                _rotationIter += 70;
            } else {
                _rotationIter += 40;
            }
            // rotation need to be from -179 to 179
            if (_rotationIter>=179){
                _rotationIter = _rotationIter - 360 + 1;
            }
            LookHelper.smoothLook(mod, newRot);
        } else if (_gesture.equals(Gesture.Easy)) {
            // turn player's back the target
            Vec3d playerPos = mod.getPlayer().getPos();
            Vec3d targetPos = entity.getPos();
            Vec3d diff = targetPos.subtract(playerPos);
            Vec3d back = new Vec3d(-diff.getZ(), diff.getY(), -diff.getX());
            Vec3d lookPos = playerPos.add(back);
            LookHelper.smoothLook(mod, lookPos);
        }
        // Debug.logMessage("_phase" + _phase);
        //Debug.logMessage("ydiff" + yDiff);
        //Debug.logMessage("shifting " + shifting + " " + _phase);
        if (yDiff >= yBorder && !_gesture.equals(Gesture.LetsFight)) {
            mod.getInputControls().tryPress(Input.JUMP);
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.JUMP, true);
        }
        if (swingHand) {
            if (_phase == 0){
                mod.getInputControls().tryPress(Input.CLICK_LEFT);
            }
        }
        if (shifting) {
            if (_phase == 0) {
                mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SNEAK, true);
            } else {
                mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SNEAK, false);
            }

        } else {
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SNEAK, false);

        }

        if (moveLeftRight) {
            if (_phase == 0) {
                mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_LEFT, true);
                mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_RIGHT, false);
            } else {
                mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_LEFT, false);
                mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_RIGHT, true);
            }
        } else {
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_RIGHT, false);
            mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_LEFT, false);
        }
        if (_shiftTimer.elapsed()){
            if (_phase > 0) {
                _phase = 0;
            } else {
                _phase += 1;
            }
            _shiftTimer.reset();
        }

        return null;
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return (getEntityTarget(mod).isEmpty() || (_started && _gestureTimer.elapsed()));
    }

    /**
     * @param mod
     */
    @Override
    protected void onStart(AltoClef mod) {

    }

    /**
     * @param mod
     * @param interruptTask
     */
    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getClientBaritone().getInputOverrideHandler().clearAllKeys();
    }

    /**
     * @param other
     * @return
     */
    @Override
    protected boolean isEqual(Task other) {
        return other instanceof GestureTask task && Objects.equals(task._target, _target);
    }

    @Override
    protected String toDebugString() {
        String target_str;
        if (_target != null && _target.getName() != null) {
            target_str = _target.getName().getString();
        } else {
            target_str = "(unreachable)";
        }

        return "Showing gesture " + _gesture.toString()
                + " to target " + target_str
                + " for time " + String.format("%.1f", _gestureTimer.getDuration());
    }
}


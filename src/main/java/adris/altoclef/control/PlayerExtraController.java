package adris.altoclef.control;

import adris.altoclef.AltoClef;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.BlockBreakingCancelEvent;
import adris.altoclef.eventbus.events.BlockBreakingEvent;
import adris.altoclef.util.helpers.KillAuraHelper;
import adris.altoclef.util.helpers.LookHelper;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import baritone.api.utils.input.Input;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PlayerExtraController {

    private final AltoClef _mod;
    private BlockPos _blockBreakPos;
    private double _blockBreakProgress;
    private float _plyYaw = 0;
    private float _plyPitch = 0;
    public static boolean IsPvpRotating;
    private static boolean _succesfulHit = false;

    public PlayerExtraController(AltoClef mod) {
        _mod = mod;

        EventBus.subscribe(BlockBreakingEvent.class, evt -> onBlockBreak(evt.blockPos, evt.progress));
        EventBus.subscribe(BlockBreakingCancelEvent.class, evt -> onBlockStopBreaking());
    }

    private void onBlockBreak(BlockPos pos, double progress) {
        _blockBreakPos = pos;
        _blockBreakProgress = progress;
    }

    private void onBlockStopBreaking() {
        _blockBreakPos = null;
        _blockBreakProgress = 0;
    }

    public BlockPos getBreakingBlockPos() {
        return _blockBreakPos;
    }

    public boolean isBreakingBlock() {
        return _blockBreakPos != null;
    }

    public double getBreakingBlockProgress() {
        return _blockBreakProgress;
    }

    public boolean inRange(Entity entity) {
        return _mod.getPlayer().isInRange(entity, _mod.getModSettings().getEntityReachRange());
    }

    public boolean attack(Entity entity, boolean DoRotates) {
        _succesfulHit = false;
        //entity.is
        if (IsPvpRotating == false) {
            new Thread(() -> {

                IsPvpRotating = true;
                sleepSec(1);
                IsPvpRotating = false;
            }).start();
        }

        if (LookHelper.cleanLineOfSight(entity.getEyePos(), 4.5)) {//(inRange(entity)) {
            if (DoRotates) {
                LookHelper.smoothLookAt(_mod, entity);
                //LookHelper.SmoothLookAt(_mod, 0.05f, true, entity);
            }
            if (true) {
                _plyPitch = _mod.getPlayer().getPitch();
                _plyYaw = _mod.getPlayer().getYaw();
                Rotation _targetRotation = RotationUtils.calcRotationFromVec3d(
                        _mod.getClientBaritone().getPlayerContext().playerHead(), entity.getPos().add(0, 1.0, 0),
                        _mod.getClientBaritone().getPlayerContext().playerRotations());
                Rotation subtractRotation = new Rotation(_plyYaw, _plyPitch).subtract(_targetRotation);
                boolean IsYawNormal =
                        Math.abs(subtractRotation.getYaw()) < 7.5 / (entity.squaredDistanceTo(_mod.getPlayer()) / 4);
                boolean IsPitchBoundBox = false;
                float zzMax = LookHelper.getLookRotation(_mod,
                        new Vec3d(entity.getX(), entity.getBoundingBox().maxY, entity.getZ())).getPitch();
                float zzMin = LookHelper.getLookRotation(_mod,
                        new Vec3d(entity.getX(), entity.getBoundingBox().minY, entity.getZ())).getPitch();
                IsPitchBoundBox = _plyPitch > zzMax & _plyPitch < zzMin;
                if (IsPitchBoundBox & IsYawNormal &
                        entity.squaredDistanceTo(_mod.getPlayer()) < 3.2 * 3.2 & !_mod.getFoodChain().isTryingToEat()) {
                    try {
                        _mod.getInputControls().tryPress(Input.CLICK_LEFT);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    _mod.getDamageTracker().onMeleeAttack(entity);
                    _succesfulHit = true;
                }

            }
        } else {
            KillAuraHelper.TimerStop();
        }
        return _succesfulHit;
    }

    public boolean attack(Entity entity) {
        return this.attack(entity, true);
    }

    private static void sleepSec(double seconds) {
        try {
            Thread.sleep((int) (1000 * seconds));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

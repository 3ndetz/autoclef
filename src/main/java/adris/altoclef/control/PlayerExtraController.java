package adris.altoclef.control;

import adris.altoclef.AltoClef;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.BlockBreakingCancelEvent;
import adris.altoclef.eventbus.events.BlockBreakingEvent;
import adris.altoclef.util.helpers.LookHelper;
import baritone.api.utils.input.Input;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

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
        return LookHelper.canHitEntity(_mod, entity); // _mod.getPlayer().isInRange(entity, _mod.getModSettings().getEntityReachRange());
    }

    public boolean attack(Entity entity, boolean DoRotates) {
        _succesfulHit = false;
        //entity.is
        // TODO FLICK
        //if(DoRotates) {
            LookHelper.smoothLook(_mod, entity);
        //}
        //double PunkRange = 3.1;

        // TODO FIX
        // FAILS WHEN TOO CLOSE (INSIDE OF A HEAD OF AN ENT)

                if (LookHelper.isLookingAtEntity(_mod, entity) && inRange(entity)) {//LookHelper.cleanLineOfSight(_mod.getPlayer(), LookHelper.getClosestPointOnEntityHitbox(_mod, entity),PunkRange)) {
                    try {
                        //if(!){
                            _mod.getInputControls().release(Input.CLICK_RIGHT);
                        //}
                        _mod.getInputControls().tryPress(Input.CLICK_LEFT);
                        _mod.getDamageTracker().onClientMeleeAttack(entity);
                        _succesfulHit = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

        return _succesfulHit;
    }


    public boolean attack(Entity entity) {
        return this.attack(entity, false);
    }

    private static void sleepSec(double seconds) {
        try {
            Thread.sleep((int) (1000 * seconds));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

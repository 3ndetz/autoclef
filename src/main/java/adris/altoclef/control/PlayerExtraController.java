package adris.altoclef.control;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.chains.FoodChain;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.BlockBreakingCancelEvent;
import adris.altoclef.eventbus.events.BlockBreakingEvent;
import adris.altoclef.util.helpers.KillAuraHelper;
import baritone.api.utils.RotationUtils;
import baritone.api.utils.input.Input;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import adris.altoclef.util.helpers.LookHelper;
import baritone.api.utils.Rotation;
import net.minecraft.util.math.Vec3d;

public class PlayerExtraController {

    private final AltoClef _mod;
    private BlockPos _blockBreakPos;
    private double _blockBreakProgress;

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

    private long _littleCounter = 0;
    private Rotation _targetRotation;
    private float _plyYaw = 0;
    private float _plyPitch = 0;
    private float _pitchCounter = 0;
    private boolean _needRotation = true;
    private boolean _needPitch = true;
    private boolean _needYaw = true;
    private float _innacuracy = 10.0f;
    private Rotation _plyRot;
    private Rotation _plyRotBad;
    private Rotation _targetRotBad;
    int _rndCounter = (int)(Math.random()*1000f);
    public static boolean IsPvpRotating;
    private static boolean _succesfulHit = false;
    public boolean attack(Entity entity, boolean DoRotates) {
        _succesfulHit = false;
        //entity.is
        if (IsPvpRotating == false) {
            new Thread(() -> {

                IsPvpRotating = true;
                sleepSec(1);
                IsPvpRotating = false;
            }).start();}

        //Debug.logMessage("ispvprotating " + LookHelper.cleanLineOfSight(entity.getEyePos(),4.5));
        if(LookHelper.cleanLineOfSight(entity.getEyePos(),4.5)){//(inRange(entity)) {
            //Debug.logMessage("seesPlayer " + LookHelper.seesPlayer(entity,_mod.getPlayer(),10));
            _innacuracy = 6.0f; //10
            if(DoRotates){
            LookHelper.SmoothLookAt(_mod,0.05f,true,entity);}
            if(true) {
                //entity.isInv
                _plyPitch = _mod.getPlayer().getPitch();
                _plyYaw = _mod.getPlayer().getYaw();
                Rotation _targetRotation = RotationUtils.calcRotationFromVec3d(_mod.getClientBaritone().getPlayerContext().playerHead(), entity.getPos().add(0, 1.0, 0), _mod.getClientBaritone().getPlayerContext().playerRotations());
                Rotation subtractRotation = new Rotation(_plyYaw,_plyPitch).subtract(_targetRotation);
                boolean IsYawNormal = Math.abs(subtractRotation.getYaw())<7.5/(entity.squaredDistanceTo(_mod.getPlayer())/4);
                boolean IsPitchBoundBox = false;
                float zzMax = LookHelper.getLookRotation(_mod, new Vec3d(entity.getX(),entity.getBoundingBox().maxY,entity.getZ())).getPitch();
                float zzMin = LookHelper.getLookRotation(_mod, new Vec3d(entity.getX(),entity.getBoundingBox().minY,entity.getZ())).getPitch();
                IsPitchBoundBox = _plyPitch>zzMax&_plyPitch<zzMin;
                //Debug.logMessage("IsPitchBoundBox "+IsPitchBoundBox);
                //Debug.logMessage("PlyPitch "+_plyPitch + " zzMax "+zzMax +" zzMin " + zzMin);
                if(IsPitchBoundBox & IsYawNormal &
                        entity.squaredDistanceTo(_mod.getPlayer()) < 3.2 * 3.2 & !_mod.getFoodChain().isTryingToEat()) {
                    //Debug.logMessage("MDA" + IsPitchBoundBox);
                    try {
                        _mod.getInputControls().tryPress(Input.CLICK_LEFT);

                        //_mod.getController().attackEntity(_mod.getPlayer(), entity); //ВОНИКАЕТ ОШИБКА TICKING ENTITY!!!!!
                        //_mod.getPlayer().swingHand(Hand.MAIN_HAND);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    _mod.getDamageTracker().onMeleeAttack(entity);
                    _succesfulHit = true;
                    //new Thread(() -> {
                        //_mod.getInputControls().hold(Input.CLICK_LEFT);
                        //sleepSec(0.01+Math.random()*0.05);
                        //_mod.getInputControls().release(Input.CLICK_LEFT);
                    //}).start();
                }
                //new Thread(() -> {
                //    for (int i = 3; i > 0; --i) {
                //        Debug.logMessage(i + "...");
                //        sleepSec(0.2f);
                //    }
                //});
                //for(int i = 0; i<10000;i++){}
                //sleepSec(0.1f+(float) Math.random()*0.2f);

                //_littleCounter = 0;
                //_rndCounter = (int) Math.floor(Math.random()*1000f);
                //_mod.getInputControls().forceLook(1.0F, 1.0F);
                //_mod.getController().attackEntity(_mod.getPlayer(), entity);
                //_mod.getPlayer().swingHand(Hand.MAIN_HAND);

            }
        }else{KillAuraHelper.TimerStop();}
        //if(_mod.getClientBaritone().getCustomGoalProcess().isActive()){
        //    KillAuraHelper.TimerStop();
        //}
        //Debug.logMessage("zzzr" + _succesfulHit);\
        //Debug.logMessage("zzzr" + _succesfulHit);
        return _succesfulHit;
    }
    public boolean attack(Entity entity) {
        return this.attack(entity,true);
    }
    private static void sleepSec(double seconds) {
        try {
            Thread.sleep((int) (1000 * seconds));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

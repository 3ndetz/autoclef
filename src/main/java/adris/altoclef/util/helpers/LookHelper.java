package adris.altoclef.util.helpers;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import baritone.api.BaritoneAPI;
import baritone.api.utils.IPlayerContext;
import baritone.api.utils.RayTraceUtils;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;

import java.util.Optional;

/**
 * Helper functions to interpret and change our player's look direction
 */
public interface LookHelper {

    static Optional<Rotation> getReach(BlockPos target, Direction side) {
        Optional<Rotation> reachable;
        IPlayerContext ctx = BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext();
        if (side == null) {
            assert MinecraftClient.getInstance().player != null;
            reachable = RotationUtils.reachable(ctx.player(), target, ctx.playerController().getBlockReachDistance());
        } else {
            Vec3i sideVector = side.getVector();
            Vec3d centerOffset = new Vec3d(0.5 + sideVector.getX() * 0.5, 0.5 + sideVector.getY() * 0.5, 0.5 + sideVector.getZ() * 0.5);

            Vec3d sidePoint = centerOffset.add(target.getX(), target.getY(), target.getZ());

            //reachable(this.ctx.player(), _target, this.ctx.playerController().getBlockReachDistance());
            reachable = RotationUtils.reachableOffset(ctx.player(), target, sidePoint, ctx.playerController().getBlockReachDistance(), false);

            // Check for right angle
            if (reachable.isPresent()) {
                // Note: If sneak, use RotationUtils.inferSneakingEyePosition
                Vec3d camPos = ctx.player().getCameraPosVec(1.0F);
                Vec3d vecToPlayerPos = camPos.subtract(sidePoint);

                double dot = vecToPlayerPos.normalize().dotProduct(new Vec3d(sideVector.getX(), sideVector.getY(), sideVector.getZ()));
                if (dot < 0) {
                    // We're perpendicular and cannot face.
                    return Optional.empty();
                }
            }
        }
        return reachable;
    }

    static Optional<Rotation> getReach(BlockPos target) {
        return getReach(target, null);
    }

    static EntityHitResult raycast(Entity from, Entity to, double reachDistance) {
        Vec3d fromPos = getCameraPos(from),
                toPos = getCameraPos(to);
        Vec3d direction = (toPos.subtract(fromPos).normalize().multiply(reachDistance));
        Box box = to.getBoundingBox();
        return ProjectileUtil.raycast(from, fromPos, fromPos.add(direction), box, entity -> entity.equals(to), 0);
    }

    static boolean seesPlayer(Entity entity, Entity player, double maxRange, Vec3d entityOffs, Vec3d playerOffs) {
        return seesPlayerOffset(entity, player, maxRange, entityOffs, playerOffs) || seesPlayerOffset(entity, player, maxRange, entityOffs, new Vec3d(0, -1, 0).add(playerOffs));
    }

    static boolean seesPlayer(Entity entity, Entity player, double maxRange) {
        return seesPlayer(entity, player, maxRange, Vec3d.ZERO, Vec3d.ZERO);
    }

    static boolean cleanLineOfSight(Entity entity, Vec3d start, Vec3d end, double maxRange) {
        return raycast(entity, start, end, maxRange).getType() == HitResult.Type.MISS;
    }

    static boolean cleanLineOfSight(Entity entity, Vec3d end, double maxRange) {
        Vec3d start = getCameraPos(entity);
        return cleanLineOfSight(entity, start, end, maxRange);
    }
    static boolean cleanLineOfSight(Vec3d end, double maxRange) {
        return cleanLineOfSight(MinecraftClient.getInstance().player, end, maxRange);
    }

    static boolean cleanLineOfSight(Entity entity, BlockPos block, double maxRange) {
        Vec3d center = WorldHelper.toVec3d(block);
        BlockHitResult hit = raycast(entity, getCameraPos(entity), center, maxRange);
        if (hit == null) return true;
        return switch (hit.getType()) {
            case MISS -> true;
            case BLOCK -> hit.getBlockPos().equals(block);
            case ENTITY -> false;
        };
    }

    static boolean shootReady(AltoClef mod, Entity target){
        if (target==null){return false;}
        if (LookHelper.cleanLineOfSight(target.getEyePos(),100)){return true;} else {
            if ((LookHelper.cleanLineOfSight(mod.getPlayer(), mod.getPlayer().getEyePos().add(0, 0.5, 0),
                    mod.getPlayer().getEyePos().relativize(new Vec3d(5, 0, 0)), 10)) &&
                    (LookHelper.cleanLineOfSight(target, target.getEyePos().add(0, 1.5, 0), target.getEyePos().add(0, 3, 0), 10))) {
                return true;
            } else {
                return false;
            }
        }
    }

    static Vec3d toVec3d(Rotation rotation) {
        return RotationUtils.calcVector3dFromRotation(rotation);
    }

    static BlockHitResult raycast(Entity entity, Vec3d start, Vec3d end, double maxRange) {
        Vec3d delta = end.subtract(start);
        if (delta.lengthSquared() > maxRange * maxRange) {
            end = start.add(delta.normalize().multiply(maxRange));
        }
        return entity.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity));
    }

    static BlockHitResult raycast(Entity entity, Vec3d end, double maxRange) {
        Vec3d start = getCameraPos(entity);
        return raycast(entity, start, end, maxRange);
    }

    static Rotation getLookRotation(Entity entity) {
        float pitch = entity.getPitch();
        float yaw = entity.getYaw();
        return new Rotation(yaw, pitch);
    }
    static Rotation getLookRotation() {
        if (MinecraftClient.getInstance().player == null) {
            return new Rotation(0,0);
        }
        return getLookRotation(MinecraftClient.getInstance().player);
    }

    static Vec3d getCameraPos(Entity entity) {
        boolean isSneaking = false;
        if (entity instanceof PlayerEntity player) {
            isSneaking = player.isSneaking();
        }
        return isSneaking ? RayTraceUtils.inferSneakingEyePosition(entity) : entity.getCameraPosVec(1.0F);
    }
    static Vec3d getCameraPos(AltoClef mod) {
        IPlayerContext ctx = BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext();
        return ctx.player().getCameraPosVec(1);
    }

    //  1: Looking straight at pos
    //  0: pos is 90 degrees to the side
    // -1: pos is 180 degrees away (looking away completely)
    static double getLookCloseness(Entity entity, Vec3d pos) {
        Vec3d rotDirection = entity.getRotationVecClient();
        Vec3d lookStart = getCameraPos(entity);
        Vec3d deltaToPos = pos.subtract(lookStart);
        Vec3d deltaDirection = deltaToPos.normalize();
        return rotDirection.dotProduct(deltaDirection);
    }

    static boolean tryAvoidingInteractable(AltoClef mod, boolean IsCollidingNonInteractBlocks) {
        if (isCollidingInteractable(mod, IsCollidingNonInteractBlocks)) {
            randomOrientation(mod);
            return false;
        }
        return true;
    }
    static boolean tryAvoidingInteractable(AltoClef mod){
        return tryAvoidingInteractable(mod,false);
    }

    private static boolean seesPlayerOffset(Entity entity, Entity player, double maxRange, Vec3d offsetEntity, Vec3d offsetPlayer) {
        Vec3d start = getCameraPos(entity).add(offsetEntity);
        Vec3d end = getCameraPos(player).add(offsetPlayer);
        return cleanLineOfSight(entity, start, end, maxRange);
    }

    private static boolean isCollidingInteractable(AltoClef mod,boolean IsCollidingNonInteractBlocks) {

        if (!(mod.getPlayer().currentScreenHandler instanceof PlayerScreenHandler)) {
            StorageHelper.closeScreen();
            return true;
        }

        HitResult result = MinecraftClient.getInstance().crosshairTarget;
        if (result == null) return false;
        if (result.getType() == HitResult.Type.BLOCK) {
            return IsCollidingNonInteractBlocks||WorldHelper.isInteractableBlock(mod, new BlockPos(result.getPos()));
        } else if (result.getType() == HitResult.Type.ENTITY) {
            if (result instanceof EntityHitResult) {
                Entity entity = ((EntityHitResult) result).getEntity();
                return entity instanceof MerchantEntity;
            }
        }
        return false;
    }
    private static boolean isCollidingInteractable(AltoClef mod) {
        return isCollidingInteractable(mod,false);
    }

    static void randomOrientation(AltoClef mod) {
        Rotation r = new Rotation((float) Math.random() * 360f, -90 + (float) Math.random() * 180f);
        lookAt(mod, r);
    }

    static boolean isLookingAt(AltoClef mod, Rotation rotation) {
        return rotation.isReallyCloseTo(getLookRotation());
    }
    static boolean isLookingAt(AltoClef mod, BlockPos blockPos) {
        return mod.getClientBaritone().getPlayerContext().isLookingAt(blockPos);
    }

    static void lookAt(AltoClef mod, Rotation rotation) {
        mod.getClientBaritone().getLookBehavior().updateTarget(rotation, true);
        mod.getPlayer().setYaw(rotation.getYaw());
        mod.getPlayer().setPitch(rotation.getPitch());
    }
    static void lookAt(AltoClef mod, Vec3d toLook) {
        Rotation targetRotation = getLookRotation(mod, toLook);
        lookAt(mod, targetRotation);
    }
    static void lookAt(AltoClef mod, BlockPos toLook, Direction side) {
        Vec3d target = new Vec3d(toLook.getX() + 0.5, toLook.getY() + 0.5, toLook.getZ() + 0.5);
        if (side != null) {
            target.add(side.getVector().getX() * 0.5, side.getVector().getY() * 0.5, side.getVector().getZ() * 0.5);
        }
        lookAt(mod, target);
    }
    static void lookAt(AltoClef mod, BlockPos toLook) {
        lookAt(mod, toLook, null);
    }

    static Rotation getLookRotation(AltoClef mod, Vec3d toLook) {
        return RotationUtils.calcRotationFromVec3d(mod.getClientBaritone().getPlayerContext().playerHead(), toLook, mod.getClientBaritone().getPlayerContext().playerRotations());
    }
    static Rotation getLookRotation(AltoClef mod, BlockPos toLook) {
        return getLookRotation(mod, WorldHelper.toVec3d(toLook));
    }


    static void SmoothLookAtWindMouse(AltoClef mod, float rotCoeffF, boolean ForceLook, Entity entity){
        if(LookHelper.cleanLineOfSight(entity.getEyePos(),4.5)  & !mod.getMobDefenseChain().isDoingAcrobatics())
            new Thread(() -> {
                for (int i = 1; i <= 10; i++) {
                    //Debug.logMessage("STARTING... ");
                    Rotation _plyRot = new Rotation(mod.getPlayer().getYaw(), mod.getPlayer().getPitch());
                    Rotation _targetRotation = RotationUtils.calcRotationFromVec3d(mod.getClientBaritone().getPlayerContext().playerHead(), entity.getPos().add(0, 1.0, 0), mod.getClientBaritone().getPlayerContext().playerRotations());
                    Rotation subtractRotation = _plyRot.subtract(_targetRotation);
                    if(KillAuraHelper.TimerStart(90)) {
                        MouseMoveHelper.PointListsReset();
                        MouseMoveHelper.windMouse(
                                (int) Math.floor(_plyRot.getYaw()),
                                (int) Math.floor(_plyRot.getPitch()),
                                (int) Math.floor(subtractRotation.getYaw()),
                                (int) Math.floor(subtractRotation.getPitch()),
                                20, 5, 15, 12);
                        //9, 3, 15, 12);
                    }
                        int MoveYaw = 0;int MovePitch = 0;
                        if(!MouseMoveHelper.PointsListX.isEmpty()){
                            MoveYaw = MouseMoveHelper.PointsListX.get(0);//MouseMoveHelper.PointsListX.size() -1),
                            MouseMoveHelper.PointsListX.remove(0);
                        }
                        if (!MouseMoveHelper.PointsListY.isEmpty()){
                            MovePitch = MouseMoveHelper.PointsListY.get(0);//MouseMoveHelper.PointsListY.size()-1));
                            MouseMoveHelper.PointsListX.remove(0);
                        }

                        //for(int x : MouseMoveHelper.PointsListX){
                        //    //Debug.logMessage("x = "+x);
                        //}
                        //Debug.logMessage("y = "+MouseMoveHelper.PointsListY.get(MouseMoveHelper.PointsListY.size()-1)+
                        //        "|x ="+MouseMoveHelper.PointsListX.get(MouseMoveHelper.PointsListX.size()-1));
                        Rotation lowedSubtractRotation = new Rotation(
                                MoveYaw*0.001f,
                                MovePitch*0.001f);
                        Rotation resultRotation = new Rotation(
                                _plyRot.subtract(lowedSubtractRotation).getYaw(),
                                _plyRot.subtract(lowedSubtractRotation).getPitch()

                        );
                        mod.getInputControls().forceLook(resultRotation.getYaw(), resultRotation.getPitch());
                        //Debug.logMessage("STOP!");
                        sleepSec(0.005);




                }
            }).start();
    }
    static void SmoothLook(AltoClef mod, Rotation DestRotation){
        boolean ForceLook = true;
        if(!mod.getMobDefenseChain().isDoingAcrobatics())
            new Thread(() -> {
                float _innacuracy = 0;
                _innacuracy = 10.0f;
                float _plyPitch,_plyYaw,YawOldSpeed;//,YawSpeed,PitchSpeed;
                YawOldSpeed = 0f;
                _plyPitch = mod.getPlayer().getPitch();
                _plyYaw = mod.getPlayer().getYaw();
                Rotation _plyRot = new Rotation(_plyYaw, _plyPitch);
                KillAuraHelper.TimerStart(90);
                //KillAuraHelper.TimerStop();
                for (int i = 1; i <= 40; i++) {
                    //Debug.logMessage("rnd1 "+KillAuraHelper.GetNextRandomY());
                    //Debug.logMessage("rnd2 "+KillAuraHelper.GetNextRandomY());
                    if(AltoClef.inGame()  & !mod.getMobDefenseChain().isDoingAcrobatics()){
                        sleepSec(0.00125);
                        Rotation _targetRotation = DestRotation;
                        Rotation subtractRotation = _plyRot.subtract(_targetRotation);

                        float LSRYaw = Math.signum(subtractRotation.getYaw())*0.01f;

                        float PlusY;
                        if (Math.abs(subtractRotation.getYaw())<=4){
                            //LSRYaw *= 0;
                            PlusY = 0;
                        }else{
                            float mult = 1.5f;
                            PlusY = -((float)Math.pow(Math.abs(subtractRotation.getYaw()),1.0/3)*mult-1.7f*mult);
                        }


                        _targetRotation = DestRotation.add(new Rotation(0,PlusY/2));
                        subtractRotation = _plyRot.subtract(_targetRotation);
                        //SPEED CORRECTION
                        float RandomCoeff = 5-(float)Math.random()*10;
                        float RandomCoeff2 = 1.1f-(float)Math.random()/2.5f;
                        float RandomCoeff3 = 1.1f-(float)Math.random()/2.5f;
                        float LSRPitch = Math.signum(subtractRotation.getPitch())*0.03f;
                        if (KillAuraHelper.YawSpeed>2 & (Math.abs(subtractRotation.getYaw())<=15)){
                            KillAuraHelper.YawSpeed /= 4; //* Math.sqrt(11-Math.abs(subtractRotation.getYaw()))/2;
                        }else{
                            KillAuraHelper.YawSpeed = (float)Math.pow((float)KillAuraHelper.TimerGoing,1.2)/15*RandomCoeff2+RandomCoeff;
                            //(float)Math.pow((float)KillAuraHelper.TimerGoing,1.0/2)*3+RandomCoeff; //TOP ROOT
                        }
                        if (KillAuraHelper.PitchSpeed>2 & (Math.abs(subtractRotation.getPitch())<=30)){
                            KillAuraHelper.PitchSpeed /= 2;//* Math.sqrt(11-Math.abs(subtractRotation.getPitch()))/2;
                        }else{
                            KillAuraHelper.PitchSpeed = (float)Math.pow((float)KillAuraHelper.TimerGoing,1.0/2)/1.5f*2*RandomCoeff3+RandomCoeff/2;//
                            // (float)Math.pow((float)KillAuraHelper.TimerGoing,1.0/2)/1.5f*3+RandomCoeff/2;//Math.abs(subtractRotation.getPitch()); //TOP ROOT
                        }

                        //YawOldSpeed+Math.signum(subtractRotation.getYaw());//1 + PlusY;


                        if(Math.abs(subtractRotation.getPitch())<3){
                            //LSRPitch = Math.signum(subtractRotation.getPitch())*0.01f;
                            //LSRPitch *= 0.1;
                            KillAuraHelper.PitchSpeed/=10;
                            //LSRPitch = 0;//0.0001f*(float)Math.signum(-0.5+Math.random());
                        }
                        if(Math.abs(subtractRotation.getYaw())<3){
                            KillAuraHelper.YawSpeed/=10;
                            //LSRYaw = 0;//0.001f*(float)Math.signum(-0.5+Math.random());
                        }
                        if((Math.abs(subtractRotation.getYaw())<0.1)&(Math.abs(subtractRotation.getPitch())<0.1))
                        {KillAuraHelper.TimerStop();KillAuraHelper.TimerStart(90);}
                        //if(InputHelper.isKeyPressed(71)){
//
                        //    Debug.logMessage("TimerGoing ="+(float)KillAuraHelper.TimerGoing + ",TG Root = "+Math.pow((float)KillAuraHelper.TimerGoing,1.0/5));//"PlusY "+PlusY + " Y "+_targetRotation.getPitch());
                        //}
                        Rotation lowedSubtractRotation = new Rotation(
                                LSRYaw*(float)Math.floor(KillAuraHelper.YawSpeed),
                                LSRPitch*(float)Math.floor(KillAuraHelper.PitchSpeed));
                        Rotation resultRotation = new Rotation(
                                _plyRot.subtract(lowedSubtractRotation).getYaw(),
                                _plyRot.subtract(lowedSubtractRotation).getPitch()

                        );
                        _plyRot = resultRotation;
                        _plyPitch = resultRotation.getPitch();
                        _plyYaw = resultRotation.getYaw();
                        YawOldSpeed = KillAuraHelper.YawSpeed;
                        if(ForceLook){
                            mod.getInputControls().forceLook(resultRotation.getYaw(),resultRotation.getPitch());
                        }else{
                            mod.getPlayer().setYaw(resultRotation.getYaw());
                            mod.getPlayer().setPitch(resultRotation.getPitch());
                        }
                    }}
            }).start();
    }
    static void SmoothLookAt(AltoClef mod, float rotCoeffF, boolean ForceLook,Vec3d LookPos){
        if(LookHelper.cleanLineOfSight(LookPos,4.5) && !mod.getMobDefenseChain().isDoingAcrobatics())
            new Thread(() -> {
                float _innacuracy = 0;
                _innacuracy = 10.0f;
                float _plyPitch,_plyYaw,YawOldSpeed;//,YawSpeed,PitchSpeed;
                YawOldSpeed = 0f;
                _plyPitch = mod.getPlayer().getPitch();
                _plyYaw = mod.getPlayer().getYaw();
                Rotation _plyRot = new Rotation(_plyYaw, _plyPitch);
                KillAuraHelper.TimerStart(90);
                //KillAuraHelper.TimerStop();
                for (int i = 1; i <= 40; i++) {
                    //Debug.logMessage("rnd1 "+KillAuraHelper.GetNextRandomY());
                    //Debug.logMessage("rnd2 "+KillAuraHelper.GetNextRandomY());
                    if(AltoClef.inGame() && LookHelper.cleanLineOfSight(LookPos,4.5)  & !mod.getMobDefenseChain().isDoingAcrobatics()){
                        sleepSec(0.00125);
                        Rotation _targetRotation = RotationUtils.calcRotationFromVec3d(mod.getClientBaritone().getPlayerContext().playerHead(), LookPos, mod.getClientBaritone().getPlayerContext().playerRotations());;
                        Rotation subtractRotation = _plyRot.subtract(_targetRotation);

                        float LSRYaw = Math.signum(subtractRotation.getYaw())*0.01f;

                        float PlusY;
                        if (Math.abs(subtractRotation.getYaw())<=4){
                            //LSRYaw *= 0;
                            PlusY = 0;
                        }else{
                            float mult = 1.5f;
                            PlusY = -((float)Math.pow(Math.abs(subtractRotation.getYaw()),1.0/3)*mult-1.7f*mult);
                        }


                        _targetRotation = RotationUtils.calcRotationFromVec3d(mod.getClientBaritone().getPlayerContext().playerHead(), LookPos.add(0,PlusY,0), mod.getClientBaritone().getPlayerContext().playerRotations());;
                        subtractRotation = _plyRot.subtract(_targetRotation);
                        //SPEED CORRECTION
                        float RandomCoeff = 5-(float)Math.random()*10;
                        float RandomCoeff2 = 1.1f-(float)Math.random()/2.5f;
                        float RandomCoeff3 = 1.1f-(float)Math.random()/2.5f;
                        float LSRPitch = Math.signum(subtractRotation.getPitch())*0.01f;
                        if (KillAuraHelper.YawSpeed>2 & (Math.abs(subtractRotation.getYaw())<=15)){
                            KillAuraHelper.YawSpeed /= 4; //* Math.sqrt(11-Math.abs(subtractRotation.getYaw()))/2;
                        }else{
                            KillAuraHelper.YawSpeed = (float)Math.pow((float)KillAuraHelper.TimerGoing,1.2)/15*RandomCoeff2+RandomCoeff;
                            //(float)Math.pow((float)KillAuraHelper.TimerGoing,1.0/2)*3+RandomCoeff; //TOP ROOT
                        }
                        if (KillAuraHelper.PitchSpeed>2 & (Math.abs(subtractRotation.getPitch())<=30)){
                            KillAuraHelper.PitchSpeed /= 2;//* Math.sqrt(11-Math.abs(subtractRotation.getPitch()))/2;
                        }else{
                            KillAuraHelper.PitchSpeed = (float)Math.pow((float)KillAuraHelper.TimerGoing,1.0/2)/1.5f*2*RandomCoeff3+RandomCoeff/2;//
                            // (float)Math.pow((float)KillAuraHelper.TimerGoing,1.0/2)/1.5f*3+RandomCoeff/2;//Math.abs(subtractRotation.getPitch()); //TOP ROOT
                        }

                        //YawOldSpeed+Math.signum(subtractRotation.getYaw());//1 + PlusY;


                        if(Math.abs(subtractRotation.getPitch())<3){
                            //LSRPitch = Math.signum(subtractRotation.getPitch())*0.01f;
                            //LSRPitch *= 0.1;
                            KillAuraHelper.PitchSpeed/=10;
                            //LSRPitch = 0;//0.0001f*(float)Math.signum(-0.5+Math.random());
                        }
                        if(Math.abs(subtractRotation.getYaw())<3){
                            KillAuraHelper.YawSpeed/=10;
                            //LSRYaw = 0;//0.001f*(float)Math.signum(-0.5+Math.random());
                        }
                        if((Math.abs(subtractRotation.getYaw())<3)&(Math.abs(subtractRotation.getPitch())<3))
                        {KillAuraHelper.TimerStop();KillAuraHelper.TimerStart(90);}
                        //if(InputHelper.isKeyPressed(71)){
//
                        //    Debug.logMessage("TimerGoing ="+(float)KillAuraHelper.TimerGoing + ",TG Root = "+Math.pow((float)KillAuraHelper.TimerGoing,1.0/5));//"PlusY "+PlusY + " Y "+_targetRotation.getPitch());
                        //}
                        Rotation lowedSubtractRotation = new Rotation(
                                LSRYaw*(float)Math.floor(KillAuraHelper.YawSpeed),
                                LSRPitch*(float)Math.floor(KillAuraHelper.PitchSpeed));
                        Rotation resultRotation = new Rotation(
                                _plyRot.subtract(lowedSubtractRotation).getYaw(),
                                _plyRot.subtract(lowedSubtractRotation).getPitch()

                        );
                        _plyRot = resultRotation;
                        _plyPitch = resultRotation.getPitch();
                        _plyYaw = resultRotation.getYaw();
                        YawOldSpeed = KillAuraHelper.YawSpeed;
                        if(ForceLook){
                            mod.getInputControls().forceLook(resultRotation.getYaw(),resultRotation.getPitch());
                        }else{
                            mod.getPlayer().setYaw(resultRotation.getYaw());
                            mod.getPlayer().setPitch(resultRotation.getPitch());
                        }
                    }}
            }).start();
    }
    static  void SmoothLookAt(AltoClef mod, float rotCoeffF,boolean ForceLook,Entity entity){
        SmoothLookAt(mod,rotCoeffF,ForceLook,entity.getEyePos());
    }
    static void SmoothLookDirectionaly(AltoClef mod,float rotCoeffF,boolean ForceLook){

        new Thread(() -> {
            Vec3d velVec = MinecraftClient.getInstance().player.getVelocity();
            double vel = velVec.lengthSquared();
            if (vel>0.01){
            float _innacuracy = 0;
            _innacuracy = 10.0f;
            float _plyPitch,_plyYaw;

            for (int i = 1; i <= 20; i++) {

                velVec = MinecraftClient.getInstance().player.getVelocity();
                vel = velVec.lengthSquared();
                Rotation _targetRotation = LookHelper.getLookRotation(mod, mod.getPlayer().getEyePos()
                        .add(0,7,0).add(velVec.multiply(100)));
                _plyPitch = mod.getPlayer().getPitch();
                _plyYaw = mod.getPlayer().getYaw();
                Rotation _plyRot = new Rotation(_plyYaw, _plyPitch);
                sleepSec(0.01);

                Rotation subtractRotation = _plyRot.subtract(_targetRotation);
                float rotCoeff = rotCoeffF;
                if (Math.abs(subtractRotation.getYaw())<_innacuracy*1.5 & Math.abs(subtractRotation.getPitch())<_innacuracy*1.5){
                    rotCoeff = rotCoeff*2.8f;
                }else{
                    rotCoeff = rotCoeff*3.2f;}
                Rotation lowedSubtractRotation = new Rotation(subtractRotation.getYaw()*rotCoeff,subtractRotation.getPitch()*rotCoeff);
                Rotation resultRotation = new Rotation(
                        _plyRot.subtract(lowedSubtractRotation).getYaw(),
                        _plyRot.subtract(lowedSubtractRotation).getPitch()

                );
                if(ForceLook){
                    LookHelper.lookAt(mod,resultRotation);
                }else{
                mod.getPlayer().setYaw(resultRotation.getYaw());
                mod.getPlayer().setPitch(resultRotation.getPitch());}
                //LookHelper.lookAt(mod, resultRotation);
            }}
        }).start();



    }

    static void SmoothLookDirectionaly(AltoClef mod,float rotCoeffF){
        SmoothLookDirectionaly(mod,rotCoeffF,false);
    }


    private static void sleepSec(double seconds) {
        try {
            Thread.sleep((int) (1000 * seconds));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }






}

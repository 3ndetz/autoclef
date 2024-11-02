package adris.altoclef.util.helpers;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import baritone.api.BaritoneAPI;
import baritone.api.utils.IPlayerContext;
import baritone.api.utils.RayTraceUtils;
import baritone.api.utils.Rotation;
import baritone.api.utils.RotationUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import org.lwjgl.opengl.GL11;

import java.util.Optional;

/**
 * Helper functions to interpret and change our player's look direction
 */
public interface LookHelper {
    float DEFAULT_SMOOTH_LOOK_SPEED = 1.0f;

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
        //lookAt(mod, r);
        smoothLook(mod, r);
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

    public static double getLookingProbability(Vec3d eyeFrom, Vec3d eyeTo, Vec3d RotationFrom){
        Vec3d toEntity = eyeTo.subtract(eyeFrom);
        double dot = toEntity.normalize().dotProduct(RotationFrom);
        return dot; //0.8 60 град, 0.9 30 град 0.95 15 град (точный взгляд
    }
    class WindMouseState {
        public static boolean isRotating = false;
        public static double windX = 0;
        public static double windY = 0;
        public static double veloX = 0;
        public static double veloY = 0;
        public static double currentX = 0;
        public static double currentY = 0;
        public static Rotation targetRotation = null;
        public static Rotation startRotation = null;
        public static float speed = DEFAULT_SMOOTH_LOOK_SPEED;
        public static long lastUpdateTime = 0;
        // Time in milliseconds before rotation stops if no new calls
        public static final long ROTATION_TIMEOUT = 200;
        // Distance threshold for deceleration
        public static final double DECELERATION_THRESHOLD = 20.0;
    }

    /**
     * Initiates a WindMouse rotation with improved smoothing
     */
    static void smoothLook(AltoClef mod, Rotation targetRot, float speed) {
        //if (mod.getMobDefenseChain().isDoingAcrobatics()) return;

        long currentTime = System.currentTimeMillis();
        boolean isNewRotation = !WindMouseState.isRotating ||
                currentTime - WindMouseState.lastUpdateTime > WindMouseState.ROTATION_TIMEOUT;

        // Reset state if this is a new rotation or significant target change
        if (isNewRotation || (WindMouseState.targetRotation != null &&
                Math.abs(normalizeAngle(targetRot.getYaw() - WindMouseState.targetRotation.getYaw())) > 5 ||
                Math.abs(targetRot.getPitch() - WindMouseState.targetRotation.getPitch()) > 5)) {

            WindMouseState.isRotating = true;
            WindMouseState.targetRotation = targetRot;
            WindMouseState.startRotation = getLookRotation(mod.getPlayer());
            WindMouseState.windX = 0;
            WindMouseState.windY = 0;
            WindMouseState.veloX = 0;
            WindMouseState.veloY = 0;
            WindMouseState.currentX = 0;
            WindMouseState.currentY = 0;
        }

        WindMouseState.speed = speed;
        WindMouseState.lastUpdateTime = currentTime;
    }
    static void smoothLook(AltoClef mod, Rotation targetRot){
        smoothLook(mod, targetRot, DEFAULT_SMOOTH_LOOK_SPEED);
    }

    /**
     * Updates the rotation with improved smoothing and natural deceleration
     */
    static boolean updateWindMouseRotation(AltoClef mod) {
        if (!WindMouseState.isRotating) return true;

        // Check rotation timeout
        long currentTime = System.currentTimeMillis();
        if (currentTime - WindMouseState.lastUpdateTime > WindMouseState.ROTATION_TIMEOUT) {
            WindMouseState.isRotating = false;
            return true;
        }

        // Base constants
        double baseWind = 2.5;
        double baseGravity = 9.0;
        double baseMaxStep = 15.0 * WindMouseState.speed;

        // Calculate current deltas
        double deltaYaw = normalizeAngle(WindMouseState.targetRotation.getYaw() -
                (WindMouseState.startRotation.getYaw() + (float)WindMouseState.currentX));
        double deltaPitch = WindMouseState.targetRotation.getPitch() -
                (WindMouseState.startRotation.getPitch() + (float)WindMouseState.currentY);

        // Calculate distance to target
        double distanceToTarget = Math.sqrt(deltaYaw * deltaYaw + deltaPitch * deltaPitch);

        // Check if we're close enough to target
        if (distanceToTarget < 0.01) {
            WindMouseState.isRotating = false;
            return true;
        }

        // Dynamic adjustment based on distance to target
        double distanceFactor = Math.min(1.0, distanceToTarget / WindMouseState.DECELERATION_THRESHOLD);

        // Adjust parameters based on distance
        double wind = baseWind * distanceFactor;
        double gravity = baseGravity * distanceFactor;
        double maxStep = baseMaxStep * (0.5 + 0.5 * distanceFactor); // Smoother deceleration

        // Update wind with reduced randomness when close to target
        WindMouseState.windX = WindMouseState.windX / Math.sqrt(3) +
                (Math.random() - 0.5) * wind * 2 * distanceFactor;
        WindMouseState.windY = WindMouseState.windY / Math.sqrt(3) +
                (Math.random() - 0.5) * wind * 2 * distanceFactor;

        // Apply gravity with smooth deceleration
        double gravityMultiplier = Math.pow(distanceFactor, 1.5); // Exponential deceleration
        WindMouseState.veloX += (Math.random() * 6 + 3) * (deltaYaw / 100.0) * gravity * gravityMultiplier;
        WindMouseState.veloY += (Math.random() * 6 + 3) * (deltaPitch / 100.0) * gravity * gravityMultiplier;

        // Apply wind with reduced effect near target
        WindMouseState.veloX += WindMouseState.windX * distanceFactor;
        WindMouseState.veloY += WindMouseState.windY * distanceFactor;

        // Add slight momentum dampening when close to target
        if (distanceFactor < 0.5) {
            WindMouseState.veloX *= 0.95;
            WindMouseState.veloY *= 0.95;
        }

        // Normalize velocity with smooth speed scaling
        double velocity = Math.sqrt(WindMouseState.veloX * WindMouseState.veloX +
                WindMouseState.veloY * WindMouseState.veloY);
        if (velocity > maxStep) {
            double scale = maxStep / velocity;
            WindMouseState.veloX *= scale;
            WindMouseState.veloY *= scale;
        }

        // Update position with additional smoothing for small movements
        WindMouseState.currentX += WindMouseState.veloX * (0.8 + 0.2 * distanceFactor);
        WindMouseState.currentY += WindMouseState.veloY * (0.8 + 0.2 * distanceFactor);

        // Apply new rotation
        float newYaw = WindMouseState.startRotation.getYaw() + (float)WindMouseState.currentX;
        float newPitch = clamp(WindMouseState.startRotation.getPitch() + (float)WindMouseState.currentY,
                -90.0f, 90.0f);

        //mod.getInputControls().forceLook(newYaw, newPitch);
        lookAt(mod, new Rotation(newYaw, newPitch));
        return false;
    }


    static void smoothLookSTANDART(AltoClef mod, Rotation targetRotation, float speed) {
        if (mod.getMobDefenseChain().isDoingAcrobatics()) return;

        // Get current rotation
        Rotation currentRotation = getLookRotation(mod.getPlayer());

        // Calculate angle differences
        float yawDiff = normalizeAngle(targetRotation.getYaw() - currentRotation.getYaw());
        float pitchDiff = targetRotation.getPitch() - currentRotation.getPitch();

        // Apply smooth interpolation
        float interpolationFactor = Math.min(1.0f, speed);

        // Add some human-like randomness to the movement
        float randomness = 0.05f;
        float randomFactor = 1.0f + (float)(Math.random() * randomness - randomness/2);

        // Calculate new rotation with interpolation and smoothing
        float newYaw = currentRotation.getYaw() + yawDiff * interpolationFactor * randomFactor;
        float newPitch = clamp(
                currentRotation.getPitch() + pitchDiff * interpolationFactor * randomFactor,
                -90.0f,
                90.0f
        );

        // Apply the rotation
        Rotation newRotation = new Rotation(newYaw, newPitch);
        mod.getInputControls().forceLook(newRotation.getYaw(), newRotation.getPitch());
    }

    /**
     * Smooth look at a position in 3D space
     */
    static void smoothLookAt(AltoClef mod, Vec3d position, float speed) {
        if (!cleanLineOfSight(position, 4.5)) return;

        Rotation targetRotation = getLookRotation(mod, position);
        smoothLook(mod, targetRotation, speed);
    }

    /**
     * Smooth look at an entity
     */
    static void smoothLookAt(AltoClef mod, Entity entity, float speed) {
        smoothLookAt(mod, entity.getEyePos(), speed);
    }
    static void smoothLookAt(AltoClef mod, Entity entity) {
        smoothLookAt(mod, entity.getEyePos(), DEFAULT_SMOOTH_LOOK_SPEED);
    }

        /**
         * Look in the direction of movement
         */
    static void smoothLookDirectional(AltoClef mod, float speed) {
        Vec3d velocity = mod.getPlayer().getVelocity();
        if (velocity.lengthSquared() <= 0.01) return;

        Vec3d targetPos = mod.getPlayer().getEyePos()
                .add(0, 7, 0)
                .add(velocity.multiply(100));

        smoothLookAt(mod, targetPos, speed);
    }

    // Utility methods
    private static float normalizeAngle(float angle) {
        angle = angle % 360.0f;
        if (angle >= 180.0f) {
            angle -= 360.0f;
        } else if (angle < -180.0f) {
            angle += 360.0f;
        }
        return angle;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }


    private static void sleepSec(double seconds) {
        try {
            Thread.sleep((int) (1000 * seconds));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }






}

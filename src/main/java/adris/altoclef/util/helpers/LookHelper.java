package adris.altoclef.util.helpers;

import adris.altoclef.AltoClef;
import adris.altoclef.util.slots.Slot;
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
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;


import java.util.Objects;
import java.util.Optional;

/**
 * Helper functions to interpret and change our player's look direction
 */
public interface LookHelper {
    static float DEFAULT_SMOOTH_LOOK_SPEED = 1.5f;
    static float DEFAULT_DECELERATION_THRESHOLD = 25.0f;
    /**
     * Calculate the reachable rotation for a given target and side.
     *
     * @param target the target block position
     * @param side the side direction
     * @return an optional rotation if reachable, otherwise empty
     */
    public static Optional<Rotation> getReach(BlockPos target, Direction side) {
        // Get the player context
        IPlayerContext context = BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext();

        // Declare the reachable rotation variable
        Optional<Rotation> reachableRotation;

        // Check if the side is null
        if (side == null) {
            // Calculate the reachable rotation from the player's position to the target position
            reachableRotation = RotationUtils.reachable(context, target);
        } else {
            // Calculate the center offset vector based on the side direction
            Vec3i sideVector = side.getVector();
            Vec3d centerOffset = new Vec3d(0.5 + sideVector.getX() * 0.5, 0.5 + sideVector.getY() * 0.5,
                    0.5 + sideVector.getZ() * 0.5);

            // Calculate the side point based on the center offset and target position
            Vec3d sidePoint = centerOffset.add(target.getX(), target.getY(), target.getZ());

            // Calculate the reachable rotation from the player's position to the side point
            reachableRotation = RotationUtils.reachableOffset(context, target, sidePoint,
                    context.playerController().getBlockReachDistance(), false);

            // Check if the reachable rotation is present
            if (reachableRotation.isPresent()) {
                // Calculate the camera position and vector to player position
                Vec3d cameraPos = context.player().getCameraPosVec(1.0F);
                Vec3d vecToPlayerPos = cameraPos.subtract(sidePoint);

                // Calculate the dot product between the vector to player position and the side vector
                double dotProduct = vecToPlayerPos.normalize().dotProduct(
                        new Vec3d(sideVector.getX(), sideVector.getY(), sideVector.getZ()));

                // Check if the dot product is less than 0
                if (dotProduct < 0) {
                    // Return an empty optional rotation
                    return Optional.empty();
                }
            }
        }

        // Return the reachable rotation
        return reachableRotation;
    }

    /**
     * Gets the reach for a given target position.
     *
     * @param target The target position.
     * @return An Optional containing the Rotation if reach is possible, or an empty Optional otherwise.
     */
    public static Optional<Rotation> getReach(BlockPos target) {

        // Delegate to the overloaded method with a null entity
        return getReach(target, null);
    }

    /**
     * Calculates a raycast from one entity to another.
     *
     * @param from The entity from which the raycast originates.
     * @param to The entity at which the raycast is aimed.
     * @param reachDistance The maximum distance the raycast can reach.
     * @return The result of the raycast.
     */
    public static EntityHitResult raycast(Entity from, Entity to, double reachDistance) {
        // Get the starting position of the raycast
        Vec3d start = getCameraPos(from);

        // Get the ending position of the raycast
        Vec3d end = getCameraPos(to);

        // Calculate the direction of the raycast
        Vec3d direction = end.subtract(start).normalize().multiply(reachDistance);

        // Get the bounding box of the target entity
        Box box = to.getBoundingBox();

        // Perform the raycast and return the result
        return ProjectileUtil.raycast(from, start, start.add(direction), box, entity -> entity.equals(to), 0);
    }

    /**
     * Check if an entity can see a player within a certain range, taking into account entity and player offsets.
     *
     * @param entity The entity to check.
     * @param player The player entity to check against.
     * @param maxRange The maximum range within which the entity can see the player.
     * @param entityOffset The offset of the entity.
     * @param playerOffset The offset of the player.
     * @return True if the entity can see the player, false otherwise.
     */
    public static boolean seesPlayer(Entity entity, Entity player, double maxRange, Vec3d entityOffset, Vec3d playerOffset) {
        return seesPlayerOffset(entity, player, maxRange, entityOffset, playerOffset)
                || seesPlayerOffset(entity, player, maxRange, entityOffset, playerOffset.add(0, -1, 0));
    }

    /**
     * Determines if the given entity can see the player within the specified range.
     *
     * @param entity the entity to check visibility from
     * @param player the player entity to check visibility to
     * @param maxRange the maximum range within which the player can be seen
     * @return true if the player is visible within the specified range, false otherwise
     */
    public static boolean seesPlayer(Entity entity, Entity player, double maxRange) {
        return seesPlayer(entity, player, maxRange, new Vec3d(0, 0, 0), new Vec3d(0, 0, 0));
    }

    /**
     * Checks if there is a clear line of sight between the start and end points for the given entity.
     *
     * @param entity The entity to check line of sight for.
     * @param start The starting position of the line of sight.
     * @param end The ending position of the line of sight.
     * @param maxRange The maximum range for the line of sight.
     * @return true if there is a clear line of sight, false otherwise.
     */
    public static boolean cleanLineOfSight(Entity entity, Vec3d start, Vec3d end, double maxRange) {
        // Perform a raycast between the start and end points with the given max range
        HitResult result = raycast(entity, start, end, maxRange);

        // Check the type of the hit result to determine if there is a clear line of sight
        return result.getType() == HitResult.Type.MISS;
    }

    /**
     * Checks if there is a clear line of sight between an entity and a specified location.
     *
     * @param entity The entity from which to check the line of sight.
     * @param end The end location to check the line of sight to.
     * @param maxRange The maximum range at which the line of sight can be checked.
     * @return True if there is a clear line of sight, false otherwise.
     */
    public static boolean cleanLineOfSight(Entity entity, Vec3d end, double maxRange) {
        // Get the starting position of the line of sight
        Vec3d start = getCameraPos(entity);

        // Check if there is a clear line of sight between the starting and end positions,
        // within the maximum range
        return cleanLineOfSight(entity, start, end, maxRange);
    }

    /**
     * Checks if there is a clear line of sight between the player and a given point.
     *
     * @param end The end point to check for line of sight.
     * @param maxRange The maximum range to check for line of sight.
     * @return True if there is a clear line of sight, false otherwise.
     */
    public static boolean cleanLineOfSight(Vec3d end, double maxRange) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        PlayerEntity playerEntity = minecraftClient.player;
        return cleanLineOfSight(playerEntity, end, maxRange);
    }

    /**
     * Checks if there is a clear line of sight between an entity and a block position within a given maximum range.
     *
     * @param entity The entity from which the line of sight is checked.
     * @param block The block position to check the line of sight to.
     * @param maxRange The maximum range to check for line of sight.
     * @return True if there is a clear line of sight, false otherwise.
     */
    public static boolean cleanLineOfSight(Entity entity, BlockPos block, double maxRange) {
        // Convert the block position to a Vec3d
        Vec3d targetPosition = WorldHelper.toVec3d(block);

        // Perform a raycast from the entity's camera position to the target position with the specified max range
        BlockHitResult hitResult = raycast(entity, getCameraPos(entity), targetPosition, maxRange);

        // Check the result of the raycast
        if (hitResult == null) {
            // No hit result, clear line of sight
            return true;
        } else {
            return switch (hitResult.getType()) {
                case MISS ->
                    // Missed the target, clear line of sight
                        true;
                case BLOCK ->
                    // Hit a block, check if it's the same as the target block
                        hitResult.getBlockPos().equals(block);
                case ENTITY ->
                    // Hit an entity, line of sight blocked
                        false;
            };
        }
    }

    /**
     * Convert a Rotation object to a Vec3d object.
     *
     * @param rotation the Rotation object to convert
     * @return the corresponding Vec3d object
     * @throws NullPointerException if the rotation is null
     */
    public static Vec3d toVec3d(Rotation rotation) throws NullPointerException {
        // make sure rotation is not null
        Objects.requireNonNull(rotation, "Rotation cannot be null");

        // calculate the look direction from the rotation
        return RotationUtils.calcLookDirectionFromRotation(rotation);
    }

    /**
     * Performs a raycast from the start point to the end point within a maximum range.
     *
     * @param entity the entity performing the raycast
     * @param start the starting point of the raycast
     * @param end the ending point of the raycast
     * @param maxRange the maximum range of the raycast
     * @return the result of the raycast
     */
    public static BlockHitResult raycast(Entity entity, Vec3d start, Vec3d end, double maxRange) {
        // Calculate the direction vector
        Vec3d direction = end.subtract(start);

        // Check if the direction vector length exceeds the maximum range
        if (direction.lengthSquared() > maxRange * maxRange) {
            // If it does, normalize the direction vector and multiply it by the maximum range
            direction = direction.normalize().multiply(maxRange);
            // Update the end point of the raycast to the new calculated position
            end = start.add(direction);
        }

        // Get the world of the entity
        World world = entity.getWorld();

        // Create a raycast context with the start and end points, shape type, fluid handling, and entity performing the raycast
        RaycastContext context = new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, entity);

        // Perform the raycast in the world and return the result
        return world.raycast(context);
    }

    /**
     * Performs a raycast from the entity's camera position to the specified end point with a maximum range.
     *
     * @param entity The entity performing the raycast
     * @param end The end point of the raycast
     * @param maxRange The maximum range of the raycast
     * @return The result of the raycast
     */
    public static BlockHitResult raycast(Entity entity, Vec3d end, double maxRange) {
        Vec3d start = getCameraPos(entity);
        return raycast(entity, start, end, maxRange);
    }

    /**
     * Get the look rotation of an entity.
     *
     * @param entity the entity to get the look rotation for
     * @return the look rotation of the entity
     */
    public static Rotation getLookRotation(Entity entity) {
        float pitch = entity.getPitch();
        float yaw = entity.getYaw();
        return new Rotation(yaw, pitch);
    }

    /**
     * Retrieves the look rotation of the player. If the player is null, returns a default rotation of (0, 0).
     *
     * @return The look rotation of the player.
     */
    public static Rotation getLookRotation() {
        // Retrieve the player instance
        PlayerEntity player = MinecraftClient.getInstance().player;

        // If the player is null, return a default rotation
        if (player == null) {
            return new Rotation(0, 0);
        }

        // Get the look rotation of the player
        return getLookRotation(player);
    }

    /**
     * Retrieves the camera position of the given entity. If the entity is a player and is sneaking, the sneaking eye
     * position is inferred. Otherwise, the default camera position of the entity is returned.
     *
     * @param entity The entity for which to retrieve the camera position.
     * @return The camera position of the entity.
     */
    public static Vec3d getCameraPos(Entity entity) {
        boolean isPlayerSneaking = entity instanceof PlayerEntity && entity.isSneaking();

        // If the entity is a player and is sneaking, infer the sneaking eye position
        if (isPlayerSneaking) {
            return RayTraceUtils.inferSneakingEyePosition(entity);
        } else {
            // Otherwise, return the default camera position of the entity
            return entity.getCameraPosVec(1.0F);
        }
    }

    /**
     * Retrieves the camera position vector of the player.
     *
     * @param mod The instance of the AltoClef mod.
     * @return The camera position vector.
     */
    public static Vec3d getCameraPos(AltoClef mod) {
        // Get the player context from the Baritone API
        IPlayerContext playerContext = BaritoneAPI.getProvider().getPrimaryBaritone().getPlayerContext();

        // Get the camera position vector from the player context
        return playerContext.player().getCameraPosVec(1);
    }

    /**
     * Calculates the closeness between an entity's look direction and a given position.
     *
     * @param entity The entity to calculate the closeness for.
     * @param pos The position to compare the look direction to.
     * @return The closeness value between the look direction and the position.
     */
    public static double getLookCloseness(Entity entity, Vec3d pos) {
        // Get the direction that the entity is facing
        Vec3d rotDirection = entity.getRotationVecClient();

        // Get the starting position of the entity's line of sight
        Vec3d lookStart = getCameraPos(entity);

        // Calculate the vector from the look start position to the given position
        Vec3d deltaToPos = pos.subtract(lookStart);

        // Normalize the delta vector to get the direction
        Vec3d deltaDirection = deltaToPos.normalize();

        // Calculate the dot product of the rotation direction and the delta direction
        return rotDirection.dotProduct(deltaDirection);
    }

    /**
     * Tries to avoid colliding with an interactable object. If a collision is detected, the function randomly changes
     * the orientation and returns false. If no collision is detected, the function returns true.
     *
     * @param mod The AltoClef object.
     * @return True if no collision is detected, false otherwise.
     */
    public static boolean tryAvoidingInteractable(AltoClef mod, boolean isCollidingNonInteractBlocks) {
        if (isCollidingInteractable(mod, isCollidingNonInteractBlocks)) {
            randomOrientation(mod);
            return false;
        }
        return true;
    }

    public static boolean tryAvoidingInteractable(AltoClef mod) {
        return tryAvoidingInteractable(mod, false);
    }

    /**
     * Determines whether an entity can see another entity with specified offsets.
     *
     * @param entity The entity that is trying to see the player.
     * @param player The player entity that is being looked at.
     * @param maxRange The maximum range within which the player can be seen.
     * @param offsetEntity The offset of the camera position for the entity.
     * @param offsetPlayer The offset of the camera position for the player.
     * @return True if the entity can see the player, false otherwise.
     */
    private static boolean seesPlayerOffset(Entity entity, Entity player, double maxRange, Vec3d offsetEntity,
                                            Vec3d offsetPlayer) {
        // Calculate the camera positions for the entity and player
        Vec3d entityCameraPos = getCameraPos(entity).add(offsetEntity);
        Vec3d playerCameraPos = getCameraPos(player).add(offsetPlayer);

        // Check if there is a clean line of sight between the entity and player within the specified range
        return cleanLineOfSight(entity, entityCameraPos, playerCameraPos, maxRange);
    }

    /**
     * Checks if the player is colliding with an interactable object.
     *
     * @param mod The instance of the AltoClef mod.
     * @return True if the player is colliding with an interactable object, false otherwise.
     */
    private static boolean isCollidingInteractable(AltoClef mod, boolean isCollidingNonInteractBlocks) {
        // Check if the player is in a screen other than the player screen
        if (!(mod.getPlayer().currentScreenHandler instanceof PlayerScreenHandler)) {
            // Get the item stack in the cursor slot
            ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();

            // Check if the cursor stack is not empty
            if (!cursorStack.isEmpty()) {
                // Find a slot in the player's inventory to move the cursor stack to
                Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
                moveTo.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));

                // Check if the cursor stack can be thrown away
                if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                    mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
                }

                // Find the garbage slot and move the cursor stack to it
                Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
                garbage.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));

                // Move the cursor stack to an undefined slot
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            } else {
                // Close the screen if the cursor stack is empty
                StorageHelper.closeScreen();
            }

            return true;
        }

        // Get the crosshair target
        HitResult result = MinecraftClient.getInstance().crosshairTarget;

        // Check if the crosshair target is null
        if (result == null) {
            return false;
        }

        // Check if the crosshair target is a block
        if (result.getType() == HitResult.Type.BLOCK) {
            // Get the block position from the crosshair target
            Vec3i resultGetPosOrigin = new Vec3i((int) result.getPos().getX(), (int) result.getPos().getY(),
                    (int) result.getPos().getZ());
            // Check if the block is an interactable block
            return isCollidingNonInteractBlocks || WorldHelper.isInteractableBlock(mod,
                    new BlockPos(resultGetPosOrigin));
        }
        // Check if the crosshair target is an entity
        else if (result.getType() == HitResult.Type.ENTITY && result instanceof EntityHitResult) {
            // Get the entity from the crosshair target
            Entity entity = ((EntityHitResult) result).getEntity();
            // Check if the entity is a merchant
            return entity instanceof MerchantEntity;
        }

        return false;
    }

    /**
     * Sets a random orientation for the given mod.
     *
     * @param mod The mod to set the orientation for.
     */
    public static void randomOrientation(AltoClef mod) {
        // Generate random rotation angles
        float randomRotationX = (float) (Math.random() * 360f);
        float randomRotationY = -90 + (float) (Math.random() * 180f);

        // Create a new Rotation object with the random angles
        Rotation r = new Rotation(randomRotationX, randomRotationY);

        // Set the mod to look at the rotation
        //lookAt(mod, r);
        smoothLook(mod, r);
    }

    /**
     * Checks if the given rotation is close to the current look rotation.
     *
     * @param mod The instance of the AltoClef class.
     * @param rotation The rotation to compare with the current look rotation.
     * @return True if the rotation is close to the current look rotation, false otherwise.
     */
    public static boolean isLookingAt(AltoClef mod, Rotation rotation) {
        return rotation.isReallyCloseTo(getLookRotation());
    }

    /**
     * Check if the player is looking at a specific block position.
     *
     * @param mod The instance of the AltoClef mod.
     * @param pos The block position to check.
     * @return True if the player is looking at the given block position, false otherwise.
     */
    public static boolean isLookingAt(AltoClef mod, BlockPos pos) {
        return mod.getClientBaritone().getPlayerContext().isLookingAt(pos);
    }

    /**
     * Updates the player's look direction and rotation.
     *
     * @param mod The instance of AltoClef.
     * @param rotation The desired rotation to look at.
     * @param withBaritone Whether to use Baritone to look.
     */
    public static void lookAt(AltoClef mod, Rotation rotation, boolean withBaritone) {
        if (withBaritone) {
            // Update the target rotation in the LookBehavior
            mod.getClientBaritone().getLookBehavior().updateTarget(rotation, true);
        }

        // Set the player's yaw and pitch
        mod.getPlayer().setYaw(rotation.getYaw());
        mod.getPlayer().setPitch(rotation.getPitch());
    }

    /**
     * Updates the player's look direction and rotation.
     *
     * @param mod The instance of AltoClef.
     * @param rotation The desired rotation to look at.
     */
    public static void lookAt(AltoClef mod, Rotation rotation) {
        // Update the target rotation in the LookBehavior
        mod.getClientBaritone().getLookBehavior().updateTarget(rotation, true);

        // Set the player's yaw and pitch
        mod.getPlayer().setYaw(rotation.getYaw());
        mod.getPlayer().setPitch(rotation.getPitch());
    }

    /**
     * Adjusts the player's look direction to the specified target position.
     *
     * @param mod The AltoClef instance.
     * @param toLook The position to look at.
     * @param withBaritone Whether to use Baritone to look.
     * @throws IllegalArgumentException if mod or toLook is null.
     */
    public static void lookAt(AltoClef mod, Vec3d toLook, boolean withBaritone) {
        if (mod == null || toLook == null) {
            throw new IllegalArgumentException("mod and toLook cannot be null");
        }

        Rotation targetRotation = getLookRotation(mod, toLook);
        lookAt(mod, targetRotation, withBaritone);
    }

    /**
     * Adjusts the player's look direction to the specified target position.
     *
     * @param mod The AltoClef instance.
     * @param toLook The position to look at.
     * @throws IllegalArgumentException if mod or toLook is null.
     */
    public static void lookAt(AltoClef mod, Vec3d toLook) {
        if (mod == null || toLook == null) {
            throw new IllegalArgumentException("mod and toLook cannot be null");
        }

        Rotation targetRotation = getLookRotation(mod, toLook);
        lookAt(mod, targetRotation, true);
    }

    /**
     * Adjusts the player's view to look at a specific location from a specific direction.
     *
     * @param mod The AltoClef mod instance.
     * @param toLook The position to look at.
     * @param side The direction to look from.
     * @param withBaritone Whether to use Baritone to look.
     */
    public static void lookAt(AltoClef mod, BlockPos toLook, Direction side, boolean withBaritone) {
        // Calculate the center coordinates of the target location
        double centerX = toLook.getX() + 0.5;
        double centerY = toLook.getY() + 0.5;
        double centerZ = toLook.getZ() + 0.5;

        // Adjust the center coordinates based on the specified side
        if (side != null) {
            double offsetX = side.getVector().getX() * 0.5;
            double offsetY = side.getVector().getY() * 0.5;
            double offsetZ = side.getVector().getZ() * 0.5;
            centerX += offsetX;
            centerY += offsetY;
            centerZ += offsetZ;
        }

        // Create a target vector based on the adjusted center coordinates
        Vec3d target = new Vec3d(centerX, centerY, centerZ);

        // Adjust the player's view to look at the target location
        lookAt(mod, target, withBaritone);
    }

    /**
     * Adjusts the player's view to look at a specific location from a specific direction.
     *
     * @param mod The AltoClef mod instance.
     * @param toLook The position to look at.
     * @param side The direction to look from.
     */
    public static void lookAt(AltoClef mod, BlockPos toLook, Direction side) {
        // Calculate the center coordinates of the target location
        double centerX = toLook.getX() + 0.5;
        double centerY = toLook.getY() + 0.5;
        double centerZ = toLook.getZ() + 0.5;

        // Adjust the center coordinates based on the specified side
        if (side != null) {
            double offsetX = side.getVector().getX() * 0.5;
            double offsetY = side.getVector().getY() * 0.5;
            double offsetZ = side.getVector().getZ() * 0.5;
            centerX += offsetX;
            centerY += offsetY;
            centerZ += offsetZ;
        }

        // Create a target vector based on the adjusted center coordinates
        Vec3d target = new Vec3d(centerX, centerY, centerZ);

        // Adjust the player's view to look at the target location
        lookAt(mod, target, true);
    }

    /**
     * Looks at the specified block position.
     *
     * @param mod The AltoClef instance.
     * @param toLook The block position to look at.
     * @param withBaritone Whether to use Baritone to look.
     */
    public static void lookAt(AltoClef mod, BlockPos toLook, boolean withBaritone) {
        lookAt(mod, toLook, null, withBaritone);
    }

    /**
     * Looks at the specified block position.
     *
     * @param mod The AltoClef instance.
     * @param toLook The block position to look at.
     */
    public static void lookAt(AltoClef mod, BlockPos toLook) {
        lookAt(mod, toLook, null, true);
    }

    /**
     * Calculates the rotation needed for a player to look at a specified point.
     *
     * @param mod The instance of the main mod class.
     * @param toLook The coordinates to look at.
     * @return The rotation needed to look at the specified point.
     */
    public static Rotation getLookRotation(AltoClef mod, Vec3d toLook) {
        // Get the player's head position
        Vec3d playerHead = mod.getClientBaritone().getPlayerContext().playerHead();

        // Get the player's current rotations
        Rotation playerRotations = mod.getClientBaritone().getPlayerContext().playerRotations();

        // Calculate the rotation needed to look at the specified point
        return RotationUtils.calcRotationFromVec3d(playerHead, toLook, playerRotations);
    }

    /**
     * Returns the rotation needed to look at a specified position.
     *
     * @param mod The AltoClef mod instance.
     * @param toLook The position to look at, specified by its BlockPos.
     * @return The Rotation object representing the rotation needed to look at the position.
     */
    public static Rotation getLookRotation(AltoClef mod, BlockPos toLook) {
        // Convert BlockPos to Vec3d
        Vec3d targetPosition = WorldHelper.toVec3d(toLook);

        // Delegate to the overloaded version of getLookRotation
        return getLookRotation(mod, targetPosition);
    }

    public static boolean shootReady(AltoClef mod, Entity target) {
        if (target == null) {
            return false;
        }
        if (LookHelper.cleanLineOfSight(target.getEyePos(), 100)) {
            return true;
        } else {
            if ((LookHelper.cleanLineOfSight(mod.getPlayer(), mod.getPlayer().getEyePos().add(0, 0.5, 0),
                    mod.getPlayer().getEyePos().relativize(new Vec3d(5, 0, 0)), 10)) &&
                    (LookHelper.cleanLineOfSight(target, target.getEyePos().add(0, 1.5, 0),
                            target.getEyePos().add(0, 3, 0), 10))) {
                return true;
            } else {
                return false;
            }
        }
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
        public static final double DECELERATION_THRESHOLD = DEFAULT_DECELERATION_THRESHOLD;
    }

    /**
     * Initiates a WindMouse rotation with improved smoothing
     */
    public static void smoothLook(AltoClef mod, Rotation targetRot, float speed) {
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
    public static void smoothLook(AltoClef mod, Rotation targetRot){
        smoothLook(mod, targetRot, DEFAULT_SMOOTH_LOOK_SPEED);
    }


    /**
     * Updates the rotation with improved smoothing and natural deceleration
     */
    public static boolean updateWindMouseRotation(AltoClef mod) {
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
        double baseMaxStep = 10 * WindMouseState.speed;

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


    public static void smoothLookSTANDART(AltoClef mod, Rotation targetRotation, float speed) {
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
    public static void smoothLookAt(AltoClef mod, Vec3d position, float speed) {
        if (!cleanLineOfSight(position, 4.5)) return;

        Rotation targetRotation = getLookRotation(mod, position);
        smoothLook(mod, targetRotation, speed);
    }

    /**
     * Smooth look at an entity
     */
    public static void smoothLookAt(AltoClef mod, Entity entity) {
        smoothLookAt(mod, entity.getEyePos(), DEFAULT_SMOOTH_LOOK_SPEED);
    }
    public static void smoothLookAt(AltoClef mod, Vec3d position) {
        smoothLookAt(mod, position, DEFAULT_SMOOTH_LOOK_SPEED);
    }
    /**
     * Look in the direction of movement
     */
    public static void smoothLookDirectional(AltoClef mod, float speed) {
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

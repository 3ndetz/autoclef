package adris.altoclef.tasks.misc;

import adris.altoclef.AltoClef;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * A wrapper class that can hold different types of positions
 */
public class PositionWrapper {
    private final Vec3d position;
    private final Entity entity;
    private final BlockPos blockPos;
    private final boolean hasPosition;

    private PositionWrapper(Vec3d position, Entity entity, BlockPos blockPos, boolean hasPosition) {
        this.position = position;
        this.entity = entity;
        this.blockPos = blockPos;
        this.hasPosition = hasPosition;
    }

    public static PositionWrapper ofVec3d(Vec3d pos) {
        return new PositionWrapper(pos, null, null, true);
    }

    public static PositionWrapper ofEntity(Entity entity) {
        return new PositionWrapper(null, entity, null, true);
    }

    public static PositionWrapper ofBlockPos(BlockPos blockPos) {
        return new PositionWrapper(null, null, blockPos, true);
    }

    public static PositionWrapper empty() {
        return new PositionWrapper(null, null, null, false);
    }

    public boolean isValid(AltoClef mod){
        if (position != null) {
            return !mod.getBlockTracker().unreachable(BlockPos.ofFloored(position));
        }
        if (entity != null) return entity.isAlive() && mod.getEntityTracker().isEntityReachable(entity);
        if (blockPos != null) {
            return mod.getBlockTracker().blockIsValid(blockPos);
        }
        return false;
    }

    public Vec3d getPos() {
        if (position != null) return position;
        if (entity != null) return entity.getPos();
        if (blockPos != null) return WorldHelper.toVec3d(blockPos);
        return null;
    }

    public boolean hasPosition() {
        return hasPosition;
    }
}
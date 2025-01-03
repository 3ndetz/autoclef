package adris.altoclef.mixins;

import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow private float yaw;
    @Shadow private float pitch;
    @Shadow private Vec3d pos;
    @Shadow private Entity focusedEntity;
    @Shadow private float lastCameraY;
    @Shadow private float cameraY;

    @Shadow
    protected abstract void setPos(double x, double y, double z);

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    // Add fields for smooth transitions
    private Vec3d targetPos = Vec3d.ZERO;
    private Vec3d lastPos = Vec3d.ZERO;
    private float lastYaw = 0.0F;
    private float lastPitch = 0.0F;
    private float smoothSpeed = 0.12F; // Position smoothing
    private float rotationSmoothSpeed = 0.08F; // Rotation smoothing

    @Inject(at = @At("TAIL"), method = "update")
    private void onUpdate(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (thirdPerson && !inverseView) {
            // GTA-style camera configuration
            float shoulderOffset = 0.7F;    // How far right from player
            float heightOffset = 1f;      // Additional height adjustment
            float distanceBack = 2f;     // How far back

// Calculate smooth interpolated base position
            double x = MathHelper.lerp(tickDelta, focusedEntity.prevX, focusedEntity.getX());
            double y = MathHelper.lerp(tickDelta, focusedEntity.prevY, focusedEntity.getY())
                    + MathHelper.lerp(tickDelta, this.lastCameraY, this.cameraY);
            double z = MathHelper.lerp(tickDelta, focusedEntity.prevZ, focusedEntity.getZ());

// Calculate shoulder offset (unchanged)
            double angleRad = Math.toRadians(this.yaw + 180);
            double shoulderX = Math.cos(angleRad) * shoulderOffset;
            double shoulderZ = Math.sin(angleRad) * shoulderOffset;

// Get the head pitch to adjust camera height based on looking up/down (unchanged)
            float pitchRadians = (float) Math.toRadians(this.pitch);
            float verticalAdjustment = (float) (Math.sin(pitchRadians)) * heightOffset
                    - (float) (Math.cos(Math.toRadians(this.pitch - 180))) * heightOffset;

// FIX: Adjust horizontalDistance so it stays behind the player and moves closer when looking up
            float horizontalDistance = - (float) Math.abs(distanceBack) * (float) Math.cos(pitchRadians);

// Calculate target position with pitch influence (shoulder offset remains on the right)
            Vec3d newTargetPos = new Vec3d(
                    x + shoulderX - Math.sin(Math.toRadians(this.yaw)) * horizontalDistance,
                    y + verticalAdjustment,
                    z + shoulderZ + Math.cos(Math.toRadians(this.yaw)) * horizontalDistance
            );
            // Initialize lastPos if needed
            if (lastPos == Vec3d.ZERO) {
                lastPos = newTargetPos;
                lastYaw = this.yaw;
                lastPitch = this.pitch;
            }

            // Smooth position transition
            double smoothX = MathHelper.lerp(tickDelta, lastPos.x, newTargetPos.x);
            double smoothY = MathHelper.lerp(tickDelta, lastPos.y, newTargetPos.y);
            double smoothZ = MathHelper.lerp(tickDelta, lastPos.z, newTargetPos.z);

            smoothX = MathHelper.lerp(smoothSpeed, lastPos.x, smoothX);
            smoothY = MathHelper.lerp(smoothSpeed, lastPos.y, smoothY);
            smoothZ = MathHelper.lerp(smoothSpeed, lastPos.z, smoothZ);
            // Smooth rotation transition
            float targetYaw = this.yaw;
            float targetPitch = this.pitch;

            // Normalize yaw angles to prevent spinning
            while (targetYaw - lastYaw > 180.0F) targetYaw -= 360.0F;
            while (targetYaw - lastYaw < -180.0F) targetYaw += 360.0F;

            float smoothYaw = MathHelper.lerp(tickDelta, lastYaw, targetYaw);
            float smoothPitch = MathHelper.lerp(tickDelta, lastPitch, targetPitch);

            smoothYaw = MathHelper.lerp(rotationSmoothSpeed, lastYaw, smoothYaw);
            smoothPitch = MathHelper.lerp(rotationSmoothSpeed, lastPitch, smoothPitch);

            // Update last positions and rotations
            lastPos = new Vec3d(smoothX, smoothY, smoothZ);
            lastYaw = smoothYaw;
            lastPitch = smoothPitch;

            // Apply smoothed position and rotation
            this.setPos(smoothX, smoothY, smoothZ);
            this.setRotation(smoothYaw, smoothPitch);
        } else {
            // Reset smoothing when not in third person
            lastPos = Vec3d.ZERO;
            lastYaw = this.yaw;
            lastPitch = this.pitch;
        }
    }
}
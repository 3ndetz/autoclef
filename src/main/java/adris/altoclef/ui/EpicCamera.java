package adris.altoclef.ui;

import adris.altoclef.util.time.TimerReal;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import baritone.api.utils.Rotation;
import java.util.LinkedList;
import java.util.Queue;

/*
 * EpicCamera approach:
 * - Camera should be positioned behind the player's head (NEED FIX)
 * - Camera should move EXACTLY RELATIVE to the player's head with straight orbit (NEED FIX)
 * - Camera should be side offesed (shoulder) to the right (WORKS GOOD)
 * 
 * Pitch and absolute positions explanation:
 * where x is forward axis relative to player's forward, y is just height
 * pitch is current player's head pitch
 * 
 * How it needs to be:
 * 
 * For the pitch = -90 (look up):
 * x: 0.5 (most forward), y: -1 (min height)
 * For the pitch = 0 (is looking forward):
 * x: -1 (most backward), y: 0 (height as player's head)
 * For the pitch = 90 (look down):
 * x: -0.5 (most half-back), y: 1 (max height)
 * 
 * (do not erase explanation upper)
 * 
 * It works like this now, but it's not perfect. TODO FAR FUTURE fix it.
 * Now we need another TODO for the proper smoothing system:
 * - smoother should store a list of last positions and last pitches and yaws, like keyframes
 * - smoother should update position smoothly following this keyframes
 * (it should follow exactly player move path, but with some delay that is specified by delay time)
 * We should not remove previous realisation, but just append new keyframe smoothing to it.
 * 
 * 
 */
public class EpicCamera {
    private static final EpicCamera INSTANCE = new EpicCamera();

    // Base camera configuration
    private static final float BASE_RIGHT = 0.7f;    // Right shoulder offset
    private static final float BASE_BACK = 4.0f;     // Default orbit radius
    private static final float BASE_UP = 0.5f;       // Base height offset from eye level

    // Zoom configuration
    private static final float MIN_BACK = 2.0f;
    private static final float MAX_BACK = 8.0f;
    private static final float ZOOM_STEP = 0.5f;

    // Camera behavior
    private static final float SMOOTH_FACTOR = 0.12f;

    // Current state
    private Vec3d lastPos = Vec3d.ZERO;
    private float lastYaw = 0;
    private float lastPitch = 0;
    private float orbitRadius = BASE_BACK;
    private float targetRadius = BASE_BACK;
    public TimerReal _lastPosTimer = new TimerReal(2);
    private long lastUpdateTime = System.currentTimeMillis();

    // Keyframe smoothing configuration
    private static final int MAX_KEYFRAMES = 20;
    private static final double KEYFRAME_LIFETIME_MS = 1000.0; // How long keyframes stay in history

    private static class CameraKeyframe {
        final Vec3d position;
        final float yaw;
        final float pitch;
        final long timestamp;

        CameraKeyframe(Vec3d position, float yaw, float pitch) {
            this.position = position;
            this.yaw = yaw;
            this.pitch = pitch;
            this.timestamp = System.currentTimeMillis();
        }

        double getAge() {
            return (System.currentTimeMillis() - timestamp) / KEYFRAME_LIFETIME_MS;
        }
    }

    private final Queue<CameraKeyframe> keyframes = new LinkedList<>();

    private EpicCamera() {}

    public static EpicCamera getInstance() {
        return INSTANCE;
    }
    public void freezeCam() {
        _lastPosTimer.reset();
    }
    public void freezeCam(double interval) {
        _lastPosTimer.setInterval(interval);
        _lastPosTimer.reset();
    }
    public void forceStopFreezing(){
        _lastPosTimer.forceElapse();
    }
    public void adjustZoom(float scrollAmount) {
        targetRadius = MathHelper.clamp(
            targetRadius - scrollAmount * ZOOM_STEP,
            MIN_BACK,
            MAX_BACK
        );
    }

    public CameraUpdate getUpdate(Entity focusedEntity, float tickDelta, Rotation modifierRotation) {
        if (focusedEntity == null) {
            reset();
            return null;
        }

        // Calculate time delta for smooth interpolation
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min(0.1f, (currentTime - lastUpdateTime) / 1000.0f);
        float smoothFactor = SMOOTH_FACTOR * deltaTime * 60;
        lastUpdateTime = currentTime;

        // Get head position
        Vec3d headPos = new Vec3d(
            interpolate(focusedEntity.prevX, focusedEntity.getX(), tickDelta),
            interpolate(focusedEntity.prevY, focusedEntity.getY(), tickDelta) + focusedEntity.getStandingEyeHeight(),
            interpolate(focusedEntity.prevZ, focusedEntity.getZ(), tickDelta)
        );

        // Get rotation angles
        float yaw = focusedEntity.getYaw();
        float pitch = focusedEntity.getPitch();
        if (modifierRotation != null) {
            if (modifierRotation.getYaw() != -500) yaw = modifierRotation.getYaw();
            if (modifierRotation.getPitch() != -500) pitch = modifierRotation.getPitch();
        }

        // Smooth orbit radius
        orbitRadius = MathHelper.lerp(smoothFactor, orbitRadius, targetRadius);

        // Calculate orbital position
        float yawRad = (float) Math.toRadians(yaw + 180);

        // Forward/backward position correction
        // At pitch 0 (looking forward): Most backward (-1)
        // At pitch -90 (looking up): Slightly forward (0.5)
        // At pitch 90 (looking down): Half-back (-0.5)
        float pitchFactor = pitch / 90.0f; // -1 to 1
        float xFactor = -1.0f; // Start at most backward position
        
        if (pitchFactor > 0) { // Looking up
            // Interpolate from -1 (at pitch 0) to 0.5 (at pitch -90)
            xFactor = -0.5f + (Math.abs(pitchFactor) * 0.65f);
        } 
        if (pitchFactor <= 0) { // Looking down
            xFactor = -0.5f + (Math.abs(pitchFactor) * 0.35f);
        } 
        // NOT MOVING WHEN THIS SHIT
        //else { // Looking down
        //    // Interpolate from -1 (at pitch 0) to -0.5 (at pitch 90)
        //    xFactor = -1.0f + (pitchFactor * 0.5f);
        // }

        // Apply the forward/back offset
        float xOffset = orbitRadius * xFactor;

        // Height stays the same as it works correctly
        float heightOffset;
        if (pitchFactor <= 0) {
            heightOffset = BASE_UP + (orbitRadius * 0.5f * pitchFactor);
        } else {
            heightOffset = BASE_UP + (orbitRadius * 0.1f * pitchFactor);
        }
        // Calculate the final position with corrected xOffset
        Vec3d orbitPos = new Vec3d(
            Math.sin(yawRad) * xOffset,
            heightOffset,
            -Math.cos(yawRad) * xOffset
        );

        // Add right shoulder offset
        Vec3d rightOffset = new Vec3d(
            Math.cos(yawRad) * BASE_RIGHT,
            0,
            Math.sin(yawRad) * BASE_RIGHT
        );

        // Combine all offsets
        Vec3d targetPos;
        if (_lastPosTimer.elapsed() || lastPos == null || lastPos == Vec3d.ZERO) {
            targetPos = headPos.add(orbitPos).add(rightOffset);
        } else {
            yaw = lastYaw;
            pitch = lastPitch;
            targetPos = lastPos;
        }

        // Clean up old keyframes
        while (!keyframes.isEmpty() && keyframes.peek().getAge() > 1.0) {
            keyframes.poll();
        }

        // Add new keyframe if not frozen
        if (!_lastPosTimer.elapsed()) {
            // If frozen, use last stored position
            if (!keyframes.isEmpty()) {
                CameraKeyframe lastFrame = ((LinkedList<CameraKeyframe>)keyframes).getLast();
                return new CameraUpdate(lastFrame.position, lastFrame.yaw, lastFrame.pitch);
            }
        } else {
            // Add new keyframe
            if (keyframes.size() >= MAX_KEYFRAMES) {
                keyframes.poll();
            }
            keyframes.offer(new CameraKeyframe(targetPos, yaw, pitch));
        }

        // Calculate smoothed position and rotation from keyframes
        if (!keyframes.isEmpty()) {
            Vec3d smoothedPos = targetPos;
            float smoothedYaw = yaw;
            float smoothedPitch = pitch;
            
            double totalWeight = 0;
            Vec3d weightedPos = Vec3d.ZERO;
            double weightedYaw = 0;
            double weightedPitch = 0;

            for (CameraKeyframe frame : keyframes) {
                double age = frame.getAge();
                double weight = 1.0 - age;
                if (weight <= 0) continue;

                totalWeight += weight;
                weightedPos = weightedPos.add(frame.position.multiply(weight));
                
                // Handle yaw wrapping
                double yawDiff = frame.yaw - yaw;
                while (yawDiff > 180.0) yawDiff -= 360.0;
                while (yawDiff < -180.0) yawDiff += 360.0;
                
                weightedYaw += (yaw + yawDiff) * weight;
                weightedPitch += frame.pitch * weight;
            }

            if (totalWeight > 0) {
                smoothedPos = weightedPos.multiply(1.0 / totalWeight);
                smoothedYaw = (float)(weightedYaw / totalWeight);
                smoothedPitch = (float)(weightedPitch / totalWeight);
            }

            // Update last positions for old smoothing system compatibility
            lastPos = smoothedPos;
            lastYaw = smoothedYaw;
            lastPitch = smoothedPitch;

            return new CameraUpdate(smoothedPos, smoothedYaw, smoothedPitch);
        }

        // Fallback to direct position if no keyframes
        lastPos = targetPos;
        lastYaw = yaw;
        lastPitch = pitch;
        return new CameraUpdate(targetPos, yaw, pitch);
    }

    private double interpolate(double prev, double current, float tickDelta) {
        return prev + (tickDelta * (current - prev));
    }

    public void reset() {
        lastPos = Vec3d.ZERO;
        lastUpdateTime = System.currentTimeMillis();
        orbitRadius = targetRadius;
        keyframes.clear();
    }

    public static class CameraUpdate {
        private final Vec3d position;
        private final float yaw;
        private final float pitch;

        public CameraUpdate(Vec3d position, float yaw, float pitch) {
            this.position = position;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public Vec3d getPosition() { return position; }
        public float getYaw() { return yaw; }
        public float getPitch() { return pitch; }
    }
}

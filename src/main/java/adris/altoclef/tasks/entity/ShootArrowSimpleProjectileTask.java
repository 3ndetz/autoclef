package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.speedrun.BeatMinecraft2Task;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.Rotation;
import baritone.api.utils.input.Input;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;

import static adris.altoclef.util.helpers.ItemHelper.ARROWS;
import static net.minecraft.item.CrossbowItem.isCharged;

public class ShootArrowSimpleProjectileTask extends Task {

    private final Entity target;
    private boolean shooting = false;
    private boolean shot = false;
    private boolean failed = false;
    private Item _rangedItem = Items.BOW;
    private final TimerGame _shotTimer = new TimerGame(0.7);

    public ShootArrowSimpleProjectileTask(Entity target) {
        this.target = target;
    }

    @Override
    protected void onStart(AltoClef mod) {
        shooting = false;
    }

    private static Rotation calculateThrowLook(AltoClef mod, Entity target) {
        // Velocity based on bow charge.
        float velocity = (mod.getPlayer().getItemUseTime() - mod.getPlayer().getItemUseTimeLeft()) / 20f;
        velocity = (velocity * velocity + velocity * 2) / 3;
        if (velocity > 1) velocity = 1;
        //boolean highAng = false;
        //boolean highAng = shouldUseHighAngle(mod, target);
        boolean highAng = !LookHelper.cleanLineOfSight(target.getEyePos(),100);
        //shouldUseHighAngle
        //if(!LookHelper.cleanLineOfSight(target.getEyePos(),100)) highAng = true;
        double velMult;
        if(highAng)
            velMult = 100;
        else velMult = 11.4;

        double velX = (target.getPos().getX() - target.prevX)*velMult;
        double velZ = (target.getPos().getZ() - target.prevZ)*velMult;
        //double velZ = target.getVelocity().getZ()*5;
        // Positions
        double posX = target.getPos().getX() + (target.getPos().getX() - target.prevX) +velX;
        double posY = target.getPos().getY() + (target.getPos().getY() - target.prevY);
        double posZ = target.getPos().getZ() + (target.getPos().getZ() - target.prevZ) + velZ;
        //Debug.logMessage("VelX = "+(target.getPos().getX() - target.prevX)+" prevX = "+target.prevX);



        // Adjusting for hitbox heights
        posY -= 1.9f - target.getHeight();

        double relativeX = posX - mod.getPlayer().getX();
        double relativeY = posY - mod.getPlayer().getY();
        double relativeZ = posZ - mod.getPlayer().getZ();

        // Calculate the pitch
        double hDistance = Math.sqrt(relativeX * relativeX + relativeZ * relativeZ);
        double hDistanceSq = hDistance * hDistance;
        float g = 0.006f;
        float velocitySq = velocity * velocity;
        float pitch = mod.getPlayer().getPitch();

        if (highAng){ //режим артиллерии
            velocitySq = velocitySq*0.7f; //скорость снаряда сильно падает когда он вверху, учитываем это
            pitch = (float) -Math.toDegrees(Math.atan2((velocitySq + Math.sqrt(velocitySq * velocitySq - g * (g * hDistanceSq + 2 * relativeY * velocitySq))),(g * hDistance)));}
        else{
            pitch = (float) -Math.toDegrees(Math.atan((velocitySq - Math.sqrt(velocitySq * velocitySq - g * (g * hDistanceSq + 2 * relativeY * velocitySq))) / (g * hDistance)));}
        // Set player rotation
        if (Float.isNaN(pitch)) {
            return new Rotation(target.getYaw(), target.getPitch());
        } else {
            return new Rotation(Vec3dToYaw(mod, new Vec3d(posX, posY, posZ)), pitch);
        }
    }

    private static float Vec3dToYaw(AltoClef mod, Vec3d vec) {
        return (mod.getPlayer().getYaw() +
                MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(vec.getZ() - mod.getPlayer().getZ(), vec.getX() - mod.getPlayer().getX())) - 90f - mod.getPlayer().getYaw()));
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if(hasArrows(mod)){
            //setDebugState("DON'T HAVE BOW OR ARROWS!");
            //return null;
            if(mod.getItemStorage().hasItemInventoryOnly(Items.BOW)){
                setDebugState("Bow");
                _rangedItem = Items.BOW;
            } else if (mod.getItemStorage().hasItemInventoryOnly(Items.CROSSBOW)) {
                setDebugState("Crossbow (EXPERIMENTAL)");
                _rangedItem = Items.CROSSBOW;
            } else {
                setDebugState("DON'T HAVE RANGED WEAPON!");
                failed = true;
                return null;
            }
        }else{
            setDebugState("DON'T HAVE ARROWS!");
            failed = true;
            return null;
        }
        int useTime = mod.getPlayer().getItemUseTime();
        if(useTime <= 1){
            //LookHelper.smoothLookAt(mod, target);
        }else {
            Rotation lookTarget = calculateThrowLook(mod, target);
            LookHelper.smoothLook(mod, lookTarget);
        }

        mod.getSlotHandler().forceEquipItem(_rangedItem);

        // check if we are holding a bow

        boolean charged;
        boolean projectileReady = false;
        boolean isBow = _rangedItem == Items.BOW;
        //Debug.logMessage(mod.getPlayer().getActiveItem().getItem().toString());
        if (isBow) {

            charged = mod.getPlayer().getActiveItem().getItem() == _rangedItem && useTime > 20;
        }else if (StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot()).getItem() == _rangedItem){
            // TODO untested
            // } else if(mod.getPlayer().getMainHandStack().getItem() == _rangedItem) {
            if (_rangedItem == Items.CROSSBOW) {
                projectileReady = isCharged(StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot()));

                setDebugState("Crossbow ready="+projectileReady+ " use=" +useTime);
                if(!projectileReady){
                    charged = useTime > 40;
                    if(!charged) {
                        setDebugState("charging crossbow...");
                        mod.getInputControls().hold(Input.CLICK_RIGHT);
                        return null;
                    } else {
                        mod.getInputControls().release(Input.CLICK_RIGHT);
                    }
                } else {
                    setDebugState("crossbow ready!");
                    charged = true;
                }
            } else {
                projectileReady = true;
                charged = true;
            }
        } else {
            setDebugState("Item not active");
            charged = false;
            return null;
        }


        //if (LookHelper.isLookingAt(mod, lookTarget) && !shooting) {
        //    mod.getInputControls().hold(Input.CLICK_RIGHT);
        //    shooting = true;
        //    shotTimer.reset();
        //}
        if (isBow) {
            if (!shooting || _shotTimer.elapsed()) { //(LookHelper.isLookingAt(mod, lookTarget) && !shooting) {
                mod.getInputControls().hold(Input.CLICK_RIGHT);
                shooting = true;
                _shotTimer.reset();
            }
        } else {
            shooting = true;
        }

        if (shooting && charged) {
            List<ProjectileEntity> arrows = mod.getEntityTracker().getTrackedEntities(ProjectileEntity.class);
            // If any of the arrows belong to us and are moving, do not shoot yet
            // Prevents from shooting multiple arrows to the same target
            for (ProjectileEntity arrow : arrows) {
                if (arrow.getOwner() == mod.getPlayer()) {
                    Vec3d velocity = arrow.getVelocity();
                    Vec3d delta = target.getPos().subtract(arrow.getPos());
                    boolean isMovingTowardsTarget = velocity.dotProduct(delta) > 0;
                    if (isMovingTowardsTarget) {
                        return null;
                    }
                }
            }

            if (BeatMinecraft2Task.getConfig().renderDistanceManipulation && MinecraftClient.getInstance().options.getSimulationDistance().getValue() < 32) {
                // For farther entities, the arrow may get stuck in the air, so we need to increase the simulation distance
                MinecraftClient.getInstance().options.getSimulationDistance().setValue(32);
            }
            if(isBow) {
                mod.getInputControls().release(Input.CLICK_RIGHT); // Release the arrow
                shot = true;
            } else if (projectileReady){
                Debug.logMessage("SHOT");
                mod.getInputControls().tryPress(Input.CLICK_RIGHT);
                shot = true;
            } else {
                Debug.logMessage("SHOT fdf");
                //mod.getInputControls().tryPress(Input.CLICK_RIGHT);
            }

        }
        setDebugState("Charging?");
        return null;
    }
    public static boolean hasArrows(AltoClef mod){
        List<Item> requiredArrows = Arrays.asList(ARROWS);
        if (requiredArrows.stream().anyMatch(mod.getItemStorage()::hasItemInventoryOnly)) {
            return true;
        }
        return false;
    }
    public static boolean hasShootingWeapon(AltoClef mod){
        if (Arrays.stream(ItemHelper.ShootWeapons).anyMatch(mod.getItemStorage()::hasItemInventoryOnly)) {
            return true;
        }
        return false;
    }
    public static boolean readyForBow(AltoClef mod){
        return hasArrows(mod) && mod.getItemStorage().hasItemInventoryOnly(Items.BOW);
    }
    public static boolean readyForCrossbow(AltoClef mod){
        return hasArrows(mod) && mod.getItemStorage().hasItemInventoryOnly(Items.CROSSBOW);
    }
    public static boolean readyForRanged(AltoClef mod){
        return hasArrows(mod) && (mod.getItemStorage().hasItemInventoryOnly(Items.BOW) || mod.getItemStorage().hasItemInventoryOnly(Items.CROSSBOW));
    }
    public static boolean checkRangedAttackTrajectory(AltoClef mod, Entity target){

        Vec3d playerPos = mod.getPlayer().getEyePos();
        Vec3d targetPos = target.getEyePos();
        double distance = playerPos.distanceTo(targetPos);

        // Check if target is too far
        if (distance > 100) return false;

        // Check low angle trajectory first (direct line)
        if (LookHelper.cleanLineOfSight(target.getEyePos(), distance)) {
            return true;
        }


        for (int i = 0; i <= 10; i++) {
            double x = playerPos.x + (targetPos.x - playerPos.x);
            double z = playerPos.z + (targetPos.z - playerPos.z);
            // Simulate parabolic arc
            int y_p = (int) playerPos.y + i;
            int y_t = (int) targetPos.y + i;
            if (!WorldHelper.isAir(mod, new BlockPos((int) playerPos.x, y_p, (int) playerPos.z))
                    || !WorldHelper.isAir(mod, new BlockPos((int) targetPos.x, y_t, (int) targetPos.z))
            ) {
                return false;
            }
        }

        // Check high angle trajectory if low angle is blocked
        Vec3d direction = targetPos.subtract(playerPos).normalize();
        double heightAtApex = Math.min(playerPos.y + 20, 319); // Max Y level is 319 in Minecraft

        int checkPoints = 10;
        // Check parabolic arc for high angle
        for (int i = 1; i <= checkPoints; i++) {
            double progress = (double) i / checkPoints;
            double x = playerPos.x + (targetPos.x - playerPos.x) * progress;
            double z = playerPos.z + (targetPos.z - playerPos.z) * progress;
            // Simulate parabolic arc
            double y = playerPos.y + (heightAtApex - playerPos.y) * Math.sin(Math.PI * progress);

            BlockPos checkPos = new BlockPos((int)x, (int)y, (int)z);
            if (!WorldHelper.isAir(mod, checkPos)) {
                return false;
            }
        }

        return true;
    }
    public static boolean canUseRanged(AltoClef mod, Entity target) {
        return readyForRanged(mod) && checkRangedAttackTrajectory(mod, target);
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getInputControls().release(Input.CLICK_RIGHT);
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return shot || failed;
    }
    public boolean isFailed(){
        return failed;
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof ShootArrowSimpleProjectileTask;
    }

    @Override
    protected String toDebugString() {
        return "Shooting at " + target.getType().getName().getString() + " using "+ _rangedItem.getName().getString();
    }
}
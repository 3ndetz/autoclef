package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.chains.DeathMenuChain;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.GetToEntityTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.KillAuraHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.slots.PlayerSlot;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

import java.util.List;

/**
 * Attacks an entity, but the target entity must be specified.
 */
public abstract class AbstractKillEntityTask extends AbstractDoToEntityTask {
    private static final double OTHER_FORCE_FIELD_RANGE = 8;

    // Not the "striking" distance, but the "ok we're close enough, lower our guard for other mobs and focus on this one" range.
    private static final double CONSIDER_COMBAT_RANGE = 10;
    AtomicBoolean threadRunning = new AtomicBoolean(false);

    public AbstractKillEntityTask() {
        this(CONSIDER_COMBAT_RANGE, OTHER_FORCE_FIELD_RANGE);
    }

    public AbstractKillEntityTask(double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
        super(combatGuardLowerRange, combatGuardLowerFieldRadius);
    }

    public AbstractKillEntityTask(double maintainDistance, double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
        super(maintainDistance, combatGuardLowerRange, combatGuardLowerFieldRadius);
    }

    public static Item bestWeapon(AltoClef mod) {
        List<ItemStack> invStacks = mod.getItemStorage().getItemStacksPlayerInventory(true);
        if (!invStacks.isEmpty()) {
            float handDamage = Float.NEGATIVE_INFINITY;
            Item bestItem = null;
            for (ItemStack invStack : invStacks) {
                if (invStack.getItem() instanceof SwordItem item) {
                    float itemDamage = item.getMaterial().getAttackDamage();
                    Item handItem = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot()).getItem();
                    if (handItem instanceof SwordItem handToolItem) {
                        handDamage = handToolItem.getMaterial().getAttackDamage();
                    }
                    if (itemDamage > handDamage) {
                        bestItem = item;
                    } else {
                        bestItem = handItem;
                    }
                }
            }
            return bestItem;
        }
        return null;
    }

    public static boolean equipWeapon(AltoClef mod) {
        if (!mod.getFoodChain().isTryingToEat()){
            Item bestWeapon = bestWeapon(mod);
            Item equipedWeapon = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot()).getItem();
            if (bestWeapon != null && bestWeapon != equipedWeapon) {
                mod.getSlotHandler().forceEquipItem(bestWeapon);
                return true;
            }
        }
        return false;
    }

    @Override
    protected Task onEntityInteract(AltoClef mod, Entity entity) {
        // Equip weapon
        //TODO if can't hit DO CLOSER
        // _ztask[0] = new GetToBlockTask(entity.getBlockPos());
        boolean isPlayer = entity.isPlayer();
        boolean canHit = LookHelper.canHitEntity(mod, entity);
        //if (isPlayer){
        //    canHit = LookHelper.canHitEntity(mod, entity, 3.7f);
        //} else {
        //    canHit = LookHelper.canHitEntity(mod, entity);
        //}
        if (canHit) {
            if (isPlayer && !mod.getClientBaritone().getPathingBehavior().isPathing() && !WorldHelper.isDangerZone(mod, mod.getPlayer().getBlockPos())) {
                boolean RotatedJump = entity.squaredDistanceTo(mod.getPlayer()) < 4.4 * 4.4;
                KillAuraHelper.GoJump(mod, RotatedJump);
            }
            if (!equipWeapon(mod)) {
                float hitProg = mod.getPlayer().getAttackCooldownProgress(0);
                LookHelper.smoothLook(mod, entity);
                boolean canPunk = hitProg >= 0.99;
                if (entity instanceof LivingEntity) {
                    LivingEntity livingEnt = (LivingEntity) entity;
                    canPunk = canPunk && livingEnt.hurtTime <= 0;
                }

                if (canPunk) {
                    if (//mod.getPlayer().isOnGround() ||
                            mod.getPlayer().getVelocity().getY() < 0 || mod.getPlayer().isTouchingWater()) {
                        //LookHelper.smoothLookAt(mod, entity.getEyePos());
                        mod.getControllerExtras().attack(entity);
                    }
                }
            }
        } else {
            return new GetToEntityTask(entity);
            // working good
            //return new GetToBlockTask(entity.getBlockPos());
        }
        return null;
    }

    protected Task onEntityInteractLolOld(AltoClef mod, Entity entity) {
        boolean LOS_Close2 = LookHelper.cleanLineOfSight(entity.getEyePos(), 5.0);

        if (LOS_Close2) {
            LookHelper.smoothLookAt(mod, entity);

            if (WorldHelper.isHellHole(mod, entity.getBlockPos())) {
                //Debug.logMessage("Цель над пропастью!");
            } else {
                boolean RotatedJump = entity.squaredDistanceTo(mod.getPlayer()) < 3.2 * 3.2;
                KillAuraHelper.GoJump(mod, RotatedJump);
            }

        }
        Task[] _ztask = {null};
        if (!threadRunning.get()) {
            new Thread(() -> {

                threadRunning.set(true);
                float hitProg = 0;
                if (DeathMenuChain.ServerIp.equals("mc.vimemc.net")) { //||DeathMenuChain.ServerIp == "mc.mineblaze.net"
                    hitProg = 1;
                } else
                    hitProg = mod.getPlayer().getAttackCooldownProgress(
                            1 - 1.5f * (float) Math.random());//-0.2f+(float)Math.random()*0.4f);// НОРМ РАБОТАЛО
                equipWeapon(mod);

                if (hitProg >= 0.99) {
                    boolean attacked = false;

                    boolean LOS_Close = LookHelper.cleanLineOfSight(entity.getEyePos(), 5.0);

                    if (LOS_Close) {
                        try {

                            //attacked = mod.getControllerExtras().attack(entity,false); //!!!! java.lang.ArrayIndexOutOfBoundsException: Index -1 out of bounds for length 2
                        } catch (Exception e) {
                            Debug.logWarning("!!! ERROR WHEN ATTACKING !!! [OFTEN CRASH AFTER THAT!!!!!!!!!]");
                            e.printStackTrace();
                        }
                    } else {
                        if (!LOS_Close) {

                            _ztask[0] = new GetToBlockTask(entity.getBlockPos());
                        }
                    }
                }
                threadRunning.set(false);
            }).start();
        }

        return _ztask[0];
    }
}
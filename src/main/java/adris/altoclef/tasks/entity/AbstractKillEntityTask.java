package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.chains.DeathMenuChain;
import adris.altoclef.chains.GameMenuTaskChain;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.GetToEntityTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.agent.Pipeline;
import adris.altoclef.util.helpers.*;
import adris.altoclef.util.slots.PlayerSlot;
import java.util.concurrent.atomic.AtomicBoolean;

import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
    private static final TimerGame _getToEntityTimer = new TimerGame(1);
    private static final TimerGame _attackStrategyTimer = new TimerGame(15);
    private static boolean _agressiveAttackStrategy = true;

    public AbstractKillEntityTask() {
        this(CONSIDER_COMBAT_RANGE, OTHER_FORCE_FIELD_RANGE);
    }

    public AbstractKillEntityTask(double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
        super(combatGuardLowerRange, combatGuardLowerFieldRadius);
    }

    public AbstractKillEntityTask(double maintainDistance, double combatGuardLowerRange, double combatGuardLowerFieldRadius) {
        super(maintainDistance, combatGuardLowerRange, combatGuardLowerFieldRadius);
    }
    public static float getAttackDamage(Item item) {
        if (item instanceof SwordItem sword) {
            return sword.getMaterial().getAttackDamage();
        } else if (item instanceof AxeItem axe) {
            return axe.getMaterial().getAttackDamage();
        }
        return 0;
    }
    public static Item bestWeapon(AltoClef mod, boolean preferAxe) {

        List<ItemStack> invStacks = mod.getItemStorage().getItemStacksPlayerInventory(true);
        if (!invStacks.isEmpty()) {
            float handDamage = Float.NEGATIVE_INFINITY;
            Item bestItem = null;
            boolean hasAxe = false;
            for (ItemStack invStack : invStacks) {
                // TODO untested
                if (AltoClef.getPipeline().equals(Pipeline.MurderMystery)) {
                    for(Item weapon : ItemHelper.MMKillerWeapons) {
                        if (invStack.isOf(weapon))
                            return weapon;
                    }
                } else if (invStack.getItem() instanceof SwordItem || invStack.getItem() instanceof AxeItem) {
                    Item item = invStack.getItem();
                    if (item instanceof AxeItem) {
                        if (!hasAxe && preferAxe) {
                            bestItem = item;
                        }
                        hasAxe = true;
                    } else if (hasAxe && preferAxe) {
                        continue;
                    }
                    float itemDamage = getAttackDamage(item);
                    Item handItem = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot()).getItem();
                    if (
                            (handItem instanceof SwordItem && !(hasAxe && preferAxe))
                            || handItem instanceof AxeItem) {
                        handDamage = getAttackDamage(handItem);
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

    public static boolean equipWeapon(AltoClef mod, boolean preferAxe) {
        if (!mod.getFoodChain().isTryingToEat()){
            Item bestWeapon = bestWeapon(mod, preferAxe);
            Item equipedWeapon = StorageHelper.getItemStackInSlot(PlayerSlot.getEquipSlot()).getItem();
            if (bestWeapon != null && bestWeapon != equipedWeapon) {
                mod.getSlotHandler().forceEquipItem(bestWeapon);
                return true;
            }
        }
        return false;
    }

    public static boolean equipWeapon(AltoClef mod) {
        return equipWeapon(mod, false);
    }

    @Override
    protected Task onEntityInteract(AltoClef mod, Entity entity) {
        if (_attackStrategyTimer.elapsed()) {
            _agressiveAttackStrategy = !_agressiveAttackStrategy;
            _attackStrategyTimer.reset();
        }
        // Equip weapon
        //TODO if can't hit DO CLOSER
        // _ztask[0] = new GetToBlockTask(entity.getBlockPos());
        boolean canHit = LookHelper.canHitEntity(mod, entity);
        boolean directViewing = LookHelper.cleanLineOfSight(entity.getBoundingBox().getCenter(), 50.0);
        double dist = entity.distanceTo(mod.getPlayer());
        //Debug.logMessage("fdsf " +_getToEntityTimer.getDuration());
        //if (!_getToEntityTimer.elapsed())
        //    return new GetToEntityTask(entity);
        //if (isPlayer){
        //    canHit = LookHelper.canHitEntity(mod, entity, 3.7f);
        //} else {
        //    canHit = LookHelper.canHitEntity(mod, entity);
        //}
        if (canHit) {
            LookHelper.smoothLook(mod, entity);
            boolean preferAxe = false;
            _getToEntityTimer.reset();
            if (entity instanceof PlayerEntity player) {
                // check player has shield
                // check player is using shield now
                // if using shield, equip axe
                if (player.isUsingItem() && player.getHandItems() != null) {
                    boolean hasShield = false;
                    for (ItemStack stack : player.getHandItems()) {
                        if (stack.getItem().equals(Items.SHIELD)) {
                            hasShield = true;
                            break;
                        }
                    }
                    if (hasShield) {
                        preferAxe = true;
                    }
                }
                if (!mod.getClientBaritone().getPathingBehavior().isPathing() && !WorldHelper.isDangerZone(mod, mod.getPlayer().getBlockPos())) {
                    // TODO untested
                    if (dist > 0.5) {
                        KillAuraHelper.GoJump(mod, dist < 4.4, true);
                        if (dist > 2.5 && dist < 3) {
                            if (!mod.getInputControls().isHeldDown(Input.JUMP) && mod.getPlayer().isOnGround()) {
                                mod.getInputControls().tryPress(Input.JUMP);
                                //mod.getInputControls().release(Input.JUMP);
                            }
                        }
                    }
                    
                }
            }

            if (!equipWeapon(mod, preferAxe)) {
                float hitProg = mod.getPlayer().getAttackCooldownProgress(0);
                boolean canPunk = hitProg >= 0.99;
                setDebugState("ATTACKING");
                if (entity instanceof LivingEntity living) {
                    canPunk = canPunk && living.hurtTime <= 0;
                }
                if (canPunk) {
                    if (
                            true // need more proper crit checking
                            //mod.getPlayer().isOnGround() ||
                            //mod.getPlayer().getVelocity().getY() < 0 ||
                            //mod.getPlayer().isTouchingWater()
                    ) {
                        //LookHelper.smoothLookAt(mod, entity.getEyePos());
                        mod.getControllerExtras().attack(entity);
                        setDebugState("PERFORMING ATTACK");

                    }
                }
            }
        } else {
            if (directViewing && _agressiveAttackStrategy) {
                setDebugState("Leaping at target!");
                LookHelper.smoothLook(mod, entity);
                KillAuraHelper.GoJump(mod, dist < 4.4, true);
            } else {
                // TODO find out how effectively fix this
                // GO FORCE NEED HERE OR IT WILL LAG DURING TARGET Shift going in air
                //if (_getToEntityTimer.elapsed())
                //    _forceGo = true;
                //_getToEntityTimer.reset();
                setDebugState("Cannot hit, getting to entity");
                return new GetToEntityTask(entity);
                // working good
                //return new GetToBlockTask(entity.getBlockPos());
            }
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
                if (GameMenuTaskChain.ServerIp.equals("mc.vimemc.net")) { //||DeathMenuChain.ServerIp == "mc.mineblaze.net"
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
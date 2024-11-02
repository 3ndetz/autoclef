package adris.altoclef.tasks.stupid;

import adris.altoclef.AltoClef;
import adris.altoclef.butler.ButlerConfig;
import adris.altoclef.tasks.entity.KillPlayerTask;
import adris.altoclef.tasks.entity.ShootArrowSimpleProjectileTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.movement.SafeRandomShimmyTask;
import adris.altoclef.tasks.movement.ThrowEnderPearlSimpleProjectileTask;
import adris.altoclef.tasks.movement.TimeoutWanderTask;
import adris.altoclef.tasks.slot.ClickSlotTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.slots.Slot;
import baritone.api.utils.input.Input;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class MurderMysteryTask extends Task {

    private Vec3d _closestPlayerLastPos;
    private Vec3d _closestPlayerLastObservePos;
    private double _closestDistance;
    private  boolean TargetIsNear = false;
    private boolean _forceWait = false;
    private Task _shootArrowTask;
    private Task _pickupTask;
    private String _stringRole;
    private List<Item> lootableItems(AltoClef mod) {
        List<Item> lootable = new ArrayList<>();
        //lootable.addAll(ArmorAndToolsNeeded(mod));
        lootable.add(Items.GOLDEN_APPLE);
        lootable.add(Items.ENCHANTED_GOLDEN_APPLE);
        lootable.add(Items.GOLDEN_CARROT);
        lootable.add(Items.GOLD_INGOT);
        lootable.add(Items.BOW);
        lootable.add(Items.ARROW);
        lootable.add(Items.GOLD_INGOT);
        lootable.add(Items.DIAMOND);
        lootable.add(Items.IRON_INGOT);
        lootable.add(Items.BOW);
        if (!mod.getItemStorage().hasItemInventoryOnly(Items.WATER_BUCKET)) {
            lootable.add(Items.WATER_BUCKET);}
        return lootable;
    }
    public MurderMysteryTask(int role) {
        switch (role){
            case 0:
                _stringRole = "невиновный";
                break;
            case 1:
                _stringRole = "детектив";
                break;
            case -1:
                _stringRole = "маньяк";
                break;
        }
    }
    @Override
    protected void onStart(AltoClef mod) {
        mod.getBehaviour().push();
        mod.getBehaviour().avoidBlockBreaking(this::avoidBlockBreak);
        mod.getBehaviour().avoidBlockPlacing(this::avoidBlockBreak);
    }
    private boolean avoidBlockBreak(BlockPos pos) {
        return true;
    }

    @Override
    protected Task onTick(AltoClef mod) {
        setDebugState("MURDER MYSTERY");
        if (ButlerConfig.getInstance().autoJoin) {
            List<String> joinItems = Arrays.asList("новая игра", "начать игру", "быстро играть");
            for (String joinItemName : joinItems) {
                Slot newGameSlot = getCustomItemSlot(mod, joinItemName);
                if (newGameSlot != null) {
                    setDebugState("Новая игра");
                    mod.getSlotHandler().forceEquipSlot(newGameSlot);
                    mod.getInputControls().tryPress(Input.CLICK_RIGHT);
                    //return new ClickSlotTask(newGameSlot, 1);
                }
            }
        }
        Optional<Entity> closest = mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(), toPunk -> shouldPunk(mod, (PlayerEntity) toPunk), PlayerEntity.class);

        if (closest.isPresent()) {

            _closestPlayerLastPos = closest.get().getPos();
            _closestPlayerLastObservePos = mod.getPlayer().getPos();
            _closestDistance = _closestPlayerLastPos.distanceTo(_closestPlayerLastObservePos);
            if (_closestDistance<=8 & mod.getEntityTracker().isEntityReachable(closest.get())) TargetIsNear = true;
            //Debug.logMessage("дистанция"+_closestDistance);

        }

        if(_forceWait && !TargetIsNear){
            //Debug.logMessage("Ждемс...");
            return null;}
        if (shouldForce(mod, _shootArrowTask)) {
            return _shootArrowTask;
        }

        if(closest.isPresent()){

            PlayerEntity entity = (PlayerEntity) closest.get();
            if(mod.getPlayer().distanceTo(entity)>10 && LookHelper.cleanLineOfSight(entity.getPos(),100)) {
                if (mod.getItemStorage().getItemCount(Items.ENDER_PEARL) > 2){
                    return new ThrowEnderPearlSimpleProjectileTask(entity.getBlockPos().add(0, -1, 0));}
                else if(UseBow(mod, entity)){
                    _shootArrowTask = new ShootArrowSimpleProjectileTask(entity);
                    return _shootArrowTask;
                }
            }
            if (mod.getItemStorage().hasItemInventoryOnly(Items.SHEARS, Items.IRON_SWORD)) {
                //tryDoFunnyMessageTo(mod, (PlayerEntity) entity);
                mod.getSlotHandler().forceEquipItem(Items.SHEARS, Items.IRON_SWORD);
                return new KillPlayerTask(entity.getName().getString());
            }
        }

        for (Item check : lootableItems(mod)) {
            if (mod.getEntityTracker().itemDropped(check)) {

                Optional<ItemEntity> closestEnt = mod.getEntityTracker().getClosestItemDrop(
                        ent -> mod.getPlayer().getPos().isInRange(ent.getEyePos(), 400),check);
                //
                if(closestEnt.isPresent()) {
                    setDebugState("Пикап предметов");
                    _pickupTask = new PickupDroppedItemTask(new ItemTarget(check), false);
                    return _pickupTask;
                }
            }
        }



        setDebugState("Скитание");
        return new SafeRandomShimmyTask();
        //return new TimeoutWanderTask();
    }
    private boolean shouldPunk(AltoClef mod, PlayerEntity player) {
        if (player == null || player.isDead() || !player.isAlive()) return false;
        if (player.isCreative() || player.isSpectator()) return false;
        if (player.isSleeping() || player.hasVehicle()) return false;
        //if (!WorldHelper.canReach(mod,player.getBlockPos())) return false;
        //mod.getEntityTracker().getCloseEntities().
        return !mod.getButler().isUserAuthorized(player.getName().getString());// && _canTerminate.test(player);
    }

    private boolean UseBow(AltoClef mod, Entity target)
    {
        return ShouldBow(mod) && ShootArrowSimpleProjectileTask.canUseBow(mod,target);
    }

    private boolean ShouldBow(AltoClef mod){
        if(mod.getItemStorage().hasItem(Items.BOW) && (mod.getItemStorage().hasItem(Items.ARROW) || mod.getItemStorage().hasItem(Items.SPECTRAL_ARROW))){
            return true; }else {return false;}
    }
    private static boolean shouldForce(AltoClef mod, Task task) {
        return task != null && task.isActive() && !task.isFinished(mod);
    }


    public Slot getCustomItemSlot(AltoClef mod, String checkItemName){

        List<ItemStack> invertoryItems = mod.getItemStorage().getItemStacksPlayerInventory(true); //mod.getPlayer().getInventory().get;
        if (AltoClef.inGame() && mod.getPlayer()!=null && invertoryItems!=null) {
            for (ItemStack item : invertoryItems){
                if(item.getItem()!=null){

                    String itemName = item.getItem().getName().getString().toLowerCase();
                    if(!itemName.equals("воздух")){
                        if(item.contains(DataComponentTypes.CUSTOM_NAME)) {
                            String itemCustomName = item.getName().getString().toLowerCase();
                            if (itemCustomName.equals(checkItemName)){
                                return mod.getItemStorage().getSlotsWithItemPlayerInventory(true, item.getItem()).get(0);
                            }
                            //return itemName+" (с названием " + itemCustomName+")";
                        }

                        //Debug.logMessage("ITEM CUSTOM NAME = "+itemCustomName);
                    }


                }
            }

        }
        return null;
    }

    public boolean hasCustomItem(AltoClef mod, String checkItemName){

        List<ItemStack> invertoryItems = mod.getItemStorage().getItemStacksPlayerInventory(true); //mod.getPlayer().getInventory().get;
        if (AltoClef.inGame() && mod.getPlayer()!=null && invertoryItems!=null) {
            for (ItemStack item : invertoryItems){
                if(item.getItem()!=null){

                    String itemName = item.getItem().getName().getString().toLowerCase();
                    if(!itemName.equals("воздух")){
                        if(item.contains(DataComponentTypes.CUSTOM_NAME)) {
                            String itemCustomName = item.getName().getString().toLowerCase();
                            return itemCustomName.contains(checkItemName);
                            //return itemName+" (с названием " + itemCustomName+")";
                        }

                        //Debug.logMessage("ITEM CUSTOM NAME = "+itemCustomName);
                    }


                }
            }

        }
        return false;
    }



    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Мардер "+_stringRole;
    }

}

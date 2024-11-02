package adris.altoclef.tasks.stupid;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.entity.KillPlayerTask;
import adris.altoclef.tasks.entity.ShootArrowSimpleProjectileTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.movement.ThrowEnderPearlSimpleProjectileTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.LookHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

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
    }

    @Override
    protected Task onTick(AltoClef mod) {
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

        if(ShouldBow(mod) && closest.isPresent()){
            _shootArrowTask = new ShootArrowSimpleProjectileTask(closest.get());
            return _shootArrowTask;
        }


        for (Item check : lootableItems(mod)) {
            if (mod.getEntityTracker().itemDropped(check)) {

                Optional<ItemEntity> closestEnt = mod.getEntityTracker().getClosestItemDrop(
                        ent -> mod.getPlayer().getPos().isInRange(ent.getEyePos(), 10),check);
                //
                if(closestEnt.isPresent()) {
                    _pickupTask = new PickupDroppedItemTask(new ItemTarget(check), true);
                    return _pickupTask;
                }
            }
        }


        if(closest.isPresent()){
            setDebugState("MURDER MYSTERY");
            PlayerEntity entity = (PlayerEntity) closest.get();
            if(mod.getPlayer().distanceTo(entity)>10 && LookHelper.cleanLineOfSight(entity.getPos(),100)) {
                if (mod.getItemStorage().getItemCount(Items.ENDER_PEARL) > 2){
                    return new ThrowEnderPearlSimpleProjectileTask(entity.getBlockPos().add(0, -1, 0));}
                else if(ShouldBow(mod)){
                    _shootArrowTask = new ShootArrowSimpleProjectileTask(entity);
                    return _shootArrowTask;
                }
            }
            return new KillPlayerTask(entity.getName().getString());
        }

        return null;
    }
    private boolean shouldPunk(AltoClef mod, PlayerEntity player) {
        if (player == null || player.isDead() || !player.isAlive()) return false;
        if (player.isCreative() || player.isSpectator()) return false;
        //if (!WorldHelper.canReach(mod,player.getBlockPos())) return false;
        //mod.getEntityTracker().getCloseEntities().
        return !mod.getButler().isUserAuthorized(player.getName().getString());// && _canTerminate.test(player);
    }
    private boolean ShouldBow(AltoClef mod){
        if(mod.getItemStorage().hasItem(Items.BOW) && (mod.getItemStorage().hasItem(Items.ARROW) || mod.getItemStorage().hasItem(Items.SPECTRAL_ARROW))){
            return true; }else {return false;}
    }
    private static boolean shouldForce(AltoClef mod, Task task) {
        return task != null && task.isActive() && !task.isFinished(mod);
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

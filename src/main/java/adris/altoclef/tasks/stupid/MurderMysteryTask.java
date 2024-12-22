package adris.altoclef.tasks.stupid;

import adris.altoclef.AltoClef;
import adris.altoclef.Playground;
import adris.altoclef.butler.ButlerConfig;
import adris.altoclef.tasks.entity.DoToClosestEntityTask;
import adris.altoclef.tasks.entity.KillPlayerTask;
import adris.altoclef.tasks.entity.ShootArrowSimpleProjectileTask;
import adris.altoclef.tasks.movement.GetToEntityTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.movement.SafeRandomShimmyTask;
import adris.altoclef.tasks.movement.ThrowEnderPearlSimpleProjectileTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;

import static adris.altoclef.util.helpers.StringHelper.removeMCFormatCodes;

public class MurderMysteryTask extends Task {

    private Vec3d _closestPlayerLastPos;
    private Vec3d _closestPlayerLastObservePos;
    private double _closestDistance;
    private  boolean TargetIsNear = false;
    private boolean _forceWait = false;
    private Task _shootArrowTask;
    private Task _pickupTask;
    public String _killerName;
    public HashMap<String, MurderRole> _roles = new HashMap<>();
    public int _chill_tactics = -1;
    public boolean _chill_tactics_changed = false;
    private MurderRole _role;
    private Task _runAwayTask;
    private final TimerGame _runAwayExtraTime = new TimerGame(5);
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
    public enum MurderRole {
        KILLER,
        DETECTIVE,
        INNOCENT,
        UNKNOWN
    }
    public MurderMysteryTask(int role) {
        switch (role){
            case 0:
                _role = MurderRole.INNOCENT;
                break;
            case 1:
                _role = MurderRole.DETECTIVE;
                break;
            case 2:
                _role = MurderRole.KILLER;
                break;
            case -1:
                _role = MurderRole.UNKNOWN;
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
    public void resetGameInfo(){
        _role = MurderRole.UNKNOWN;
        _roles.clear();
        _killerName = null;
    }
    public boolean clickCustomItem(AltoClef mod, String... joinItems) {
        for (String joinItemName : joinItems) {
            Slot newGameSlot = getCustomItemSlot(mod, joinItemName);
            if (newGameSlot != null) {
                setDebugState("Новая игра");
                mod.getSlotHandler().forceEquipSlot(newGameSlot);
                mod.getInputControls().tryPress(Input.CLICK_RIGHT);
                // reset roles
                return true;
                //return new ClickSlotTask(newGameSlot, 1);
            }
        }
        return false;
    }
    @Override
    protected Task onTick(AltoClef mod) {

        if (ButlerConfig.getInstance().autoJoin) {
            if (clickCustomItem(mod, "новая игра", "начать игру", "быстро играть (пкм)")){
                resetGameInfo();
            }
        }
        Optional<Entity> closestDanger = Optional.empty();
        if(!isReadyToPunk(mod)) {   // shouldAvoid()
            closestDanger = mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(), toPunk -> shouldAvoid(mod, (PlayerEntity) toPunk), PlayerEntity.class);
        }
        Optional<Entity> closest = mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(), toPunk -> isEnemy(mod, (PlayerEntity) toPunk), PlayerEntity.class);

        if (closest.isPresent()) {

            _closestPlayerLastPos = closest.get().getPos();
            _closestPlayerLastObservePos = mod.getPlayer().getPos();
            _closestDistance = _closestPlayerLastPos.distanceTo(_closestPlayerLastObservePos);
            if (_closestDistance<=8 & mod.getEntityTracker().isEntityReachable(closest.get())) TargetIsNear = true;
            //Debug.logMessage("дистанция"+_closestDistance);

        }

        if(_forceWait && !TargetIsNear){
            //Debug.logMessage("Ждемс...");
            return null;
        }

        if (shouldForce(mod, _shootArrowTask)) {
            return _shootArrowTask;
        }
        if (shouldForce(mod, _runAwayTask)){
            if(_runAwayExtraTime.elapsed()){
                _runAwayTask = null;
            } else {
                return _runAwayTask;
            }
        }


        if (hasKillerWeapon(mod)) {
            // if(_role.equals(MurderRole.UNKNOWN))  //TODO update only after unknown; unknown auto setting when world reload
            _role = MurderRole.KILLER;
        }
        Optional<Entity> closestKiller = mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(), toPunk -> hasKillerWeapon((PlayerEntity) toPunk), PlayerEntity.class);
        Optional<Entity> closestDetective = mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(), toPunk -> hasDetectiveWeapon((PlayerEntity) toPunk), PlayerEntity.class);
        if (closestKiller.isPresent()) {
            _killerName = closestKiller.get().getName().getString();
            _roles.put(_killerName, MurderRole.KILLER);
        }
        if (closestDetective.isPresent()) {
            String name = closestDetective.get().getName().getString();
            // handling: Killer can have ranged weapon too
            if(!Objects.equals(_roles.get(name), (MurderRole.KILLER))) {
                _roles.put(closestDetective.get().getName().getString(), MurderRole.DETECTIVE);
            }
        }


        if (closestDanger.isPresent()){
            Entity danger = closestDanger.get();
            if (mod.getPlayer().distanceTo(danger) < 20) {
                setDebugState("RUNNING FROM DANGER");
                _runAwayExtraTime.reset();
                _runAwayTask = new TerminatorTask.RunAwayFromPlayersTask(danger, 20);
                return _runAwayTask;
            }
        }

        if(closest.isPresent() && isReadyToPunk(mod)){

            PlayerEntity entity = (PlayerEntity) closest.get();
            float dist = mod.getPlayer().distanceTo(entity);
            boolean tooClose = dist < 10f;
            if (_role.equals(MurderRole.KILLER)) {
                //tryDoFunnyMessageTo(mod, (PlayerEntity) entity);
                if(tooClose) {
                    mod.getSlotHandler().forceEquipItem(Items.SHEARS, Items.IRON_SWORD);
                }else{
                    mod.getSlotHandler().forceDeequip(stack -> stack.getItem() instanceof ShearsItem || stack.getItem() instanceof SwordItem);
                }
                return new KillPlayerTask(entity.getName().getString());
            }

            // AGRESSIVE DETECTIVE TACTICS

            if(LookHelper.cleanLineOfSight(entity.getPos(),mod.getPlayer().distanceTo(entity))) {
                String name = entity.getName().getString();
                if (mod.getItemStorage().getItemCount(Items.ENDER_PEARL) > 2){
                    return new ThrowEnderPearlSimpleProjectileTask(entity.getBlockPos().add(0, -1, 0));
                } else if (Objects.equals(_roles.get(name), MurderRole.KILLER)){
                    if (UseBow(mod, entity)) {
                        _shootArrowTask = new ShootArrowSimpleProjectileTask(entity);
                        return _shootArrowTask;
                    }
                }
            } else {
                setDebugState("PURSUE ENEMY!");
                return new GetToEntityTask(entity);
            }


        }
        if(!isValidPlayerMM(mod.getPlayer())){  // injured
            setDebugState("Вы ранены и погибаете, остаётся ждать доктора");
            return new SafeRandomShimmyTask();
        }
        for (Item check : lootableItems(mod)) {
            if (mod.getEntityTracker().itemDropped(check)) {

                Optional<ItemEntity> closestEnt = mod.getEntityTracker().getClosestItemDrop(
                        ent -> mod.getPlayer().getPos().isInRange(ent.getEyePos(), 400),check);
                //
                if(closestEnt.isPresent()) {
                    setDebugState("Собирательство");
                    _pickupTask = new PickupDroppedItemTask(new ItemTarget(check), false, false);
                    return _pickupTask;
                }
            }
        }

        Random random = new Random();
        if(_chill_tactics == -1){
            _chill_tactics = random.nextInt(2);
            _chill_tactics_changed = true;
        }

        // Only set _chill_tactics_changed to false when actually changing tasks
        if (_chill_tactics_changed) {
            //Debug.logMessage("ChillTactics: " + _chill_tactics);
        }
        setDebugState("Чилл");
        Optional<Entity> chiller = mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(), toPunk -> true, PlayerEntity.class);
        if (chiller.isPresent() && chiller.get() instanceof PlayerEntity playerEntity) {

            return new DoToClosestEntityTask(entity -> {
                    switch (_chill_tactics){
                        case 0:
                            return new KillPlayerTask(playerEntity.getName().getString());
                        case 1:
                            _runAwayTask = new TerminatorTask.RunAwayFromPlayersTask(playerEntity, 5);
                            _runAwayExtraTime.reset();
                            return _runAwayTask;
                        default:
                            _chill_tactics = 0;
                            return new SafeRandomShimmyTask();
                    }

                }, PlayerEntity.class
            );
        }

        //return new Kil
        return new SafeRandomShimmyTask();
        //return new TimeoutWanderTask();
    }
    private boolean isReadyToPunk(AltoClef mod){
        if(_role.equals(MurderRole.KILLER)){
            return hasKillerWeapon(mod);
        } else {
            return ShootArrowSimpleProjectileTask.hasArrows(mod) && ShootArrowSimpleProjectileTask.hasShootingWeapon(mod);
        }

    }

    private boolean hasKillerWeapon(PlayerEntity entity) {
        for(Item weapon : ItemHelper.MMKillerWeapons) {
            boolean has_weapon = entity.getMainHandStack().isOf(weapon);
            if (has_weapon) return true;
        }
        return false;
    }
    private boolean hasDetectiveWeapon(PlayerEntity entity) {
        for(Item weapon : ItemHelper.MMDetectiveWeapons) {
            boolean has_weapon = entity.getMainHandStack().isOf(weapon);
            if (has_weapon) return true;
        }
        return false;
    }
    private boolean hasKillerWeapon(AltoClef mod){
        return mod.getItemStorage().hasItemInventoryOnly(ItemHelper.MMKillerWeapons);
    }
    private boolean isValidPlayerMM(PlayerEntity player){
        if (player == null || player.isDead() || !player.isAlive()) return false;
        if (player.isCreative() || player.isSpectator()) return false;
        if (player.isSleeping() || player.hasVehicle()) return false;
        if (player.getName() == null) return false;
        return true;
    }
    private boolean shouldAvoid(AltoClef mod, PlayerEntity player) { return isValidPlayerMM(player) && shouldAvoid(mod, player.getName().getString());}
    private boolean shouldAvoid(AltoClef mod, String name){

        MurderRole role = _roles.get(name);
        if (_role.equals(MurderRole.KILLER)) {
            // killer should not avoid everyone!
            // but he can kill detectives at last / first time...
            // if (role.equals(MurderRole.DETECTIVE) ){ return true;}
            return false;
        } else {
            if (Objects.equals(role, MurderRole.KILLER)) {
                return true;
            }
            return false;
        }
    }
    private boolean isEnemy(AltoClef mod, PlayerEntity player) { return isValidPlayerMM(player) && isEnemy(mod, player.getName().getString());}
    private boolean isEnemy(AltoClef mod, String name){

        MurderRole role = _roles.get(name);
        if (_role.equals(MurderRole.KILLER)) {
            return true;
        } else {
            if (Objects.equals(role, MurderRole.KILLER)) {
                return true;
            }
            return false;
        }
    }
    private boolean shouldPunk(AltoClef mod, PlayerEntity player) {
        if (!isValidPlayerMM(player)) return false;
        if(!_role.equals(MurderRole.KILLER)) {
            if (Objects.equals(_roles.get(player.getName().getString()), MurderRole.KILLER)) return true;
        }
        return !mod.getButler().isUserAuthorized(player.getName().getString());// && _canTerminate.test(player);
    }

    private boolean UseBow(AltoClef mod, Entity target)
    {
        return ShouldBow(mod) && ShootArrowSimpleProjectileTask.canUseRanged(mod,target);
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
                            String itemCustomName = removeMCFormatCodes(item.getName().getString().toLowerCase());
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
        if(_killerName == null || _role.equals(MurderRole.KILLER)){
            return "MurderMystery: " + _role.toString();
        } else {
            return "MurderMystery: подозреваемый " + _killerName;
        }
    }

}

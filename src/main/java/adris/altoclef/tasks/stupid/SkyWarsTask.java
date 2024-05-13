package adris.altoclef.tasks.stupid;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.Subscription;
import adris.altoclef.eventbus.events.BlockPlaceEvent;
import adris.altoclef.tasks.container.LootContainerTask;
import adris.altoclef.tasks.entity.KillPlayerTask;
import adris.altoclef.tasks.entity.ShootArrowSimpleProjectileTask;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.movement.SearchChunksExploreTask;
import adris.altoclef.tasks.movement.ThrowEnderPearlSimpleProjectileTask;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.ui.MessagePriority;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.*;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * SlotHandler 39 timer override изменил
 */
public class SkyWarsTask extends Task {

    private static final int FEAR_SEE_DISTANCE = 30;
    private static final int FEAR_DISTANCE = 20;
    private static final int RUN_AWAY_DISTANCE = 80;

    private static final int MIN_BUILDING_BLOCKS = 10;
    private static final int PREFERRED_BUILDING_BLOCKS = 60;

    private static Item[] GEAR_TO_COLLECT = new Item[]{
            Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_SWORD, Items.WATER_BUCKET
    };
    private final Task _prepareDiamondMiningEquipmentTask = TaskCatalogue.getSquashedItemTask(
            new ItemTarget(Items.IRON_PICKAXE, 3), new ItemTarget(Items.IRON_SWORD, 1)
    );
    private final Task _foodTask = new CollectFoodTask(80);
    private final TimerGame _runAwayExtraTime = new TimerGame(10);
    private final Predicate<PlayerEntity> _canTerminate;
    private final ScanChunksInRadius _scanTask;
    private final TimerGame _funnyMessageTimer = new TimerGame(10);
    private final TimerGame _performExtraActionsTimer = new TimerGame(2.5);
    private Vec3d _closestPlayerLastPos;
    private Vec3d _closestPlayerLastObservePos;
    private double _closestDistance;



    private Task _runAwayTask;
    private String _currentVisibleTarget;
    private boolean _forceWait = false;
    private boolean _isEatingStrength = false;
    private boolean _isEatingGapple = false;
    private final TimerGame _eatingGappleTimer = new TimerGame(3);
    private Task _armorTask;
    private Task _shootArrowTask;
    private Task _lootTask;//new CataloguedResourceTask(new ItemTarget(Items.ENDER_PEARL));
    private Task _pickupTask;
    private boolean _finishOnKilled = false;
    private static Item[] _itemsToLoot = ItemHelper.DIAMOND_TOOLS;

    private List<Item> lootableItems(AltoClef mod) {
        List<Item> lootable = new ArrayList<>();
        lootable.addAll(ArmorAndToolsNeeded(mod));
        //lootable.addAll(Arrays.stream(ItemHelper.NETHERITE_TOOLS).toList());
        //lootable.addAll(Arrays.stream(ItemHelper.DIAMOND_TOOLS).toList());
        //lootable.addAll(Arrays.stream(ItemHelper.HelmetsTopPriority).toList());
        //lootable.addAll(Arrays.stream(ItemHelper.ChestplatesTopPriority).toList());
        //lootable.addAll(Arrays.stream(ItemHelper.LeggingsTopPriority).toList());
        //lootable.addAll(Arrays.stream(ItemHelper.BootsTopPriority).toList());
        lootable.addAll(Arrays.stream(ItemHelper.PLANKS).toList());
        lootable.add(Items.GOLDEN_APPLE);
        lootable.add(Items.ENCHANTED_GOLDEN_APPLE);
        lootable.add(Items.GOLDEN_CARROT);
        lootable.add(Items.STONE);
        lootable.add(Items.BOW);
        lootable.add(Items.ARROW);
        lootable.add(Items.GUNPOWDER);
        lootable.add(Items.ENDER_PEARL);
        if (!mod.getItemStorage().hasItemInventoryOnly(Items.WATER_BUCKET)) {
            lootable.add(Items.WATER_BUCKET);}
        return lootable;
    }


    private Subscription<BlockPlaceEvent> _blockPlaceSubscription;
    public SkyWarsTask(BlockPos center, double scanRadius, Predicate<PlayerEntity> canTerminate, boolean FinishOnKilled) {
        _canTerminate = canTerminate;
        _finishOnKilled = FinishOnKilled;
        _scanTask = new ScanChunksInRadius(center, scanRadius);
    }

    public SkyWarsTask(BlockPos center, double scanRadius, boolean FinishOnKilled) {
        this(center, scanRadius, accept -> true, FinishOnKilled);
    }

    private static final Block[] TO_SCAN = Stream.concat(Arrays.stream(new Block[]{Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL}), Arrays.stream(ItemHelper.itemsToBlocks(ItemHelper.SHULKER_BOXES))).toArray(Block[]::new);

    @Override
    protected void onStart(AltoClef mod) {
        //Debug.logMessage("стейт = "+mod.getInfoSender().getState());

        mod.getInfoSender().setState(String.valueOf(mod.getItemStorage().hasItem(Items.ENDER_PEARL)));
        mod.getBehaviour().push();
        mod.getBlockTracker().trackBlock(TO_SCAN);
        mod.getBehaviour().setForceFieldPlayers(true);
        //mod.getExtraBaritoneSettings()
        _blockPlaceSubscription = EventBus.subscribe(BlockPlaceEvent.class, evt -> {
            OnBlockPlace(mod,evt.blockPos,evt.blockState);
        });
        //Debug.logMessage("мдааа");
        //AddNearestPlayerToFriends(mod,10);

    }

    protected void OnBlockPlace(AltoClef mod, BlockPos blockPos, BlockState blockState){

        if(this._forceWait == false && mod.getClientBaritone().getCustomGoalProcess().isActive() &
                mod.getPlayer().isSneaking() &
            mod.getPlayer().getBlockPos().isWithinDistance(new Vec3i(blockPos.getX(),blockPos.getY(),blockPos.getZ()),3) ){
            //mod.getClientBaritone().getGetToBlockProcess().
            //Debug.logMessage("!!Блок поставил я!");


            new Thread(() ->{

                int ping = 100;
                //try{
                //    ping = mod.getPlayer().networkHandler.getPlayerListEntry(mod.getPlayer().getUuid()).getLatency();}
                //catch (NullPointerException e){e.printStackTrace(); ping = 500;}
                //Goal goal = mod.getClientBaritone().getCustomGoalProcess().getGoal();
                //boolean oldval = mod.getClientBaritoneSettings().allowPlace.value;
                //mod.getClientBaritoneSettings().

                //mod.getClientBaritone().getCustomGoalProcess().setGoal(new GoalBlock(0,0,0));
                //mod.getClientBaritoneSettings().allowPlace.value = false;
                this._forceWait = true;
                //if(mod.getClientBaritone().getPathingBehavior().isPathing()) # БЫЛО ДО ЭТОГО!
                    //mod.getClientBaritone().getPathingBehavior().forceCancel();
                //mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SNEAK,true);
                //mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD,true);
                mod.getInputControls().hold(Input.SNEAK);
                mod.getInputControls().hold(Input.MOVE_FORWARD);
                mod.getInputControls().hold(Input.CLICK_RIGHT);
                //Debug.logMessage("Остановка.. ");
                //mod.getMobDefenseChain()._doingFunkyStuff =true;
                sleepSec(0.4);

                //mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.SNEAK,false);
                //mod.getClientBaritone().getInputOverrideHandler().setInputForceState(Input.MOVE_FORWARD,false);
                mod.getInputControls().release(Input.CLICK_RIGHT);
                mod.getInputControls().release(Input.SNEAK);
                mod.getInputControls().release(Input.MOVE_FORWARD);

                //mod.getPlayer().
                Debug.logMessage("Блок поставила я!  "+WorldHelper.isAir(mod,blockPos) + " ыы пинг "+ping);
                if(WorldHelper.isAir(mod,blockPos)){
                    Debug.logMessage("Блок на позиции "+blockPos + " не поставился! пинг "+ping);
                    //for(int i = 0;i<10;i++){
                    //LookHelper.SmoothLookDirectionaly(mod,0.0015f);
                    mod.getInputControls().hold(Input.SNEAK);
                    mod.getInputControls().hold(Input.MOVE_BACK);
                    mod.getInputControls().hold(Input.CLICK_RIGHT);
                    sleepSec(6);
                    //sleepSec(3+((30+ping)*2)/1000);
                    mod.getInputControls().release(Input.MOVE_BACK);
                    sleepSec(1);
                    mod.getInputControls().release(Input.SNEAK);
                    mod.getInputControls().release(Input.CLICK_RIGHT);
                    //}
                    //sleepSec(4);
                }
                //mod.getBehaviour().
                //mod.getMobDefenseChain()._doingFunkyStuff =false;
                this._forceWait = false;

                //mod.getClientBaritoneSettings().allowPlace.value = oldval;

                //if(mod.getClientBaritone().getCustomGoalProcess().isActive()){
                //    mod.getClientBaritone().getCustomGoalProcess().setGoalAndPath(goal);
                //}
                //try{
                //    mod.getClientBaritone().getCustomGoalProcess().wait(200);
                //} catch (InterruptedException e) {
                //    e.printStackTrace();
                //}
                //mod.getClientBaritone().getBuilderProcess().pause();
                //sleepSec(0.5);
                //mod.getClientBaritone().getBuilderProcess().resume();
            }).start();
        }
    }
    private BlockPos _lastLootPos;
    @Override
    protected Task onTick(AltoClef mod){
        Optional<Entity> closest = mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(), toPunk -> shouldPunk(mod, (PlayerEntity) toPunk), PlayerEntity.class);
        boolean TargetIsNear = false;


        if(InputHelper.isKeyPressed(71)  && mod.getClientBaritone().getPathingBehavior().estimatedTicksToGoal().isPresent())
            Debug.logMessage("Эвристика **стика "+mod.getClientBaritone().getPathingBehavior().estimatedTicksToGoal().get());

        if (closest.isPresent()) {

            _closestPlayerLastPos = closest.get().getPos();
            _closestPlayerLastObservePos = mod.getPlayer().getPos();
            _closestDistance = _closestPlayerLastPos.distanceTo(_closestPlayerLastObservePos);
            if (_closestDistance<=8 & mod.getEntityTracker().isEntityReachable(closest.get())) TargetIsNear = true;
            //Debug.logMessage("дистанция"+_closestDistance);

        }
        int ping = 100;
        //try{ping = mod.getPlayer().networkHandler.getPlayerListEntry(mod.getPlayer().getUuid()).getLatency();}
        //catch (NullPointerException e){e.printStackTrace(); ping = 500;}
        //if(InputHelper.isKeyPressed(71)){
        //    Debug.logMessage("Ping = "+ping);//"PlusY "+PlusY + " Y "+_targetRotation.getPitch());}
        //}
        if(ping>499){
            setDebugState("ИСПЫТЫВАЕМ ЛЮТЫЙ ПИНГ = "+ping+"!!! Ожидаем окончания этого ..");
            return null;}
        //Predicate<BlockPos> validContainer = blockPos -> {
        //    if(!WorldHelper.isUnopenedChest(mod, blockPos)|| !mod.getPlayer().getBlockPos().isWithinDistance(blockPos, 15))//!WorldHelper.isUnopenedChest(mod, blockPos)||
        //        return false;
        //    else {
        //        return true;
        //    }
        //};
        if(_forceWait && !TargetIsNear){
            //Debug.logMessage("Ждемс...");
            return null;}
        if (shouldForce(mod, _shootArrowTask)) {
            return _shootArrowTask;
        }

        if(!TargetIsNear) {
            //ОДЕВАЕМСЯ КАК ПОЛОЖЕНО!!!
            //Item[] helmetsTopPriority = new Item[] {Items.NETHERITE_HELMET, Items.DIAMOND_HELMET, Items.IRON_HELMET, Items.CHAINMAIL_HELMET, Items.GOLDEN_HELMET, Items.LEATHER_HELMET};

            //if(InputHelper.isKeyPressed(71))Debug.logMessage("hasHelmetLevel ="+hasHelmetLevel+" helmetLevel="+helmetLevel);//"PlusY "+PlusY + " Y "+_targetRotation.getPitch());}

            if (shouldForce(mod, _armorTask)) {
                return _armorTask;
            }
            //if (shouldForce(mod, _pickupTask)) {
            //    return _pickupTask;
            //}

            boolean reachableLootCont = true;
            if(_lastLootPos!=null) reachableLootCont = WorldHelper.canReach(mod,_lastLootPos);
            if (reachableLootCont && shouldForce(mod, _lootTask)) {
                return _lootTask;
            }

            if(_isEatingStrength)
                _isEatingStrength = false;
            //ЮЗАТЬ СМЕСЬ СИЛЫ
            if(!mod.getPlayer().hasStatusEffect(StatusEffects.STRENGTH)&&mod.getItemStorage().hasItem(Items.GUNPOWDER)){
                //mod.getItemStorage().getItem
                if(LookHelper.tryAvoidingInteractable(mod,true)) {
                    setDebugState("Найдена смесь силы; надо понюхать");
                    mod.getSlotHandler().forceEquipItem(new Item[]{Items.GUNPOWDER}); //"true" because it's food
                    mod.getInputControls().hold(Input.CLICK_RIGHT);
                    //mod.getExtraBaritoneSettings().setInteractionPaused(true);
                    mod.getInputControls().release(Input.CLICK_RIGHT);
                    //mod.getExtraBaritoneSettings().setInteractionPaused(false);
                    _isEatingStrength = true;



                }else{
                    setDebugState("Нюхаем смесь силы: меняем угол обзора чтобы не интерактить ни с какими блоками");
                }
                return null;
            }

            //
            //ЖРАТЬ ЯБЛОЧКИ
            boolean NeedEatGapple = !mod.getPlayer().hasStatusEffect(StatusEffects.ABSORPTION) || (mod.getPlayer().getHealth()<18&&_eatingGappleTimer.getDuration()>6);
            if(NeedEatGapple&&mod.getItemStorage().hasItemInventoryOnly(Items.GOLDEN_APPLE,Items.ENCHANTED_GOLDEN_APPLE)){
                if(LookHelper.tryAvoidingInteractable(mod) && !_isEatingGapple) {
                    setDebugState("Есть яблоко, почему бы не пожрать..");
                    //mod.getSlotHandler().forceEquipSlot(new Slot(0,0,0,0));
                    mod.getSlotHandler().forceEquipItem(new Item[]{Items.GOLDEN_APPLE,Items.ENCHANTED_GOLDEN_APPLE},true);//,true); //"true" because it's food
                    mod.getInputControls().hold(Input.CLICK_RIGHT);
                    //mod.getSlotHandler().wait();
                    mod.getExtraBaritoneSettings().setInteractionPaused(true);
                    _eatingGappleTimer.reset();
                    _isEatingGapple= true;
                }
                else{
                    if(_isEatingGapple && _eatingGappleTimer.elapsed()){
                        _isEatingGapple= false;
                        setDebugState("Яблоко не съелось! Попытка 2!");
                    }else{
                        setDebugState("Жрем геплы: меняем угол обзора чтобы не интерактить с сущностями");
                    }
                }
                return null;
            }else{
                if(_isEatingGapple){
                    mod.getInputControls().release(Input.CLICK_RIGHT);
                    mod.getExtraBaritoneSettings().setInteractionPaused(false);
                    _isEatingGapple = false;}
            }
            //if(_pickupTask.)
            //if(mod.getPlayer().getEf){}
            //ШЛЕМ
            int armorEquipNeed = IsArmorNeededToEquip(mod,ItemHelper.HelmetsTopPriority);
            if (armorEquipNeed != -1){
                _armorTask = new EquipArmorTask(true, Arrays.stream(ItemHelper.HelmetsTopPriority).toList().get(armorEquipNeed));
                return _armorTask;
            }
            //ЧЕСТПЛЕЙТ
            armorEquipNeed = IsArmorNeededToEquip(mod,ItemHelper.ChestplatesTopPriority);
            if (armorEquipNeed != -1){
                _armorTask = new EquipArmorTask(true, Arrays.stream(ItemHelper.ChestplatesTopPriority).toList().get(armorEquipNeed));
                return _armorTask;
            }
            //ПЕНТС
            armorEquipNeed = IsArmorNeededToEquip(mod,ItemHelper.LeggingsTopPriority);
            if (armorEquipNeed != -1){
                _armorTask = new EquipArmorTask(true, Arrays.stream(ItemHelper.LeggingsTopPriority).toList().get(armorEquipNeed));
                return _armorTask;
            }
            //БУТС
            armorEquipNeed = IsArmorNeededToEquip(mod,ItemHelper.BootsTopPriority);
            if (armorEquipNeed != -1){
                _armorTask = new EquipArmorTask(true, Arrays.stream(ItemHelper.BootsTopPriority).toList().get(armorEquipNeed));
                return _armorTask;
            }

            //if (!StorageHelper.isArmorEquipped(mod, topHelmet )) {
            //    if (mod.getItemStorage().hasItem(topHelmet)) {
            //        _armorTask = new EquipArmorTask(true, topHelmet);
            //        return _armorTask;
            //    }
            //}

            //ТЕПЕРЬ ЛУТАЕМ СУНДУЧАРЫ!!!

            //Optional<BlockPos> closestCont = mod.getBlockTracker().getNearestTracking(validContainer,TO_SCAN);
            Optional<BlockPos> closestCont = mod.getBlockTracker().getNearestTracking(
                    blockPos -> WorldHelper.isUnopenedChest(mod, blockPos) &&
                            mod.getPlayer().getBlockPos().isWithinDistance(blockPos, 10)&&
                            WorldHelper.canReach(mod,blockPos), Blocks.CHEST) ;
            if (closestCont.isPresent() && WorldHelper.canReach(mod,closestCont.get()) && TimersHelper.CanChestInteract()) {
                setDebugState("Поиск ресурсов -> контейнеры:");
                _lastLootPos = closestCont.get();
                _lootTask = new LootContainerTask(closestCont.get(), lootableItems(mod));
                //_lootTask = new MineAndCollectTask(new ItemTarget(Items.CHEST), new Block[]{Blocks.CHEST}, MiningRequirement.HAND);
                return _lootTask;
            }

            //ПИКАЕМ ДРОП
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

            if(closest.isPresent() && ShouldBow(mod,closest.get())){
                _shootArrowTask = new ShootArrowSimpleProjectileTask(closest.get());
                return _shootArrowTask;
            }
        }else{
            if(_isEatingGapple){
                mod.getInputControls().release(Input.CLICK_RIGHT);
                mod.getExtraBaritoneSettings().setInteractionPaused(false);
                _isEatingGapple = false;}

        }

        if(closest.isPresent()){
            setDebugState("УНИЧТОЖИТЬ");
            PlayerEntity entity = (PlayerEntity) closest.get();
            if(mod.getPlayer().distanceTo(entity)>10 && LookHelper.cleanLineOfSight(entity.getPos(),100)) {
                if (mod.getItemStorage().getItemCount(Items.ENDER_PEARL) > 2){
                    return new ThrowEnderPearlSimpleProjectileTask(entity.getBlockPos().add(0, -0.5, 0));}
                else if(ShouldBow(mod, entity)){
                    _shootArrowTask = new ShootArrowSimpleProjectileTask(entity);
                    return _shootArrowTask;
                }
            }
            //tryDoFunnyMessageTo(mod, (PlayerEntity) entity);
            return new KillPlayerTask(entity.getName().getString());
        }



        setDebugState("Поиск сущностей...");
        _currentVisibleTarget = null;
        if (_scanTask.failedSearch()) {
            Debug.logMessage("Перегрузка поиска, восстановление...");
            _scanTask.resetSearch(mod);
        }

        return _scanTask;
    }
    private Optional<BlockPos> locateClosestUnopenedChest(AltoClef mod) {
        //if (WorldHelper.getCurrentDimension() != Dimension.OVERWORLD) {
        //    return Optional.empty();
        //}
        return mod.getBlockTracker().getNearestTracking(blockPos -> mod.getPlayer().getBlockPos().isWithinDistance(blockPos, 15), Blocks.CHEST);
        //mod.getBlockTracker().getNearestTracking(blockPos -> WorldHelper.isUnopenedChest(mod, blockPos) && mod.getPlayer().getBlockPos().isWithinDistance(blockPos, 15), Blocks.CHEST);
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {

        mod.getBehaviour().pop();
        mod.getBlockTracker().stopTracking(TO_SCAN);
        EventBus.unsubscribe(_blockPlaceSubscription);
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof SkyWarsTask;
    }

    @Override
    protected String toDebugString() {
        return "Активна игра в SkyWars";
    }
    private boolean ShouldBow(AltoClef mod, Entity target){
        if(LookHelper.shootReady(mod,target)&&mod.getItemStorage().hasItem(Items.BOW) && (mod.getItemStorage().hasItem(Items.ARROW) || mod.getItemStorage().hasItem(Items.SPECTRAL_ARROW)))
        {
        return true; }else {return false;}
    }

    private List<Item> ArmorAndToolsNeeded(AltoClef mod) {
        List<Item> Needed = new ArrayList<>();
        //БРОНЯ
        Needed.addAll(ItemsNeeded(mod,ItemHelper.HelmetsTopPriority));
        Needed.addAll(ItemsNeeded(mod,ItemHelper.ChestplatesTopPriority));
        Needed.addAll(ItemsNeeded(mod,ItemHelper.LeggingsTopPriority));
        Needed.addAll(ItemsNeeded(mod,ItemHelper.BootsTopPriority));
        //ИНСТРУМЕНТЫ
        Needed.addAll(ItemsNeeded(mod,ItemHelper.SwordsTopPriority));
        Needed.addAll(ItemsNeeded(mod,ItemHelper.AxesTopPriority));
        Needed.addAll(ItemsNeeded(mod,ItemHelper.PickaxesTopPriority));
        Needed.addAll(ItemsNeeded(mod,ItemHelper.ShovelsTopPriority));
        Needed.addAll(ItemsNeeded(mod,ItemHelper.HoesTopPriority));
        //Needed.addAll(ItemsNeeded(mod,ItemHelper.Tool));
        return Needed;
    }
    private List<Item> ItemsNeeded(AltoClef mod,Item[] PriorityCheckArr){
        List<Item> NeededItems = new ArrayList<>();

        //NeededItems.add(Items.GOLDEN_APPLE);
        int level = GetHighestItemLevel(mod,PriorityCheckArr);
        int iii = 0;
        for (Item i : PriorityCheckArr){
            if(iii<level){
                NeededItems.add(Arrays.stream(PriorityCheckArr).toList().get(iii));
            }
            iii++;
        }
        //NeededItems.addAll(Arrays.stream(ItemHelper.NETHERITE_TOOLS).toList());
        return NeededItems;
    }
    private int GetHighestItemLevel(AltoClef mod,Item[] PriorityCheckArr){
        int iii = 0;
        int Level = 7;
        for(Item i : PriorityCheckArr) {
            if (StorageHelper.isArmorEquipped(mod, i) || mod.getItemStorage().hasItem(i)) {
                if(Level>iii)
                    Level = iii;
            }
            iii++;
        }
        return Level;
    }
    private int IsArmorNeededToEquip(AltoClef mod, Item[] ArmorsTopPriority){

        int iii = 0;
        int Level = -1;
        int hasLevel = 7;
        //if()

        for(Item armorItem : ArmorsTopPriority){
            if (StorageHelper.isArmorEquipped(mod, armorItem )) {
                Level = iii;
            }
            if (mod.getItemStorage().hasItem(armorItem)) {
                if(hasLevel>iii)
                    hasLevel = iii;
            }

            iii++;
        }
        if(Level==-1)Level=7;
        if (hasLevel<Level){
            return hasLevel;
        }else{ return -1;}

    }
    private boolean isReadyToPunk(AltoClef mod) {
        if (mod.getPlayer().getHealth() <= 5) return false; // We need to heal.
        return StorageHelper.isArmorEquippedAll(mod, ItemHelper.DIAMOND_ARMORS) && mod.getItemStorage().hasItem(Items.DIAMOND_SWORD);
    }

    private boolean shouldPunk(AltoClef mod, PlayerEntity player) {
        if (player == null || player.isDead() || !player.isAlive()) return false;
        if (player.isCreative() || player.isSpectator()) return false;
        //if (!WorldHelper.canReach(mod,player.getBlockPos())) return false;
        //mod.getEntityTracker().getCloseEntities().
        return !mod.getButler().isUserAuthorized(player.getName().getString());// && _canTerminate.test(player);
    }


    private void tryDoFunnyMessageTo(AltoClef mod, PlayerEntity player) {
        if (_funnyMessageTimer.elapsed()) {
            if (LookHelper.seesPlayer(player, mod.getPlayer(), 80)) {
                String name = player.getName().getString();
                if (_currentVisibleTarget == null || !_currentVisibleTarget.equals(name)) {
                    _currentVisibleTarget = name;
                    _funnyMessageTimer.reset();
                    String funnyMessage = getRandomFunnyMessage();
                    mod.getMessageSender().enqueueWhisper(name, funnyMessage, MessagePriority.ASAP);
                }
            }
        }
    }

    private String getRandomFunnyMessage() {
        return "Советую спрятаться, кид";
    }
    private static boolean shouldForce(AltoClef mod, Task task) {
        return task != null && task.isActive() && !task.isFinished(mod);
    }

    private class ScanChunksInRadius extends SearchChunksExploreTask {

        private final BlockPos _center;
        private final double _radius;

        public ScanChunksInRadius(BlockPos center, double radius) {
            _center = center;
            _radius = radius;
        }

        @Override
        protected boolean isChunkWithinSearchSpace(AltoClef mod, ChunkPos pos) {
            double cx = (pos.getStartX() + pos.getEndX()) / 2.0;
            double cz = (pos.getStartZ() + pos.getEndZ()) / 2.0;
            double dx = _center.getX() - cx,
                    dz = _center.getZ() - cz;
            return dx * dx + dz * dz < _radius * _radius;
        }

        @Override
        protected ChunkPos getBestChunkOverride(AltoClef mod, List<ChunkPos> chunks) {
            // Prioritise the chunk we last saw a player in.
            if (_closestPlayerLastPos != null) {
                double lowestScore = Double.POSITIVE_INFINITY;
                ChunkPos bestChunk = null;
                for (ChunkPos toSearch : chunks) {
                    double cx = (toSearch.getStartX() + toSearch.getEndX() + 1) / 2.0, cz = (toSearch.getStartZ() + toSearch.getEndZ() + 1) / 2.0;
                    double px = mod.getPlayer().getX(), pz = mod.getPlayer().getZ();
                    double distanceSq = (cx - px) * (cx - px) + (cz - pz) * (cz - pz);
                    double pdx = _closestPlayerLastPos.getX() - cx, pdz = _closestPlayerLastPos.getZ() - cz;
                    double distanceToLastPlayerPos = pdx * pdx + pdz * pdz;
                    Vec3d direction = _closestPlayerLastPos.subtract(_closestPlayerLastObservePos).multiply(1, 0, 1).normalize();
                    double dirx = direction.x, dirz = direction.z;
                    double correctDistance = pdx * dirx + pdz * dirz;
                    double tempX = dirx * correctDistance,
                            tempZ = dirz * correctDistance;
                    double perpendicularDistance = ((pdx - tempX) * (pdx - tempX)) + ((pdz - tempZ) * (pdz - tempZ));
                    double score = distanceSq + distanceToLastPlayerPos * 0.6 - correctDistance * 2 + perpendicularDistance * 0.5;
                    if (score < lowestScore) {
                        lowestScore = score;
                        bestChunk = toSearch;
                    }
                }
                return bestChunk;
            }
            return super.getBestChunkOverride(mod, chunks);
        }

        @Override
        protected boolean isEqual(Task other) {
            if (other instanceof ScanChunksInRadius scan) {
                return scan._center.equals(_center) && Math.abs(scan._radius - _radius) <= 1;
            }
            return false;
        }

        @Override
        protected String toDebugString() {
            return "Сканирование территории...";
        }

    }
    private static void sleepSec(double seconds) {
        try {
            Thread.sleep((int) (1000 * seconds));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /*
    @Override
    protected Task onTick(AltoClef mod) {

        Optional<Entity> closest = mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(), toPunk -> shouldPunk(mod, (PlayerEntity) toPunk), PlayerEntity.class);

        if (closest.isPresent()) {
            _closestPlayerLastPos = closest.get().getPos();
            _closestPlayerLastObservePos = mod.getPlayer().getPos();
        }

        if (!isReadyToPunk(mod)) {

            if (_runAwayTask != null && _runAwayTask.isActive() && !_runAwayTask.isFinished(mod)) {
                // If our last "scare" was too long ago or there are no more nearby players...
                boolean noneRemote = (closest.isEmpty() || !closest.get().isInRange(mod.getPlayer(), FEAR_DISTANCE));
                if (_runAwayExtraTime.elapsed() && noneRemote) {
                    Debug.logMessage("Stop running away, we're good.");
                    // Stop running away.
                    _runAwayTask = null;
                } else {
                    return _runAwayTask;
                }
            }

            // See if there's anyone nearby.
            if (mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(), entityAccept -> {
                if (!shouldPunk(mod, (PlayerEntity) entityAccept)) {
                    return false;
                }
                if (entityAccept.isInRange(mod.getPlayer(), 15)) {
                    // We're close, count us.
                    return true;
                } else {
                    // Too far away.
                    if (!entityAccept.isInRange(mod.getPlayer(), FEAR_DISTANCE)) return false;
                    // We may be far and obstructed, check.
                    return LookHelper.seesPlayer(entityAccept, mod.getPlayer(), FEAR_SEE_DISTANCE);
                }
            }, PlayerEntity.class).isPresent()) {
                // RUN!

                _runAwayExtraTime.reset();
                try {
                    _runAwayTask = new RunAwayFromPlayersTask(() -> {
                        Stream<PlayerEntity> stream = mod.getEntityTracker().getTrackedEntities(PlayerEntity.class).stream();
                        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
                            return stream.filter(toAccept -> shouldPunk(mod, toAccept)).collect(Collectors.toList());
                        }
                    }, RUN_AWAY_DISTANCE);
                } catch (ConcurrentModificationException e) {
                    // oof
                    Debug.logWarning("Duct tape over ConcurrentModificationException (see log)");
                    e.printStackTrace();
                }
                setDebugState("Running away from players.");
                return _runAwayTask;
            }
        } else {
            // We can totally punk
            if (_runAwayTask != null) {
                _runAwayTask = null;
                Debug.logMessage("Stopped running away because we can now punk.");
            }
            // Get building materials if we don't have them.
            if (PlaceStructureBlockTask.getMaterialCount(mod) < MIN_BUILDING_BLOCKS) {
                setDebugState("Collecting building materials");
                return PlaceBlockTask.getMaterialTask(PREFERRED_BUILDING_BLOCKS);
            }

            // Get some food so we can last a little longer.
            if ((mod.getPlayer().getHungerManager().getFoodLevel() < (20 - 3 * 2) || mod.getPlayer().getHealth() < 10) && StorageHelper.calculateInventoryFoodScore(mod) <= 0) {
                return _foodTask;
            }

            if (mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(), toPunk -> shouldPunk(mod, (PlayerEntity) toPunk), PlayerEntity.class).isPresent()) {
                setDebugState("Punking.");
                return new DoToClosestEntityTask(
                        entity -> {
                            if (entity instanceof PlayerEntity) {
                                tryDoFunnyMessageTo(mod, (PlayerEntity) entity);
                                return new KillPlayerTask(entity.getName().getString());
                            }
                            // Should never happen.
                            Debug.logWarning("This should never happen.");
                            return _scanTask;
                        },
                        interact -> shouldPunk(mod, (PlayerEntity) interact),
                        PlayerEntity.class
                );
            }
        }

        // Get stacked first
        // Equip diamond armor asap
        if (_armorTask != null && _armorTask.isActive() && !_armorTask.isFinished(mod)) {
            setDebugState("Collecting Diamond Armor");
            return _armorTask;
        }

        // Get iron pickaxes first
        if (!mod.getItemStorage().hasItem(Items.DIAMOND_PICKAXE) && mod.getItemStorage().getItemCount(Items.DIAMOND) < 3) {
            if (!mod.getItemStorage().hasItem(Items.IRON_PICKAXE) || (_prepareDiamondMiningEquipmentTask.isActive() && !_prepareDiamondMiningEquipmentTask.isFinished(mod))) {
                setDebugState("Getting iron pickaxes to mine diamonds");
                return _prepareDiamondMiningEquipmentTask;
            }
        }

        // Collect food
        if (StorageHelper.calculateInventoryFoodScore(mod) <= 0 || (_foodTask.isActive() && !_foodTask.isFinished(mod))) {
            setDebugState("Collecting food");
            return _foodTask;
        }
        // Raw food
        for (Item raw : ItemHelper.RAW_FOODS) {
            if (mod.getItemStorage().hasItem(raw)) {
                Optional<Item> cooked = ItemHelper.getCookedFood(raw);
                if (cooked.isPresent()) {
                    int targetCount = mod.getItemStorage().getItemCount(cooked.get()) + mod.getItemStorage().getItemCount(raw);
                    setDebugState("Smelting raw food: " + ItemHelper.stripItemName(raw));
                    return new SmeltInFurnaceTask(new SmeltTarget(new ItemTarget(cooked.get(), targetCount), new ItemTarget(raw, targetCount)));
                }
            }
        }

        // If we're not all equip, do equip
        if (!StorageHelper.isArmorEquippedAll(mod, ItemHelper.DIAMOND_ARMORS)) {
            _armorTask = new EquipArmorTask(ItemHelper.DIAMOND_ARMORS);
            return _armorTask;
        }

        // Get gear one by one...
        for (Item gear : GEAR_TO_COLLECT) {
            if (!mod.getItemStorage().hasItem(gear)) {
                setDebugState("Collecting gear");
                return TaskCatalogue.getItemTask(gear, 1);
            }
        }


        setDebugState("Scanning for players...");
        _currentVisibleTarget = null;
        if (_scanTask.failedSearch()) {
            Debug.logMessage("Re-searching missed places.");
            _scanTask.resetSearch(mod);
        }

        return _scanTask;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof TerminatorTask;
    }

    @Override
    protected String toDebugString() {
        return "Мочим челов";
    }

    private boolean isReadyToPunk(AltoClef mod) {
        if (mod.getPlayer().getHealth() <= 5) return false; // We need to heal.
        return StorageHelper.isArmorEquippedAll(mod, ItemHelper.DIAMOND_ARMORS) && mod.getItemStorage().hasItem(Items.DIAMOND_SWORD);
    }

    private boolean shouldPunk(AltoClef mod, PlayerEntity player) {
        if (player == null || player.isDead()) return false;
        if (player.isCreative() || player.isSpectator()) return false;
        return !mod.getButler().isUserAuthorized(player.getName().getString()) && _canTerminate.test(player);
    }

    private void tryDoFunnyMessageTo(AltoClef mod, PlayerEntity player) {
        if (_funnyMessageTimer.elapsed()) {
            if (LookHelper.seesPlayer(player, mod.getPlayer(), 80)) {
                String name = player.getName().getString();
                if (_currentVisibleTarget == null || !_currentVisibleTarget.equals(name)) {
                    _currentVisibleTarget = name;
                    _funnyMessageTimer.reset();
                    String funnyMessage = getRandomFunnyMessage();
                    mod.getMessageSender().enqueueWhisper(name, funnyMessage, MessagePriority.ASAP);
                }
            }
        }
    }

    private String getRandomFunnyMessage() {
        return "Prepare to get punked, kid";
    }

    private class ScanChunksInRadius extends SearchChunksExploreTask {

        private final BlockPos _center;
        private final double _radius;

        public ScanChunksInRadius(BlockPos center, double radius) {
            _center = center;
            _radius = radius;
        }

        @Override
        protected boolean isChunkWithinSearchSpace(AltoClef mod, ChunkPos pos) {
            double cx = (pos.getStartX() + pos.getEndX()) / 2.0;
            double cz = (pos.getStartZ() + pos.getEndZ()) / 2.0;
            double dx = _center.getX() - cx,
                    dz = _center.getZ() - cz;
            return dx * dx + dz * dz < _radius * _radius;
        }

        @Override
        protected ChunkPos getBestChunkOverride(AltoClef mod, List<ChunkPos> chunks) {
            // Prioritise the chunk we last saw a player in.
            if (_closestPlayerLastPos != null) {
                double lowestScore = Double.POSITIVE_INFINITY;
                ChunkPos bestChunk = null;
                for (ChunkPos toSearch : chunks) {
                    double cx = (toSearch.getStartX() + toSearch.getEndX() + 1) / 2.0, cz = (toSearch.getStartZ() + toSearch.getEndZ() + 1) / 2.0;
                    double px = mod.getPlayer().getX(), pz = mod.getPlayer().getZ();
                    double distanceSq = (cx - px) * (cx - px) + (cz - pz) * (cz - pz);
                    double pdx = _closestPlayerLastPos.getX() - cx, pdz = _closestPlayerLastPos.getZ() - cz;
                    double distanceToLastPlayerPos = pdx * pdx + pdz * pdz;
                    Vec3d direction = _closestPlayerLastPos.subtract(_closestPlayerLastObservePos).multiply(1, 0, 1).normalize();
                    double dirx = direction.x, dirz = direction.z;
                    double correctDistance = pdx * dirx + pdz * dirz;
                    double tempX = dirx * correctDistance,
                            tempZ = dirz * correctDistance;
                    double perpendicularDistance = ((pdx - tempX) * (pdx - tempX)) + ((pdz - tempZ) * (pdz - tempZ));
                    double score = distanceSq + distanceToLastPlayerPos * 0.6 - correctDistance * 2 + perpendicularDistance * 0.5;
                    if (score < lowestScore) {
                        lowestScore = score;
                        bestChunk = toSearch;
                    }
                }
                return bestChunk;
            }
            return super.getBestChunkOverride(mod, chunks);
        }

        @Override
        protected boolean isEqual(Task other) {
            if (other instanceof ScanChunksInRadius scan) {
                return scan._center.equals(_center) && Math.abs(scan._radius - _radius) <= 1;
            }
            return false;
        }

        @Override
        protected String toDebugString() {
            return "Scanning around a radius";
        }
    }

    private static class RunAwayFromPlayersTask extends RunAwayFromEntitiesTask {

        public RunAwayFromPlayersTask(Supplier<List<Entity>> toRunAwayFrom, double distanceToRun) {
            super(toRunAwayFrom, distanceToRun, true, 0.1);
            // More lenient progress checker
            _checker = new MovementProgressChecker(2);
        }

        @Override
        protected boolean isEqual(Task other) {
            return other instanceof RunAwayFromPlayersTask;
        }

        @Override
        protected String toDebugString() {
            return "Running away from players";
        }
    }
    */
}

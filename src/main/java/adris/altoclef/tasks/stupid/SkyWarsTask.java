package adris.altoclef.tasks.stupid;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.Subscription;
import adris.altoclef.eventbus.events.BlockPlaceEvent;
import adris.altoclef.tasks.DoToClosestBlockTask;
import adris.altoclef.tasks.container.LootContainerTask;
import adris.altoclef.tasks.container.StoreInContainerTask;
import adris.altoclef.tasks.entity.KillPlayerTask;
import adris.altoclef.tasks.entity.ShootArrowSimpleProjectileTask;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.movement.SearchChunksExploreTask;
import adris.altoclef.tasks.movement.ThrowEnderPearlSimpleProjectileTask;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasks.resources.GetBuildingMaterialsTask;
import adris.altoclef.tasks.resources.MineAndCollectTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.ui.MessagePriority;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.*;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.time.TimerGame;
import baritone.api.pathing.calc.IPath;
import baritone.api.pathing.calc.IPathFinder;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.utils.input.Input;
import baritone.pathing.path.PathExecutor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
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

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static adris.altoclef.tasks.speedrun.BeatMinecraft2Task.toItemTargets;
import static java.beans.Beans.isInstanceOf;

/**
 * SlotHandler 39 timer override изменил
 */
public class SkyWarsTask extends Task {

    private final Predicate<PlayerEntity> _canTerminate;
    private final ScanChunksInRadius _scanTask;
    private Vec3d _closestPlayerLastPos;
    private Vec3d _closestPlayerLastObservePos;
    private boolean _forceWait = false;
    boolean _thePitTask = false;
    private BlockPos _startedPos;
    private boolean _finishOnKilled = false;
    private static Item[] _itemsToLoot = ItemHelper.DIAMOND_TOOLS;

    private static final int SEARCH_RADIUS = 10;
    private static final int TARGET_RANGE = 20;
    private static final int LOOT_RANGE = 10;
    private static final double COMBAT_RANGE = 3.0;

    // Allow these to be configured
    private Task _armorTask;
    private int searchRadius = SEARCH_RADIUS;
    private int targetRange = TARGET_RANGE;
    private int lootRange = LOOT_RANGE;
    private double combatRange = COMBAT_RANGE;
    private boolean _started = false;
    private Task _lootTask;
    private Task _structureMaterialsTask;
    private Block[] buildableBlocks = {Blocks.STONE, Blocks.COBBLESTONE, Blocks.DIRT, Blocks.GRASS, Blocks.GRASS_BLOCK};

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
        lootable.addAll(Arrays.stream(ItemHelper.blocksToItems(buildableBlocks)).toList());
        lootable.add(Items.GOLDEN_APPLE);
        lootable.add(Items.COBBLESTONE);
        lootable.add(Items.STONE);
        lootable.add(Items.DIRT);
        lootable.add(Items.ENCHANTED_GOLDEN_APPLE);
        lootable.add(Items.GOLDEN_CARROT);
        lootable.add(Items.STONE);
        lootable.add(Items.BOW);
        lootable.add(Items.ARROW);
        lootable.add(Items.GUNPOWDER);
        lootable.add(Items.ENDER_PEARL);
        if (!mod.getItemStorage().hasItemInventoryOnly(Items.WATER_BUCKET)) {
            lootable.add(Items.WATER_BUCKET);
        }
        return lootable;
    }




    private Subscription<BlockPlaceEvent> _blockPlaceSubscription;

    public SkyWarsTask(BlockPos center, double scanRadius, Predicate<PlayerEntity> canTerminate, boolean FinishOnKilled, boolean thePitTask) {
        _thePitTask = thePitTask;
        _canTerminate = canTerminate;
        _finishOnKilled = FinishOnKilled;
        _startedPos = center;

        //_structureMaterialsTask = new GetBuildingMaterialsTask(32);
        _scanTask = new ScanChunksInRadius(center, scanRadius);
    }

    public SkyWarsTask(BlockPos center, double scanRadius, Predicate<PlayerEntity> canTerminate, boolean FinishOnKilled) {
        this(center, scanRadius, canTerminate, FinishOnKilled, false);
    }

    public SkyWarsTask(BlockPos center, boolean thePitTask, boolean FinishOnKilled) {
        this(center, 100, accept -> true, FinishOnKilled, thePitTask);
    }

    public SkyWarsTask(BlockPos center, double scanRadius, boolean FinishOnKilled) {
        this(center, scanRadius, accept -> true, FinishOnKilled);
    }

    private static final Block[] TO_SCAN = Stream.concat(Arrays.stream(new Block[]{Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL}), Arrays.stream(ItemHelper.itemsToBlocks(ItemHelper.SHULKER_BOXES))).toArray(Block[]::new);

    @Override
    protected void onStart(AltoClef mod) {
        //Debug.logMessage("стейт = "+mod.getInfoSender().getState());

        mod.getInfoSender().setState(String.valueOf(mod.getItemStorage().hasItem(Items.ENDER_PEARL)));

        mod.getBlockTracker().trackBlock(TO_SCAN);

        mod.getBehaviour().setForceFieldPlayers(true);
        //mod.getExtraBaritoneSettings()

        if (_thePitTask) {
            mod.getBehaviour().avoidBlockBreaking(this::avoidBlockBreak);
            mod.getBehaviour().avoidBlockPlacing(this::avoidBlockBreak);
        }

        _blockPlaceSubscription = EventBus.subscribe(BlockPlaceEvent.class, evt -> {
            OnBlockPlace(mod, evt.blockPos, evt.blockState);
        });
        //Debug.logMessage("мдааа");
        //AddNearestPlayerToFriends(mod,10);
        mod.getBehaviour().push();
        if(_started){
            mod.getItemStorage().invalidateCachedCotainers();
        }
    }

    private boolean avoidBlockBreak(BlockPos pos) {
        return true;
    }

    protected void OnBlockPlace(AltoClef mod, BlockPos blockPos, BlockState blockState) {

        if (this._forceWait == false && mod.getClientBaritone().getCustomGoalProcess().isActive() &
                mod.getPlayer().isSneaking() &
                mod.getPlayer().getBlockPos().isWithinDistance(new Vec3i(blockPos.getX(), blockPos.getY(), blockPos.getZ()), 3)) {
            //mod.getClientBaritone().getGetToBlockProcess().
            //Debug.logMessage("!!Блок поставил я!");


            new Thread(() -> {

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
                Debug.logMessage("Блок поставила я!  " + WorldHelper.isAir(mod, blockPos) + " ыы пинг " + ping);
                if (WorldHelper.isAir(mod, blockPos)) {
                    Debug.logMessage("Блок на позиции " + blockPos + " не поставился! пинг " + ping);
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
    protected Task onTick(AltoClef mod) {
        if (mod.getFoodChain().isTryingToEat()) return null;
        boolean alert = false;

        if (_thePitTask) {
            setDebugState("ThePit");
            if (mod.getPlayer().getBlockPos().isWithinDistance(_startedPos, 10)) {
                setDebugState("МЫ НА СПАВНЕ! НАДО ВЫБРАТЬСЯ");
                mod.getInputControls().tryPress(Input.MOVE_FORWARD);
                return null;
            }
        }
        if (mod.getFoodChain().isTryingToEat()) return null;

        if (shouldForce(mod, _armorTask)) {
            return _armorTask;
        }
        if (_lootTask != null) {
            if(isInstanceOf(_lootTask, LootContainerTask.class)) {
                LootContainerTask _thisLootTask = (LootContainerTask) _lootTask;
                if (_thisLootTask.IsInChest()) {
                    return _thisLootTask;
                }
            }
            if(isInstanceOf(_lootTask, MineAndCollectTask.class)) {
                MineAndCollectTask _thisLootTask = (MineAndCollectTask) _lootTask;
                if (_thisLootTask.isMining()) {
                    return _thisLootTask;
                }
            }
        }
        _armorTask = autoArmor(mod);
        if (_armorTask != null){
            return _armorTask;
        }

        // Get nearest target
        Optional<Entity> target = mod.getEntityTracker().getClosestEntity(
                mod.getPlayer().getPos(),
                toPunk -> shouldPunk(mod, (PlayerEntity) toPunk),
                PlayerEntity.class

        );
        Vec3d pos = mod.getPlayer().getPos();
        float minCost = Float.POSITIVE_INFINITY;
        Optional<BlockPos> closestCont = mod.getBlockTracker().getNearestTracking(
                blockPos -> WorldHelper.isUnopenedChest(mod, blockPos) &&
                        mod.getPlayer().getBlockPos().isWithinDistance(blockPos, 50)&&
                        WorldHelper.canReach(mod,blockPos), Blocks.CHEST);

        Optional<ItemEntity> closestDrop = mod.getEntityTracker().getClosestItemDrop(pos,toItemTargets(lootableItems(mod).toArray(new Item[0])));

        float costContainer = Float.POSITIVE_INFINITY;
        float costTarget = Float.POSITIVE_INFINITY;
        float costDrop = Float.POSITIVE_INFINITY;
        if (closestCont.isPresent()) {
            costContainer = getPathCost(mod, pos, closestCont.get());
        }
        if (target.isPresent()) {
            costTarget = getPathCost(mod, pos, target.get().getPos());
        }
        if(closestDrop.isPresent()) {
            costDrop = getPathCost(mod, pos, closestDrop.get().getPos());
        }

        if (costContainer < minCost) {
            minCost = costContainer;
        }
        if (costTarget < minCost) {
            minCost = costTarget;
        }
        if (costDrop < minCost) {
            minCost = costDrop;
        }

        if(InputHelper.isKeyPressed(71)) {
            Debug.logMessage("Эвристики  конт " + costContainer + " тарг" + costTarget + " дроп" + costDrop);
        }

        if (minCost == Float.POSITIVE_INFINITY){}
        else if (minCost>400) {
            // Get building blocks
            int buildCount = mod.getItemStorage().getItemCount(ItemHelper.blocksToItems(buildableBlocks));
            if (buildCount < 32 && (buildCount == 0 || _structureMaterialsTask.isActive())) {
                setDebugState("Добыча ресурсов...");
                _structureMaterialsTask = new MineAndCollectTask(toItemTargets(ItemHelper.blocksToItems(buildableBlocks)), buildableBlocks, MiningRequirement.HAND);
                //_structureMaterialsTask = new CataloguedResourceTask(new ItemTarget(new Item[]{Items.DIRT, Items.COBBLESTONE, Items.STONE}));

                return _structureMaterialsTask;
            }

        } else if (minCost == costTarget) {
            return new KillPlayerTask(target.get().getName().getString());
        }
        else if (minCost == costDrop) {
            return new PickupDroppedItemTask(toItemTargets(lootableItems(mod).toArray(new Item[0])), true);
        } else if (minCost == costContainer) {
            setDebugState("Поиск ресурсов -> контейнеры: дорога");
            _lastLootPos = closestCont.get();
            _lootTask = new LootContainerTask(closestCont.get(), lootableItems(mod));
            //Random random = new Random();
            //if (_lootTask != null && _lootTask.isActive()) {} else {
            //    if (random.nextDouble() < 0.5) {
            //        _lootTask = new LootContainerTask(closestCont.get(), lootableItems(mod));
            //    } else {
            //        _lootTask = new MineAndCollectTask(new ItemTarget(Items.CHEST), new Block[]{Blocks.CHEST}, MiningRequirement.HAND);
            //    }
            //}
            return _lootTask;
        }


        //if(closestDrop.isPresent()) {
        //    _pickupTask =
        //    return new PickupDroppedItemTask(new ItemTarget(check), true);
        //}
        // Handle combat
        if (target.isPresent()) {
            PlayerEntity player = (PlayerEntity) target.get();
            alert = mod.getPlayer().distanceTo(player) <= 15;
            if (alert) {
                return new KillPlayerTask(player.getName().getString());
            }
            // Use ender pearl or bow at range
            if (LookHelper.cleanLineOfSight(player.getPos(), 100)) {
                if (mod.getItemStorage().getItemCount(Items.ENDER_PEARL) > 2) {
                    return new ThrowEnderPearlSimpleProjectileTask(player.getBlockPos().add(0, -0.5, 0));
                }
            }
            if (canUseRangedWeapon(mod) && ShootArrowSimpleProjectileTask.canUseBow(mod,player)) {
                return new ShootArrowSimpleProjectileTask(player);
            }

        }

        // Exploration
        //return _scanTask;
        return null;
    }
    public float getPathCostHard(AltoClef mod, Vec3d startPos, Vec3d goalPos){
        // Start pathing to goal
        mod.getClientBaritone().getCustomGoalProcess().setGoal(new GoalBlock(new BlockPos(goalPos)));
        mod.getClientBaritone().getCustomGoalProcess().path();
        // Get cost from current path if one exists
//
        if (mod.getClientBaritone().getPathingBehavior().estimatedTicksToGoal().isPresent()) {
            //PathExecutor currentPath = mod.getClientBaritone().getPathingBehavior().getCurrent();
            return mod.getClientBaritone().getPathingBehavior().estimatedTicksToGoal().get().floatValue();
        }
        //return (float) BaritoneHelper.calculateGenericHeuristic(goalPos, startPos);
        return Float.POSITIVE_INFINITY;
    }
    public float getPathCostVeryHard(AltoClef mod, Vec3d startPos, Vec3d goalPos) {
        // First try quick heuristic calculation
        double quickEstimate = BaritoneHelper.calculateGenericHeuristic(startPos, goalPos);

        // If positions are very close, just return the quick estimate
        if (quickEstimate < 5) {
            return (float)quickEstimate;
        }

        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            // Get current active goal/path state so we can restore it
            Goal currentGoal = mod.getClientBaritone().getCustomGoalProcess().getGoal();

            try {
                mod.getClientBaritone().getCustomGoalProcess().setGoal(new GoalBlock(new BlockPos(goalPos)));
                mod.getClientBaritone().getCustomGoalProcess().path();
                // Get cost from current path if one exists

                if (mod.getClientBaritone().getPathingBehavior().estimatedTicksToGoal().isPresent()) {
                    float cost = mod.getClientBaritone().getPathingBehavior().estimatedTicksToGoal().get().floatValue();
                    //PathExecutor currentPath = mod.getClientBaritone().getPathingBehavior().getCurrent();
                    mod.getClientBaritone().getCustomGoalProcess().setGoal(currentGoal);
                    return cost;
                }
                //return (float) BaritoneHelper.calculateGenericHeuristic(goalPos, startPos);
                mod.getClientBaritone().getCustomGoalProcess().setGoal(currentGoal);
                return Float.POSITIVE_INFINITY;

            } catch (Exception e) {
                Debug.logWarning("Path cost calculation failed: " + e.getMessage());
                return (float)quickEstimate;
            }
        }
    }
    public float getPathCost(AltoClef mod, Vec3d startPos, Vec3d goalPos) {
        // First try quick heuristic calculation
        return (float) BaritoneHelper.calculateGenericHeuristic(startPos, goalPos);
    }
    public float getPathCost(AltoClef mod, Vec3d startPos, BlockPos goalPos){
        return getPathCost(mod, WorldHelper.toVec3d(goalPos), startPos);
    }
    private boolean canUseRangedWeapon(AltoClef mod) {
        return  mod.getItemStorage().hasItem(Items.BOW) &&
                (mod.getItemStorage().hasItem(Items.ARROW) || mod.getItemStorage().hasItem(Items.SPECTRAL_ARROW));
    }
    private Task autoArmor(AltoClef mod){
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
        return null;

    }
    private Optional<BlockPos> findNearestLootable(AltoClef mod) {
        return mod.getBlockTracker().getNearestTracking(
                blockPos -> WorldHelper.isUnopenedChest(mod, blockPos) &&
                        mod.getPlayer().getBlockPos().isWithinDistance(blockPos, 10) &&
                        WorldHelper.canReach(mod, blockPos),
                Blocks.CHEST
        );
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

    private boolean ShouldBow(AltoClef mod, Entity target) {
        if (LookHelper.shootReady(mod, target) && mod.getItemStorage().hasItem(Items.BOW) && (mod.getItemStorage().hasItem(Items.ARROW) || mod.getItemStorage().hasItem(Items.SPECTRAL_ARROW))) {
            return true;
        } else {
            return false;
        }
    }

    private List<Item> ArmorAndToolsNeeded(AltoClef mod) {
        List<Item> Needed = new ArrayList<>();
        //БРОНЯ
        Needed.addAll(ItemsNeeded(mod, ItemHelper.HelmetsTopPriority));
        Needed.addAll(ItemsNeeded(mod, ItemHelper.ChestplatesTopPriority));
        Needed.addAll(ItemsNeeded(mod, ItemHelper.LeggingsTopPriority));
        Needed.addAll(ItemsNeeded(mod, ItemHelper.BootsTopPriority));
        //ИНСТРУМЕНТЫ
        Needed.addAll(ItemsNeeded(mod, ItemHelper.SwordsTopPriority));
        Needed.addAll(ItemsNeeded(mod, ItemHelper.AxesTopPriority));
        Needed.addAll(ItemsNeeded(mod, ItemHelper.PickaxesTopPriority));
        Needed.addAll(ItemsNeeded(mod, ItemHelper.ShovelsTopPriority));
        Needed.addAll(ItemsNeeded(mod, ItemHelper.HoesTopPriority));
        //Needed.addAll(ItemsNeeded(mod,ItemHelper.Tool));
        return Needed;
    }

    private List<Item> ItemsNeeded(AltoClef mod, Item[] PriorityCheckArr) {
        List<Item> NeededItems = new ArrayList<>();

        //NeededItems.add(Items.GOLDEN_APPLE);
        int level = GetHighestItemLevel(mod, PriorityCheckArr);
        int iii = 0;
        for (Item i : PriorityCheckArr) {
            if (iii < level) {
                NeededItems.add(Arrays.stream(PriorityCheckArr).toList().get(iii));
            }
            iii++;
        }
        //NeededItems.addAll(Arrays.stream(ItemHelper.NETHERITE_TOOLS).toList());
        return NeededItems;
    }

    private int GetHighestItemLevel(AltoClef mod, Item[] PriorityCheckArr) {
        int iii = 0;
        int Level = 7;
        for (Item i : PriorityCheckArr) {
            if (StorageHelper.isArmorEquipped(mod, i) || mod.getItemStorage().hasItem(i)) {
                if (Level > iii)
                    Level = iii;
            }
            iii++;
        }
        return Level;
    }

    private int IsArmorNeededToEquip(AltoClef mod, Item[] armorPriority) {
        // Get currently equipped armor level
        int equippedLevel = -1;
        for (int i = 0; i < armorPriority.length; i++) {
            if (StorageHelper.isArmorEquipped(mod, armorPriority[i])) {
                equippedLevel = i;
                break;
            }
        }

        // Find best available armor
        int bestAvailable = -1;
        for (int i = 0; i < armorPriority.length; i++) {
            if (mod.getItemStorage().hasItem(armorPriority[i])) {
                bestAvailable = i;
                break;
            }
        }

        // Return better armor level if available
        return (bestAvailable != -1 && (equippedLevel == -1 || bestAvailable < equippedLevel))
                ? bestAvailable
                : -1;
    }



    private boolean shouldPunk(AltoClef mod, PlayerEntity player) {
        return player != null &&
                player.isAlive() &&
                !player.isCreative() &&
                !player.isSpectator() &&
                !mod.getButler().isUserAuthorized(player.getName().getString());
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
}
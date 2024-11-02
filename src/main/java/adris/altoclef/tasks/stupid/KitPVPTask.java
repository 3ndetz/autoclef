package adris.altoclef.tasks.stupid;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.Subscription;
import adris.altoclef.eventbus.events.BlockPlaceEvent;
import adris.altoclef.tasks.container.LootContainerTask;
import adris.altoclef.tasks.entity.KillEntityTask;
import adris.altoclef.tasks.entity.ShootArrowSimpleProjectileTask;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.tasks.movement.PickupDroppedItemTask;
import adris.altoclef.tasks.movement.ThrowEnderPearlSimpleProjectileTask;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.InputHelper;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.TimersHelper;
import adris.altoclef.util.helpers.WorldHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

/**
 * SlotHandler 39 timer override изменил
 */
public class KitPVPTask extends Task {

    private final Task _foodTask = new CollectFoodTask(80);
    private final TimerGame _runAwayExtraTime = new TimerGame(10);
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
            lootable.add(Items.WATER_BUCKET);
        }
        return lootable;
    }


    private Subscription<BlockPlaceEvent> _blockPlaceSubscription;

    public KitPVPTask(BlockPos center, double scanRadius, boolean FinishOnKilled) {
        _finishOnKilled = FinishOnKilled;
    }

    private static final Block[] TO_SCAN = Stream.concat(
            Arrays.stream(new Block[] {Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL}),
            Arrays.stream(ItemHelper.itemsToBlocks(ItemHelper.SHULKER_BOXES))).toArray(Block[]::new);

    @Override
    protected void onStart(AltoClef mod) {
        //Debug.logMessage("стейт = "+mod.getInfoSender().getState());

        mod.getInfoSender().setState(String.valueOf(mod.getItemStorage().hasItem(Items.ENDER_PEARL)));
        mod.getBehaviour().push();
        mod.getBlockTracker().trackBlock(TO_SCAN);
        mod.getBehaviour().setForceFieldPlayers(true);
        //mod.getExtraBaritoneSettings()
        _blockPlaceSubscription = EventBus.subscribe(BlockPlaceEvent.class, evt -> {
            OnBlockPlace(mod, evt.blockPos, evt.blockState);
        });
        //Debug.logMessage("мдааа");
        //AddNearestPlayerToFriends(mod,10);

    }

    protected void OnBlockPlace(AltoClef mod, BlockPos blockPos, BlockState blockState) {

        if (this._forceWait == false && mod.getClientBaritone().getCustomGoalProcess().isActive() &
                mod.getPlayer().isSneaking() &
                mod.getPlayer().getBlockPos()
                        .isWithinDistance(new Vec3i(blockPos.getX(), blockPos.getY(), blockPos.getZ()), 3)) {

            new Thread(() -> {

                int ping = 100;
                this._forceWait = true;
                mod.getInputControls().hold(Input.SNEAK);
                mod.getInputControls().hold(Input.MOVE_FORWARD);
                mod.getInputControls().hold(Input.CLICK_RIGHT);
                sleepSec(0.4);

                mod.getInputControls().release(Input.CLICK_RIGHT);
                mod.getInputControls().release(Input.SNEAK);
                mod.getInputControls().release(Input.MOVE_FORWARD);

                if (WorldHelper.isAir(mod, blockPos)) {
                    Debug.logMessage("Блок на позиции " + blockPos + " не поставился! пинг " + ping);
                    mod.getInputControls().hold(Input.SNEAK);
                    mod.getInputControls().hold(Input.MOVE_BACK);
                    mod.getInputControls().hold(Input.CLICK_RIGHT);
                    sleepSec(6);
                    mod.getInputControls().release(Input.MOVE_BACK);
                    sleepSec(1);
                    mod.getInputControls().release(Input.SNEAK);
                    mod.getInputControls().release(Input.CLICK_RIGHT);
                }
                this._forceWait = false;

            }).start();
        }
    }

    private BlockPos _lastLootPos;

    @Override
    protected Task onTick(AltoClef mod) {
        Optional<Entity> closest = mod.getEntityTracker()
                .getClosestEntity(mod.getPlayer().getPos(), toPunk -> shouldPunk(mod, (PlayerEntity) toPunk),
                        PlayerEntity.class);
        boolean TargetIsNear = false;

        if (InputHelper.isKeyPressed(71) && mod.getClientBaritone().getPathingBehavior().estimatedTicksToGoal()
                .isPresent()) {
            Debug.logMessage(
                    "Эвристика **истика " + mod.getClientBaritone().getPathingBehavior().estimatedTicksToGoal().get());
        }

        if (closest.isPresent()) {

            _closestPlayerLastPos = closest.get().getPos();
            _closestPlayerLastObservePos = mod.getPlayer().getPos();
            _closestDistance = _closestPlayerLastPos.distanceTo(_closestPlayerLastObservePos);
            if (_closestDistance <= 8 & mod.getEntityTracker().isEntityReachable(closest.get())) {
                TargetIsNear = true;
            }


        }
        int ping = 100;
        if (ping > 499) {
            setDebugState("ИСПЫТЫВАЕМ ЛЮТЫЙ ПИНГ = " + ping + "!!! Ожидаем окончания этого дерьма");
            return null;
        }
        if (_forceWait && !TargetIsNear) {
            //Debug.logMessage("Ждемс...");
            return null;
        }
        if (shouldForce(mod, _shootArrowTask)) {
            return _shootArrowTask;
        }

        if (!TargetIsNear) {
            if (shouldForce(mod, _armorTask)) {
                return _armorTask;
            }
            boolean reachableLootCont = true;
            if (_lastLootPos != null) {
                reachableLootCont = WorldHelper.canReach(mod, _lastLootPos);
            }
            if (reachableLootCont && shouldForce(mod, _lootTask)) {
                return _lootTask;
            }

            if (_isEatingStrength) {
                _isEatingStrength = false;
            }
            //ЮЗАТЬ СМЕСЬ СИЛЫ
            if (!mod.getPlayer().hasStatusEffect(StatusEffects.STRENGTH) && mod.getItemStorage()
                    .hasItem(Items.GUNPOWDER)) {
                //mod.getItemStorage().getItem
                if (LookHelper.tryAvoidingInteractable(mod, true)) {
                    setDebugState("Найдена смесь силы; надо понюхать");
                    mod.getSlotHandler().forceEquipItem(new Item[] {Items.GUNPOWDER}); //"true" because it's food
                    mod.getInputControls().hold(Input.CLICK_RIGHT);
                    mod.getInputControls().release(Input.CLICK_RIGHT);
                    _isEatingStrength = true;


                } else {
                    setDebugState("Нюхаем смесь силы: меняем угол обзора чтобы не интерактить ни с какими блоками");
                }
                return null;
            }

            //
            //ЖРАТЬ ЯБЛОЧКИ
            boolean NeedEatGapple =
                    !mod.getPlayer().hasStatusEffect(StatusEffects.ABSORPTION) || (mod.getPlayer().getHealth() < 18
                            && _eatingGappleTimer.getDuration() > 6);
            if (NeedEatGapple && mod.getItemStorage()
                    .hasItemInventoryOnly(Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE)) {
                if (LookHelper.tryAvoidingInteractable(mod) && !_isEatingGapple) {
                    setDebugState("Есть яблоко, почему бы не пожрать..");
                    mod.getSlotHandler().forceEquipItem(new Item[] {Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE},
                            true);//,true); //"true" because it's food
                    mod.getInputControls().hold(Input.CLICK_RIGHT);
                    mod.getExtraBaritoneSettings().setInteractionPaused(true);
                    _eatingGappleTimer.reset();
                    _isEatingGapple = true;
                } else {
                    if (_isEatingGapple && _eatingGappleTimer.elapsed()) {
                        _isEatingGapple = false;
                        setDebugState("Яблоко не съелось! Попытка 2!");
                    } else {
                        setDebugState("Жрем геплы: меняем угол обзора чтобы не интерактить с сущностями");
                    }
                }
                return null;
            } else {
                if (_isEatingGapple) {
                    mod.getInputControls().release(Input.CLICK_RIGHT);
                    mod.getExtraBaritoneSettings().setInteractionPaused(false);
                    _isEatingGapple = false;
                }
            }

            int armorEquipNeed = IsArmorNeededToEquip(mod, ItemHelper.HelmetsTopPriority);
            if (armorEquipNeed != -1) {
                _armorTask = new EquipArmorTask(true,
                        Arrays.stream(ItemHelper.HelmetsTopPriority).toList().get(armorEquipNeed));
                return _armorTask;
            }
            //ЧЕСТПЛЕЙТ
            armorEquipNeed = IsArmorNeededToEquip(mod, ItemHelper.ChestplatesTopPriority);
            if (armorEquipNeed != -1) {
                _armorTask = new EquipArmorTask(true,
                        Arrays.stream(ItemHelper.ChestplatesTopPriority).toList().get(armorEquipNeed));
                return _armorTask;
            }
            //ПЕНТС
            armorEquipNeed = IsArmorNeededToEquip(mod, ItemHelper.LeggingsTopPriority);
            if (armorEquipNeed != -1) {
                _armorTask = new EquipArmorTask(true,
                        Arrays.stream(ItemHelper.LeggingsTopPriority).toList().get(armorEquipNeed));
                return _armorTask;
            }
            //БУТС
            armorEquipNeed = IsArmorNeededToEquip(mod, ItemHelper.BootsTopPriority);
            if (armorEquipNeed != -1) {
                _armorTask = new EquipArmorTask(true,
                        Arrays.stream(ItemHelper.BootsTopPriority).toList().get(armorEquipNeed));
                return _armorTask;
            }
            Optional<BlockPos> closestCont = mod.getBlockTracker().getNearestTracking(
                    blockPos -> WorldHelper.isUnopenedChest(mod, blockPos) &&
                            mod.getPlayer().getBlockPos().isWithinDistance(blockPos, 10) &&
                            WorldHelper.canReach(mod, blockPos), Blocks.CHEST);
            if (closestCont.isPresent() && WorldHelper.canReach(mod, closestCont.get())
                    && TimersHelper.CanChestInteract()) {
                setDebugState("Поиск ресурсов -> контейнеры:");
                _lastLootPos = closestCont.get();
                _lootTask = new LootContainerTask(closestCont.get(), lootableItems(mod));
                //_loo
                // tTask = new MineAndCollectTask(new ItemTarget(Items.CHEST), new Block[]{Blocks.CHEST}, MiningRequirement.HAND);
                return _lootTask;
            }

            //ПИКАЕМ ДРОП
            for (Item check : lootableItems(mod)) {
                if (mod.getEntityTracker().itemDropped(check)) {

                    Optional<ItemEntity> closestEnt = mod.getEntityTracker().getClosestItemDrop(
                            ent -> mod.getPlayer().getPos().isInRange(ent.getEyePos(), 10), check);
                    //
                    if (closestEnt.isPresent()) {
                        _pickupTask = new PickupDroppedItemTask(new ItemTarget(check), true);
                        return _pickupTask;
                    }
                }
            }

            if (ShouldBow(mod) && closest.isPresent()) {
                _shootArrowTask = new ShootArrowSimpleProjectileTask(closest.get());
                return _shootArrowTask;
            }
        } else {
            if (_isEatingGapple) {
                mod.getInputControls().release(Input.CLICK_RIGHT);
                mod.getExtraBaritoneSettings().setInteractionPaused(false);
                _isEatingGapple = false;
            }

        }

        if (closest.isPresent()) {
            setDebugState("УНИЧТОЖИТЬ");
            PlayerEntity entity = (PlayerEntity) closest.get();
            if (LookHelper.cleanLineOfSight(entity.getPos(), 100)) {
                if (mod.getPlayer().distanceTo(entity) > 10) {
                    if (mod.getItemStorage().getItemCount(Items.ENDER_PEARL) > 2) {
                        return new ThrowEnderPearlSimpleProjectileTask(entity.getBlockPos().add(0, -1, 0));
                    } else if (ShouldBow(mod)) {
                        _shootArrowTask = new ShootArrowSimpleProjectileTask(entity);
                        return _shootArrowTask;
                    }
                }
                return new KillEntityTask(entity);
            } else {
                return null;
            }

        }

        setDebugState("Поиск низших сущностей...");
        _currentVisibleTarget = null;
        return null;
    }

    private Optional<BlockPos> locateClosestUnopenedChest(AltoClef mod) {
        return mod.getBlockTracker()
                .getNearestTracking(blockPos -> mod.getPlayer().getBlockPos().isWithinDistance(blockPos, 15),
                        Blocks.CHEST);
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {

        mod.getBehaviour().pop();
        mod.getBlockTracker().stopTracking(TO_SCAN);
        EventBus.unsubscribe(_blockPlaceSubscription);
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof KitPVPTask;
    }

    @Override
    protected String toDebugString() {
        return "Режим терминатора (кпвп): уничтожить";
    }

    private boolean ShouldBow(AltoClef mod) {
        if (mod.getItemStorage().hasItem(Items.BOW) && (mod.getItemStorage().hasItem(Items.ARROW)
                || mod.getItemStorage().hasItem(Items.SPECTRAL_ARROW))) {
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
                if (Level > iii) {
                    Level = iii;
                }
            }
            iii++;
        }
        return Level;
    }

    private int IsArmorNeededToEquip(AltoClef mod, Item[] ArmorsTopPriority) {

        int iii = 0;
        int Level = -1;
        int hasLevel = 7;
        //if()

        for (Item armorItem : ArmorsTopPriority) {
            if (StorageHelper.isArmorEquipped(mod, armorItem)) {
                Level = iii;
            }
            if (mod.getItemStorage().hasItem(armorItem)) {
                if (hasLevel > iii) {
                    hasLevel = iii;
                }
            }

            iii++;
        }
        if (Level == -1) {
            Level = 7;
        }
        if (hasLevel < Level) {
            return hasLevel;
        } else {
            return -1;
        }

    }

    private boolean shouldPunk(AltoClef mod, PlayerEntity player) {
        if (player == null || player.isDead() || !player.isAlive()) {
            return false;
        }
        if (player.isCreative() || player.isSpectator()) {
            return false;
        }
        //if (!WorldHelper.canReach(mod,player.getBlockPos())) return false;
        //mod.getEntityTracker().getCloseEntities().
        return !mod.getButler().isUserAuthorized(player.getName().getString());// && _canTerminate.test(player);
    }

    private static boolean shouldForce(AltoClef mod, Task task) {
        return task != null && task.isActive() && !task.isFinished(mod);
    }

    private static void sleepSec(double seconds) {
        try {
            Thread.sleep((int) (1000 * seconds));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

package adris.altoclef.tasks.container;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.slot.EnsureFreeInventorySlotTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ContainerType;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.TimersHelper;
import adris.altoclef.util.slots.Slot;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;


public class LootContainerTask extends Task {

    public final BlockPos chest;
    public final List<Item> targets = new ArrayList<>();
    private final Predicate<ItemStack> _check;
    public boolean _isInChest = false;
    private boolean _weDoneHere = false;
    private final TimerGame _lootTimer = new TimerGame(1);

    public LootContainerTask(BlockPos chestPos, List<Item> items) {
        chest = chestPos;
        targets.addAll(items);
        _check = x -> true;
    }

    public LootContainerTask(BlockPos chestPos, List<Item> items, Predicate<ItemStack> pred) {
        chest = chestPos;
        targets.addAll(items);
        _check = pred;
    }

    @Override
    protected void onStart(AltoClef mod) {
        mod.getBehaviour().push();
        _lootTimer.reset();
        for (Item item : targets) {
            if (!mod.getBehaviour().isProtected(item)) {
                mod.getBehaviour().addProtectedItems(item);
            }
        }
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (!ContainerType.screenHandlerMatches(ContainerType.CHEST)) {
            setDebugState("Активация контейнера"); //TRS "Interact with container"
            _isInChest = false;
            return new InteractWithBlockTask(chest);
        }
        _isInChest = true;
        ItemStack cursor = StorageHelper.getItemStackInCursorSlot();
        if (!cursor.isEmpty()) {
            Optional<Slot> toFit = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursor, false);
            if (toFit.isPresent()) {
                setDebugState("Перемещение курсора в инвентарь"); //"Putting cursor in inventory"
                _lootTimer.reset();
                TimersHelper.ChestInteractTimerReset();
                mod.getSlotHandler().clickSlot(toFit.get(), 0, SlotActionType.PICKUP);
                return null;
            } else {
                setDebugState("Сканирование на свободные слоты");//"Ensuring space"
                return new EnsureFreeInventorySlotTask();
            }
        }
        Optional<Slot> optimal = getAMatchingSlot(mod);
        if (optimal.isEmpty()) {
            if(lootTimerElapsed())
                _weDoneHere = true;
            //_isInChest = false;
            return null;
        }
        setDebugState("Взять: " + targets);//"Looting items: "
        _lootTimer.reset();
        mod.getSlotHandler().clickSlot(optimal.get(), 0, SlotActionType.PICKUP);
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task task) {
        ItemStack cursorStack = StorageHelper.getItemStackInCursorSlot();
        if (!cursorStack.isEmpty()) {
            Optional<Slot> moveTo = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursorStack, false);
            moveTo.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            if (ItemHelper.canThrowAwayStack(mod, cursorStack)) {
                mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
            }
            Optional<Slot> garbage = StorageHelper.getGarbageSlot(mod);
            // Try throwing away cursor slot if it's garbage
            garbage.ifPresent(slot -> mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP));
            mod.getSlotHandler().clickSlot(Slot.UNDEFINED, 0, SlotActionType.PICKUP);
        } else {
            StorageHelper.closeScreen();
        }
        mod.getBehaviour().pop();
    }
    public boolean isInChest(){
        return _isInChest || !lootTimerElapsed();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof LootContainerTask && targets == ((LootContainerTask) other).targets;
    }

    private Optional<Slot> getAMatchingSlot(AltoClef mod) {
        for (Item item : targets) {
            List<Slot> slots = mod.getItemStorage().getSlotsWithItemContainer(item);
            if (!slots.isEmpty()) {
                for (Slot slot : slots) {
                    if (_check.test(StorageHelper.getItemStackInSlot(slot))) {
                        return Optional.of(slot);
                    }
                }
            }
        }
        return Optional.empty();
    }
    public boolean lootTimerElapsed(){
        return _lootTimer.getDuration() > 0.8;
    }
    @Override
    public boolean isFinished(AltoClef mod) {
        // СУНДУК НЕ УСПЕВАЕТ ПРОГРУЗИТЬСЯ - ОН ВИДИТ ПУСТЫЕ СЛОТЫ И ГОВОРИТ - FINISHED! ПОЭТОМУ И ЛОМАЕТСЯ!
        return //_lootTimer.getDuration() > 0.8 &&
                (_weDoneHere || (ContainerType.screenHandlerMatchesAny() &&
                getAMatchingSlot(mod).isEmpty() && !isInChest()));
    }

    @Override
    protected String toDebugString() {
        return "Looting a container";
    }
}

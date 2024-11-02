package adris.altoclef.tasks.container;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasks.InteractWithBlockTask;
import adris.altoclef.tasks.slot.ClickSlotTask;
import adris.altoclef.tasks.slot.EnsureFreeInventorySlotTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ContainerType;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.TimersHelper;
import adris.altoclef.util.slots.Slot;

import adris.altoclef.util.time.TimerGame;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;


public class LootContainerTask extends Task {
    private boolean _weDoneHere = false;
    private int _doneHereIter = 0;
    //private final TimerGame _chestInteractTimer = new TimerGame(0);
    private final Predicate<ItemStack> _check;
    public final BlockPos chest;
    public final List<Item> targets = new ArrayList<>();
    public boolean _isInChest = false;
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
        //Debug.logMessage("sss"+CantGoLooting());
        //CanGoLooting();
        for (Item item : targets) {
            if (!mod.getBehaviour().isProtected(item)) {
                mod.getBehaviour().addProtectedItems(item);
            }
        }
    }

    @Override
    protected Task onTick(AltoClef mod) {


        if(!ContainerType.screenHandlerMatches(ContainerType.CHEST)) {
            _isInChest = false;
            setDebugState("Активация контейнера"); //TRS "Interact with container"
            return new InteractWithBlockTask(chest);
        }
        _isInChest = true;
        //if(!CantGoLooting()){
        //    setDebugState("Ждемс..."+_lootTimer.getDuration());
        //    //Debug.logMessage("ПРИВЕТ)))");
        //    return null;
        //}
        ItemStack cursor = StorageHelper.getItemStackInCursorSlot();
        if (!cursor.isEmpty()) {
            //_lootTimer.reset();
            Optional<Slot> toFit = mod.getItemStorage().getSlotThatCanFitInPlayerInventory(cursor, false).or(() -> StorageHelper.getGarbageSlot(mod));
            if (toFit.isPresent()) {
                setDebugState("Перемещение курсора в инвентарь"); //"Putting cursor in inventory"
                TimersHelper.ChestInteractTimerReset();
                return new ClickSlotTask(toFit.get());
            } else {
                setDebugState("Сканирование на свободные слоты");//"Ensuring space"
                return new EnsureFreeInventorySlotTask();
            }

        }

        Optional<Slot> optimal = getAMatchingSlot(mod);
        if (optimal.isEmpty()) {
                //_lootTimer.reset();
                //Debug.logMessage("We done here "+_doneHereIter);
                _isInChest = false;
                _weDoneHere = true;
                return null;

        }
        setDebugState("Взять: " + targets);//"Looting items: "
        return new ClickSlotTask(optimal.get());
    }

    @Override
    protected void onStop(AltoClef mod, Task task) {
        //_lootTimer.reset();
        //_doneHereIter =0;
        //Debug.logMessage("We done here "+_doneHereIter);
        //_lootTimer.reset();
        //StorageHelper.is
        StorageHelper.closeScreen();
        mod.getBehaviour().pop();
        //TimersHelper.ChestInteractTimerReset();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof LootContainerTask && targets == ((LootContainerTask) other).targets;
    }
    public boolean IsInChest(){
        return _isInChest;
    }
    private Optional<Slot> getAMatchingSlot(AltoClef mod) {
        for (Item item : targets) {
            List<Slot> slots = mod.getItemStorage().getSlotsWithItemContainer(item);
            if (!slots.isEmpty()) for (Slot slot : slots) {
                if (_check.test(StorageHelper.getItemStackInSlot(slot))) return Optional.of(slot);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        //Debug.logMessage("We done here "+_lootTimer.getDuration());
        return (_weDoneHere || (ContainerType.screenHandlerMatchesAny() &&
                getAMatchingSlot(mod).isEmpty()));
    }

    @Override
    protected String toDebugString() {
        return "Ап лута в контейнерах"; //"Looting a container";
    }
}

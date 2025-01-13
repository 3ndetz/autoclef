package adris.altoclef.tasks.multiplayer;

import adris.altoclef.AltoClef;
import adris.altoclef.butler.ButlerConfig;
import adris.altoclef.tasks.slot.ClickSlotTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ContainerType;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.Slot;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.SlotActionType;
import org.apache.commons.lang3.ArrayUtils;

public class LobbyTask extends Task {
    public boolean _clicked = false;
    public boolean _joined = false;

    @Override
    protected void onStart(AltoClef mod) {
    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (ButlerConfig.getInstance().autoJoin) {
            // in choose menu
            if (ContainerType.screenHandlerMatches(ContainerType.CHEST)) {
                setDebugState("In menu");
                //StorageHelper.closeScreen();
                //_lootTask = null;
                String[] MinigamesTitles = new String[] {"мини-игры", "МИНИ-ИГРЫ", "МИНИИГРЫ"};
                String[] SkywarsTitles = new String[] {"SkyWars", "skywars", "скайварс", "скай-варс"};
                String[] MurderMysteryTitles = new String[] {"MurderMystery", "murdermystery", "МардерМистери", "Murder"};
                Slot slot = ItemHelper.getCustomItemSlot(mod, ArrayUtils.addAll(MinigamesTitles));

                if (slot != null){
                    mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP);
                } else {
                    slot = ItemHelper.getCustomItemSlot(mod, ArrayUtils.addAll(MinigamesTitles, MurderMysteryTitles));
                    if (slot != null) {
                        mod.getSlotHandler().clickSlot(slot, 0, SlotActionType.PICKUP);
                        _clicked = true;
                    }
                }
                return null;
            }
            setDebugState("Chill");
            if (ItemHelper.clickCustomItem(mod, "Выбор сервера", "Выбор лобби")) {

                //reset();
            }

            if (ItemHelper.clickCustomItem(mod, "новая игра", "начать игру", "быстро играть (пкм)")) {
                //reset();
            }
        }
        if (_clicked) {
            _joined = true;
            _clicked = false;
        }
        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {

    }
    @Override
    protected boolean isEqual(Task obj) {
        return obj instanceof LobbyTask;
    }

    @Override
    protected String toDebugString() {
        return "Server lobby move handler";
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return this._joined;  // TODO untested
    }
}
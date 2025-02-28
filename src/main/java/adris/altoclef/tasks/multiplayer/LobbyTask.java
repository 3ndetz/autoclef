package adris.altoclef.tasks.multiplayer;

import adris.altoclef.AltoClef;
import adris.altoclef.butler.ButlerConfig;
import adris.altoclef.tasks.slot.ClickSlotTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.storage.ContainerType;
import adris.altoclef.util.agent.Pipeline;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.Slot;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.SlotActionType;
import org.apache.commons.lang3.ArrayUtils;

public class LobbyTask extends Task {
    public boolean _clicked = false;
    public boolean _joined = false;
    public Pipeline _pipeline;

    public LobbyTask(){
        this(AltoClef.getPipeline());
    }

    public LobbyTask(Pipeline pipeline){
        _pipeline = pipeline;
    }

    @Override
    protected void onStart(AltoClef mod) {
    }



    @Override
    protected Task onTick(AltoClef mod) {
        this._joined = true;
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
        return "Server chest menu click handler";
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return this._joined;  // TODO untested
    }
}
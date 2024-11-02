package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.MiningRequirement;
import adris.altoclef.util.helpers.StorageHelper;
import net.minecraft.item.Item;

public class GetBuildingMaterialsTask extends Task {
    private final int _count;
    private MiningRequirement _req = MiningRequirement.HAND;
    public ItemTarget[] _toCollectTargets;

    public GetBuildingMaterialsTask(int count) {
        _count = count;
        _req = MiningRequirement.WOOD;

    }
    public GetBuildingMaterialsTask(Item ...toCollect) {
        _count = 32;
        _toCollectTargets = new ItemTarget[]{new ItemTarget(toCollect, _count)};
    }
    @Override
    protected void onStart(AltoClef mod) {

    }

    @Override
    protected Task onTick(AltoClef mod) {
        if(_toCollectTargets == null) {
            Item[] throwaways = mod.getModSettings().getThrowawayItems(mod, true);
            _toCollectTargets = new ItemTarget[]{new ItemTarget(throwaways, _count)};
        }
        return new MineAndCollectTask(_toCollectTargets, _req);
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof GetBuildingMaterialsTask task) {
            return task._count == _count;
        }
        return false;
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return StorageHelper.getBuildingMaterialCount(mod) >= _count;
    }

    @Override
    protected String toDebugString() {
        return "Collecting " + _count + " building materials.";
    }
}

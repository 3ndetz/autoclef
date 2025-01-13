package adris.altoclef.tasks.misc;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.AbstractDoToClosestObjectTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * A task that chooses the best strategy based on navigation distances using Baritone's pathfinding
 */
//
public class ChooseStrategyTask<T extends Enum<T>> extends AbstractDoToClosestObjectTask<T> {
    private final Map<T, PositionWrapper> strategyMap;
    private final Function<T, Task> strategyTaskProvider;

    public ChooseStrategyTask(Function<T, Task> strategyTaskProvider, Map<T, PositionWrapper> strategyMap) {
        this.strategyTaskProvider = strategyTaskProvider;
        this.strategyMap = new HashMap<>(strategyMap);
    }

    @Override
    protected Vec3d getPos(AltoClef mod, T strategy) {
        PositionWrapper wrapper = strategyMap.get(strategy);
        return wrapper != null ? wrapper.getPos() : null;
    }

    @Override
    protected Optional<T> getClosestTo(AltoClef mod, Vec3d pos) {
        // Filter out strategies without valid positions
        return strategyMap.entrySet().stream()
                .filter(entry -> entry.getValue().hasPosition() && entry.getValue().getPos() != null)
                .min((e1, e2) -> {
                    double dist1 = e1.getValue().getPos().squaredDistanceTo(pos);
                    double dist2 = e2.getValue().getPos().squaredDistanceTo(pos);
                    return Double.compare(dist1, dist2);
                })
                .map(Map.Entry::getKey);
    }

    @Override
    protected Vec3d getOriginPos(AltoClef mod) {
        return mod.getPlayer().getPos();
    }

    @Override
    protected Task getGoalTask(T strategy) {
        return strategyTaskProvider.apply(strategy);
    }

    @Override
    protected boolean isValid(AltoClef mod, T strategy) {
        PositionWrapper wrapper = strategyMap.get(strategy);
        return wrapper != null && wrapper.isValid(mod); // && wrapper.hasPosition() && wrapper.getPos() != null;
    }

    @Override
    protected void onStart(AltoClef mod) {
        // No tracking needed
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        // No cleanup needed
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof ChooseStrategyTask<?> task) {
            return task.strategyMap.keySet().equals(this.strategyMap.keySet());
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Choosing best strategy based on navigation distance...";
    }
}
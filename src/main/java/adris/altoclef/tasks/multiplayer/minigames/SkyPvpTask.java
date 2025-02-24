package adris.altoclef.tasks.multiplayer.minigames;

import adris.altoclef.AltoClef;
import adris.altoclef.butler.ButlerConfig;
import adris.altoclef.tasks.entity.KillPlayerTask;
import adris.altoclef.tasks.entity.ShootArrowSimpleProjectileTask;
import adris.altoclef.tasks.movement.*;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.trackers.threats.DamageTrackerStrategy;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.ItemHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.time.TimerGame;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;

import static adris.altoclef.util.helpers.ItemHelper.clickCustomItem;

public class SkyPvpTask extends Task {


    /**
     * @param mod
     */
    @Override
    protected void onStart(AltoClef mod) {

    }

    /**
     * @param mod
     * @return
     */
    @Override
    protected Task onTick(AltoClef mod) {
        return null;
    }

    /**
     * @param mod
     * @param interruptTask
     */
    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {

    }

    /**
     * @param other
     * @return
     */
    @Override
    protected boolean isEqual(Task other) {
        return false;
    }

    /**
     * @return
     */
    @Override
    protected String toDebugString() {
        return "";
    }
}
package adris.altoclef.tasks.construction.compound;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.construction.DestroyBlockTask;
import adris.altoclef.tasks.construction.PlaceBlockTask;
import adris.altoclef.tasks.construction.PlaceSignTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Objects;

/**
 * Construct grave task should be like a construct golem task and needed to:
 * find a place for grave to fit grave
 * clear blocks that mess the structure
 * build a simple grave consist of sign and 4 blocks:
 * - 2 blocks of cobblestone, vertical
 * - 2 blocks of cobblestone_slab, horizontal, connected to the forward of vertical bottom cobblestone block
 * - sign, connected to the forward of vertical top cobblestone block
 * - sign text should be the text given in task constructor
 * The final result in Z axis should look like:
 * ---
 * axis x=1     x=2     x=3
 * y=2  ||      <sign>
 * y=1  ||      __      __
 * ---
 * As you can see, there is only 2 dimensions using, so we have Y height and X length, no width.
 */
public class ConstructGraveTask extends Task {
    protected BlockPos _position;
    private final String _signText;
    private boolean _finished = false;
    private Task _placeSignTask;
    private boolean _useSmoothStoneSlabs = false; // false for standart
    public ConstructGraveTask(String signText) {
        _signText = signText;
    }
    public ConstructGraveTask(BlockPos pos, String signText) {
        _position = pos;
        _signText = signText;
    }
    
    @Override
    protected void onStart(AltoClef mod) {
        mod.getBehaviour().push();
        mod.getBehaviour().addProtectedItems(Items.COBBLESTONE, Items.COBBLESTONE_SLAB, Items.OAK_SIGN);
        //mod.getClientBaritoneSettings().blocksToAvoidBreaking.value.add(Blocks.COBBLESTONE);
        //mod.getClientBaritoneSettings().blocksToAvoidBreaking.value.add(Blocks.COBBLESTONE_SLAB);
    }
    public static boolean hasGraveMaterials(AltoClef mod) {
        return StorageHelper.itemTargetsMetInventory(mod, graveMaterials())
                || StorageHelper.itemTargetsMetInventory(mod, graveMaterialsPrettier());
    }
    @Override
    protected Task onTick(AltoClef mod) {
        // Get required materials first
        if (StorageHelper.itemTargetsMetInventory(mod, graveMaterialsPrettier())) {
            _useSmoothStoneSlabs = true;
        } else {
            if (!StorageHelper.itemTargetsMetInventory(mod, graveMaterials())) {
                setDebugState("Getting materials for the grave");
                return TaskCatalogue.getSquashedItemTask(graveMaterials());
            }
            _useSmoothStoneSlabs = false;
        }



        // Find position for grave if not set
        if (_position == null) {
            for (BlockPos pos : WorldHelper.scanRegion(mod,
                    new BlockPos(mod.getPlayer().getBlockX(), 64, mod.getPlayer().getBlockZ()),
                    new BlockPos(mod.getPlayer().getBlockX(), 128, mod.getPlayer().getBlockZ()))) {
                if (mod.getWorld().getBlockState(pos).getBlock() == Blocks.AIR) {
                    _position = pos;
                    break;
                }
            }
            if (_position == null) {
                _position = mod.getPlayer().getBlockPos();
            }
        }

        // Build the vertical cobblestone blocks
        if (!WorldHelper.isBlock(mod, _position, Blocks.COBBLESTONE)) {
            if (!WorldHelper.isBlock(mod, _position, Blocks.AIR)) {
                setDebugState("Destroying block in way of bottom cobblestone");
                return new DestroyBlockTask(_position);
            }
            setDebugState("Placing bottom cobblestone");
            mod.getBehaviour().avoidBlockBreaking(_position);
            return new PlaceBlockTask(_position, Blocks.COBBLESTONE);
        }

        if (!WorldHelper.isBlock(mod, _position.up(), Blocks.COBBLESTONE)) {
            if (!WorldHelper.isBlock(mod, _position.up(), Blocks.AIR)) {

                setDebugState("Destroying block in way of top cobblestone");
                return new DestroyBlockTask(_position.up());
            }
            setDebugState("Placing top cobblestone");
            mod.getBehaviour().avoidBlockBreaking(_position.up());
            return new PlaceBlockTask(_position.up(), Blocks.COBBLESTONE);
        }

        // Place side slabs
        boolean SIDE_OR_FORWARD_SLABS = false;
        BlockPos firstSlab;
        BlockPos secSlab;
        if (SIDE_OR_FORWARD_SLABS) {
            //side
            firstSlab = _position.south();
            secSlab = _position.north();
        } else {
            firstSlab = _position.west();
            secSlab = _position.west(2);
        }

        Block targetSlab = _useSmoothStoneSlabs ? Blocks.SMOOTH_STONE_SLAB : Blocks.COBBLESTONE_SLAB;

        if (!WorldHelper.isBlock(mod, firstSlab, targetSlab)) {
            if (!WorldHelper.isBlock(mod, firstSlab, Blocks.AIR)) {
                setDebugState("Destroying block in way of left slab");
                return new DestroyBlockTask(firstSlab);
            }
            setDebugState("Placing left slab");
            return new PlaceBlockTask(firstSlab, targetSlab);
        }


        if (!WorldHelper.isBlock(mod, secSlab, targetSlab)) {
            if (!WorldHelper.isBlock(mod, secSlab, Blocks.AIR)) {
                setDebugState("Destroying block in way of right slab");
                return new DestroyBlockTask(secSlab);
            }
            setDebugState("Placing right slab");
            return new PlaceBlockTask(secSlab, targetSlab);
        }

        // Place sign at the top
        BlockPos signPos = _position.up().west();
        //if (!WorldHelper.isBlock(mod, signPos, Blocks.OAK_SIGN)) {
            //if (!WorldHelper.isBlock(mod, signPos, Blocks.AIR)) {
            //    setDebugState("Destroying block in way of sign");
            //    return new DestroyBlockTask(signPos);
            //}
            setDebugState("Placing grave sign");

            if (_placeSignTask != null) {
                if (_placeSignTask.isFinished(mod)) {
                    _finished = true;
                    return null;
                } else {
                    return _placeSignTask;
                }
            } else {
                _placeSignTask = new PlaceSignTask(signPos, Direction.WEST, _signText);
                return _placeSignTask;
            }
            //return
        //}

        //return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {
        mod.getBehaviour().pop();
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof ConstructGraveTask task) {
            return task._signText.equals(_signText)
                    && Objects.equals(task._position, _position);
        }
        return false;
    }

    @Override
    public boolean isFinished(AltoClef mod) {
        return _finished;
               // && _position != null &&
               //WorldHelper.isBlock(mod, _position, Blocks.COBBLESTONE) &&
               //WorldHelper.isBlock(mod, _position.up(), Blocks.COBBLESTONE) &&
               //WorldHelper.isBlock(mod, _position.south(), Blocks.COBBLESTONE_SLAB) &&
               //WorldHelper.isBlock(mod, _position.north(), Blocks.COBBLESTONE_SLAB) &&
               //WorldHelper.isBlock(mod, _position.up().west(), Blocks.OAK_SIGN);
    }

    @Override
    protected String toDebugString() {
        return "Constructing Grave for " + _signText;
    }

    public static ItemTarget[] graveMaterials() {
        return new ItemTarget[]{
                new ItemTarget(Items.COBBLESTONE, 2),
                new ItemTarget(Items.COBBLESTONE_SLAB, 2),
                new ItemTarget(Items.OAK_SIGN, 1)
        };
    }
    public static ItemTarget[] graveMaterialsPrettier() {
        return new ItemTarget[]{
                new ItemTarget(Items.COBBLESTONE, 2),
                new ItemTarget(Items.SMOOTH_STONE_SLAB, 2),
                new ItemTarget(Items.OAK_SIGN, 1)
        };
    }
}

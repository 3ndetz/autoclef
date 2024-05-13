package adris.altoclef.eventbus.events;

import adris.altoclef.Debug;
import baritone.altoclef.AltoClefSettings;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class BlockPlaceEvent {
    public BlockPos blockPos;
    public BlockState blockState;

    public BlockPlaceEvent(BlockPos blockPos, BlockState blockState) {
        this.blockPos = blockPos;
        this.blockState = blockState;
        //Debug.logMessage("поставлен блок"+blockPos.toString()+" "+blockState.toString());
    }
}

package scheduler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Implement this on one of your Blocks and call a schedule method on it.
 */
public interface Scheduleable {
    /**
     * All of these parameters are passed by yourself in the scheduling call
     */
    void onScheduleEnd(World world, BlockPos pos, int scheduleId, CompoundTag additionalData);
}
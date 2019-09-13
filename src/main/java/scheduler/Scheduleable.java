package scheduler;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface Scheduleable {
    void onScheduleEnd(World world, BlockPos pos, int scheduleId, CompoundTag additionalData);
}
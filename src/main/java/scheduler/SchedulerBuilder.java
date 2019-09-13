package scheduler;


import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@SuppressWarnings({"unused"})
public class SchedulerBuilder<T extends Block & Scheduleable> {
    private T scheduleable;
    private World world;
    private int scheduleId = 0;
    private BlockPos blockPos = BlockPos.ORIGIN;
    private CompoundTag additionalData = new CompoundTag();

    /**
     * Use Scheduler.Builder instead.
     */
    SchedulerBuilder(T scheduleable, World world) {
        this.scheduleable = scheduleable;
        this.world = world;
    }

    public SchedulerBuilder scheduleId(int id) {
        this.scheduleId = id;
        return this;
    }

    public SchedulerBuilder pos(BlockPos pos) {
        this.blockPos = pos;
        return this;
    }

    public SchedulerBuilder additionalData(CompoundTag tag) {
        this.additionalData = tag;
        return this;
    }

    public CancellationToken schedule(int ticksUntilEnd) {
        return BlockScheduler.INSTANCE.schedule(ticksUntilEnd, scheduleable, world, scheduleId, blockPos, additionalData);
    }

    public CancellationToken repeat(int repeatAmount, int tickInterval) {
        return BlockScheduler.INSTANCE.repeat(tickInterval, repeatAmount, scheduleable, world, scheduleId, blockPos, additionalData);
    }

    public CancellationToken repeatFor(int ticksUntilEnd, int tickInterval) {
        return BlockScheduler.INSTANCE.repeatFor(ticksUntilEnd, tickInterval, scheduleable, world, scheduleId, blockPos, additionalData);
    }

}



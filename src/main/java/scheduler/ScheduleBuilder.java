package scheduler;


import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@SuppressWarnings({"unused"})
public class ScheduleBuilder<T extends Block & Scheduleable> {
    private T scheduleable;
    private World world;
    private int scheduleId = 0;
    private BlockPos blockPos = BlockPos.ORIGIN;
    private CompoundTag additionalData = new CompoundTag();

    public ScheduleBuilder(T scheduleable, World world) {
        this.scheduleable = scheduleable;
        this.world = world;
    }

    public ScheduleBuilder scheduleId(int id) {
        this.scheduleId = id;
        return this;
    }

    public ScheduleBuilder atPos(BlockPos pos) {
        this.blockPos = pos;
        return this;
    }

    public ScheduleBuilder additionalData(CompoundTag tag) {
        this.additionalData = tag;
        return this;
    }

    public CancellationToken schedule(int ticksUntilEnd) {
        return Scheduler.INSTANCE.schedule(ticksUntilEnd, scheduleable, world, scheduleId, blockPos, additionalData);
    }

    public CancellationToken repeat(int repeatAmount, int tickInterval) {
        return Scheduler.INSTANCE.repeat(tickInterval, repeatAmount, scheduleable, world, scheduleId, blockPos, additionalData);
    }

    public CancellationToken repeatFor(int ticksUntilEnd, int tickInterval) {
        return Scheduler.INSTANCE.repeatFor(ticksUntilEnd, tickInterval, scheduleable, world, scheduleId, blockPos, additionalData);
    }

}



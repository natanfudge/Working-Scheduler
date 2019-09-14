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

    SchedulerBuilder(T scheduleable, World world) {
        this.scheduleable = scheduleable;
        this.world = world;
    }

    /**
     * Pass an integer to onScheduleEnd. Useful for differentiating between different scheduling calls.
     */
    public SchedulerBuilder scheduleId(int id) {
        this.scheduleId = id;
        return this;
    }

    /**
     * Pass a BlockPos to onScheduleEnd.
     */
    public SchedulerBuilder pos(BlockPos pos) {
        this.blockPos = pos;
        return this;
    }

    /**
     * Pass a CompoundTag to onScheduleEnd.
     */
    public SchedulerBuilder additionalData(CompoundTag tag) {
        this.additionalData = tag;
        return this;
    }

    /**
     * Execute onScheduleEnd with the specified configuration after the specified amount of ticks.
     */
    public CancellationToken schedule(int ticksUntilEnd) {
        return BlockScheduler.INSTANCE.schedule(ticksUntilEnd, scheduleable, world, scheduleId, blockPos, additionalData);
    }

    /**
     * Execute onScheduleEnd multiple times, by specifying how many times it will repeat.
     */
    public CancellationToken repeat(int repeatAmount, int tickInterval) {
        return BlockScheduler.INSTANCE.repeat(tickInterval, repeatAmount, scheduleable, world, scheduleId, blockPos, additionalData);
    }


    /**
     * Execute onScheduleEnd multiple times, by specifying in how much time the repeating will end.
     */
    public CancellationToken repeatFor(int ticksUntilEnd, int tickInterval) {
        return BlockScheduler.INSTANCE.repeatFor(ticksUntilEnd, tickInterval, scheduleable, world, scheduleId, blockPos, additionalData);
    }

}



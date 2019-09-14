package scheduler;

import net.minecraft.block.Block;
import net.minecraft.world.World;

@SuppressWarnings("unused")
public class Scheduler {
    /**
     * Must be called first for any scheduling call. Follow up by chaining methods from SchedulerBuilder.
     */
    public static <T extends Block & Scheduleable> SchedulerBuilder Builder(T scheduleable, World world) {
        return new SchedulerBuilder<>(scheduleable, world);
    }
}

package scheduler;

import net.minecraft.block.Block;
import net.minecraft.world.World;

public class Scheduler {
    public static <T extends Block & Scheduleable> SchedulerBuilder Builder(T scheduleable, World world) {
        return new SchedulerBuilder<>(scheduleable, world);
    }
}

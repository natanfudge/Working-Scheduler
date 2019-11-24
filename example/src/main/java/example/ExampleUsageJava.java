package example;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import scheduler.Scheduler;
import scheduler.SchedulerBuilder;

import static example.ExampleUsageKt.SchedulingPlayerIdKey;


public class ExampleUsageJava extends AbstractExampleBlock {
    @Override
    public ActionResult onUse(BlockState blockState_1, World world, BlockPos pos, PlayerEntity player, Hand hand_1, BlockHitResult blockHitResult_1) {
        CompoundTag scheduleData = new CompoundTag();
        scheduleData.putUuid(SchedulingPlayerIdKey, player.getUuid());

        Scheduler.Builder(this, world)
                .scheduleId(world.isClient ? ScheduleIds.Client.ScheduleExample : ScheduleIds.Server.ScheduleExample)
                .pos(pos)
                .additionalData(scheduleData)
                .schedule(100);

        Scheduler.Builder(this, world)
                .scheduleId(world.isClient ? ScheduleIds.Client.RepeatExample : ScheduleIds.Server.RepeatExample)
                .pos(pos)
                .additionalData(scheduleData)
                .repeat(5, 5);

        this.setRepeatForCancellationToken(Scheduler.Builder(this, world)
                .scheduleId(world.isClient ? ScheduleIds.Client.RepeatForExample : ScheduleIds.Server.RepeatForExample)
                .pos(pos)
                .additionalData(scheduleData)
                .repeatFor(500, 15));

        return ActionResult.SUCCESS;
    }

}



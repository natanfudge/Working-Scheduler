package example;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import scheduler.SchedulerBuilder;

import static example.ExampleUsageKt.SchedulingPlayerIdKey;


public class ExampleUsageJava extends AbstractExampleBlock {
    @Override
    public boolean activate(BlockState blockState_1, World world, BlockPos pos, PlayerEntity player, Hand hand_1, BlockHitResult blockHitResult_1) {
        CompoundTag scheduleData = new CompoundTag();
        scheduleData.putUuid(SchedulingPlayerIdKey, player.getUuid());

        new SchedulerBuilder<>(this, world)
                .scheduleId(world.isClient ? ScheduleIds.Client.ScheduleExample : ScheduleIds.Server.ScheduleExample)
                .atPos(pos)
                .additionalData(scheduleData)
                .schedule(100);

        new SchedulerBuilder<>(this, world)
                .scheduleId(world.isClient ? ScheduleIds.Client.RepeatExample : ScheduleIds.Server.RepeatExample)
                .atPos(pos)
                .additionalData(scheduleData)
                .repeat(5, 5);

        this.setRepeatForCancellationToken(new SchedulerBuilder<>(this, world)
                .scheduleId(world.isClient ? ScheduleIds.Client.RepeatForExample : ScheduleIds.Server.RepeatForExample)
                .atPos(pos)
                .additionalData(scheduleData)
                .repeatFor(500, 15));

        return true;
    }

}
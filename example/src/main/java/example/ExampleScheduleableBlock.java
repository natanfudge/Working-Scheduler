package example;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import scheduler.CancellationToken;
import scheduler.Scheduleable;

public class ExampleScheduleableBlock extends Block implements Scheduleable {
    @Override
    public void onScheduleEnd(World world, BlockPos pos, int scheduleId, CompoundTag additionalData) {
        // Note: you should validate that the player exists and the additionalData was not tampered with.
        // No validation is done for the sake of simplicity.
        PlayerEntity player = world.getPlayerByUuid(additionalData.getUuid("player"));
        if (scheduleId == 1) {
            player.sendMessage(new LiteralText("Normal schedule ended at pos " + pos));
        } else if (scheduleId == 2) {
            player.sendMessage(new LiteralText("Repeating schedule ended at pos " + pos));
        }
    }

    CancellationToken cancellationToken;

    @Override
    public void onBlockRemoved(BlockState beforeState, World world, BlockPos pos, BlockState afterState, boolean bool) {
        this.cancellationToken.cancel(world);
    }

    //    override fun
//
//    onBlockRemoved(
//            blockState_1:BlockState?,
//            world:World,
//            blockPos_1:BlockPos?,
//            blockState_2:BlockState?,
//            boolean_1:Boolean
//    ) {
//        // Note: will only cancel the server one
//        this.repeatForCancellationToken ?.cancel(world)
//    }


    public ExampleScheduleableBlock(Settings block$Settings_1) {
        super(block$Settings_1);
    }

}

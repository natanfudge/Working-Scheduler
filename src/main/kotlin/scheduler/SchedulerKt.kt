@file:UseSerializers(ForUuid::class)
@file:Suppress("RedundantVisibilityModifier", "unused")

package scheduler

import drawer.ForUuid
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.block.Block
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import scheduler.internal.*
import scheduler.internal.CancelTickingInServerPacket
import scheduler.internal.Repetition
import scheduler.internal.cancelScheduleServer
import scheduler.internal.logWarning
import scheduler.internal.schedule
import scheduler.internal.util.isServer
import scheduler.internal.util.sendPacketToServer
import java.util.*


public interface Scheduleable {
    fun onScheduleEnd(world: World, pos: BlockPos, scheduleId: Int, additionalData: CompoundTag)
}


public object BlockScheduler {
    /**
     * @param scheduleId The same value will be available in [Scheduleable.onScheduleEnd] for you to be able
     * to differentiate between different schedules. This is not needed when you only schedule one thing in a block.
     * @param blockPos The same value will be available in [Scheduleable.onScheduleEnd] for you to use.
     * You can opt to not provide a value, but note that you will just get [BlockPos.ORIGIN] in the callback.
     */
    public fun <T> schedule(
        ticksUntilEnd: Int,
        block: T, world: World,
        scheduleId: Int = 0,
        blockPos: BlockPos = BlockPos.ORIGIN,
        additionalData: CompoundTag = CompoundTag()
    ): CancellationToken
            where T : Scheduleable, T : Block = schedule(
        block, world, scheduleId, blockPos, additionalData, repetition = Repetition.Once(world.time + ticksUntilEnd)
    )


    public fun <T> repeat(
        repeatAmount: Int,
        tickInterval: Int,
        block: T, world: World,
        scheduleId: Int = 0,
        blockPos: BlockPos = BlockPos.ORIGIN,
        additionalData: CompoundTag = CompoundTag()
    ): CancellationToken
            where T : Scheduleable, T : Block = schedule(
        block, world, scheduleId, blockPos, additionalData,
        repetition = Repetition.RepeatAmount(
            repeatInterval = tickInterval, amountLeft = repeatAmount, nextTickTime = world.time + tickInterval
        )
    )

    public fun <T> repeatFor(
        ticksUntilStop: Int,
        tickInterval: Int,
        block: T, world: World,
        scheduleId: Int = 0,
        blockPos: BlockPos = BlockPos.ORIGIN,
        additionalData: CompoundTag = CompoundTag()
    ): CancellationToken
            where T : Scheduleable, T : Block = schedule(
        block, world, scheduleId, blockPos, additionalData,
        repetition = Repetition.RepeatUntil(
            repeatInterval = tickInterval,
            nextTickTime = world.time + tickInterval,
            stopTime = world.time + ticksUntilStop
        )
    )


}

@Serializable
public data class CancellationToken internal constructor(
    /**
     * To correctly identify the scheduled action
     */
    private val cancellationUUID: UUID
) {
    fun cancel(world: World) {
        if (world.isServer && world is ServerWorld) {
            cancelScheduleServer(world, cancellationUUID)
        } else if (world.isClient) {
            sendPacketToServer(CancelTickingInServerPacket(cancellationUUID))
        } else {
            logWarning("Attempt to cancel a schedule in a world that is ClientWorld but with isClient = false. ")
        }
    }
}









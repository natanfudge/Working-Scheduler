@file:UseSerializers(ForUuid::class)
@file:Suppress("RedundantVisibilityModifier", "unused", "MemberVisibilityCanBePrivate")

package scheduler

import drawer.ForUuid
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.UseSerializers
import net.minecraft.block.Block
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import scheduler.internal.*
import scheduler.internal.util.isServer
import scheduler.internal.util.sendPacketToServer
import java.util.*

public object BlockScheduler {
    /**
     * Calls the [block]'s onScheduleEnd method after [ticksUntilEnd] ticks.
     * All other parameters of this function will simply reappear in the [Scheduleable.onScheduleEnd] for you to use.
     *
     * @param scheduleId is useful for differentiating between different schedules.
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


    /**
     * Calls the [block]'s [repeatAmount] times, with an interval of [tickInterval] ticks.
     * All other parameters of this function will simply reappear in the [Scheduleable.onScheduleEnd] for you to use.
     *
     * @param scheduleId is useful for differentiating between different schedules.
     */
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

    /**
     * Calls the [block]'s onScheduleEnd method every [tickInterval] ticks, for [ticksUntilStop] ticks.
     * All other parameters of this function will simply reappear in the [Scheduleable.onScheduleEnd] for you to use.
     *
     * @param scheduleId is useful for differentiating between different schedules.
     */
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


/**
 * Use file:UseSerializers(CancellationTokenSerializer::class)
 * to serialize CancellationTokens that are nested in a @Serializable class,
 * And use CancellationTokenSerializer.put() / CancellationTokenSerializer.getFrom() to serialize a singular CancellationToken.
 *
 * (This is because of a loom bug)
 */
@Serializer(forClass = CancellationToken::class)
public object CancellationTokenSerializer

/**
 * Returned by every scheduling call. Call [cancel] to cancel the scheduled action.
 * Usually this needs to be stored, by a BlockEntity for example. [CancellationTokenSerializer] is useful for this if you use Drawer.
 */
@Serializable
public data class CancellationToken(
    /**
     * To correctly identify the scheduled action
     */
    private val cancellationUUID: UUID
) {
    /**
     * Will cancel the scheduling call. Make sure to call on the same [World].
     */
    public fun cancel(world: World) {
        if (world.isServer && world is ServerWorld) {
            cancelScheduleServer(world, cancellationUUID)
        } else if (world.isClient) {
            sendPacketToServer(CancelTickingInServerPacket(cancellationUUID))
        } else {
            logWarning("Attempt to cancel a schedule in a world that is ClientWorld but with isClient = false. ")
        }
    }
}









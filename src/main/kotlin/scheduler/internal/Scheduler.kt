package scheduler.internal

import net.minecraft.block.Block
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import scheduler.CancellationToken
import scheduler.Scheduleable
import scheduler.internal.util.getMinecraftClient
import scheduler.internal.util.isServer
import scheduler.internal.util.sendPacketToServer
import java.util.*

internal fun <T> schedule(
    block: T,
    world: World,
    scheduleId: Int,
    blockPos: BlockPos,
    additionalData: CompoundTag,
    repetition: Repetition
): CancellationToken
        where T : Scheduleable, T : Block {


    val clientToSendTo = if (world.isServer && world is ServerWorld) {
        null
    } else if (world.isClient) {
        getMinecraftClient().player.uuid
    } else {
        logWarning(
            "Attempt to schedule in a world that is ClientWorld but with isClient = false. " +
                    "You might get a ClassNotFound exception here!"
        )
        getMinecraftClient().player.uuid
    }

    val cancellationUUID = UUID.randomUUID()

    val schedule = Schedule(
        repetition = repetition,
        context = ScheduleContext(
            blockId = Registry.BLOCK.getId(block),
            blockPos = blockPos,
            scheduleId = scheduleId,
            additionalData = additionalData
        ),
        clientRequestingSchedule = clientToSendTo,
        cancellationUUID = cancellationUUID
    )

    if (clientToSendTo != null) {
        sendPacketToServer(TickInServerPacket(schedule))
    } else {
        scheduleServer(world as ServerWorld, schedule, block)
    }

    return CancellationToken(cancellationUUID)


}

internal fun scheduleServer(world: ServerWorld, schedule: Schedule, schedulingBlock: Scheduleable) {
    val tickState = world.persistentStateManager.getOrCreate(SchedulerId) { TickerState() }
    tickState.add(schedule.apply { scheduleable = schedulingBlock })
    tickState.isDirty = true
}

internal fun cancelScheduleServer(world: ServerWorld, cancellationUUID: UUID) = world.persistentStateManager
    .getOrCreate(SchedulerId) { TickerState() }
    .cancel(cancellationUUID)



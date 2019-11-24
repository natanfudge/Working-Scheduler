@file:UseSerializers(ForIdentifier::class, ForUuid::class)

package scheduler.internal

import drawer.ForIdentifier
import drawer.ForUuid
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.server.world.ServerWorld
import scheduler.internal.util.InternalC2SPacket
import scheduler.internal.util.InternalS2CPacket
import scheduler.internal.util.world
import java.util.*

internal interface C2SPacket<T : C2SPacket<T>> : InternalC2SPacket<T> {
    override val modId get() = ModId
}

internal interface S2CPacket<T : S2CPacket<T>> : InternalS2CPacket<T> {
    override val modId get() = ModId
}

@Serializable
internal data class TickInServerPacket(val schedule: Schedule) : C2SPacket<TickInServerPacket> {
    override fun use(context: PacketContext) {
        if (context.world !is ServerWorld || context.world.isClient) {
            logWarning("A packet to the server is somehow not in a server world.")
            return
        }
        val scheduleable = getScheduleableFromRegistry(schedule.context.blockId) ?: return
        scheduleServer(context.world as ServerWorld, schedule, scheduleable)

    }

    @Transient
    override val serializer = serializer()

}


@Serializable
internal data class FinishScheduleInClientPacket(val scheduleContext: ScheduleContext) :
    S2CPacket<FinishScheduleInClientPacket> {
    override fun use(context: PacketContext) {
        val scheduleable = getScheduleableFromRegistry(scheduleContext.blockId) ?: return
        scheduleable.onScheduleEnd(
            context.world,
            scheduleContext.blockPos,
            scheduleContext.scheduleId,
            scheduleContext.additionalData
        )
    }

    @Transient
    override val serializer = serializer()
}

@Serializable
internal data class CancelTickingInServerPacket(val cancellationUUID: UUID) : C2SPacket<CancelTickingInServerPacket> {
    override fun use(context: PacketContext) {
        if (context.world !is ServerWorld || context.world.isClient) {
            logWarning("A packet to the server is somehow not in a server world.")
            return
        }
        cancelScheduleServer(context.world as ServerWorld, cancellationUUID)
    }

    @Transient
    override val serializer = serializer()
}


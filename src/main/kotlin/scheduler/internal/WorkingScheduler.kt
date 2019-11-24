package scheduler.internal

import net.fabricmc.fabric.api.event.world.WorldTickCallback
import net.minecraft.server.world.ServerWorld
import scheduler.internal.util.*

internal const val ModId = "working-scheduler"

@Suppress("unused")
internal fun init() = initCommon(ModId) {
    WorldTickCallback.EVENT.register(WorldTickCallback { if (it is ServerWorld && it.isServer) worldTick(it) })
    registerC2S(TickInServerPacket.serializer())
    registerC2S(CancelTickingInServerPacket.serializer())

}


@Suppress("unused")
internal fun initClient() = initClientOnly(ModId) {
    registerS2C(FinishScheduleInClientPacket.serializer())
}


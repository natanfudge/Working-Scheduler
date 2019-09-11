package scheduler.internal

import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.fabricmc.fabric.api.event.world.WorldTickCallback
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.LiteralText
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import scheduler.CancellationToken
import scheduler.Scheduleable
import scheduler.Scheduler
import scheduler.internal.util.*
import java.util.*

const val ModId = "working-scheduler"

@Suppress("unused")
internal fun init() = initCommon(ModId) {
    WorldTickCallback.EVENT.register(WorldTickCallback { if (it is ServerWorld && it.isServer) worldTick(it) })
    registerC2S(TickInServerPacket.serializer(), TickerSerializersModule)
    registerC2S(CancelTickingInServerPacket.serializer())

}


@Suppress("unused")
internal fun initClient() = initClientOnly(ModId) {
    registerS2C(FinishScheduleInClientPacket.serializer())

}


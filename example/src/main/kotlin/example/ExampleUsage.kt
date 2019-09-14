package example

import kotlinx.serialization.Serializable
import net.fabricmc.fabric.api.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.nbt.CompoundTag
import net.minecraft.text.LiteralText
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import scheduler.BlockScheduler
import scheduler.CancellationToken
import scheduler.Scheduleable
import java.util.*

const val ModId = "example_usage"
val JavaBlock = ExampleUsageJava()
fun init() {
    Registry.register(Registry.BLOCK, Identifier(ModId, "example_block"), ExampleBlock)
    Registry.register(
        Registry.ITEM, Identifier(ModId, "example_block"), BlockItem(
            ExampleBlock, Item.Settings().group(
                ItemGroup.MISC
            )
        )
    )

    Registry.register(Registry.BLOCK, Identifier(ModId, "example_block_java"), JavaBlock)
    Registry.register(
        Registry.ITEM, Identifier(ModId, "example_block_java"), BlockItem(
            JavaBlock, Item.Settings().group(
                ItemGroup.MISC
            )
        )
    )
}


const val SchedulingPlayerIdKey = "player"

abstract class AbstractExampleBlock : Block(FabricBlockSettings.of(Material.STONE).build()), Scheduleable {
    override fun onScheduleEnd(world: World, pos: BlockPos, scheduleId: Int, additionalData: CompoundTag) {
        val player = getSchedulingPlayer(additionalData, world) ?: return
        val messageToSend = when (scheduleId) {
            ScheduleIds.Client.ScheduleExample -> "Schedule ended on client"
            ScheduleIds.Client.RepeatExample -> "Repeat for an amount has been repeated on client"
            ScheduleIds.Client.RepeatForExample -> "Repeat Until has been repeated on client"
            ScheduleIds.Server.ScheduleExample -> "Schedule ended on server"
            ScheduleIds.Server.RepeatExample -> "Repeat for an amount has been repeated on server"
            ScheduleIds.Server.RepeatForExample -> "Repeat Until has been repeated on server"
            else -> "Unexpected schedule id"
        }

        player.sendMessage(LiteralText("$messageToSend at pos $pos."))

        when (scheduleId) {
            ScheduleIds.Client.ScheduleExample, ScheduleIds.Client.RepeatExample, ScheduleIds.Client.RepeatForExample -> {
                assert(world.isClient)
            }
            else -> assert(!world.isClient)
        }

    }

    private fun getSchedulingPlayer(additionalData: CompoundTag, world: World): PlayerEntity? {
        val playerId = additionalData.getUuid(SchedulingPlayerIdKey)
        if (playerId == UUID(0, 0)) {
            println("Warning: the player information was corrupted.")
            return null
        }
        val player = world.getPlayerByUuid(playerId)
        if (player == null) {
            println("Warning: player that scheduled no longer in the world.")
        }
        return player
    }

    override fun onBlockRemoved(
        blockState_1: BlockState?,
        world: World,
        blockPos_1: BlockPos?,
        blockState_2: BlockState?,
        boolean_1: Boolean
    ) {
        // Note: will only cancel the server one
        this.repeatForCancellationToken?.cancel(world)
    }


    object ScheduleIds {
        object Client {
            const val ScheduleExample = 1
            const val RepeatExample = 2
            const val RepeatForExample = 3
        }

        object Server {
            const val ScheduleExample = 4
            const val RepeatExample = 5
            const val RepeatForExample = 6
        }
    }

    var repeatForCancellationToken: CancellationToken? = null
}


object ExampleBlock : AbstractExampleBlock() {
    override fun activate(
        blockState: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity?,
        hand: Hand?,
        hitResult: BlockHitResult?
    ): Boolean {
        val scheduleData = CompoundTag().apply {
            putUuid(SchedulingPlayerIdKey, player?.uuid ?: UUID(0, 0))
        }
        BlockScheduler.schedule(
            ticksUntilEnd = 100,
            block = this,
            scheduleId = if (world.isClient) ScheduleIds.Client.ScheduleExample else ScheduleIds.Server.ScheduleExample,
            world = world,
            blockPos = pos,
            additionalData = scheduleData
        )

        BlockScheduler.repeat(
            tickInterval = 5,
            repeatAmount = 5,
            block = this,
            scheduleId = if (world.isClient) ScheduleIds.Client.RepeatExample else ScheduleIds.Server.RepeatExample,
            world = world,
            blockPos = pos,
            additionalData = scheduleData

        )

        repeatForCancellationToken = BlockScheduler.repeatFor(
            tickInterval = 15,
            ticksUntilStop = 500,
            block = this,
            scheduleId = if (world.isClient) ScheduleIds.Client.RepeatForExample else ScheduleIds.Server.RepeatForExample,
            world = world,
            blockPos = pos,
            additionalData = scheduleData
        )


        return true
    }
}


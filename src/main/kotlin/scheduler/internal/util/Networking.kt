package scheduler.internal.util

import drawer.readFrom
import drawer.write
import io.netty.buffer.Unpooled
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptyModule
import kotlinx.serialization.modules.SerialModule
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.network.PacketContext
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.Identifier
import net.minecraft.world.World
import java.util.stream.Stream


/*******
 * Fabric Api Wrappers
 ********/

private fun CommonModInitializationContext.registerClientToServerPacket(
        packetId: String, packetConsumer: (PacketContext, PacketByteBuf) -> Unit) {
    ServerSidePacketRegistry.INSTANCE.register(Identifier(modId, packetId), packetConsumer)
}

private fun ClientModInitializationContext.registerServerToClientPacket(
        packetId: String, packetConsumer: (PacketContext, PacketByteBuf) -> Unit) {
    ClientSidePacketRegistry.INSTANCE.register(Identifier(modId, packetId), packetConsumer)
}

private fun PlayerEntity.sendPacket(packetId: Identifier, packetBuilder: PacketByteBuf.() -> Unit) {
    val packet = PacketByteBuf(Unpooled.buffer()).apply(packetBuilder)
    ServerSidePacketRegistry.INSTANCE.sendToPlayer(this, packetId, packet)

}


private fun sendPacketToServer(packetId: Identifier, packetBuilder: PacketByteBuf.() -> Unit) {
    val packet = PacketByteBuf(Unpooled.buffer()).apply(packetBuilder)
    ClientSidePacketRegistry.INSTANCE.sendToServer(packetId, packet)

}

/******************************
 * Automatic Serializer Wrappers
 ******************************/


internal fun CommonModInitializationContext.registerC2S(serializer: KSerializer<out InternalC2SPacket<*>>, context: SerialModule) {
    registerClientToServerPacket(serializer.packetId) { packetContext, packetByteBuf ->
        serializer.readFrom(packetByteBuf,context = context).apply {
            packetContext.taskQueue.execute {
                use(packetContext)
            }
        }
    }
}

internal fun ClientModInitializationContext.registerS2C(vararg serializers: KSerializer<out InternalS2CPacket<*>>, context: SerialModule = EmptyModule) {
    for (serializer in serializers) registerS2C(serializer, context)
}


internal  fun ClientModInitializationContext.registerS2C(serializer: KSerializer<out InternalS2CPacket<*>>, context: SerialModule) {
    registerServerToClientPacket(serializer.packetId) { packetContext, packetByteBuf ->
        serializer.readFrom(packetByteBuf,context = context).apply {
            packetContext.taskQueue.execute {
                use(packetContext)
            }
        }
    }
}

internal fun CommonModInitializationContext.registerC2S(vararg serializers: KSerializer<out InternalC2SPacket<*>>, context: SerialModule = EmptyModule) {
    for (serializer in serializers) registerC2S(serializer, context)
}


/**
 * Sends a packet from the server to the client for all the players in the stream.
 */
internal fun <T : InternalS2CPacket<T>> PlayerEntity.sendPacket(packet: T) {
    sendPacket(packetId = Identifier(packet.modId, packet.serializer.packetId)) {
        packet.serializer.write(packet, this, context = packet.serializationContext)
    }
}

/**
 * Sends a packet from the server to the client for all the players in the stream.
 */
internal fun <T : InternalC2SPacket<T>> sendPacketToServer(packet: T) {
    sendPacketToServer(Identifier(packet.modId, packet.serializer.packetId)) {
        packet.serializer.write(packet, this, context = packet.serializationContext)
    }
}

internal val PacketContext.world: World get() = player.world


private val <T : Packet<out T>> KSerializer<out T>.packetId get() = descriptor.name.toLowerCase()


internal interface Packet<T : Packet<T>> {


    val serializer: KSerializer<T>

    val modId: String
    fun use(context: PacketContext)
    val serializationContext : SerialModule get() = EmptyModule
}

internal interface InternalC2SPacket<T : Packet<T>> : Packet<T>
internal interface InternalS2CPacket<T : Packet<T>> : Packet<T>


package scheduler.internal.util

import net.minecraft.client.MinecraftClient
import net.minecraft.world.World

internal fun getMinecraftClient(): MinecraftClient = MinecraftClient.getInstance()
internal val World.isServer get() = !isClient
/**
 * Should be called at the init method of the mod. Do all of your registry here.
 */
internal inline fun initCommon(modId: String, init: CommonModInitializationContext.() -> Unit) {
    CommonModInitializationContext(modId).init()
}


internal inline fun initClientOnly(modId: String, init: ClientModInitializationContext.() -> Unit) {
    ClientModInitializationContext(modId).apply {
        init()
    }

}


internal class CommonModInitializationContext(val modId: String)


internal class ClientModInitializationContext(val modId: String)


package scheduler.internal.util

import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry
import net.fabricmc.fabric.api.client.render.BlockEntityRendererRegistry
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
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


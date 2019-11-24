package scheduler.internal

import net.minecraft.world.PersistentState
import net.minecraft.world.PersistentStateManager
import org.apache.logging.log4j.LogManager

internal fun <T : PersistentState> PersistentStateManager.getOrCreate(id: String, creator: () -> T): T =
    getOrCreate(creator, id)

private val Logger = LogManager.getLogger("Working Ticker")

internal fun logWarning(warning: String) = Logger.warn(warning)

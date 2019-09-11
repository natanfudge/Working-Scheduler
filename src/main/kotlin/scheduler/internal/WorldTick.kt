package scheduler.internal

import net.minecraft.server.world.ServerWorld
import scheduler.internal.util.sendPacket

// Occurs every tick
internal fun worldTick(world: ServerWorld) {
    val worldTickers = world.persistentStateManager.getOrCreate(SchedulerId) { TickerState() }
    while (worldTickers.hasAnyTickers) {
        val top = worldTickers.closestToEnd
        if (world.time >= top.repetition.nextTickTime) {
            if (top.clientRequestingSchedule != null) {
                // If the player no longer exists then tough luck for him! He missed the schedule.
                world.getPlayerByUuid(top.clientRequestingSchedule)
                    ?.sendPacket(FinishScheduleInClientPacket(top.context))
            } else {
                top.scheduleable.onScheduleEnd(
                    world,
                    top.context.blockPos,
                    top.context.scheduleId,
                    top.context.additionalData
                )
            }

            removeIfNeeded(top, world, worldTickers)
        } else {
            break
        }

    }

}

private fun removeIfNeeded(top: Schedule, world: ServerWorld, worldTickers: TickerState) {
    var addBack = true
    when (val repetition = top.repetition) {
        is Repetition.RepeatAmount -> {
            repetition.amountLeft--
            repetition.nextTickTime = world.time + repetition.repeatInterval

            if (repetition.amountLeft <= 0) addBack = false
        }
        is Repetition.RepeatUntil -> {
            if (world.time < repetition.stopTime) {
                repetition.nextTickTime = world.time + repetition.repeatInterval
            } else addBack = false
        }
        is Repetition.Once -> addBack = false
    }

    // We remove and add back in the case we want it to keep ticking, so it gets put in the right place.
    worldTickers.removeClosestToEnd()
    if (addBack) worldTickers.add(top)
}
package visualise.coroutines.simulation

import java.util.PriorityQueue

/**
 * Virtual time clock for simulation
 * Manages simulation time, speed control, and event scheduling
 */
class SimulationClock(
    private val tickDuration: Long = 10L
) {
    private var _currentTime: Long = 0L
    private var _isPaused: Boolean = true
    private var _speed: Double = 1.0

    private var eventSequence: Long = 0L
    private val scheduledEvents = PriorityQueue<ScheduledEvent>(
        compareBy<ScheduledEvent> { it.time }.thenBy { it.sequence }
    )

    /**
     * Current simulation time in milliseconds
     */
    val currentTime: Long
        get() = _currentTime

    /**
     * Whether the clock is paused
     */
    val isPaused: Boolean
        get() = _isPaused

    /**
     * Current speed multiplier
     */
    val speed: Double
        get() = _speed

    /**
     * Pause the clock
     */
    fun pause() {
        _isPaused = true
    }

    /**
     * Resume the clock
     */
    fun resume() {
        _isPaused = false
    }

    /**
     * Set the speed multiplier
     */
    fun setSpeed(newSpeed: Double) {
        _speed = newSpeed
    }

    /**
     * Advance time by a delta amount
     */
    fun advanceTime(delta: Long) {
        if (delta <= 0) return

        val targetTime = _currentTime + delta
        advanceTimeTo(targetTime)
    }

    /**
     * Advance time to a specific point
     */
    fun advanceTimeTo(targetTime: Long) {
        if (targetTime <= _currentTime) return

        // Execute all events up to and including target time
        while (scheduledEvents.isNotEmpty() && scheduledEvents.peek().time <= targetTime) {
            val event = scheduledEvents.poll()
            _currentTime = event.time
            event.handler()
        }

        // Update to target time
        _currentTime = targetTime
    }

    /**
     * Advance time by one tick
     */
    fun tick(duration: Long = tickDuration) {
        advanceTime(duration)
    }

    /**
     * Advance to the next scheduled event (step-through debugging)
     */
    fun step() {
        val nextTime = nextEventTime() ?: return
        advanceTimeTo(nextTime)
    }

    /**
     * Reset the clock to initial state
     */
    fun reset() {
        _currentTime = 0L
        _isPaused = true
        eventSequence = 0L
        scheduledEvents.clear()
    }

    /**
     * Schedule an event at a specific time
     * If the time is in the past or present, executes immediately
     */
    fun scheduleAt(time: Long, handler: () -> Unit) {
        if (time <= _currentTime) {
            // Execute immediately if time is in the past or present
            handler()
        } else {
            scheduledEvents.add(ScheduledEvent(time, eventSequence++, handler))
        }
    }

    /**
     * Get the number of pending (not yet executed) events
     */
    fun pendingEventCount(): Int = scheduledEvents.size

    /**
     * Clear all pending events
     */
    fun clearPendingEvents() {
        scheduledEvents.clear()
    }

    /**
     * Get the time of the next scheduled event, or null if none
     */
    fun nextEventTime(): Long? = scheduledEvents.peek()?.time

    private data class ScheduledEvent(
        val time: Long,
        val sequence: Long,
        val handler: () -> Unit
    )
}

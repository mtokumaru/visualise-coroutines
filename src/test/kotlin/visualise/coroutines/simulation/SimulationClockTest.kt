package visualise.coroutines.simulation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SimulationClockTest {

    // ===== Basic Clock Tests =====

    @Test
    fun `test clock starts at zero`() {
        val clock = SimulationClock()
        assertEquals(0L, clock.currentTime)
    }

    @Test
    fun `test clock starts paused`() {
        val clock = SimulationClock()
        assertTrue(clock.isPaused)
    }

    @Test
    fun `test clock starts with 1x speed`() {
        val clock = SimulationClock()
        assertEquals(1.0, clock.speed)
    }

    // ===== Pause/Resume Tests =====

    @Test
    fun `test pause and resume`() {
        val clock = SimulationClock()

        assertTrue(clock.isPaused)

        clock.resume()
        assertFalse(clock.isPaused)

        clock.pause()
        assertTrue(clock.isPaused)
    }

    @Test
    fun `test pause when already paused is idempotent`() {
        val clock = SimulationClock()

        clock.pause()
        assertTrue(clock.isPaused)

        clock.pause()
        assertTrue(clock.isPaused)
    }

    @Test
    fun `test resume when already running is idempotent`() {
        val clock = SimulationClock()

        clock.resume()
        assertFalse(clock.isPaused)

        clock.resume()
        assertFalse(clock.isPaused)
    }

    @Test
    fun `test reset pauses the clock`() {
        val clock = SimulationClock()
        clock.resume()

        clock.reset()

        assertTrue(clock.isPaused)
    }

    // ===== Speed Control Tests =====

    @Test
    fun `test set speed to 0_25x`() {
        val clock = SimulationClock()
        clock.setSpeed(0.25)

        assertEquals(0.25, clock.speed)
    }

    @Test
    fun `test set speed to 0_5x`() {
        val clock = SimulationClock()
        clock.setSpeed(0.5)

        assertEquals(0.5, clock.speed)
    }

    @Test
    fun `test set speed to 2x`() {
        val clock = SimulationClock()
        clock.setSpeed(2.0)

        assertEquals(2.0, clock.speed)
    }

    @Test
    fun `test set speed to 4x`() {
        val clock = SimulationClock()
        clock.setSpeed(4.0)

        assertEquals(4.0, clock.speed)
    }

    @Test
    fun `test speed change does not affect paused state`() {
        val clock = SimulationClock()
        assertTrue(clock.isPaused)

        clock.setSpeed(2.0)
        assertTrue(clock.isPaused)

        clock.resume()
        assertFalse(clock.isPaused)

        clock.setSpeed(0.5)
        assertFalse(clock.isPaused)
    }

    // ===== Time Advancement Tests =====

    @Test
    fun `test advance time by delta`() {
        val clock = SimulationClock()
        assertEquals(0L, clock.currentTime)

        clock.advanceTime(100L)
        assertEquals(100L, clock.currentTime)

        clock.advanceTime(50L)
        assertEquals(150L, clock.currentTime)
    }

    @Test
    fun `test advance time to specific time`() {
        val clock = SimulationClock()

        clock.advanceTimeTo(500L)
        assertEquals(500L, clock.currentTime)

        clock.advanceTimeTo(1000L)
        assertEquals(1000L, clock.currentTime)
    }

    @Test
    fun `test advance time to earlier time does nothing`() {
        val clock = SimulationClock()
        clock.advanceTime(100L)

        clock.advanceTimeTo(50L)
        assertEquals(100L, clock.currentTime)
    }

    @Test
    fun `test reset sets time to zero`() {
        val clock = SimulationClock()
        clock.advanceTime(1000L)

        clock.reset()

        assertEquals(0L, clock.currentTime)
    }

    // ===== Event Scheduling Tests =====

    @Test
    fun `test schedule single event`() {
        val clock = SimulationClock()
        var executed = false

        clock.scheduleAt(100L) {
            executed = true
        }

        assertFalse(executed)

        clock.advanceTimeTo(100L)
        assertTrue(executed)
    }

    @Test
    fun `test schedule multiple events`() {
        val clock = SimulationClock()
        val executedEvents = mutableListOf<Int>()

        clock.scheduleAt(100L) { executedEvents.add(1) }
        clock.scheduleAt(200L) { executedEvents.add(2) }
        clock.scheduleAt(300L) { executedEvents.add(3) }

        clock.advanceTimeTo(150L)
        assertEquals(listOf(1), executedEvents)

        clock.advanceTimeTo(250L)
        assertEquals(listOf(1, 2), executedEvents)

        clock.advanceTimeTo(350L)
        assertEquals(listOf(1, 2, 3), executedEvents)
    }

    @Test
    fun `test events scheduled at same time execute in order`() {
        val clock = SimulationClock()
        val executionOrder = mutableListOf<String>()

        clock.scheduleAt(100L) { executionOrder.add("first") }
        clock.scheduleAt(100L) { executionOrder.add("second") }
        clock.scheduleAt(100L) { executionOrder.add("third") }

        clock.advanceTimeTo(100L)

        assertEquals(listOf("first", "second", "third"), executionOrder)
    }

    @Test
    fun `test schedule event in the past executes immediately`() {
        val clock = SimulationClock()
        clock.advanceTime(200L)

        var executed = false
        clock.scheduleAt(100L) {
            executed = true
        }

        assertTrue(executed)
    }

    @Test
    fun `test pending events count`() {
        val clock = SimulationClock()

        assertEquals(0, clock.pendingEventCount())

        clock.scheduleAt(100L) { }
        assertEquals(1, clock.pendingEventCount())

        clock.scheduleAt(200L) { }
        assertEquals(2, clock.pendingEventCount())

        clock.advanceTimeTo(150L)
        assertEquals(1, clock.pendingEventCount())

        clock.advanceTimeTo(250L)
        assertEquals(0, clock.pendingEventCount())
    }

    @Test
    fun `test clear pending events`() {
        val clock = SimulationClock()

        clock.scheduleAt(100L) { }
        clock.scheduleAt(200L) { }
        clock.scheduleAt(300L) { }

        assertEquals(3, clock.pendingEventCount())

        clock.clearPendingEvents()

        assertEquals(0, clock.pendingEventCount())
    }

    @Test
    fun `test reset clears pending events`() {
        val clock = SimulationClock()

        clock.scheduleAt(100L) { }
        clock.scheduleAt(200L) { }

        clock.reset()

        assertEquals(0, clock.pendingEventCount())
    }

    // ===== Tick-based Execution Tests =====

    @Test
    fun `test tick advances time by tick duration`() {
        val clock = SimulationClock(tickDuration = 10L)

        clock.tick()
        assertEquals(10L, clock.currentTime)

        clock.tick()
        assertEquals(20L, clock.currentTime)

        clock.tick()
        assertEquals(30L, clock.currentTime)
    }

    @Test
    fun `test tick executes scheduled events`() {
        val clock = SimulationClock(tickDuration = 10L)
        val executed = mutableListOf<Int>()

        clock.scheduleAt(5L) { executed.add(1) }
        clock.scheduleAt(15L) { executed.add(2) }
        clock.scheduleAt(25L) { executed.add(3) }

        clock.tick()
        assertEquals(listOf(1), executed)

        clock.tick()
        assertEquals(listOf(1, 2), executed)

        clock.tick()
        assertEquals(listOf(1, 2, 3), executed)
    }

    @Test
    fun `test multiple ticks with different durations`() {
        val clock = SimulationClock()

        clock.tick(10L)
        assertEquals(10L, clock.currentTime)

        clock.tick(25L)
        assertEquals(35L, clock.currentTime)

        clock.tick(5L)
        assertEquals(40L, clock.currentTime)
    }

    // ===== Next Event Time Tests =====

    @Test
    fun `test get next event time`() {
        val clock = SimulationClock()

        clock.scheduleAt(100L) { }
        assertEquals(100L, clock.nextEventTime())

        clock.scheduleAt(50L) { }
        assertEquals(50L, clock.nextEventTime())
    }

    @Test
    fun `test next event time returns null when no events`() {
        val clock = SimulationClock()

        assertEquals(null, clock.nextEventTime())
    }

    @Test
    fun `test next event time updates as events execute`() {
        val clock = SimulationClock()

        clock.scheduleAt(100L) { }
        clock.scheduleAt(200L) { }

        assertEquals(100L, clock.nextEventTime())

        clock.advanceTimeTo(150L)
        assertEquals(200L, clock.nextEventTime())

        clock.advanceTimeTo(250L)
        assertEquals(null, clock.nextEventTime())
    }

    // ===== Step Execution Tests =====

    @Test
    fun `test step advances to next event`() {
        val clock = SimulationClock()
        var executed = false

        clock.scheduleAt(100L) {
            executed = true
        }

        clock.step()

        assertEquals(100L, clock.currentTime)
        assertTrue(executed)
    }

    @Test
    fun `test step with no events does nothing`() {
        val clock = SimulationClock()

        clock.step()

        assertEquals(0L, clock.currentTime)
    }

    @Test
    fun `test multiple steps through events`() {
        val clock = SimulationClock()
        val executed = mutableListOf<Int>()

        clock.scheduleAt(100L) { executed.add(1) }
        clock.scheduleAt(200L) { executed.add(2) }
        clock.scheduleAt(300L) { executed.add(3) }

        clock.step()
        assertEquals(100L, clock.currentTime)
        assertEquals(listOf(1), executed)

        clock.step()
        assertEquals(200L, clock.currentTime)
        assertEquals(listOf(1, 2), executed)

        clock.step()
        assertEquals(300L, clock.currentTime)
        assertEquals(listOf(1, 2, 3), executed)
    }

    // ===== Edge Cases =====

    @Test
    fun `test advance by zero does not change time`() {
        val clock = SimulationClock()
        clock.advanceTime(100L)

        clock.advanceTime(0L)

        assertEquals(100L, clock.currentTime)
    }

    @Test
    fun `test schedule event at current time executes immediately`() {
        val clock = SimulationClock()
        clock.advanceTime(100L)

        var executed = false
        clock.scheduleAt(100L) {
            executed = true
        }

        assertTrue(executed)
    }

    @Test
    fun `test large time values`() {
        val clock = SimulationClock()

        clock.advanceTimeTo(Long.MAX_VALUE - 1000)
        assertEquals(Long.MAX_VALUE - 1000, clock.currentTime)

        clock.advanceTime(500L)
        assertEquals(Long.MAX_VALUE - 500, clock.currentTime)
    }

    @Test
    fun `test scheduling large number of events`() {
        val clock = SimulationClock()
        val eventCount = 1000

        repeat(eventCount) { i ->
            clock.scheduleAt(i.toLong() * 10) { }
        }

        // Event at time 0 executes immediately (clock starts at 0)
        // So we have 999 pending events (times 10, 20, ..., 9990)
        assertEquals(eventCount - 1, clock.pendingEventCount())

        // Advance to 5000: events at times 10, 20, ..., 5000 execute
        // That's 500 events (i=1 to i=500), leaving 499
        clock.advanceTimeTo(5000L)
        assertEquals(eventCount - 501, clock.pendingEventCount())
    }
}

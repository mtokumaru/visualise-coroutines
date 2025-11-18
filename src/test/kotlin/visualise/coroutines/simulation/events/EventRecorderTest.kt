package visualise.coroutines.simulation.events

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EventRecorderTest {

    @Test
    fun `test recording single event`() {
        val recorder = EventRecorder()
        val event = CoroutineCreated(100L, 1, null, "Default")

        recorder.record(event)

        val events = recorder.getEvents()
        assertEquals(1, events.size)
        assertEquals(event, events[0])
    }

    @Test
    fun `test recording multiple events`() {
        val recorder = EventRecorder()
        val events = listOf(
            CoroutineCreated(100L, 1, null, "Default"),
            CoroutineStarted(200L, 1),
            CoroutineCompleted(300L, 1)
        )

        events.forEach { recorder.record(it) }

        val recorded = recorder.getEvents()
        assertEquals(3, recorded.size)
        assertEquals(events, recorded)
    }

    @Test
    fun `test events are recorded in order`() {
        val recorder = EventRecorder()
        val events = listOf(
            CoroutineStarted(200L, 1),
            CoroutineCreated(100L, 1, null, "Default"),
            CoroutineCompleted(300L, 1)
        )

        events.forEach { recorder.record(it) }

        val recorded = recorder.getEvents()
        assertEquals(events, recorded)
    }

    @Test
    fun `test getEventsSorted returns events in chronological order`() {
        val recorder = EventRecorder()
        val events = listOf(
            CoroutineCompleted(300L, 1),
            CoroutineCreated(100L, 1, null, "Default"),
            CoroutineStarted(200L, 1)
        )

        events.forEach { recorder.record(it) }

        val sorted = recorder.getEventsSorted()
        assertEquals(100L, sorted[0].timestamp)
        assertEquals(200L, sorted[1].timestamp)
        assertEquals(300L, sorted[2].timestamp)
    }

    @Test
    fun `test clear removes all events`() {
        val recorder = EventRecorder()
        recorder.record(CoroutineCreated(100L, 1, null, "Default"))
        recorder.record(CoroutineStarted(200L, 1))

        assertEquals(2, recorder.getEvents().size)

        recorder.clear()

        assertTrue(recorder.getEvents().isEmpty())
    }

    @Test
    fun `test getEventCount returns correct count`() {
        val recorder = EventRecorder()

        assertEquals(0, recorder.getEventCount())

        recorder.record(CoroutineCreated(100L, 1, null, "Default"))
        assertEquals(1, recorder.getEventCount())

        recorder.record(CoroutineStarted(200L, 1))
        assertEquals(2, recorder.getEventCount())

        recorder.clear()
        assertEquals(0, recorder.getEventCount())
    }

    @Test
    fun `test getEventsInRange returns events within time window`() {
        val recorder = EventRecorder()
        val events = listOf(
            CoroutineCreated(100L, 1, null, "Default"),
            CoroutineStarted(200L, 1),
            CoroutineSuspended(300L, 1),
            CoroutineResumed(400L, 1),
            CoroutineCompleted(500L, 1)
        )

        events.forEach { recorder.record(it) }

        val range = recorder.getEventsInRange(200L, 400L)

        assertEquals(3, range.size)
        assertEquals(200L, range[0].timestamp)
        assertEquals(300L, range[1].timestamp)
        assertEquals(400L, range[2].timestamp)
    }

    @Test
    fun `test getEventsInRange with empty result`() {
        val recorder = EventRecorder()
        recorder.record(CoroutineCreated(100L, 1, null, "Default"))
        recorder.record(CoroutineCompleted(500L, 1))

        val range = recorder.getEventsInRange(200L, 300L)

        assertTrue(range.isEmpty())
    }

    @Test
    fun `test getEventsForCoroutine returns events for specific coroutine`() {
        val recorder = EventRecorder()
        val events = listOf(
            CoroutineCreated(100L, 1, null, "Default"),
            CoroutineCreated(150L, 2, null, "Default"),
            CoroutineStarted(200L, 1),
            CoroutineStarted(250L, 2),
            CoroutineCompleted(300L, 1),
            CoroutineCompleted(350L, 2)
        )

        events.forEach { recorder.record(it) }

        val coroutine1Events = recorder.getEventsForCoroutine(1)

        assertEquals(3, coroutine1Events.size)
        coroutine1Events.forEach { event ->
            when (event) {
                is CoroutineCreated -> assertEquals(1, event.coroutineId)
                is CoroutineStarted -> assertEquals(1, event.coroutineId)
                is CoroutineCompleted -> assertEquals(1, event.coroutineId)
                else -> throw AssertionError("Unexpected event type")
            }
        }
    }

    @Test
    fun `test replay from start`() {
        val recorder = EventRecorder()
        val events = listOf(
            CoroutineCreated(100L, 1, null, "Default"),
            CoroutineStarted(200L, 1),
            CoroutineCompleted(300L, 1)
        )

        events.forEach { recorder.record(it) }

        var replayedEvents = 0
        recorder.replay { event ->
            assertEquals(events[replayedEvents], event)
            replayedEvents++
        }

        assertEquals(3, replayedEvents)
    }

    @Test
    fun `test replay from specific timestamp`() {
        val recorder = EventRecorder()
        val events = listOf(
            CoroutineCreated(100L, 1, null, "Default"),
            CoroutineStarted(200L, 1),
            CoroutineSuspended(300L, 1),
            CoroutineResumed(400L, 1),
            CoroutineCompleted(500L, 1)
        )

        events.forEach { recorder.record(it) }

        var replayedEvents = 0
        recorder.replay(fromTimestamp = 300L) { event ->
            assertTrue(event.timestamp >= 300L)
            replayedEvents++
        }

        assertEquals(3, replayedEvents)
    }

    @Test
    fun `test replay until specific timestamp`() {
        val recorder = EventRecorder()
        val events = listOf(
            CoroutineCreated(100L, 1, null, "Default"),
            CoroutineStarted(200L, 1),
            CoroutineSuspended(300L, 1),
            CoroutineResumed(400L, 1),
            CoroutineCompleted(500L, 1)
        )

        events.forEach { recorder.record(it) }

        var replayedEvents = 0
        recorder.replay(toTimestamp = 300L) { event ->
            assertTrue(event.timestamp <= 300L)
            replayedEvents++
        }

        assertEquals(3, replayedEvents)
    }

    @Test
    fun `test replay with time range`() {
        val recorder = EventRecorder()
        val events = listOf(
            CoroutineCreated(100L, 1, null, "Default"),
            CoroutineStarted(200L, 1),
            CoroutineSuspended(300L, 1),
            CoroutineResumed(400L, 1),
            CoroutineCompleted(500L, 1)
        )

        events.forEach { recorder.record(it) }

        var replayedEvents = 0
        recorder.replay(fromTimestamp = 200L, toTimestamp = 400L) { event ->
            assertTrue(event.timestamp >= 200L && event.timestamp <= 400L)
            replayedEvents++
        }

        assertEquals(3, replayedEvents)
    }

    @Test
    fun `test replay with empty recorder`() {
        val recorder = EventRecorder()

        var replayedEvents = 0
        recorder.replay { _ ->
            replayedEvents++
        }

        assertEquals(0, replayedEvents)
    }

    @Test
    fun `test getLastTimestamp returns highest timestamp`() {
        val recorder = EventRecorder()
        val events = listOf(
            CoroutineStarted(200L, 1),
            CoroutineCreated(100L, 1, null, "Default"),
            CoroutineCompleted(300L, 1)
        )

        events.forEach { recorder.record(it) }

        assertEquals(300L, recorder.getLastTimestamp())
    }

    @Test
    fun `test getLastTimestamp with empty recorder returns 0`() {
        val recorder = EventRecorder()

        assertEquals(0L, recorder.getLastTimestamp())
    }

    @Test
    fun `test recording large number of events`() {
        val recorder = EventRecorder()
        val eventCount = 1000

        repeat(eventCount) { i ->
            recorder.record(CoroutineCreated(i.toLong(), i, null, "Default"))
        }

        assertEquals(eventCount, recorder.getEventCount())
        assertEquals(eventCount.toLong() - 1, recorder.getLastTimestamp())
    }

    @Test
    fun `test replay maintains event integrity`() {
        val recorder = EventRecorder()
        val originalEvents = listOf(
            CoroutineCreated(100L, 1, null, "Default"),
            ThreadAssigned(150L, 1, 0),
            CoroutineStarted(200L, 1),
            CoroutineSuspended(300L, 1),
            ThreadReleased(350L, 1, 0),
            ThreadAssigned(400L, 1, 1),
            CoroutineResumed(450L, 1),
            CoroutineCompleted(500L, 1),
            ThreadReleased(550L, 1, 1)
        )

        originalEvents.forEach { recorder.record(it) }

        val replayedEvents = mutableListOf<SimulationEvent>()
        recorder.replay { event ->
            replayedEvents.add(event)
        }

        assertEquals(originalEvents, replayedEvents)
    }
}

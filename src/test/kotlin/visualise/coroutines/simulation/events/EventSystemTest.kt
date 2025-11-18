package visualise.coroutines.simulation.events

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EventSystemTest {

    private val json = Json { prettyPrint = false }

    // ===== Event Serialization Tests =====

    @Test
    fun `test CoroutineCreated serialization`() {
        val event = CoroutineCreated(
            timestamp = 100L,
            coroutineId = 1,
            parentId = null,
            dispatcher = "Default"
        )

        val jsonString = json.encodeToString<SimulationEvent>(event)
        val decoded = json.decodeFromString<SimulationEvent>(jsonString)

        assertEquals(event, decoded)
        assertTrue(jsonString.contains("\"timestamp\":100"))
        assertTrue(jsonString.contains("\"coroutineId\":1"))
        assertTrue(jsonString.contains("\"dispatcher\":\"Default\""))
    }

    @Test
    fun `test CoroutineCreated with parent serialization`() {
        val event = CoroutineCreated(
            timestamp = 150L,
            coroutineId = 2,
            parentId = 1,
            dispatcher = "Default"
        )

        val jsonString = json.encodeToString<SimulationEvent>(event)
        val decoded = json.decodeFromString<SimulationEvent>(jsonString)

        assertEquals(event, decoded)
        assertTrue(jsonString.contains("\"parentId\":1"))
    }

    @Test
    fun `test CoroutineStarted serialization`() {
        val event = CoroutineStarted(timestamp = 200L, coroutineId = 1)

        val jsonString = json.encodeToString<SimulationEvent>(event)
        val decoded = json.decodeFromString<SimulationEvent>(jsonString)

        assertEquals(event, decoded)
    }

    @Test
    fun `test CoroutineSuspended serialization`() {
        val event = CoroutineSuspended(timestamp = 300L, coroutineId = 1)

        val jsonString = json.encodeToString<SimulationEvent>(event)
        val decoded = json.decodeFromString<SimulationEvent>(jsonString)

        assertEquals(event, decoded)
    }

    @Test
    fun `test CoroutineResumed serialization`() {
        val event = CoroutineResumed(timestamp = 400L, coroutineId = 1)

        val jsonString = json.encodeToString<SimulationEvent>(event)
        val decoded = json.decodeFromString<SimulationEvent>(jsonString)

        assertEquals(event, decoded)
    }

    @Test
    fun `test CoroutineCompleted serialization`() {
        val event = CoroutineCompleted(timestamp = 500L, coroutineId = 1)

        val jsonString = json.encodeToString<SimulationEvent>(event)
        val decoded = json.decodeFromString<SimulationEvent>(jsonString)

        assertEquals(event, decoded)
    }

    @Test
    fun `test CoroutineCancelled serialization`() {
        val event = CoroutineCancelled(timestamp = 600L, coroutineId = 1)

        val jsonString = json.encodeToString<SimulationEvent>(event)
        val decoded = json.decodeFromString<SimulationEvent>(jsonString)

        assertEquals(event, decoded)
    }

    @Test
    fun `test ThreadAssigned serialization`() {
        val event = ThreadAssigned(timestamp = 700L, coroutineId = 1, threadId = 0)

        val jsonString = json.encodeToString<SimulationEvent>(event)
        val decoded = json.decodeFromString<SimulationEvent>(jsonString)

        assertEquals(event, decoded)
        assertTrue(jsonString.contains("\"threadId\":0"))
    }

    @Test
    fun `test ThreadReleased serialization`() {
        val event = ThreadReleased(timestamp = 800L, coroutineId = 1, threadId = 0)

        val jsonString = json.encodeToString<SimulationEvent>(event)
        val decoded = json.decodeFromString<SimulationEvent>(jsonString)

        assertEquals(event, decoded)
    }

    @Test
    fun `test DispatcherQueued serialization`() {
        val event = DispatcherQueued(
            timestamp = 900L,
            coroutineId = 1,
            dispatcher = "Default"
        )

        val jsonString = json.encodeToString<SimulationEvent>(event)
        val decoded = json.decodeFromString<SimulationEvent>(jsonString)

        assertEquals(event, decoded)
        assertTrue(jsonString.contains("\"dispatcher\":\"Default\""))
    }

    // ===== Event Ordering Tests =====

    @Test
    fun `test events can be sorted by timestamp`() {
        val events = listOf(
            CoroutineCompleted(500L, 1),
            CoroutineCreated(100L, 1, null, "Default"),
            CoroutineStarted(200L, 1),
            CoroutineSuspended(300L, 1)
        )

        val sorted = events.sortedBy { it.timestamp }

        assertEquals(100L, sorted[0].timestamp)
        assertEquals(200L, sorted[1].timestamp)
        assertEquals(300L, sorted[2].timestamp)
        assertEquals(500L, sorted[3].timestamp)
    }

    @Test
    fun `test timestamp ordering is preserved during serialization`() {
        val events = listOf(
            CoroutineCreated(100L, 1, null, "Default"),
            ThreadAssigned(150L, 1, 0),
            CoroutineStarted(200L, 1),
            CoroutineSuspended(300L, 1),
            ThreadReleased(350L, 1, 0)
        )

        val jsonStrings = events.map { json.encodeToString<SimulationEvent>(it) }
        val decoded = jsonStrings.map { json.decodeFromString<SimulationEvent>(it) }

        decoded.forEachIndexed { index, event ->
            assertEquals(events[index].timestamp, event.timestamp)
        }
    }

    @Test
    fun `test events with same timestamp maintain insertion order`() {
        val events = listOf(
            CoroutineCreated(100L, 1, null, "Default"),
            CoroutineCreated(100L, 2, null, "Default"),
            CoroutineCreated(100L, 3, null, "Default")
        )

        val sorted = events.sortedBy { it.timestamp }

        assertEquals(1, (sorted[0] as CoroutineCreated).coroutineId)
        assertEquals(2, (sorted[1] as CoroutineCreated).coroutineId)
        assertEquals(3, (sorted[2] as CoroutineCreated).coroutineId)
    }

    // ===== Event List Serialization =====

    @Test
    fun `test list of events serialization`() {
        val events = listOf(
            CoroutineCreated(100L, 1, null, "Default"),
            ThreadAssigned(150L, 1, 0),
            CoroutineStarted(200L, 1),
            CoroutineCompleted(300L, 1),
            ThreadReleased(350L, 1, 0)
        )

        val jsonString = json.encodeToString<List<SimulationEvent>>(events)
        val decoded = json.decodeFromString<List<SimulationEvent>>(jsonString)

        assertEquals(events.size, decoded.size)
        events.forEachIndexed { index, event ->
            assertEquals(event, decoded[index])
        }
    }

    @Test
    fun `test empty event list serialization`() {
        val events = emptyList<SimulationEvent>()

        val jsonString = json.encodeToString<List<SimulationEvent>>(events)
        val decoded = json.decodeFromString<List<SimulationEvent>>(jsonString)

        assertTrue(decoded.isEmpty())
    }

    // ===== Event Lifecycle Tests =====

    @Test
    fun `test complete coroutine lifecycle events`() {
        val events = listOf(
            CoroutineCreated(0L, 1, null, "Default"),
            DispatcherQueued(10L, 1, "Default"),
            ThreadAssigned(20L, 1, 0),
            CoroutineStarted(30L, 1),
            CoroutineSuspended(40L, 1),
            ThreadReleased(50L, 1, 0),
            ThreadAssigned(60L, 1, 1),
            CoroutineResumed(70L, 1),
            CoroutineCompleted(80L, 1),
            ThreadReleased(90L, 1, 1)
        )

        // Serialize all events
        val jsonStrings = events.map { json.encodeToString<SimulationEvent>(it) }
        val decoded = jsonStrings.map { json.decodeFromString<SimulationEvent>(it) }

        // Verify all events decoded correctly
        assertEquals(events.size, decoded.size)
        events.forEachIndexed { index, event ->
            assertEquals(event.timestamp, decoded[index].timestamp)
        }

        // Verify chronological order
        val timestamps = decoded.map { it.timestamp }
        assertEquals(timestamps, timestamps.sorted())
    }

    @Test
    fun `test parent-child coroutine events`() {
        val events = listOf(
            CoroutineCreated(0L, 1, null, "Default"), // Parent
            CoroutineCreated(10L, 2, 1, "Default"),   // Child 1
            CoroutineCreated(20L, 3, 1, "Default"),   // Child 2
            CoroutineStarted(30L, 1),
            CoroutineStarted(40L, 2),
            CoroutineStarted(50L, 3),
            CoroutineCompleted(60L, 2),
            CoroutineCompleted(70L, 3),
            CoroutineCompleted(80L, 1)
        )

        val jsonString = json.encodeToString<List<SimulationEvent>>(events)
        val decoded = json.decodeFromString<List<SimulationEvent>>(jsonString)

        // Verify parent-child relationships preserved
        val child1 = decoded[1] as CoroutineCreated
        val child2 = decoded[2] as CoroutineCreated

        assertEquals(1, child1.parentId)
        assertEquals(1, child2.parentId)
    }

    // ===== Event Dispatcher Tests =====

    @Test
    fun `test events on different dispatchers`() {
        val events = listOf(
            CoroutineCreated(0L, 1, null, "Default"),
            CoroutineCreated(10L, 2, null, "IO"),
            DispatcherQueued(20L, 1, "Default"),
            DispatcherQueued(30L, 2, "IO")
        )

        val jsonString = json.encodeToString<List<SimulationEvent>>(events)
        val decoded = json.decodeFromString<List<SimulationEvent>>(jsonString)

        val event1 = decoded[0] as CoroutineCreated
        val event2 = decoded[1] as CoroutineCreated

        assertEquals("Default", event1.dispatcher)
        assertEquals("IO", event2.dispatcher)
    }

    // ===== Edge Cases =====

    @Test
    fun `test event with zero timestamp`() {
        val event = CoroutineCreated(0L, 1, null, "Default")

        val jsonString = json.encodeToString<SimulationEvent>(event)
        val decoded = json.decodeFromString<SimulationEvent>(jsonString)

        assertEquals(0L, decoded.timestamp)
    }

    @Test
    fun `test event with large timestamp`() {
        val event = CoroutineCreated(Long.MAX_VALUE, 1, null, "Default")

        val jsonString = json.encodeToString<SimulationEvent>(event)
        val decoded = json.decodeFromString<SimulationEvent>(jsonString)

        assertEquals(Long.MAX_VALUE, decoded.timestamp)
    }

    @Test
    fun `test event with large coroutine id`() {
        val event = CoroutineCreated(100L, Int.MAX_VALUE, null, "Default")

        val jsonString = json.encodeToString<SimulationEvent>(event)
        val decoded = json.decodeFromString<SimulationEvent>(jsonString) as CoroutineCreated

        assertEquals(Int.MAX_VALUE, decoded.coroutineId)
    }

    @Test
    fun `test all event types can be serialized as base type`() {
        val events: List<SimulationEvent> = listOf(
            CoroutineCreated(100L, 1, null, "Default"),
            CoroutineStarted(200L, 1),
            CoroutineSuspended(300L, 1),
            CoroutineResumed(400L, 1),
            CoroutineCompleted(500L, 1),
            CoroutineCancelled(600L, 2),
            ThreadAssigned(700L, 1, 0),
            ThreadReleased(800L, 1, 0),
            DispatcherQueued(900L, 1, "Default")
        )

        events.forEach { event ->
            val jsonString = json.encodeToString<SimulationEvent>(event)
            val decoded = json.decodeFromString<SimulationEvent>(jsonString)
            assertEquals(event, decoded)
        }
    }
}

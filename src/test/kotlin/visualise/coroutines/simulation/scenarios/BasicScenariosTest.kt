package visualise.coroutines.simulation.scenarios

import visualise.coroutines.simulation.events.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BasicScenariosTest {

    // ===== Scenario 1: Single Launch =====

    @Test
    fun `test single launch scenario generates events`() {
        val scenario = SingleLaunchScenario()
        val events = scenario.generateEvents()

        assertTrue(events.isNotEmpty(), "Scenario should generate events")
    }

    @Test
    fun `test single launch scenario has correct event sequence`() {
        val scenario = SingleLaunchScenario()
        val events = scenario.generateEvents()

        // Should have: Created, Queued, ThreadAssigned, Started, Completed, ThreadReleased
        assertTrue(events.size >= 6, "Should have at least 6 events")

        val createdEvent = events.filterIsInstance<CoroutineCreated>().firstOrNull()
        assertNotNull(createdEvent, "Should have CoroutineCreated event")

        val queuedEvent = events.filterIsInstance<DispatcherQueued>().firstOrNull()
        assertNotNull(queuedEvent, "Should have DispatcherQueued event")

        val threadAssignedEvent = events.filterIsInstance<ThreadAssigned>().firstOrNull()
        assertNotNull(threadAssignedEvent, "Should have ThreadAssigned event")

        val startedEvent = events.filterIsInstance<CoroutineStarted>().firstOrNull()
        assertNotNull(startedEvent, "Should have CoroutineStarted event")

        val completedEvent = events.filterIsInstance<CoroutineCompleted>().firstOrNull()
        assertNotNull(completedEvent, "Should have CoroutineCompleted event")

        val threadReleasedEvent = events.filterIsInstance<ThreadReleased>().firstOrNull()
        assertNotNull(threadReleasedEvent, "Should have ThreadReleased event")
    }

    @Test
    fun `test single launch events are chronologically ordered`() {
        val scenario = SingleLaunchScenario()
        val events = scenario.generateEvents()

        val timestamps = events.map { it.timestamp }
        assertEquals(timestamps, timestamps.sorted(), "Events should be in chronological order")
    }

    @Test
    fun `test single launch coroutine uses default dispatcher`() {
        val scenario = SingleLaunchScenario()
        val events = scenario.generateEvents()

        val createdEvent = events.filterIsInstance<CoroutineCreated>().first()
        assertEquals("Default", createdEvent.dispatcher)

        val queuedEvent = events.filterIsInstance<DispatcherQueued>().first()
        assertEquals("Default", queuedEvent.dispatcher)
    }

    @Test
    fun `test single launch coroutine has no parent`() {
        val scenario = SingleLaunchScenario()
        val events = scenario.generateEvents()

        val createdEvent = events.filterIsInstance<CoroutineCreated>().first()
        assertEquals(null, createdEvent.parentId, "Root coroutine should have no parent")
    }

    @Test
    fun `test single launch thread assignment and release use same thread`() {
        val scenario = SingleLaunchScenario()
        val events = scenario.generateEvents()

        val assignedEvent = events.filterIsInstance<ThreadAssigned>().first()
        val releasedEvent = events.filterIsInstance<ThreadReleased>().first()

        assertEquals(assignedEvent.threadId, releasedEvent.threadId, "Should use same thread")
    }

    @Test
    fun `test single launch lifecycle order`() {
        val scenario = SingleLaunchScenario()
        val events = scenario.generateEvents()

        val eventTypes = events.map { it::class.simpleName }

        val createdIndex = eventTypes.indexOf("CoroutineCreated")
        val queuedIndex = eventTypes.indexOf("DispatcherQueued")
        val assignedIndex = eventTypes.indexOf("ThreadAssigned")
        val startedIndex = eventTypes.indexOf("CoroutineStarted")
        val completedIndex = eventTypes.indexOf("CoroutineCompleted")
        val releasedIndex = eventTypes.indexOf("ThreadReleased")

        assertTrue(createdIndex < queuedIndex, "Created before Queued")
        assertTrue(queuedIndex < assignedIndex, "Queued before ThreadAssigned")
        assertTrue(assignedIndex < startedIndex, "ThreadAssigned before Started")
        assertTrue(startedIndex < completedIndex, "Started before Completed")
        assertTrue(completedIndex < releasedIndex, "Completed before ThreadReleased")
    }

    // ===== Scenario 2: Multiple Launches =====

    @Test
    fun `test multiple launches scenario generates events for all coroutines`() {
        val scenario = MultipleLaunchesScenario(count = 10)
        val events = scenario.generateEvents()

        val createdEvents = events.filterIsInstance<CoroutineCreated>()
        assertEquals(10, createdEvents.size, "Should create 10 coroutines")

        val completedEvents = events.filterIsInstance<CoroutineCompleted>()
        assertEquals(10, completedEvents.size, "All 10 coroutines should complete")
    }

    @Test
    fun `test multiple launches all use default dispatcher`() {
        val scenario = MultipleLaunchesScenario(count = 5)
        val events = scenario.generateEvents()

        val createdEvents = events.filterIsInstance<CoroutineCreated>()
        createdEvents.forEach { event ->
            assertEquals("Default", event.dispatcher)
        }
    }

    @Test
    fun `test multiple launches events are chronologically ordered`() {
        val scenario = MultipleLaunchesScenario(count = 10)
        val events = scenario.generateEvents()

        val timestamps = events.map { it.timestamp }
        assertEquals(timestamps, timestamps.sorted())
    }

    @Test
    fun `test multiple launches shows queueing behavior`() {
        val scenario = MultipleLaunchesScenario(count = 10)
        val events = scenario.generateEvents()

        // With limited threads, some coroutines should be queued
        val queuedEvents = events.filterIsInstance<DispatcherQueued>()
        assertTrue(queuedEvents.size >= 10, "All coroutines should be queued initially")
    }

    @Test
    fun `test multiple launches each coroutine gets unique ID`() {
        val scenario = MultipleLaunchesScenario(count = 10)
        val events = scenario.generateEvents()

        val createdEvents = events.filterIsInstance<CoroutineCreated>()
        val ids = createdEvents.map { it.coroutineId }
        val uniqueIds = ids.toSet()

        assertEquals(ids.size, uniqueIds.size, "All coroutine IDs should be unique")
    }

    @Test
    fun `test multiple launches all coroutines have no parent`() {
        val scenario = MultipleLaunchesScenario(count = 5)
        val events = scenario.generateEvents()

        val createdEvents = events.filterIsInstance<CoroutineCreated>()
        createdEvents.forEach { event ->
            assertEquals(null, event.parentId, "All launches are independent (no parent)")
        }
    }

    // ===== Scenario 3: Launch vs Async =====

    @Test
    fun `test launch vs async creates two coroutines`() {
        val scenario = LaunchVsAsyncScenario()
        val events = scenario.generateEvents()

        val createdEvents = events.filterIsInstance<CoroutineCreated>()
        assertEquals(2, createdEvents.size, "Should create launch and async coroutines")
    }

    @Test
    fun `test launch vs async both complete`() {
        val scenario = LaunchVsAsyncScenario()
        val events = scenario.generateEvents()

        val completedEvents = events.filterIsInstance<CoroutineCompleted>()
        assertEquals(2, completedEvents.size, "Both coroutines should complete")
    }

    @Test
    fun `test launch vs async events are ordered`() {
        val scenario = LaunchVsAsyncScenario()
        val events = scenario.generateEvents()

        val timestamps = events.map { it.timestamp }
        assertEquals(timestamps, timestamps.sorted())
    }

    @Test
    fun `test launch vs async both use default dispatcher`() {
        val scenario = LaunchVsAsyncScenario()
        val events = scenario.generateEvents()

        val createdEvents = events.filterIsInstance<CoroutineCreated>()
        createdEvents.forEach { event ->
            assertEquals("Default", event.dispatcher)
        }
    }

    @Test
    fun `test launch vs async different coroutine IDs`() {
        val scenario = LaunchVsAsyncScenario()
        val events = scenario.generateEvents()

        val createdEvents = events.filterIsInstance<CoroutineCreated>()
        val ids = createdEvents.map { it.coroutineId }.toSet()

        assertEquals(2, ids.size, "Launch and async should have different IDs")
    }

    // ===== Scenario Metadata Tests =====

    @Test
    fun `test single launch scenario has name and description`() {
        val scenario = SingleLaunchScenario()

        assertTrue(scenario.name.isNotEmpty())
        assertTrue(scenario.description.isNotEmpty())
    }

    @Test
    fun `test multiple launches scenario has name and description`() {
        val scenario = MultipleLaunchesScenario()

        assertTrue(scenario.name.isNotEmpty())
        assertTrue(scenario.description.isNotEmpty())
    }

    @Test
    fun `test launch vs async scenario has name and description`() {
        val scenario = LaunchVsAsyncScenario()

        assertTrue(scenario.name.isNotEmpty())
        assertTrue(scenario.description.isNotEmpty())
    }
}

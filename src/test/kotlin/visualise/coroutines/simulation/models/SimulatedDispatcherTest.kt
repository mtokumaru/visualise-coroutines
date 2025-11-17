package visualise.coroutines.simulation.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SimulatedDispatcherTest {

    @Test
    fun `test dispatcher creation with default values`() {
        val dispatcher = SimulatedDispatcher(
            name = "Default",
            type = DispatcherType.DEFAULT,
            maxThreads = 4
        )

        assertEquals("Default", dispatcher.name)
        assertEquals(DispatcherType.DEFAULT, dispatcher.type)
        assertEquals(4, dispatcher.maxThreads)
        assertTrue(dispatcher.threads.isEmpty())
        assertTrue(dispatcher.queue.isEmpty())
    }

    @Test
    fun `test dispatcher with threads`() {
        val threads = listOf(
            SimulatedThread(0, DispatcherType.DEFAULT),
            SimulatedThread(1, DispatcherType.DEFAULT),
            SimulatedThread(2, DispatcherType.DEFAULT)
        )

        val dispatcher = SimulatedDispatcher(
            name = "Default",
            type = DispatcherType.DEFAULT,
            maxThreads = 4,
            threads = threads
        )

        assertEquals(3, dispatcher.threads.size)
        assertEquals(4, dispatcher.maxThreads)
    }

    @Test
    fun `test dispatcher with queue`() {
        val queue = listOf(1, 2, 3, 4, 5)

        val dispatcher = SimulatedDispatcher(
            name = "Default",
            type = DispatcherType.DEFAULT,
            maxThreads = 2,
            queue = queue
        )

        assertEquals(5, dispatcher.queue.size)
        assertEquals(listOf(1, 2, 3, 4, 5), dispatcher.queue)
    }

    @Test
    fun `test Default dispatcher configuration`() {
        val cpuCount = 4
        val dispatcher = SimulatedDispatcher(
            name = "Default",
            type = DispatcherType.DEFAULT,
            maxThreads = cpuCount
        )

        assertEquals("Default", dispatcher.name)
        assertEquals(DispatcherType.DEFAULT, dispatcher.type)
        assertEquals(cpuCount, dispatcher.maxThreads)
    }

    @Test
    fun `test IO dispatcher configuration`() {
        val dispatcher = SimulatedDispatcher(
            name = "IO",
            type = DispatcherType.IO,
            maxThreads = 64
        )

        assertEquals("IO", dispatcher.name)
        assertEquals(DispatcherType.IO, dispatcher.type)
        assertEquals(64, dispatcher.maxThreads)
    }

    @Test
    fun `test Main dispatcher configuration`() {
        val dispatcher = SimulatedDispatcher(
            name = "Main",
            type = DispatcherType.MAIN,
            maxThreads = 1
        )

        assertEquals("Main", dispatcher.name)
        assertEquals(DispatcherType.MAIN, dispatcher.type)
        assertEquals(1, dispatcher.maxThreads)
    }

    @Test
    fun `test custom dispatcher configuration`() {
        val dispatcher = SimulatedDispatcher(
            name = "CustomPool",
            type = DispatcherType.CUSTOM,
            maxThreads = 8
        )

        assertEquals("CustomPool", dispatcher.name)
        assertEquals(DispatcherType.CUSTOM, dispatcher.type)
        assertEquals(8, dispatcher.maxThreads)
    }

    @Test
    fun `test adding to queue`() {
        var dispatcher = SimulatedDispatcher(
            name = "Default",
            type = DispatcherType.DEFAULT,
            maxThreads = 2
        )

        assertTrue(dispatcher.queue.isEmpty())

        dispatcher = dispatcher.copy(queue = dispatcher.queue + 1)
        assertEquals(1, dispatcher.queue.size)
        assertEquals(listOf(1), dispatcher.queue)

        dispatcher = dispatcher.copy(queue = dispatcher.queue + 2)
        assertEquals(2, dispatcher.queue.size)
        assertEquals(listOf(1, 2), dispatcher.queue)
    }

    @Test
    fun `test removing from queue`() {
        var dispatcher = SimulatedDispatcher(
            name = "Default",
            type = DispatcherType.DEFAULT,
            maxThreads = 2,
            queue = listOf(1, 2, 3)
        )

        assertEquals(3, dispatcher.queue.size)

        dispatcher = dispatcher.copy(queue = dispatcher.queue.drop(1))
        assertEquals(2, dispatcher.queue.size)
        assertEquals(listOf(2, 3), dispatcher.queue)

        dispatcher = dispatcher.copy(queue = dispatcher.queue.drop(1))
        assertEquals(1, dispatcher.queue.size)
        assertEquals(listOf(3), dispatcher.queue)
    }

    @Test
    fun `test dispatcher immutability`() {
        val dispatcher1 = SimulatedDispatcher(
            name = "Default",
            type = DispatcherType.DEFAULT,
            maxThreads = 4,
            queue = listOf(1, 2)
        )

        val dispatcher2 = dispatcher1.copy(queue = dispatcher1.queue + 3)

        assertEquals(2, dispatcher1.queue.size)
        assertEquals(3, dispatcher2.queue.size)
    }

    @Test
    fun `test dispatcher equality`() {
        val d1 = SimulatedDispatcher("Default", DispatcherType.DEFAULT, 4)
        val d2 = SimulatedDispatcher("Default", DispatcherType.DEFAULT, 4)
        val d3 = SimulatedDispatcher("Default", DispatcherType.DEFAULT, 8)
        val d4 = SimulatedDispatcher("IO", DispatcherType.IO, 4)

        assertEquals(d1, d2)
        assertNotEquals(d1, d3)
        assertNotEquals(d1, d4)
    }

    @Test
    fun `test dispatcher with full thread pool`() {
        val threads = (0 until 4).map {
            SimulatedThread(it, DispatcherType.DEFAULT, ThreadState.RUNNING)
        }

        val dispatcher = SimulatedDispatcher(
            name = "Default",
            type = DispatcherType.DEFAULT,
            maxThreads = 4,
            threads = threads
        )

        assertEquals(4, dispatcher.threads.size)
        assertEquals(dispatcher.maxThreads, dispatcher.threads.size)
        assertTrue(dispatcher.threads.all { it.state == ThreadState.RUNNING })
    }

    @Test
    fun `test dispatcher queue FIFO behavior`() {
        var dispatcher = SimulatedDispatcher(
            name = "Default",
            type = DispatcherType.DEFAULT,
            maxThreads = 2
        )

        // Add coroutines to queue
        dispatcher = dispatcher.copy(queue = dispatcher.queue + 1)
        dispatcher = dispatcher.copy(queue = dispatcher.queue + 2)
        dispatcher = dispatcher.copy(queue = dispatcher.queue + 3)

        assertEquals(listOf(1, 2, 3), dispatcher.queue)

        // Remove first (FIFO)
        val first = dispatcher.queue.first()
        dispatcher = dispatcher.copy(queue = dispatcher.queue.drop(1))

        assertEquals(1, first)
        assertEquals(listOf(2, 3), dispatcher.queue)
    }
}

package visualise.coroutines.simulation.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class SimulatedCoroutineTest {

    @Test
    fun `test coroutine creation with default values`() {
        val coroutine = SimulatedCoroutine(
            id = 1,
            dispatcher = DispatcherType.DEFAULT
        )

        assertEquals(1, coroutine.id)
        assertNull(coroutine.parentId)
        assertEquals(CoroutineState.CREATED, coroutine.state)
        assertEquals(DispatcherType.DEFAULT, coroutine.dispatcher)
        assertNull(coroutine.threadId)
    }

    @Test
    fun `test coroutine with parent`() {
        val parent = SimulatedCoroutine(id = 1, dispatcher = DispatcherType.DEFAULT)
        val child = SimulatedCoroutine(
            id = 2,
            parentId = parent.id,
            dispatcher = DispatcherType.DEFAULT
        )

        assertEquals(1, parent.id)
        assertNull(parent.parentId)

        assertEquals(2, child.id)
        assertEquals(1, child.parentId)
    }

    @Test
    fun `test coroutine with all states`() {
        val created = SimulatedCoroutine(1, dispatcher = DispatcherType.DEFAULT, state = CoroutineState.CREATED)
        val active = SimulatedCoroutine(2, dispatcher = DispatcherType.DEFAULT, state = CoroutineState.ACTIVE)
        val suspended = SimulatedCoroutine(3, dispatcher = DispatcherType.DEFAULT, state = CoroutineState.SUSPENDED)
        val resumed = SimulatedCoroutine(4, dispatcher = DispatcherType.DEFAULT, state = CoroutineState.RESUMED)
        val completed = SimulatedCoroutine(5, dispatcher = DispatcherType.DEFAULT, state = CoroutineState.COMPLETED)
        val cancelled = SimulatedCoroutine(6, dispatcher = DispatcherType.DEFAULT, state = CoroutineState.CANCELLED)

        assertEquals(CoroutineState.CREATED, created.state)
        assertEquals(CoroutineState.ACTIVE, active.state)
        assertEquals(CoroutineState.SUSPENDED, suspended.state)
        assertEquals(CoroutineState.RESUMED, resumed.state)
        assertEquals(CoroutineState.COMPLETED, completed.state)
        assertEquals(CoroutineState.CANCELLED, cancelled.state)
    }

    @Test
    fun `test coroutine lifecycle transitions`() {
        var coroutine = SimulatedCoroutine(1, dispatcher = DispatcherType.DEFAULT)
        assertEquals(CoroutineState.CREATED, coroutine.state)
        assertNull(coroutine.threadId)

        // Assign thread and become active
        coroutine = coroutine.copy(state = CoroutineState.ACTIVE, threadId = 0)
        assertEquals(CoroutineState.ACTIVE, coroutine.state)
        assertEquals(0, coroutine.threadId)

        // Suspend
        coroutine = coroutine.copy(state = CoroutineState.SUSPENDED, threadId = null)
        assertEquals(CoroutineState.SUSPENDED, coroutine.state)
        assertNull(coroutine.threadId)

        // Resume
        coroutine = coroutine.copy(state = CoroutineState.RESUMED, threadId = 1)
        assertEquals(CoroutineState.RESUMED, coroutine.state)
        assertEquals(1, coroutine.threadId)

        // Complete
        coroutine = coroutine.copy(state = CoroutineState.COMPLETED, threadId = null)
        assertEquals(CoroutineState.COMPLETED, coroutine.state)
        assertNull(coroutine.threadId)
    }

    @Test
    fun `test coroutine on different dispatchers`() {
        val onDefault = SimulatedCoroutine(1, dispatcher = DispatcherType.DEFAULT)
        val onIO = SimulatedCoroutine(2, dispatcher = DispatcherType.IO)
        val onMain = SimulatedCoroutine(3, dispatcher = DispatcherType.MAIN)
        val onCustom = SimulatedCoroutine(4, dispatcher = DispatcherType.CUSTOM)

        assertEquals(DispatcherType.DEFAULT, onDefault.dispatcher)
        assertEquals(DispatcherType.IO, onIO.dispatcher)
        assertEquals(DispatcherType.MAIN, onMain.dispatcher)
        assertEquals(DispatcherType.CUSTOM, onCustom.dispatcher)
    }

    @Test
    fun `test coroutine dispatcher switch with withContext`() {
        var coroutine = SimulatedCoroutine(
            id = 1,
            dispatcher = DispatcherType.DEFAULT,
            state = CoroutineState.ACTIVE,
            threadId = 0
        )

        assertEquals(DispatcherType.DEFAULT, coroutine.dispatcher)
        assertEquals(0, coroutine.threadId)

        // Switch to IO dispatcher
        coroutine = coroutine.copy(
            dispatcher = DispatcherType.IO,
            state = CoroutineState.SUSPENDED,
            threadId = null
        )

        assertEquals(DispatcherType.IO, coroutine.dispatcher)
        assertEquals(CoroutineState.SUSPENDED, coroutine.state)
        assertNull(coroutine.threadId)

        // Resume on IO thread
        coroutine = coroutine.copy(
            state = CoroutineState.ACTIVE,
            threadId = 10
        )

        assertEquals(DispatcherType.IO, coroutine.dispatcher)
        assertEquals(10, coroutine.threadId)
    }

    @Test
    fun `test parent-child hierarchy`() {
        val parent = SimulatedCoroutine(1, dispatcher = DispatcherType.DEFAULT)
        val child1 = SimulatedCoroutine(2, parentId = 1, dispatcher = DispatcherType.DEFAULT)
        val child2 = SimulatedCoroutine(3, parentId = 1, dispatcher = DispatcherType.DEFAULT)
        val grandchild = SimulatedCoroutine(4, parentId = 2, dispatcher = DispatcherType.DEFAULT)

        assertNull(parent.parentId)
        assertEquals(1, child1.parentId)
        assertEquals(1, child2.parentId)
        assertEquals(2, grandchild.parentId)
    }

    @Test
    fun `test coroutine cancellation`() {
        var coroutine = SimulatedCoroutine(
            id = 1,
            dispatcher = DispatcherType.DEFAULT,
            state = CoroutineState.ACTIVE,
            threadId = 0
        )

        coroutine = coroutine.copy(
            state = CoroutineState.CANCELLED,
            threadId = null
        )

        assertEquals(CoroutineState.CANCELLED, coroutine.state)
        assertNull(coroutine.threadId)
    }

    @Test
    fun `test coroutine immutability`() {
        val coroutine1 = SimulatedCoroutine(
            id = 1,
            dispatcher = DispatcherType.DEFAULT,
            state = CoroutineState.CREATED
        )

        val coroutine2 = coroutine1.copy(state = CoroutineState.ACTIVE, threadId = 0)

        assertEquals(CoroutineState.CREATED, coroutine1.state)
        assertNull(coroutine1.threadId)

        assertEquals(CoroutineState.ACTIVE, coroutine2.state)
        assertEquals(0, coroutine2.threadId)
    }

    @Test
    fun `test coroutine equality`() {
        val c1 = SimulatedCoroutine(1, dispatcher = DispatcherType.DEFAULT)
        val c2 = SimulatedCoroutine(1, dispatcher = DispatcherType.DEFAULT)
        val c3 = SimulatedCoroutine(2, dispatcher = DispatcherType.DEFAULT)
        val c4 = SimulatedCoroutine(1, dispatcher = DispatcherType.IO)
        val c5 = SimulatedCoroutine(1, dispatcher = DispatcherType.DEFAULT, state = CoroutineState.ACTIVE)

        assertEquals(c1, c2)
        assertNotEquals(c1, c3)
        assertNotEquals(c1, c4)
        assertNotEquals(c1, c5)
    }

    @Test
    fun `test multiple coroutines with same parent`() {
        val parentId = 1
        val children = (2..5).map {
            SimulatedCoroutine(
                id = it,
                parentId = parentId,
                dispatcher = DispatcherType.DEFAULT
            )
        }

        assertEquals(4, children.size)
        children.forEach {
            assertEquals(parentId, it.parentId)
        }
    }

    @Test
    fun `test coroutine thread assignment and release`() {
        var coroutine = SimulatedCoroutine(1, dispatcher = DispatcherType.DEFAULT)
        assertNull(coroutine.threadId)

        // Assign thread
        coroutine = coroutine.copy(threadId = 5, state = CoroutineState.ACTIVE)
        assertEquals(5, coroutine.threadId)
        assertEquals(CoroutineState.ACTIVE, coroutine.state)

        // Release thread (suspend)
        coroutine = coroutine.copy(threadId = null, state = CoroutineState.SUSPENDED)
        assertNull(coroutine.threadId)
        assertEquals(CoroutineState.SUSPENDED, coroutine.state)

        // Reassign different thread
        coroutine = coroutine.copy(threadId = 7, state = CoroutineState.RESUMED)
        assertEquals(7, coroutine.threadId)
        assertEquals(CoroutineState.RESUMED, coroutine.state)
    }
}

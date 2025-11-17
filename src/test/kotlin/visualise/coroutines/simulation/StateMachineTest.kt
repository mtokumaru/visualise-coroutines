package visualise.coroutines.simulation

import visualise.coroutines.simulation.models.CoroutineState
import visualise.coroutines.simulation.models.ThreadState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class StateMachineTest {

    // ===== Coroutine State Transitions =====

    @Test
    fun `test valid coroutine transition from CREATED to ACTIVE`() {
        val result = CoroutineStateMachine.transition(CoroutineState.CREATED, CoroutineState.ACTIVE)
        assertTrue(result.isValid)
        assertEquals(CoroutineState.ACTIVE, result.newState)
    }

    @Test
    fun `test valid coroutine transition from ACTIVE to SUSPENDED`() {
        val result = CoroutineStateMachine.transition(CoroutineState.ACTIVE, CoroutineState.SUSPENDED)
        assertTrue(result.isValid)
        assertEquals(CoroutineState.SUSPENDED, result.newState)
    }

    @Test
    fun `test valid coroutine transition from SUSPENDED to RESUMED`() {
        val result = CoroutineStateMachine.transition(CoroutineState.SUSPENDED, CoroutineState.RESUMED)
        assertTrue(result.isValid)
        assertEquals(CoroutineState.RESUMED, result.newState)
    }

    @Test
    fun `test valid coroutine transition from RESUMED to ACTIVE`() {
        val result = CoroutineStateMachine.transition(CoroutineState.RESUMED, CoroutineState.ACTIVE)
        assertTrue(result.isValid)
        assertEquals(CoroutineState.ACTIVE, result.newState)
    }

    @Test
    fun `test valid coroutine transition from ACTIVE to COMPLETED`() {
        val result = CoroutineStateMachine.transition(CoroutineState.ACTIVE, CoroutineState.COMPLETED)
        assertTrue(result.isValid)
        assertEquals(CoroutineState.COMPLETED, result.newState)
    }

    @Test
    fun `test valid coroutine transition from RESUMED to COMPLETED`() {
        val result = CoroutineStateMachine.transition(CoroutineState.RESUMED, CoroutineState.COMPLETED)
        assertTrue(result.isValid)
        assertEquals(CoroutineState.COMPLETED, result.newState)
    }

    @Test
    fun `test valid coroutine cancellation from CREATED`() {
        val result = CoroutineStateMachine.transition(CoroutineState.CREATED, CoroutineState.CANCELLED)
        assertTrue(result.isValid)
        assertEquals(CoroutineState.CANCELLED, result.newState)
    }

    @Test
    fun `test valid coroutine cancellation from ACTIVE`() {
        val result = CoroutineStateMachine.transition(CoroutineState.ACTIVE, CoroutineState.CANCELLED)
        assertTrue(result.isValid)
        assertEquals(CoroutineState.CANCELLED, result.newState)
    }

    @Test
    fun `test valid coroutine cancellation from SUSPENDED`() {
        val result = CoroutineStateMachine.transition(CoroutineState.SUSPENDED, CoroutineState.CANCELLED)
        assertTrue(result.isValid)
        assertEquals(CoroutineState.CANCELLED, result.newState)
    }

    @Test
    fun `test valid coroutine cancellation from RESUMED`() {
        val result = CoroutineStateMachine.transition(CoroutineState.RESUMED, CoroutineState.CANCELLED)
        assertTrue(result.isValid)
        assertEquals(CoroutineState.CANCELLED, result.newState)
    }

    @Test
    fun `test invalid coroutine transition from COMPLETED to ACTIVE`() {
        assertFailsWith<IllegalStateException> {
            CoroutineStateMachine.transitionOrThrow(CoroutineState.COMPLETED, CoroutineState.ACTIVE)
        }
    }

    @Test
    fun `test invalid coroutine transition from CANCELLED to ACTIVE`() {
        assertFailsWith<IllegalStateException> {
            CoroutineStateMachine.transitionOrThrow(CoroutineState.CANCELLED, CoroutineState.ACTIVE)
        }
    }

    @Test
    fun `test invalid coroutine transition from COMPLETED to CANCELLED`() {
        assertFailsWith<IllegalStateException> {
            CoroutineStateMachine.transitionOrThrow(CoroutineState.COMPLETED, CoroutineState.CANCELLED)
        }
    }

    @Test
    fun `test invalid coroutine transition from CREATED to SUSPENDED`() {
        assertFailsWith<IllegalStateException> {
            CoroutineStateMachine.transitionOrThrow(CoroutineState.CREATED, CoroutineState.SUSPENDED)
        }
    }

    @Test
    fun `test invalid coroutine transition from CREATED to RESUMED`() {
        assertFailsWith<IllegalStateException> {
            CoroutineStateMachine.transitionOrThrow(CoroutineState.CREATED, CoroutineState.RESUMED)
        }
    }

    @Test
    fun `test invalid coroutine transition from ACTIVE to RESUMED`() {
        assertFailsWith<IllegalStateException> {
            CoroutineStateMachine.transitionOrThrow(CoroutineState.ACTIVE, CoroutineState.RESUMED)
        }
    }

    @Test
    fun `test coroutine lifecycle path - normal completion`() {
        var state = CoroutineState.CREATED

        state = CoroutineStateMachine.transitionOrThrow(state, CoroutineState.ACTIVE)
        assertEquals(CoroutineState.ACTIVE, state)

        state = CoroutineStateMachine.transitionOrThrow(state, CoroutineState.SUSPENDED)
        assertEquals(CoroutineState.SUSPENDED, state)

        state = CoroutineStateMachine.transitionOrThrow(state, CoroutineState.RESUMED)
        assertEquals(CoroutineState.RESUMED, state)

        state = CoroutineStateMachine.transitionOrThrow(state, CoroutineState.COMPLETED)
        assertEquals(CoroutineState.COMPLETED, state)
    }

    @Test
    fun `test coroutine lifecycle path - early cancellation`() {
        var state = CoroutineState.CREATED

        state = CoroutineStateMachine.transitionOrThrow(state, CoroutineState.ACTIVE)
        assertEquals(CoroutineState.ACTIVE, state)

        state = CoroutineStateMachine.transitionOrThrow(state, CoroutineState.CANCELLED)
        assertEquals(CoroutineState.CANCELLED, state)

        // Cannot transition from CANCELLED
        assertFailsWith<IllegalStateException> {
            CoroutineStateMachine.transitionOrThrow(state, CoroutineState.COMPLETED)
        }
    }

    // ===== Thread State Transitions =====

    @Test
    fun `test valid thread transition from IDLE to RUNNING`() {
        val result = ThreadStateMachine.transition(ThreadState.IDLE, ThreadState.RUNNING)
        assertTrue(result.isValid)
        assertEquals(ThreadState.RUNNING, result.newState)
    }

    @Test
    fun `test valid thread transition from RUNNING to WAITING`() {
        val result = ThreadStateMachine.transition(ThreadState.RUNNING, ThreadState.WAITING)
        assertTrue(result.isValid)
        assertEquals(ThreadState.WAITING, result.newState)
    }

    @Test
    fun `test valid thread transition from WAITING to RUNNING`() {
        val result = ThreadStateMachine.transition(ThreadState.WAITING, ThreadState.RUNNING)
        assertTrue(result.isValid)
        assertEquals(ThreadState.RUNNING, result.newState)
    }

    @Test
    fun `test valid thread transition from RUNNING to PARKED`() {
        val result = ThreadStateMachine.transition(ThreadState.RUNNING, ThreadState.PARKED)
        assertTrue(result.isValid)
        assertEquals(ThreadState.PARKED, result.newState)
    }

    @Test
    fun `test valid thread transition from PARKED to RUNNING`() {
        val result = ThreadStateMachine.transition(ThreadState.PARKED, ThreadState.RUNNING)
        assertTrue(result.isValid)
        assertEquals(ThreadState.RUNNING, result.newState)
    }

    @Test
    fun `test valid thread transition from RUNNING to IDLE`() {
        val result = ThreadStateMachine.transition(ThreadState.RUNNING, ThreadState.IDLE)
        assertTrue(result.isValid)
        assertEquals(ThreadState.IDLE, result.newState)
    }

    @Test
    fun `test valid thread transition from IDLE to TERMINATED`() {
        val result = ThreadStateMachine.transition(ThreadState.IDLE, ThreadState.TERMINATED)
        assertTrue(result.isValid)
        assertEquals(ThreadState.TERMINATED, result.newState)
    }

    @Test
    fun `test valid thread transition from RUNNING to TERMINATED`() {
        val result = ThreadStateMachine.transition(ThreadState.RUNNING, ThreadState.TERMINATED)
        assertTrue(result.isValid)
        assertEquals(ThreadState.TERMINATED, result.newState)
    }

    @Test
    fun `test invalid thread transition from TERMINATED to RUNNING`() {
        assertFailsWith<IllegalStateException> {
            ThreadStateMachine.transitionOrThrow(ThreadState.TERMINATED, ThreadState.RUNNING)
        }
    }

    @Test
    fun `test invalid thread transition from TERMINATED to IDLE`() {
        assertFailsWith<IllegalStateException> {
            ThreadStateMachine.transitionOrThrow(ThreadState.TERMINATED, ThreadState.IDLE)
        }
    }

    @Test
    fun `test invalid thread transition from IDLE to WAITING`() {
        assertFailsWith<IllegalStateException> {
            ThreadStateMachine.transitionOrThrow(ThreadState.IDLE, ThreadState.WAITING)
        }
    }

    @Test
    fun `test invalid thread transition from IDLE to PARKED`() {
        assertFailsWith<IllegalStateException> {
            ThreadStateMachine.transitionOrThrow(ThreadState.IDLE, ThreadState.PARKED)
        }
    }

    @Test
    fun `test invalid thread transition from WAITING to PARKED`() {
        assertFailsWith<IllegalStateException> {
            ThreadStateMachine.transitionOrThrow(ThreadState.WAITING, ThreadState.PARKED)
        }
    }

    @Test
    fun `test thread lifecycle path - normal execution`() {
        var state = ThreadState.IDLE

        state = ThreadStateMachine.transitionOrThrow(state, ThreadState.RUNNING)
        assertEquals(ThreadState.RUNNING, state)

        state = ThreadStateMachine.transitionOrThrow(state, ThreadState.WAITING)
        assertEquals(ThreadState.WAITING, state)

        state = ThreadStateMachine.transitionOrThrow(state, ThreadState.RUNNING)
        assertEquals(ThreadState.RUNNING, state)

        state = ThreadStateMachine.transitionOrThrow(state, ThreadState.IDLE)
        assertEquals(ThreadState.IDLE, state)
    }

    @Test
    fun `test thread lifecycle path - with parking`() {
        var state = ThreadState.IDLE

        state = ThreadStateMachine.transitionOrThrow(state, ThreadState.RUNNING)
        assertEquals(ThreadState.RUNNING, state)

        state = ThreadStateMachine.transitionOrThrow(state, ThreadState.PARKED)
        assertEquals(ThreadState.PARKED, state)

        state = ThreadStateMachine.transitionOrThrow(state, ThreadState.RUNNING)
        assertEquals(ThreadState.RUNNING, state)

        state = ThreadStateMachine.transitionOrThrow(state, ThreadState.IDLE)
        assertEquals(ThreadState.IDLE, state)
    }

    @Test
    fun `test thread termination from running state`() {
        var state = ThreadState.IDLE

        state = ThreadStateMachine.transitionOrThrow(state, ThreadState.RUNNING)
        assertEquals(ThreadState.RUNNING, state)

        state = ThreadStateMachine.transitionOrThrow(state, ThreadState.TERMINATED)
        assertEquals(ThreadState.TERMINATED, state)

        // Cannot transition from TERMINATED
        assertFailsWith<IllegalStateException> {
            ThreadStateMachine.transitionOrThrow(state, ThreadState.IDLE)
        }
    }

    // ===== Edge Cases =====

    @Test
    fun `test transition result contains error message for invalid transition`() {
        val result = CoroutineStateMachine.transition(CoroutineState.COMPLETED, CoroutineState.ACTIVE)
        assertTrue(!result.isValid)
        assertTrue(result.errorMessage != null)
        assertTrue(result.errorMessage!!.contains("COMPLETED"))
        assertTrue(result.errorMessage!!.contains("ACTIVE"))
    }

    @Test
    fun `test same state transition is allowed for idempotency`() {
        val coroutineResult = CoroutineStateMachine.transition(CoroutineState.ACTIVE, CoroutineState.ACTIVE)
        assertTrue(coroutineResult.isValid)

        val threadResult = ThreadStateMachine.transition(ThreadState.RUNNING, ThreadState.RUNNING)
        assertTrue(threadResult.isValid)
    }

    @Test
    fun `test all terminal coroutine states reject transitions`() {
        val terminalStates = listOf(CoroutineState.COMPLETED, CoroutineState.CANCELLED)
        val targetStates = listOf(
            CoroutineState.CREATED,
            CoroutineState.ACTIVE,
            CoroutineState.SUSPENDED,
            CoroutineState.RESUMED
        )

        terminalStates.forEach { from ->
            targetStates.forEach { to ->
                assertFailsWith<IllegalStateException> {
                    CoroutineStateMachine.transitionOrThrow(from, to)
                }
            }
        }
    }

    @Test
    fun `test thread terminated state rejects all transitions except to itself`() {
        val targetStates = listOf(
            ThreadState.IDLE,
            ThreadState.RUNNING,
            ThreadState.WAITING,
            ThreadState.PARKED
        )

        targetStates.forEach { to ->
            assertFailsWith<IllegalStateException> {
                ThreadStateMachine.transitionOrThrow(ThreadState.TERMINATED, to)
            }
        }
    }
}

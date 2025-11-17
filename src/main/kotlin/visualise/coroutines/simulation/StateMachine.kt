package visualise.coroutines.simulation

import visualise.coroutines.simulation.models.CoroutineState
import visualise.coroutines.simulation.models.ThreadState

/**
 * Result of a state transition attempt
 */
data class TransitionResult<T>(
    val isValid: Boolean,
    val newState: T,
    val errorMessage: String? = null
)

/**
 * State machine for coroutine state transitions
 */
object CoroutineStateMachine {

    private val validTransitions: Map<CoroutineState, Set<CoroutineState>> = mapOf(
        CoroutineState.CREATED to setOf(
            CoroutineState.CREATED,
            CoroutineState.ACTIVE,
            CoroutineState.CANCELLED
        ),
        CoroutineState.ACTIVE to setOf(
            CoroutineState.ACTIVE,
            CoroutineState.SUSPENDED,
            CoroutineState.COMPLETED,
            CoroutineState.CANCELLED
        ),
        CoroutineState.SUSPENDED to setOf(
            CoroutineState.SUSPENDED,
            CoroutineState.RESUMED,
            CoroutineState.CANCELLED
        ),
        CoroutineState.RESUMED to setOf(
            CoroutineState.RESUMED,
            CoroutineState.ACTIVE,
            CoroutineState.COMPLETED,
            CoroutineState.CANCELLED
        ),
        CoroutineState.COMPLETED to setOf(
            CoroutineState.COMPLETED
        ),
        CoroutineState.CANCELLED to setOf(
            CoroutineState.CANCELLED
        )
    )

    /**
     * Attempt a state transition and return the result
     */
    fun transition(from: CoroutineState, to: CoroutineState): TransitionResult<CoroutineState> {
        val allowed = validTransitions[from] ?: emptySet()

        return if (to in allowed) {
            TransitionResult(isValid = true, newState = to)
        } else {
            TransitionResult(
                isValid = false,
                newState = from,
                errorMessage = "Invalid coroutine state transition from $from to $to"
            )
        }
    }

    /**
     * Attempt a state transition and throw if invalid
     */
    fun transitionOrThrow(from: CoroutineState, to: CoroutineState): CoroutineState {
        val result = transition(from, to)
        if (!result.isValid) {
            throw IllegalStateException(result.errorMessage)
        }
        return result.newState
    }

    /**
     * Check if a transition is valid without performing it
     */
    fun isValidTransition(from: CoroutineState, to: CoroutineState): Boolean {
        return to in (validTransitions[from] ?: emptySet())
    }

    /**
     * Get all valid next states for a given state
     */
    fun getValidNextStates(from: CoroutineState): Set<CoroutineState> {
        return validTransitions[from] ?: emptySet()
    }
}

/**
 * State machine for thread state transitions
 */
object ThreadStateMachine {

    private val validTransitions: Map<ThreadState, Set<ThreadState>> = mapOf(
        ThreadState.IDLE to setOf(
            ThreadState.IDLE,
            ThreadState.RUNNING,
            ThreadState.TERMINATED
        ),
        ThreadState.RUNNING to setOf(
            ThreadState.RUNNING,
            ThreadState.WAITING,
            ThreadState.PARKED,
            ThreadState.IDLE,
            ThreadState.TERMINATED
        ),
        ThreadState.WAITING to setOf(
            ThreadState.WAITING,
            ThreadState.RUNNING
        ),
        ThreadState.PARKED to setOf(
            ThreadState.PARKED,
            ThreadState.RUNNING
        ),
        ThreadState.TERMINATED to setOf(
            ThreadState.TERMINATED
        )
    )

    /**
     * Attempt a state transition and return the result
     */
    fun transition(from: ThreadState, to: ThreadState): TransitionResult<ThreadState> {
        val allowed = validTransitions[from] ?: emptySet()

        return if (to in allowed) {
            TransitionResult(isValid = true, newState = to)
        } else {
            TransitionResult(
                isValid = false,
                newState = from,
                errorMessage = "Invalid thread state transition from $from to $to"
            )
        }
    }

    /**
     * Attempt a state transition and throw if invalid
     */
    fun transitionOrThrow(from: ThreadState, to: ThreadState): ThreadState {
        val result = transition(from, to)
        if (!result.isValid) {
            throw IllegalStateException(result.errorMessage)
        }
        return result.newState
    }

    /**
     * Check if a transition is valid without performing it
     */
    fun isValidTransition(from: ThreadState, to: ThreadState): Boolean {
        return to in (validTransitions[from] ?: emptySet())
    }

    /**
     * Get all valid next states for a given state
     */
    fun getValidNextStates(from: ThreadState): Set<ThreadState> {
        return validTransitions[from] ?: emptySet()
    }
}

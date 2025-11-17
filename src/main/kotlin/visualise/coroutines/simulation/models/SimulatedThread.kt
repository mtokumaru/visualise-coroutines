package visualise.coroutines.simulation.models

/**
 * Represents a simulated thread with state
 */
data class SimulatedThread(
    val id: Int,
    val dispatcherType: DispatcherType,
    val state: ThreadState = ThreadState.IDLE
)

enum class ThreadState {
    IDLE,
    RUNNING,
    WAITING,
    PARKED,
    TERMINATED
}

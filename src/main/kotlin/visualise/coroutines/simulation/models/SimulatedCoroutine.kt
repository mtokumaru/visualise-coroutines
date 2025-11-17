package visualise.coroutines.simulation.models

/**
 * Represents a simulated coroutine
 */
data class SimulatedCoroutine(
    val id: Int,
    val parentId: Int? = null,
    val state: CoroutineState = CoroutineState.CREATED,
    val dispatcher: DispatcherType,
    val threadId: Int? = null
)

enum class CoroutineState {
    CREATED,
    ACTIVE,
    SUSPENDED,
    RESUMED,
    COMPLETED,
    CANCELLED
}

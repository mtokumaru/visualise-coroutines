package visualise.coroutines.simulation.models

/**
 * Represents a simulated coroutine dispatcher
 */
data class SimulatedDispatcher(
    val name: String,
    val type: DispatcherType,
    val maxThreads: Int,
    val threads: List<SimulatedThread> = emptyList(),
    val queue: List<Int> = emptyList() // coroutine IDs in queue
)

enum class DispatcherType {
    DEFAULT,
    IO,
    MAIN,
    CUSTOM
}

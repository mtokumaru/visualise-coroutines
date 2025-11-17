package visualise.coroutines.simulation.models

/**
 * Represents a simulated CPU core
 */
data class SimulatedCpu(
    val id: Int,
    val active: Boolean = false
)

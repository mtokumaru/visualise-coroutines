package visualise.coroutines.simulation.events

import kotlinx.serialization.Serializable

/**
 * Base class for all simulation events
 */
@Serializable
sealed class SimulationEvent {
    abstract val timestamp: Long
}

@Serializable
data class CoroutineCreated(
    override val timestamp: Long,
    val coroutineId: Int,
    val parentId: Int? = null,
    val dispatcher: String
) : SimulationEvent()

@Serializable
data class CoroutineStarted(
    override val timestamp: Long,
    val coroutineId: Int
) : SimulationEvent()

@Serializable
data class CoroutineSuspended(
    override val timestamp: Long,
    val coroutineId: Int
) : SimulationEvent()

@Serializable
data class CoroutineResumed(
    override val timestamp: Long,
    val coroutineId: Int
) : SimulationEvent()

@Serializable
data class CoroutineCompleted(
    override val timestamp: Long,
    val coroutineId: Int
) : SimulationEvent()

@Serializable
data class CoroutineCancelled(
    override val timestamp: Long,
    val coroutineId: Int
) : SimulationEvent()

@Serializable
data class ThreadAssigned(
    override val timestamp: Long,
    val coroutineId: Int,
    val threadId: Int
) : SimulationEvent()

@Serializable
data class ThreadReleased(
    override val timestamp: Long,
    val coroutineId: Int,
    val threadId: Int
) : SimulationEvent()

@Serializable
data class DispatcherQueued(
    override val timestamp: Long,
    val coroutineId: Int,
    val dispatcher: String
) : SimulationEvent()

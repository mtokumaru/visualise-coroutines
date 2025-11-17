package visualise.coroutines.simulation.scenarios

import visualise.coroutines.simulation.events.SimulationEvent

/**
 * Base interface for all simulation scenarios
 */
interface Scenario {
    val name: String
    val description: String
    fun generateEvents(): List<SimulationEvent>
}

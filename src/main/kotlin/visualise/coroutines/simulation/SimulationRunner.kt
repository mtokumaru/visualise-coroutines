package visualise.coroutines.simulation

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import visualise.coroutines.simulation.events.SimulationEvent
import visualise.coroutines.simulation.scenarios.Scenario

/**
 * Runs simulation scenarios in real-time using coroutines
 * Broadcasts events via Flow for reactive streaming
 */
class SimulationRunner {
    private val _events = MutableSharedFlow<SimulationEvent>(replay = 0)
    val events: SharedFlow<SimulationEvent> = _events.asSharedFlow()

    private var playbackJob: Job? = null
    private var eventList: List<SimulationEvent> = emptyList()
    private var eventIndex: Int = 0

    private var _currentTime: Long = 0L
    private var _speed: Double = 1.0
    private var _isRunning: Boolean = false

    var loadedScenario: Scenario? = null
        private set

    val isRunning: Boolean
        get() = _isRunning

    val currentTime: Long
        get() = _currentTime

    val speed: Double
        get() = _speed

    /**
     * Set playback speed multiplier
     * @throws IllegalArgumentException if speed is negative
     */
    fun setSpeed(newSpeed: Double) {
        require(newSpeed >= 0.0) { "Speed must be non-negative" }
        _speed = newSpeed
    }

    /**
     * Load a scenario without starting playback
     */
    fun load(scenario: Scenario) {
        eventList = scenario.generateEvents().sortedBy { it.timestamp }
        loadedScenario = scenario
        eventIndex = 0
        _currentTime = 0L
    }

    /**
     * Play the loaded scenario or load and play a new one
     * @throws IllegalStateException if already running
     */
    suspend fun play(scenario: Scenario) {
        if (_isRunning) {
            throw IllegalStateException("Simulation is already running. Call pause() or reset() first.")
        }

        if (loadedScenario != scenario || eventList.isEmpty()) {
            load(scenario)
        }

        _isRunning = true

        playbackJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                while (eventIndex < eventList.size && isActive) {
                    val event = eventList[eventIndex]

                    // Calculate delay to next event
                    val delay = if (eventIndex == 0) {
                        event.timestamp
                    } else {
                        event.timestamp - _currentTime
                    }

                    // Wait with speed adjustment (skip if speed is 0 or delay is 0)
                    if (delay > 0 && _speed > 0) {
                        delay((delay / _speed).toLong())
                    }

                    _currentTime = event.timestamp
                    _events.emit(event)
                    eventIndex++
                }

                _isRunning = false
            } catch (e: CancellationException) {
                _isRunning = false
                throw e
            }
        }

        playbackJob?.join()
    }

    /**
     * Pause the simulation
     */
    fun pause() {
        playbackJob?.cancel()
        playbackJob = null
        _isRunning = false
    }

    /**
     * Reset simulation to initial state
     */
    fun reset() {
        pause()
        eventIndex = 0
        _currentTime = 0L
    }

    /**
     * Step to the next event (for debugging)
     */
    suspend fun step() {
        if (eventList.isEmpty() || eventIndex >= eventList.size) return

        val event = eventList[eventIndex]
        _currentTime = event.timestamp
        _events.emit(event)
        eventIndex++
    }
}

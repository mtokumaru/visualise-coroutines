package visualise.coroutines.simulation.events

/**
 * Records simulation events and provides replay capability
 */
class EventRecorder {
    private val events = mutableListOf<SimulationEvent>()

    /**
     * Record a single event
     */
    fun record(event: SimulationEvent) {
        events.add(event)
    }

    /**
     * Get all recorded events in the order they were recorded
     */
    fun getEvents(): List<SimulationEvent> = events.toList()

    /**
     * Get all recorded events sorted by timestamp
     */
    fun getEventsSorted(): List<SimulationEvent> = events.sortedBy { it.timestamp }

    /**
     * Clear all recorded events
     */
    fun clear() {
        events.clear()
    }

    /**
     * Get the total number of recorded events
     */
    fun getEventCount(): Int = events.size

    /**
     * Get events within a specific time range (inclusive)
     */
    fun getEventsInRange(fromTimestamp: Long, toTimestamp: Long): List<SimulationEvent> {
        return getEventsSorted().filter { it.timestamp in fromTimestamp..toTimestamp }
    }

    /**
     * Get all events for a specific coroutine ID
     */
    fun getEventsForCoroutine(coroutineId: Int): List<SimulationEvent> {
        return events.filter { event ->
            when (event) {
                is CoroutineCreated -> event.coroutineId == coroutineId
                is CoroutineStarted -> event.coroutineId == coroutineId
                is CoroutineSuspended -> event.coroutineId == coroutineId
                is CoroutineResumed -> event.coroutineId == coroutineId
                is CoroutineCompleted -> event.coroutineId == coroutineId
                is CoroutineCancelled -> event.coroutineId == coroutineId
                is ThreadAssigned -> event.coroutineId == coroutineId
                is ThreadReleased -> event.coroutineId == coroutineId
                is DispatcherQueued -> event.coroutineId == coroutineId
            }
        }
    }

    /**
     * Replay all recorded events in chronological order
     * @param fromTimestamp Only replay events from this timestamp onwards (inclusive)
     * @param toTimestamp Only replay events up to this timestamp (inclusive)
     * @param handler Function called for each event during replay
     */
    fun replay(
        fromTimestamp: Long = 0L,
        toTimestamp: Long = Long.MAX_VALUE,
        handler: (SimulationEvent) -> Unit
    ) {
        getEventsSorted()
            .filter { it.timestamp >= fromTimestamp && it.timestamp <= toTimestamp }
            .forEach(handler)
    }

    /**
     * Get the timestamp of the last recorded event
     * Returns 0 if no events have been recorded
     */
    fun getLastTimestamp(): Long {
        return events.maxOfOrNull { it.timestamp } ?: 0L
    }
}

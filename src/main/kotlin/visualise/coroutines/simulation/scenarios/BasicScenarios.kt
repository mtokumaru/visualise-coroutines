package visualise.coroutines.simulation.scenarios

import visualise.coroutines.simulation.events.*

/**
 * Scenario 1: Single coroutine launch
 * Demonstrates basic coroutine lifecycle on Default dispatcher
 */
class SingleLaunchScenario : Scenario {
    override val name = "Single Launch"
    override val description = "One coroutine on Default dispatcher showing complete lifecycle"

    override fun generateEvents(): List<SimulationEvent> {
        val coroutineId = 1
        val threadId = 0

        return listOf(
            // Coroutine is created
            CoroutineCreated(
                timestamp = 0L,
                coroutineId = coroutineId,
                parentId = null,
                dispatcher = "Default"
            ),

            // Queued on Default dispatcher
            DispatcherQueued(
                timestamp = 10L,
                coroutineId = coroutineId,
                dispatcher = "Default"
            ),

            // Thread assigned from pool
            ThreadAssigned(
                timestamp = 20L,
                coroutineId = coroutineId,
                threadId = threadId
            ),

            // Coroutine starts executing
            CoroutineStarted(
                timestamp = 30L,
                coroutineId = coroutineId
            ),

            // Coroutine completes
            CoroutineCompleted(
                timestamp = 130L,
                coroutineId = coroutineId
            ),

            // Thread released back to pool
            ThreadReleased(
                timestamp = 140L,
                coroutineId = coroutineId,
                threadId = threadId
            )
        )
    }
}

/**
 * Scenario 2: Multiple concurrent coroutines
 * Demonstrates thread pool behavior and queuing
 */
class MultipleLaunchesScenario(private val count: Int = 10) : Scenario {
    override val name = "Multiple Launches"
    override val description = "$count coroutines launched concurrently on Default dispatcher"

    override fun generateEvents(): List<SimulationEvent> {
        val events = mutableListOf<SimulationEvent>()
        val threadPoolSize = 4
        var time = 0L

        // Create all coroutines at roughly the same time
        for (i in 1..count) {
            events.add(
                CoroutineCreated(
                    timestamp = time,
                    coroutineId = i,
                    parentId = null,
                    dispatcher = "Default"
                )
            )
            events.add(
                DispatcherQueued(
                    timestamp = time + 5,
                    coroutineId = i,
                    dispatcher = "Default"
                )
            )
            time += 2
        }

        // Simulate thread pool execution
        // First batch: up to threadPoolSize can run immediately
        time = 50L
        val activeCoroutines = mutableSetOf<Int>()
        val queue = (1..count).toMutableList()

        while (queue.isNotEmpty() || activeCoroutines.isNotEmpty()) {
            // Assign threads to waiting coroutines
            while (activeCoroutines.size < threadPoolSize && queue.isNotEmpty()) {
                val coroutineId = queue.removeFirst()
                val threadId = activeCoroutines.size

                events.add(
                    ThreadAssigned(
                        timestamp = time,
                        coroutineId = coroutineId,
                        threadId = threadId
                    )
                )
                events.add(
                    CoroutineStarted(
                        timestamp = time + 5,
                        coroutineId = coroutineId
                    )
                )
                activeCoroutines.add(coroutineId)
                time += 10
            }

            // Complete one coroutine
            if (activeCoroutines.isNotEmpty()) {
                val completingCoroutine = activeCoroutines.first()
                activeCoroutines.remove(completingCoroutine)

                events.add(
                    CoroutineCompleted(
                        timestamp = time + 50,
                        coroutineId = completingCoroutine
                    )
                )
                events.add(
                    ThreadReleased(
                        timestamp = time + 55,
                        coroutineId = completingCoroutine,
                        threadId = (completingCoroutine - 1) % threadPoolSize
                    )
                )
                time += 60
            }
        }

        return events.sortedBy { it.timestamp }
    }
}

/**
 * Scenario 3: Launch vs Async comparison
 * Shows the difference between launch (Job) and async (Deferred)
 */
class LaunchVsAsyncScenario : Scenario {
    override val name = "Launch vs Async"
    override val description = "Comparison of launch and async coroutine builders"

    override fun generateEvents(): List<SimulationEvent> {
        val launchId = 1
        val asyncId = 2

        return listOf(
            // Launch coroutine created
            CoroutineCreated(
                timestamp = 0L,
                coroutineId = launchId,
                parentId = null,
                dispatcher = "Default"
            ),

            // Async coroutine created
            CoroutineCreated(
                timestamp = 0L,
                coroutineId = asyncId,
                parentId = null,
                dispatcher = "Default"
            ),

            // Both queued
            DispatcherQueued(
                timestamp = 10L,
                coroutineId = launchId,
                dispatcher = "Default"
            ),
            DispatcherQueued(
                timestamp = 10L,
                coroutineId = asyncId,
                dispatcher = "Default"
            ),

            // Launch starts first
            ThreadAssigned(
                timestamp = 20L,
                coroutineId = launchId,
                threadId = 0
            ),
            CoroutineStarted(
                timestamp = 25L,
                coroutineId = launchId
            ),

            // Async starts
            ThreadAssigned(
                timestamp = 35L,
                coroutineId = asyncId,
                threadId = 1
            ),
            CoroutineStarted(
                timestamp = 40L,
                coroutineId = asyncId
            ),

            // Launch completes
            CoroutineCompleted(
                timestamp = 120L,
                coroutineId = launchId
            ),
            ThreadReleased(
                timestamp = 125L,
                coroutineId = launchId,
                threadId = 0
            ),

            // Async completes (producing a result)
            CoroutineCompleted(
                timestamp = 145L,
                coroutineId = asyncId
            ),
            ThreadReleased(
                timestamp = 150L,
                coroutineId = asyncId,
                threadId = 1
            )
        )
    }
}

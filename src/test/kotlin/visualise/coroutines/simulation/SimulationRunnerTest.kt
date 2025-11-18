package visualise.coroutines.simulation

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.launch
import visualise.coroutines.simulation.scenarios.SingleLaunchScenario
import visualise.coroutines.simulation.events.CoroutineCreated
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SimulationRunnerTest {

    @Test
    fun `test runner starts in stopped state`() {
        val runner = SimulationRunner()
        assertFalse(runner.isRunning)
    }

    @Test
    fun `test runner starts with speed 1x`() {
        val runner = SimulationRunner()
        assertEquals(1.0, runner.speed)
    }

    @Test
    fun `test set speed`() {
        val runner = SimulationRunner()
        runner.setSpeed(2.0)
        assertEquals(2.0, runner.speed)
    }

    @Test
    fun `test negative speed throws exception`() {
        val runner = SimulationRunner()

        try {
            runner.setSpeed(-1.0)
            assertTrue(false, "Should throw exception")
        } catch (e: IllegalArgumentException) {
            assertTrue(true)
        }
    }

    @Test
    fun `test load scenario without playing`() {
        val runner = SimulationRunner()
        val scenario = SingleLaunchScenario()

        runner.load(scenario)

        assertFalse(runner.isRunning)
        assertEquals(0L, runner.currentTime)
    }

    @Test
    fun `test step emits one event`() = runTest {
        val runner = SimulationRunner()
        val scenario = SingleLaunchScenario()

        runner.load(scenario)

        // Trigger step in background
        backgroundScope.launch {
            runner.step()
        }

        val firstEvent = runner.events.first()

        assertTrue(firstEvent is CoroutineCreated)
    }

    @Test
    fun `test reset clears time`() {
        val runner = SimulationRunner()
        val scenario = SingleLaunchScenario()

        runner.load(scenario)
        runner.reset()

        assertEquals(0L, runner.currentTime)
        assertFalse(runner.isRunning)
    }

    @Test
    fun `test pause stops running state`() {
        val runner = SimulationRunner()

        runner.pause()

        assertFalse(runner.isRunning)
    }

    @Test
    fun `test speed zero is allowed`() {
        val runner = SimulationRunner()
        runner.setSpeed(0.0)
        assertEquals(0.0, runner.speed)
    }

    @Test
    fun `test very high speed is allowed`() {
        val runner = SimulationRunner()
        runner.setSpeed(100.0)
        assertEquals(100.0, runner.speed)
    }

    @Test
    fun `test load sets up scenario events`() {
        val runner = SimulationRunner()
        val scenario = SingleLaunchScenario()

        runner.load(scenario)

        // After load, we should be ready to play
        assertEquals(0L, runner.currentTime)
    }
}

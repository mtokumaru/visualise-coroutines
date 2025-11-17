package visualise.coroutines.simulation.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SimulatedThreadTest {

    @Test
    fun `test thread creation with default state`() {
        val thread = SimulatedThread(id = 1, dispatcherType = DispatcherType.DEFAULT)

        assertEquals(1, thread.id)
        assertEquals(DispatcherType.DEFAULT, thread.dispatcherType)
        assertEquals(ThreadState.IDLE, thread.state)
    }

    @Test
    fun `test thread creation with custom state`() {
        val thread = SimulatedThread(
            id = 2,
            dispatcherType = DispatcherType.IO,
            state = ThreadState.RUNNING
        )

        assertEquals(2, thread.id)
        assertEquals(DispatcherType.IO, thread.dispatcherType)
        assertEquals(ThreadState.RUNNING, thread.state)
    }

    @Test
    fun `test thread with all dispatcher types`() {
        val defaultThread = SimulatedThread(0, DispatcherType.DEFAULT)
        val ioThread = SimulatedThread(1, DispatcherType.IO)
        val mainThread = SimulatedThread(2, DispatcherType.MAIN)
        val customThread = SimulatedThread(3, DispatcherType.CUSTOM)

        assertEquals(DispatcherType.DEFAULT, defaultThread.dispatcherType)
        assertEquals(DispatcherType.IO, ioThread.dispatcherType)
        assertEquals(DispatcherType.MAIN, mainThread.dispatcherType)
        assertEquals(DispatcherType.CUSTOM, customThread.dispatcherType)
    }

    @Test
    fun `test thread with all states`() {
        val idleThread = SimulatedThread(0, DispatcherType.DEFAULT, ThreadState.IDLE)
        val runningThread = SimulatedThread(1, DispatcherType.DEFAULT, ThreadState.RUNNING)
        val waitingThread = SimulatedThread(2, DispatcherType.DEFAULT, ThreadState.WAITING)
        val parkedThread = SimulatedThread(3, DispatcherType.DEFAULT, ThreadState.PARKED)
        val terminatedThread = SimulatedThread(4, DispatcherType.DEFAULT, ThreadState.TERMINATED)

        assertEquals(ThreadState.IDLE, idleThread.state)
        assertEquals(ThreadState.RUNNING, runningThread.state)
        assertEquals(ThreadState.WAITING, waitingThread.state)
        assertEquals(ThreadState.PARKED, parkedThread.state)
        assertEquals(ThreadState.TERMINATED, terminatedThread.state)
    }

    @Test
    fun `test thread state transitions using copy`() {
        var thread = SimulatedThread(0, DispatcherType.DEFAULT, ThreadState.IDLE)
        assertEquals(ThreadState.IDLE, thread.state)

        thread = thread.copy(state = ThreadState.RUNNING)
        assertEquals(ThreadState.RUNNING, thread.state)

        thread = thread.copy(state = ThreadState.WAITING)
        assertEquals(ThreadState.WAITING, thread.state)

        thread = thread.copy(state = ThreadState.IDLE)
        assertEquals(ThreadState.IDLE, thread.state)
    }

    @Test
    fun `test thread immutability`() {
        val thread1 = SimulatedThread(1, DispatcherType.DEFAULT, ThreadState.IDLE)
        val thread2 = thread1.copy(state = ThreadState.RUNNING)

        assertEquals(ThreadState.IDLE, thread1.state)
        assertEquals(ThreadState.RUNNING, thread2.state)
        assertEquals(thread1.id, thread2.id)
        assertEquals(thread1.dispatcherType, thread2.dispatcherType)
    }

    @Test
    fun `test thread equality`() {
        val thread1 = SimulatedThread(1, DispatcherType.DEFAULT, ThreadState.IDLE)
        val thread2 = SimulatedThread(1, DispatcherType.DEFAULT, ThreadState.IDLE)
        val thread3 = SimulatedThread(1, DispatcherType.DEFAULT, ThreadState.RUNNING)
        val thread4 = SimulatedThread(2, DispatcherType.DEFAULT, ThreadState.IDLE)
        val thread5 = SimulatedThread(1, DispatcherType.IO, ThreadState.IDLE)

        assertEquals(thread1, thread2)
        assertNotEquals(thread1, thread3)
        assertNotEquals(thread1, thread4)
        assertNotEquals(thread1, thread5)
    }

    @Test
    fun `test thread pool creation`() {
        val threadPool = (0..3).map {
            SimulatedThread(it, DispatcherType.DEFAULT)
        }

        assertEquals(4, threadPool.size)
        threadPool.forEach {
            assertEquals(DispatcherType.DEFAULT, it.dispatcherType)
            assertEquals(ThreadState.IDLE, it.state)
        }
    }

    @Test
    fun `test IO dispatcher thread pool`() {
        val ioThreads = (0 until 64).map {
            SimulatedThread(it, DispatcherType.IO)
        }

        assertEquals(64, ioThreads.size)
        ioThreads.forEach {
            assertEquals(DispatcherType.IO, it.dispatcherType)
        }
    }
}

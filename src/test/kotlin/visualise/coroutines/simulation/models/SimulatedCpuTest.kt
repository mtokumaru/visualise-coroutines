package visualise.coroutines.simulation.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SimulatedCpuTest {

    @Test
    fun `test cpu creation with default values`() {
        val cpu = SimulatedCpu(id = 0)

        assertEquals(0, cpu.id)
        assertFalse(cpu.active)
    }

    @Test
    fun `test cpu creation with custom values`() {
        val cpu = SimulatedCpu(id = 2, active = true)

        assertEquals(2, cpu.id)
        assertTrue(cpu.active)
    }

    @Test
    fun `test cpu immutability with copy`() {
        val cpu1 = SimulatedCpu(id = 0, active = false)
        val cpu2 = cpu1.copy(active = true)

        assertFalse(cpu1.active)
        assertTrue(cpu2.active)
        assertEquals(cpu1.id, cpu2.id)
    }

    @Test
    fun `test cpu equality`() {
        val cpu1 = SimulatedCpu(id = 1, active = false)
        val cpu2 = SimulatedCpu(id = 1, active = false)
        val cpu3 = SimulatedCpu(id = 1, active = true)
        val cpu4 = SimulatedCpu(id = 2, active = false)

        assertEquals(cpu1, cpu2)
        assertNotEquals(cpu1, cpu3)
        assertNotEquals(cpu1, cpu4)
    }

    @Test
    fun `test cpu toString contains id and active state`() {
        val cpu = SimulatedCpu(id = 3, active = true)
        val str = cpu.toString()

        assertTrue(str.contains("3"))
        assertTrue(str.contains("true"))
    }

    @Test
    fun `test multiple cpus with different ids`() {
        val cpus = (0..3).map { SimulatedCpu(id = it) }

        assertEquals(4, cpus.size)
        assertEquals(0, cpus[0].id)
        assertEquals(3, cpus[3].id)
        cpus.forEach { assertFalse(it.active) }
    }

    @Test
    fun `test cpu activation toggle`() {
        var cpu = SimulatedCpu(id = 0, active = false)
        assertFalse(cpu.active)

        cpu = cpu.copy(active = true)
        assertTrue(cpu.active)

        cpu = cpu.copy(active = false)
        assertFalse(cpu.active)
    }
}

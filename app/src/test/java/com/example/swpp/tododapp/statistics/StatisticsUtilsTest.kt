package com.example.swpp.tododapp.statistics

import com.example.swpp.tododapp.data.Task
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

// Tutorial appendix: retain the three exercised skeleton scenarios in one parameterized test.
@RunWith(Parameterized::class)
class StatisticsUtilsTest(
    private val name: String,
    private val input: List<Task>,
    private val expectedActive: Float,
    private val expectedCompleted: Float
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): List<Array<Any>> = listOf(
            arrayOf(
                "half half",
                listOf(Task("t1", "desc", true, "Test_id1"), Task("t2", "desc", false, "Test_id2")),
                50f, 50f
            ),
            arrayOf("empty", emptyList<Task>(), 0f, 0f),
            arrayOf(
                "all completed",
                listOf(Task("t1", "desc", true, "Test_id1"), Task("t2", "desc", true, "Test_id2")),
                0f, 100f
            )
        )
    }

    @Test
    fun getActiveAndCompletedStats_returnsExpectedValues() {
        // GIVEN - The input and literal expected percentages from the parameter table.
        val tasks = input
        // WHEN - Compute statistics as in the original skeleton exercise.
        val result = getActiveAndCompletedStats(tasks)
        // THEN - Both percentages match the independently specified expected values.
        assertEquals(expectedActive, result.activeTasksPercent)
        assertEquals(expectedCompleted, result.completedTasksPercent)
    }
}

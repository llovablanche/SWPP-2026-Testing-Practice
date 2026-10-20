package com.example.swpp.tododapp.tasks

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DueDateUtilsTest {

    // U01: Completion takes precedence over absence, past, present, and future due dates.
    @Test
    fun dueDateLabel_completedAnyDueDate_returnsClear() {
        // GIVEN - Exact dated cases and independently specified literal labels.
        val cases = listOf(
            Case("2026-10-20", null, true, "CLEAR"),
            Case("2026-10-20", "2026-10-19", true, "CLEAR"),
            Case("2026-10-20", "2026-10-20", true, "CLEAR"),
            Case("2026-10-20", "2026-10-21", true, "CLEAR"),
            Case("2026-10-20", "2026-11-21", true, "CLEAR")
        )
        for (case in cases) {
            // WHEN - Calculate the label using the supplied date, never the system clock.
            val result = dueDateLabel(case.dueDate?.let(LocalDate::parse), LocalDate.parse(case.today), case.completed)
            // THEN - Compare with the literal oracle from the specification.
            assertEquals(case.toString(), case.expected, result)
        }
    }

    // U02: An active task without a due date returns exactly the empty string.
    @Test
    fun dueDateLabel_activeWithoutDueDate_returnsEmpty() {
        // GIVEN - Exact dated cases and independently specified literal labels.
        val cases = listOf(
            Case("2026-10-20", null, false, "")
        )
        for (case in cases) {
            // WHEN - Calculate the label using the supplied date, never the system clock.
            val result = dueDateLabel(case.dueDate?.let(LocalDate::parse), LocalDate.parse(case.today), case.completed)
            // THEN - Compare with the literal oracle from the specification.
            assertEquals(case.toString(), case.expected, result)
        }
    }

    // U03: An active task due today returns exactly D-Day.
    @Test
    fun dueDateLabel_activeDueToday_returnsDDay() {
        // GIVEN - Exact dated cases and independently specified literal labels.
        val cases = listOf(
            Case("2026-10-20", "2026-10-20", false, "D-Day")
        )
        for (case in cases) {
            // WHEN - Calculate the label using the supplied date, never the system clock.
            val result = dueDateLabel(case.dueDate?.let(LocalDate::parse), LocalDate.parse(case.today), case.completed)
            // THEN - Compare with the literal oracle from the specification.
            assertEquals(case.toString(), case.expected, result)
        }
    }

    // U04: An active task due in N calendar days returns D-N with a positive, unpadded decimal N.
    @Test
    fun dueDateLabel_activeFuture_returnsDMinus() {
        // GIVEN - Exact dated cases and independently specified literal labels.
        val cases = listOf(
            Case("2026-10-20", "2026-10-23", false, "D-3")
        )
        for (case in cases) {
            // WHEN - Calculate the label using the supplied date, never the system clock.
            val result = dueDateLabel(case.dueDate?.let(LocalDate::parse), LocalDate.parse(case.today), case.completed)
            // THEN - Compare with the literal oracle from the specification.
            assertEquals(case.toString(), case.expected, result)
        }
    }

    // U05: An active task overdue by N calendar days returns D+N with a positive, unpadded decimal N.
    @Test
    fun dueDateLabel_activeOverdue_returnsDPlus() {
        // GIVEN - Exact dated cases and independently specified literal labels.
        val cases = listOf(
            Case("2026-10-20", "2026-10-17", false, "D+3")
        )
        for (case in cases) {
            // WHEN - Calculate the label using the supplied date, never the system clock.
            val result = dueDateLabel(case.dueDate?.let(LocalDate::parse), LocalDate.parse(case.today), case.completed)
            // THEN - Compare with the literal oracle from the specification.
            assertEquals(case.toString(), case.expected, result)
        }
    }

    // U06: Crossing a month boundary preserves the total calendar-day count.
    @Test
    fun dueDateLabel_shortMonthCrossing_countsCalendarDays() {
        // GIVEN - Exact dated cases and independently specified literal labels.
        val cases = listOf(
            Case("2026-01-30", "2026-02-02", false, "D-3"),
            Case("2026-02-02", "2026-01-30", false, "D+3")
        )
        for (case in cases) {
            // WHEN - Calculate the label using the supplied date, never the system clock.
            val result = dueDateLabel(case.dueDate?.let(LocalDate::parse), LocalDate.parse(case.today), case.completed)
            // THEN - Compare with the literal oracle from the specification.
            assertEquals(case.toString(), case.expected, result)
        }
    }

    // U07: Crossing a year boundary preserves the total calendar-day count.
    @Test
    fun dueDateLabel_shortYearCrossing_countsCalendarDays() {
        // GIVEN - Exact dated cases and independently specified literal labels.
        val cases = listOf(
            Case("2025-12-30", "2026-01-02", false, "D-3"),
            Case("2026-01-02", "2025-12-30", false, "D+3")
        )
        for (case in cases) {
            // WHEN - Calculate the label using the supplied date, never the system clock.
            val result = dueDateLabel(case.dueDate?.let(LocalDate::parse), LocalDate.parse(case.today), case.completed)
            // THEN - Compare with the literal oracle from the specification.
            assertEquals(case.toString(), case.expected, result)
        }
    }

    // U08: Leap day counts as a real calendar day in both directions.
    @Test
    fun dueDateLabel_leapDayCrossing_countsTwoDays() {
        // GIVEN - Exact dated cases and independently specified literal labels.
        val cases = listOf(
            Case("2024-02-28", "2024-03-01", false, "D-2"),
            Case("2024-03-01", "2024-02-28", false, "D+2")
        )
        for (case in cases) {
            // WHEN - Calculate the label using the supplied date, never the system clock.
            val result = dueDateLabel(case.dueDate?.let(LocalDate::parse), LocalDate.parse(case.today), case.completed)
            // THEN - Compare with the literal oracle from the specification.
            assertEquals(case.toString(), case.expected, result)
        }
    }

    // U09: None. Null due dates and both completion states are valid; representative inputs return their specified labels without rejection.
    @Test
    fun dueDateLabel_validInputMatrix_returnsSpecifiedLabels() {
        // GIVEN - Exact dated cases and independently specified literal labels.
        val cases = listOf(
            Case("2026-10-20", null, false, ""),
            Case("2026-10-20", null, true, "CLEAR"),
            Case("2026-10-20", "2026-10-19", false, "D+1"),
            Case("2026-10-20", "2026-10-20", false, "D-Day"),
            Case("2026-10-20", "2026-10-21", false, "D-1"),
            Case("2026-10-20", "2026-10-21", true, "CLEAR")
        )
        for (case in cases) {
            // WHEN - Calculate the label using the supplied date, never the system clock.
            val result = dueDateLabel(case.dueDate?.let(LocalDate::parse), LocalDate.parse(case.today), case.completed)
            // THEN - Compare with the literal oracle from the specification.
            assertEquals(case.toString(), case.expected, result)
        }
    }

    // U10: At the past/present/future boundary, yesterday, today, and tomorrow yield D+1, D-Day, and D-1.
    @Test
    fun dueDateLabel_yesterdayTodayTomorrow_showsBothBoundarySides() {
        // GIVEN - Exact dated cases and independently specified literal labels.
        val cases = listOf(
            Case("2026-10-20", "2026-10-19", false, "D+1"),
            Case("2026-10-20", "2026-10-20", false, "D-Day"),
            Case("2026-10-20", "2026-10-21", false, "D-1")
        )
        for (case in cases) {
            // WHEN - Calculate the label using the supplied date, never the system clock.
            val result = dueDateLabel(case.dueDate?.let(LocalDate::parse), LocalDate.parse(case.today), case.completed)
            // THEN - Compare with the literal oracle from the specification.
            assertEquals(case.toString(), case.expected, result)
        }
    }

    // U11: A complete month and the day on either side use total days, including in reverse; a whole month must not become zero days.
    @Test
    fun dueDateLabel_wholeMonthAndAdjacentDays_countsTotalDays() {
        // GIVEN - Exact dated cases and independently specified literal labels.
        val cases = listOf(
            Case("2026-01-15", "2026-02-14", false, "D-30"),
            Case("2026-01-15", "2026-02-15", false, "D-31"),
            Case("2026-01-15", "2026-02-16", false, "D-32"),
            Case("2026-02-15", "2026-01-14", false, "D+32"),
            Case("2026-02-15", "2026-01-15", false, "D+31"),
            Case("2026-02-15", "2026-01-16", false, "D+30")
        )
        for (case in cases) {
            // WHEN - Calculate the label using the supplied date, never the system clock.
            val result = dueDateLabel(case.dueDate?.let(LocalDate::parse), LocalDate.parse(case.today), case.completed)
            // THEN - Compare with the literal oracle from the specification.
            assertEquals(case.toString(), case.expected, result)
        }
    }

    // U12: A complete year and the day on either side use total days in both directions, without discarding the year component.
    @Test
    fun dueDateLabel_wholeYearAndAdjacentDays_countsTotalDays() {
        // GIVEN - Exact dated cases and independently specified literal labels.
        val cases = listOf(
            Case("2025-12-30", "2026-12-29", false, "D-364"),
            Case("2025-12-30", "2026-12-30", false, "D-365"),
            Case("2025-12-30", "2026-12-31", false, "D-366"),
            Case("2026-12-30", "2025-12-29", false, "D+366"),
            Case("2026-12-30", "2025-12-30", false, "D+365"),
            Case("2026-12-30", "2025-12-31", false, "D+364")
        )
        for (case in cases) {
            // WHEN - Calculate the label using the supplied date, never the system clock.
            val result = dueDateLabel(case.dueDate?.let(LocalDate::parse), LocalDate.parse(case.today), case.completed)
            // THEN - Compare with the literal oracle from the specification.
            assertEquals(case.toString(), case.expected, result)
        }
    }

    // U13: The days immediately before, at, and after a month end remain consecutive.
    @Test
    fun dueDateLabel_monthEndYesterdayTodayTomorrow_countsConsecutiveDays() {
        // GIVEN - Exact dated cases and independently specified literal labels.
        val cases = listOf(
            Case("2026-01-31", "2026-01-30", false, "D+1"),
            Case("2026-01-31", "2026-01-31", false, "D-Day"),
            Case("2026-01-31", "2026-02-01", false, "D-1")
        )
        for (case in cases) {
            // WHEN - Calculate the label using the supplied date, never the system clock.
            val result = dueDateLabel(case.dueDate?.let(LocalDate::parse), LocalDate.parse(case.today), case.completed)
            // THEN - Compare with the literal oracle from the specification.
            assertEquals(case.toString(), case.expected, result)
        }
    }

    // U14: The days immediately before, at, and after leap day remain consecutive.
    @Test
    fun dueDateLabel_leapDayYesterdayTodayTomorrow_countsConsecutiveDays() {
        // GIVEN - Exact dated cases and independently specified literal labels.
        val cases = listOf(
            Case("2024-02-29", "2024-02-28", false, "D+1"),
            Case("2024-02-29", "2024-02-29", false, "D-Day"),
            Case("2024-02-29", "2024-03-01", false, "D-1")
        )
        for (case in cases) {
            // WHEN - Calculate the label using the supplied date, never the system clock.
            val result = dueDateLabel(case.dueDate?.let(LocalDate::parse), LocalDate.parse(case.today), case.completed)
            // THEN - Compare with the literal oracle from the specification.
            assertEquals(case.toString(), case.expected, result)
        }
    }

    // U15: The end of February in common and leap years, and the end of a 30-day month, preserve total days immediately before, at, and after the next month begins.
    @Test
    fun dueDateLabel_differentMonthLengths_countsTotalDays() {
        // GIVEN - Exact dated cases and independently specified literal labels.
        val cases = listOf(
            Case("2026-02-01", "2026-02-28", false, "D-27"),
            Case("2026-02-01", "2026-03-01", false, "D-28"),
            Case("2026-02-01", "2026-03-02", false, "D-29"),
            Case("2024-02-01", "2024-02-29", false, "D-28"),
            Case("2024-02-01", "2024-03-01", false, "D-29"),
            Case("2024-02-01", "2024-03-02", false, "D-30"),
            Case("2026-04-01", "2026-04-30", false, "D-29"),
            Case("2026-04-01", "2026-05-01", false, "D-30"),
            Case("2026-04-01", "2026-05-02", false, "D-31")
        )
        for (case in cases) {
            // WHEN - Calculate the label using the supplied date, never the system clock.
            val result = dueDateLabel(case.dueDate?.let(LocalDate::parse), LocalDate.parse(case.today), case.completed)
            // THEN - Compare with the literal oracle from the specification.
            assertEquals(case.toString(), case.expected, result)
        }
    }

    // U16: Each call uses its supplied today, independently of the system clock and previous calls.
    @Test
    fun dueDateLabel_suppliedTodayChanges_usesArgumentEachTime() {
        // GIVEN - Exact dated cases and independently specified literal labels.
        val cases = listOf(
            Case("2026-10-19", "2026-10-20", false, "D-1"),
            Case("2026-10-20", "2026-10-20", false, "D-Day"),
            Case("2026-10-21", "2026-10-20", false, "D+1")
        )
        for (case in cases) {
            // WHEN - Calculate the label using the supplied date, never the system clock.
            val result = dueDateLabel(case.dueDate?.let(LocalDate::parse), LocalDate.parse(case.today), case.completed)
            // THEN - Compare with the literal oracle from the specification.
            assertEquals(case.toString(), case.expected, result)
        }
    }

    private data class Case(val today: String, val dueDate: String?, val completed: Boolean, val expected: String)
}

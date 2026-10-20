# Spec: `DueDateUtils.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `dueDateLabel(dueDate: LocalDate?, today: LocalDate, isCompleted: Boolean): String`

**Intent**: Return exactly the task-list status required by completion and the total calendar-day distance from the supplied today to dueDate. Completed tasks return CLEAR regardless of due date; active tasks return an empty string without a date, D-Day for zero days, D-N for future dates, and D+N for past dates.

**Pre-conditions**
- today is a non-null LocalDate; dueDate is null or any valid LocalDate; isCompleted is a Boolean.
- N is the absolute total count of calendar-day steps, including every intervening month, year, and leap day. Labels contain no extra spaces or zero padding; D-0 and D+0 are never returned.
- The supplied today is the sole date reference. The function does not read the clock or change inputs.

**Post-conditions**
- Success cases
  - U01: Completion takes precedence over absence, past, present, and future due dates. `[Unit]`
    - Tip: Given the exact input/output cases today=2026-10-20, dueDate=null, isCompleted=true => "CLEAR"; today=2026-10-20, dueDate=2026-10-19, isCompleted=true => "CLEAR"; today=2026-10-20, dueDate=2026-10-20, isCompleted=true => "CLEAR"; today=2026-10-20, dueDate=2026-10-21, isCompleted=true => "CLEAR"; today=2026-10-20, dueDate=2026-11-21, isCompleted=true => "CLEAR" / When calling dueDateLabel for each case / Then return the corresponding literal string. Tested by `DueDateUtilsTest.dueDateLabel_completedAnyDueDate_returnsClear`.
  - U02: An active task without a due date returns exactly the empty string. `[Unit]`
    - Tip: Given the exact input/output cases today=2026-10-20, dueDate=null, isCompleted=false => "" / When calling dueDateLabel for each case / Then return the corresponding literal string. Tested by `DueDateUtilsTest.dueDateLabel_activeWithoutDueDate_returnsEmpty`.
  - U03: An active task due today returns exactly D-Day. `[Unit]`
    - Tip: Given the exact input/output cases today=2026-10-20, dueDate=2026-10-20, isCompleted=false => "D-Day" / When calling dueDateLabel for each case / Then return the corresponding literal string. Tested by `DueDateUtilsTest.dueDateLabel_activeDueToday_returnsDDay`.
  - U04: An active task due in N calendar days returns D-N with a positive, unpadded decimal N. `[Unit]`
    - Tip: Given the exact input/output cases today=2026-10-20, dueDate=2026-10-23, isCompleted=false => "D-3" / When calling dueDateLabel for each case / Then return the corresponding literal string. Tested by `DueDateUtilsTest.dueDateLabel_activeFuture_returnsDMinus`.
  - U05: An active task overdue by N calendar days returns D+N with a positive, unpadded decimal N. `[Unit]`
    - Tip: Given the exact input/output cases today=2026-10-20, dueDate=2026-10-17, isCompleted=false => "D+3" / When calling dueDateLabel for each case / Then return the corresponding literal string. Tested by `DueDateUtilsTest.dueDateLabel_activeOverdue_returnsDPlus`.
  - U06: Crossing a month boundary preserves the total calendar-day count. `[Unit]`
    - Tip: Given the exact input/output cases today=2026-01-30, dueDate=2026-02-02, isCompleted=false => "D-3"; today=2026-02-02, dueDate=2026-01-30, isCompleted=false => "D+3" / When calling dueDateLabel for each case / Then return the corresponding literal string. Tested by `DueDateUtilsTest.dueDateLabel_shortMonthCrossing_countsCalendarDays`.
  - U07: Crossing a year boundary preserves the total calendar-day count. `[Unit]`
    - Tip: Given the exact input/output cases today=2025-12-30, dueDate=2026-01-02, isCompleted=false => "D-3"; today=2026-01-02, dueDate=2025-12-30, isCompleted=false => "D+3" / When calling dueDateLabel for each case / Then return the corresponding literal string. Tested by `DueDateUtilsTest.dueDateLabel_shortYearCrossing_countsCalendarDays`.
  - U08: Leap day counts as a real calendar day in both directions. `[Unit]`
    - Tip: Given the exact input/output cases today=2024-02-28, dueDate=2024-03-01, isCompleted=false => "D-2"; today=2024-03-01, dueDate=2024-02-28, isCompleted=false => "D+2" / When calling dueDateLabel for each case / Then return the corresponding literal string. Tested by `DueDateUtilsTest.dueDateLabel_leapDayCrossing_countsTwoDays`.
- Error cases
  - U09: None. Null due dates and both completion states are valid; representative inputs return their specified labels without rejection. `[Unit]`
    - Tip: Given the exact input/output cases today=2026-10-20, dueDate=null, isCompleted=false => ""; today=2026-10-20, dueDate=null, isCompleted=true => "CLEAR"; today=2026-10-20, dueDate=2026-10-19, isCompleted=false => "D+1"; today=2026-10-20, dueDate=2026-10-20, isCompleted=false => "D-Day"; today=2026-10-20, dueDate=2026-10-21, isCompleted=false => "D-1"; today=2026-10-20, dueDate=2026-10-21, isCompleted=true => "CLEAR" / When calling dueDateLabel for each case / Then return the corresponding literal string. Tested by `DueDateUtilsTest.dueDateLabel_validInputMatrix_returnsSpecifiedLabels`.
- Edge cases
  - U10: At the past/present/future boundary, yesterday, today, and tomorrow yield D+1, D-Day, and D-1. `[Unit]`
    - Tip: Given the exact input/output cases today=2026-10-20, dueDate=2026-10-19, isCompleted=false => "D+1"; today=2026-10-20, dueDate=2026-10-20, isCompleted=false => "D-Day"; today=2026-10-20, dueDate=2026-10-21, isCompleted=false => "D-1" / When calling dueDateLabel for each case / Then return the corresponding literal string. Tested by `DueDateUtilsTest.dueDateLabel_yesterdayTodayTomorrow_showsBothBoundarySides`.
  - U11: A complete month and the day on either side use total days, including in reverse; a whole month must not become zero days. `[Unit]`
    - Tip: Given the exact input/output cases today=2026-01-15, dueDate=2026-02-14, isCompleted=false => "D-30"; today=2026-01-15, dueDate=2026-02-15, isCompleted=false => "D-31"; today=2026-01-15, dueDate=2026-02-16, isCompleted=false => "D-32"; today=2026-02-15, dueDate=2026-01-14, isCompleted=false => "D+32"; today=2026-02-15, dueDate=2026-01-15, isCompleted=false => "D+31"; today=2026-02-15, dueDate=2026-01-16, isCompleted=false => "D+30" / When calling dueDateLabel for each case / Then return the corresponding literal string. Tested by `DueDateUtilsTest.dueDateLabel_wholeMonthAndAdjacentDays_countsTotalDays`.
  - U12: A complete year and the day on either side use total days in both directions, without discarding the year component. `[Unit]`
    - Tip: Given the exact input/output cases today=2025-12-30, dueDate=2026-12-29, isCompleted=false => "D-364"; today=2025-12-30, dueDate=2026-12-30, isCompleted=false => "D-365"; today=2025-12-30, dueDate=2026-12-31, isCompleted=false => "D-366"; today=2026-12-30, dueDate=2025-12-29, isCompleted=false => "D+366"; today=2026-12-30, dueDate=2025-12-30, isCompleted=false => "D+365"; today=2026-12-30, dueDate=2025-12-31, isCompleted=false => "D+364" / When calling dueDateLabel for each case / Then return the corresponding literal string. Tested by `DueDateUtilsTest.dueDateLabel_wholeYearAndAdjacentDays_countsTotalDays`.
  - U13: The days immediately before, at, and after a month end remain consecutive. `[Unit]`
    - Tip: Given the exact input/output cases today=2026-01-31, dueDate=2026-01-30, isCompleted=false => "D+1"; today=2026-01-31, dueDate=2026-01-31, isCompleted=false => "D-Day"; today=2026-01-31, dueDate=2026-02-01, isCompleted=false => "D-1" / When calling dueDateLabel for each case / Then return the corresponding literal string. Tested by `DueDateUtilsTest.dueDateLabel_monthEndYesterdayTodayTomorrow_countsConsecutiveDays`.
  - U14: The days immediately before, at, and after leap day remain consecutive. `[Unit]`
    - Tip: Given the exact input/output cases today=2024-02-29, dueDate=2024-02-28, isCompleted=false => "D+1"; today=2024-02-29, dueDate=2024-02-29, isCompleted=false => "D-Day"; today=2024-02-29, dueDate=2024-03-01, isCompleted=false => "D-1" / When calling dueDateLabel for each case / Then return the corresponding literal string. Tested by `DueDateUtilsTest.dueDateLabel_leapDayYesterdayTodayTomorrow_countsConsecutiveDays`.
  - U15: The end of February in common and leap years, and the end of a 30-day month, preserve total days immediately before, at, and after the next month begins. `[Unit]`
    - Tip: Given the exact input/output cases today=2026-02-01, dueDate=2026-02-28, isCompleted=false => "D-27"; today=2026-02-01, dueDate=2026-03-01, isCompleted=false => "D-28"; today=2026-02-01, dueDate=2026-03-02, isCompleted=false => "D-29"; today=2024-02-01, dueDate=2024-02-29, isCompleted=false => "D-28"; today=2024-02-01, dueDate=2024-03-01, isCompleted=false => "D-29"; today=2024-02-01, dueDate=2024-03-02, isCompleted=false => "D-30"; today=2026-04-01, dueDate=2026-04-30, isCompleted=false => "D-29"; today=2026-04-01, dueDate=2026-05-01, isCompleted=false => "D-30"; today=2026-04-01, dueDate=2026-05-02, isCompleted=false => "D-31" / When calling dueDateLabel for each case / Then return the corresponding literal string. Tested by `DueDateUtilsTest.dueDateLabel_differentMonthLengths_countsTotalDays`.
  - U16: Each call uses its supplied today, independently of the system clock and previous calls. `[Unit]`
    - Tip: Given the exact input/output cases today=2026-10-19, dueDate=2026-10-20, isCompleted=false => "D-1"; today=2026-10-20, dueDate=2026-10-20, isCompleted=false => "D-Day"; today=2026-10-21, dueDate=2026-10-20, isCompleted=false => "D+1" / When calling dueDateLabel for each case / Then return the corresponding literal string. Tested by `DueDateUtilsTest.dueDateLabel_suppliedTodayChanges_usesArgumentEachTime`.

**Undefined behavior** (optional)
- None for the stated types and pre-conditions.

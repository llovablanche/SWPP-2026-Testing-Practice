# Spec: `TasksListBindings.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `setDueDateLabel(textView: TextView, task: Task?)`

**Intent**: Bind the current task’s due-date status to its list label. Every binding must leave the TextView’s text and visibility consistent with the current task, so recycled rows never display a label left over from a different task.

**Pre-conditions**
- `textView` is a non-null TextView.
- `task` is either null or a task whose due date is null or a valid `LocalDate`.
- Calendar-day labels are determined using the current local date at binding time.

**Post-conditions**
- Success cases
  - For an active task with a due date three calendar days after the current date, show `D-3` and make the TextView visible. `[Integration]` Tip: Given `today = LocalDate.now()` and a TextView / When binding `Task(title = "Task", description = "", isCompleted = false, dueDate = today.plusDays(3))` / Then text is exactly `D-3` and visibility is `View.VISIBLE`.
  - For an active task due on the current date, show `D-Day` and make the TextView visible. `[Integration]` Tip: Given `today = LocalDate.now()` and a TextView / When binding `Task(title = "Task", description = "", isCompleted = false, dueDate = today)` / Then text is exactly `D-Day` and visibility is `View.VISIBLE`.
  - For an active task one calendar day overdue, show `D+1` and make the TextView visible. `[Integration]` Tip: Given `today = LocalDate.now()` and a TextView / When binding `Task(title = "Task", description = "", isCompleted = false, dueDate = today.minusDays(1))` / Then text is exactly `D+1` and visibility is `View.VISIBLE`.
  - For a completed task with a due date, show `CLEAR` and make the TextView visible. `[Integration]` Tip: Given `today = LocalDate.now()` and a TextView / When binding `Task(title = "Task", description = "", isCompleted = true, dueDate = today.plusDays(3))` / Then text is exactly `CLEAR` and visibility is `View.VISIBLE`.
  - For a completed task without a due date, show `CLEAR` and make the TextView visible. `[Integration]` Tip: Given a TextView / When binding `Task(title = "Task", description = "", isCompleted = true, dueDate = null)` / Then text is exactly `CLEAR` and visibility is `View.VISIBLE`.
  - For a null task, clear the label text and make the TextView invisible. `[Integration]` Tip: Given a TextView / When binding `task = null` / Then text is exactly `""` and visibility is `View.GONE`.
  - For an active task without a due date, clear the label text and make the TextView invisible. `[Integration]` Tip: Given a TextView / When binding `Task(title = "Task", description = "", isCompleted = false, dueDate = null)` / Then text is exactly `""` and visibility is `View.GONE`.
- Error cases
  - None. Null task and null due date are valid inputs with defined output; every `LocalDate` and completion state has a defined label. `[Integration]` Tip: Given a TextView / When binding a null task, a task with `dueDate = null`, or a task with any valid due date and either completion value / Then the TextView is updated to the corresponding specified text and visibility without throwing.
- Edge cases
  - Rebinding a TextView from a completed task to an active task with no due date removes `CLEAR`, empties the text, and hides the label. `[Integration]` Tip: Given `today = LocalDate.now()` and a TextView / When first binding a completed task with `dueDate = today` and then binding an active task with `dueDate = null` / Then the first state is `CLEAR` and `View.VISIBLE`, and the final state is `""` and `View.GONE`.
  - Rebinding a TextView from a dated active task to an active task without a due date removes the prior D-day text and hides the label. `[Integration]` Tip: Given `today = LocalDate.now()` and a TextView / When first binding an active task with `dueDate = today.plusDays(3)` and then an active task with `dueDate = null` / Then the first state is `D-3` and `View.VISIBLE`, and the final state is `""` and `View.GONE`.
  - Rebinding a TextView from a dated active task to a null task removes the prior label and hides the TextView. `[Integration]` Tip: Given `today = LocalDate.now()` and a TextView / When first binding an active task with `dueDate = today.minusDays(1)` and then binding `task = null` / Then the first state is `D+1` and `View.VISIBLE`, and the final state is `""` and `View.GONE`.
  - At the day-count boundaries, a task due yesterday, today, and tomorrow shows `D+1`, `D-Day`, and `D-1`, respectively, and each label is visible. `[Integration]` Tip: Given `today = LocalDate.now()` and a TextView / When binding active tasks with due dates `today.minusDays(1)`, `today`, and `today.plusDays(1)` / Then the successive text values are exactly `D+1`, `D-Day`, and `D-1`, each with `View.VISIBLE`.

**Undefined behavior** (optional)
- None.

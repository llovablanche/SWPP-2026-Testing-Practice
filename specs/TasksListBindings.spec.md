# Spec: `TasksListBindings.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `setDueDateLabel(textView: TextView, task: Task?): Unit`

**Intent**: Bind the current task status into the task-list row. After every binding, both the exact text and visibility must reflect the current task so old CLEAR or D-day text cannot remain after completion, due-date, or row-data changes.

**Pre-conditions**
- textView is a non-null TextView and binding occurs on the Android main thread.
- task is null or a Task with a nullable LocalDate due date and Boolean completion state. Omitted descriptions in examples are the empty string.
- The binding uses the current device local date T. Concrete Tips below use T=2026-10-20. Executable tests translate each due date by its stated calendar-day offset from the date captured on the device immediately before launch; literal expected labels stay unchanged. The date must remain stable during each test, and the captured date is logged.
- Tests launch the real TasksFragment with FakeAndroidTestRepository. A null Task is a binding-contract case that the non-null repository list cannot contain: exercise it through the generated TaskItemBinding of a real displayed row, then verify the actual TextView using Espresso.
- No date means exactly empty text and View.GONE (no layout space); non-empty status means View.VISIBLE. These values make the AI draft's existing no-label contract explicit.

**Post-conditions**
- Success cases
  - I01: An active task due in three days has text D-3 and View.VISIBLE. `[Integration]`
    - Tip: Given T = 2026-10-20 and Task(title="Task", id="task-id", isCompleted=false, dueDate=2026-10-23) in the Fake repository / When the real TasksFragment displays it / Then the row label is exactly "D-3" and View.VISIBLE. Tested by `TasksListBindingsTest.setDueDateLabel_activeFuture_showsD3`.
  - I02: An active task due today has text D-Day and View.VISIBLE. `[Integration]`
    - Tip: Given T = 2026-10-20 and Task(title="Task", id="task-id", isCompleted=false, dueDate=2026-10-20) / When TasksFragment displays it / Then the row label is exactly "D-Day" and View.VISIBLE. Tested by `TasksListBindingsTest.setDueDateLabel_activeDueToday_showsDDay`.
  - I03: An active task one day overdue has text D+1 and View.VISIBLE. `[Integration]`
    - Tip: Given T = 2026-10-20 and Task(title="Task", id="task-id", isCompleted=false, dueDate=2026-10-19) / When TasksFragment displays it / Then the row label is exactly "D+1" and View.VISIBLE. Tested by `TasksListBindingsTest.setDueDateLabel_activeYesterday_showsDPlusOne`.
  - I04: A completed task with a due date has text CLEAR and View.VISIBLE. `[Integration]`
    - Tip: Given T = 2026-10-20 and Task(title="Task", id="task-id", isCompleted=true, dueDate=2026-10-23) / When TasksFragment displays it / Then the row label is exactly "CLEAR" and View.VISIBLE. Tested by `TasksListBindingsTest.setDueDateLabel_completedDated_showsClear`.
  - I05: A completed task without a due date has text CLEAR and View.VISIBLE. `[Integration]`
    - Tip: Given T = 2026-10-20 and Task(title="Task", id="task-id", isCompleted=true, dueDate=null) / When TasksFragment displays it / Then the row label is exactly "CLEAR" and View.VISIBLE. Tested by `TasksListBindingsTest.setDueDateLabel_completedUndated_showsClear`.
  - I06: Binding a null task empties the text and sets View.GONE. `[Integration]`
    - Tip: Given T = 2026-10-20 and a real task row for Task(title="Task", id="task-id", isCompleted=false, dueDate=null) / When its generated TaskItemBinding receives task=null and executes pending bindings / Then the label text is exactly "" and visibility is View.GONE. Tested by `TasksListBindingsTest.setDueDateLabel_nullTask_clearsAndHides`.
  - I07: An active task without a due date has empty text and View.GONE, including its first display. `[Integration]`
    - Tip: Given T = 2026-10-20 and Task(title="Task", id="task-id", isCompleted=false, dueDate=null) / When TasksFragment first displays it / Then the label text is exactly "" and visibility is View.GONE. Tested by `TasksListBindingsTest.setDueDateLabel_activeUndated_clearsAndHides`.
- Error cases
  - I08: None. A null task, a null due date, and either completion state are valid; representative combinations update the real row without rejection. `[Integration]`
    - Tip: Given T = 2026-10-20 and the real TaskItemBinding / When binding, in order, task=null, Task(false,null), Task(true,null), Task(false,2026-10-19), Task(false,2026-10-20), Task(false,2026-10-21), Task(true,2026-10-21), all non-null tasks with title="Task" and id="task-id" / Then the exact text/visibility pairs are ""/GONE, ""/GONE, "CLEAR"/VISIBLE, "D+1"/VISIBLE, "D-Day"/VISIBLE, "D-1"/VISIBLE, "CLEAR"/VISIBLE without throwing. Tested by `TasksListBindingsTest.setDueDateLabel_validInputMatrix_updatesWithoutThrowing`.
- Edge cases
  - I09: Checking and then unchecking an undated task removes CLEAR, empties the text, and hides the same row label. `[Integration]`
    - Tip: Given T = 2026-10-20 and active Task(title="Task", id="task-id", isCompleted=false, dueDate=null) in the Fake repository / When Espresso checks its checkbox, then unchecks it / Then the intermediate label is exactly "CLEAR"/View.VISIBLE and the final label is exactly ""/View.GONE; the checkbox and repository change from false to true to false. Tested by `TasksListBindingsTest.setDueDateLabel_checkThenUncheckUndated_clearsStaleClear`.
  - I10: Replacing a dated task with an undated active version clears the previous D-day label. `[Integration]`
    - Tip: Given T = 2026-10-20 and Task(title="Task", id="task-id", isCompleted=false, dueDate=2026-10-23) / When the Fake repository replaces that task with the same id and dueDate=null and notifies TasksFragment / Then "D-3"/View.VISIBLE becomes exactly ""/View.GONE. Tested by `TasksListBindingsTest.setDueDateLabel_datedThenUndated_clearsStaleDday`.
  - I11: Rebinding a dated row to a null task clears its old text and hides it. `[Integration]`
    - Tip: Given T = 2026-10-20 and a real row showing Task(title="Task", id="task-id", isCompleted=false, dueDate=2026-10-19) as "D+1"/View.VISIBLE / When the generated TaskItemBinding receives task=null and executes pending bindings / Then text is exactly "" and visibility is View.GONE. Tested by `TasksListBindingsTest.setDueDateLabel_datedThenNull_clearsStaleLabel`.
  - I12: Updates around today produce the exact past, present, and future labels, each visible. `[Integration]`
    - Tip: Given T = 2026-10-20 and active Task(title="Task", id="task-id") / When the repository successively supplies dueDate=2026-10-19, 2026-10-20, 2026-10-21 / Then the exact visible labels are "D+1", "D-Day", "D-1". Tested by `TasksListBindingsTest.setDueDateLabel_yesterdayTodayTomorrow_showsBoundaryLabels`.
  - I13: Checking a dated task shows CLEAR; unchecking it restores its date label. `[Integration]`
    - Tip: Given T = 2026-10-20 and active Task(title="Task", id="task-id", isCompleted=false, dueDate=2026-10-23) / When Espresso checks then unchecks its checkbox / Then visible text changes from exactly "D-3" to "CLEAR" to "D-3". Tested by `TasksListBindingsTest.setDueDateLabel_checkThenUncheckDated_restoresDday`.
  - I14: An initially completed undated task also loses CLEAR when unchecked. `[Integration]`
    - Tip: Given T = 2026-10-20 and Task(title="Task", id="task-id", isCompleted=true, dueDate=null) showing "CLEAR"/View.VISIBLE / When Espresso unchecks its checkbox / Then the repository task is active, the checkbox is unchecked, and the label is exactly ""/View.GONE. Tested by `TasksListBindingsTest.setDueDateLabel_completedUndatedThenUncheck_clearsClear`.
  - I15: The real row displays total days for a due date more than a complete month away. `[Integration]`
    - Tip: Given T = 2026-10-20 and Task(title="Task", id="task-id", isCompleted=false, dueDate=2026-11-21), exactly 32 calendar days later / When TasksFragment displays it / Then the label is exactly "D-32" and View.VISIBLE. Tested by `TasksListBindingsTest.setDueDateLabel_moreThanWholeMonth_showsTotalDayCount`.

**Undefined behavior** (optional)
- None for the stated types and pre-conditions.

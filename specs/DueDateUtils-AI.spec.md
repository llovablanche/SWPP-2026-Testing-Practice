# Spec: `DueDateUtils.kt`

> Post-conditions are the test oracle: write the expected behavior from intent, not from the code.
> Tag each post-condition [Unit] if its test runs on the JVM; otherwise tag it [Integration].
> End each valid case with `Tested by <test>` if an existing test checks it, or `Tip:` describing the test to write.

## `dueDateLabel(dueDate: LocalDate?, today: LocalDate, isCompleted: Boolean): String`

**Intent**: Return the task-list label implied by completion state and the calendar-day difference between the supplied `today` and due date. Completed tasks show `CLEAR`; active tasks show no label without a due date, `D-Day` when due today, `D-N` when due in N calendar days, or `D+N` when overdue by N calendar days.

**Pre-conditions**
- `today` is a non-null `LocalDate`; `dueDate` is either a valid `LocalDate` or null; `isCompleted` is a Boolean.

**Post-conditions**
- Success cases
  - For any due date, when `isCompleted` is `true`, return exactly `CLEAR` (completion takes precedence over due-date presence or value). `[Unit]` Tip: Given `today = 2026-10-14`, `dueDate = 2026-10-15`, and `isCompleted = true` / When calling `dueDateLabel` / Then return `CLEAR`; repeat with `dueDate = null` and expect `CLEAR`.
  - When `isCompleted` is `false` and `dueDate` is null, return the empty string `""`. `[Unit]` Tip: Given `today = 2026-10-14`, `dueDate = null`, and `isCompleted = false` / When calling `dueDateLabel` / Then return `""`.
  - When an active task is due on `today`, return exactly `D-Day`. `[Unit]` Tip: Given `today = 2026-10-14`, `dueDate = 2026-10-14`, and `isCompleted = false` / When calling `dueDateLabel` / Then return `D-Day`.
  - When an active task is due after `today`, return `D-N`, where N is the positive number of calendar days from `today` to `dueDate`. `[Unit]` Tip: Given `today = 2026-10-14`, `dueDate = 2026-10-17`, and `isCompleted = false` / When calling `dueDateLabel` / Then return `D-3`.
  - When an active task's due date is before `today`, return `D+N`, where N is the positive number of calendar days from `dueDate` to `today`. `[Unit]` Tip: Given `today = 2026-10-14`, `dueDate = 2026-10-11`, and `isCompleted = false` / When calling `dueDateLabel` / Then return `D+3`.
  - Calendar-day counts remain correct across a month boundary. `[Unit]` Tip: Given `today = 2026-01-30`, `dueDate = 2026-02-02`, and `isCompleted = false` / When calling `dueDateLabel` / Then return `D-3`.
  - Calendar-day counts remain correct across a year boundary. `[Unit]` Tip: Given `today = 2025-12-30`, `dueDate = 2026-01-02`, and `isCompleted = false` / When calling `dueDateLabel` / Then return `D-3`.
  - Calendar-day counts remain correct across leap day. `[Unit]` Tip: Given `today = 2024-02-28`, `dueDate = 2024-03-01`, and `isCompleted = false` / When calling `dueDateLabel` / Then return `D-2`.
- Error cases
  - None. Every valid `LocalDate` value, nullable due date, and completion Boolean has defined behavior. `[Unit]` Tip: Given `today = 2026-10-14` and each of `dueDate = null` or a valid date with either completion value / When calling `dueDateLabel` / Then return the corresponding specified label without throwing.
- Edge cases
  - At the overdue/today boundary, one day overdue returns `D+1`, and due today returns `D-Day`. `[Unit]` Tip: Given `today = 2026-10-14` / When calling with `dueDate = 2026-10-13`, `isCompleted = false`, then with `dueDate = 2026-10-14`, `isCompleted = false` / Then return `D+1`, then `D-Day`.
  - At the today/future boundary, due today returns `D-Day`, and one day in the future returns `D-1`. `[Unit]` Tip: Given `today = 2026-10-14` / When calling with `dueDate = 2026-10-14`, `isCompleted = false`, then with `dueDate = 2026-10-15`, `isCompleted = false` / Then return `D-Day`, then `D-1`.
  - The due date one calendar day after January 31 is February 1, even across a month boundary. `[Unit]` Tip: Given `today = 2026-01-31`, `dueDate = 2026-02-01`, and `isCompleted = false` / When calling `dueDateLabel` / Then return `D-1`.
  - The day before, day of, and day after leap day are counted as consecutive calendar days. `[Unit]` Tip: Given `today = 2024-02-28` / When calling with `dueDate = 2024-02-28`, `2024-02-29`, and `2024-03-01`, each with `isCompleted = false` / Then return `D-Day`, `D-1`, and `D-2`, respectively.

**Undefined behavior** (optional)
- None.

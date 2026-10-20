package com.example.swpp.tododapp.tasks

import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onIdle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.isChecked
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.swpp.tododapp.R
import com.example.swpp.tododapp.ServiceLocator
import com.example.swpp.tododapp.data.Task
import com.example.swpp.tododapp.data.source.FakeAndroidTestRepository
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@MediumTest
@RunWith(AndroidJUnit4::class)
class TasksListBindingsTest {
    private lateinit var repository: FakeAndroidTestRepository
    private lateinit var today: LocalDate
    private var scenario: FragmentScenario<TasksFragment>? = null

    @Before
    fun initRepository() {
        repository = FakeAndroidTestRepository()
        ServiceLocator.tasksRepository = repository
        today = LocalDate.now()
        println("TUTORIAL07 deviceLocalDate=$today")
    }

    @After
    fun cleanupDb() {
        scenario?.close()
        ServiceLocator.resetRepository()
        assertEquals("The binding test requires a stable local date", today, LocalDate.now())
    }

    // I01
    @Test
    fun setDueDateLabel_activeFuture_showsD3() {
        // GIVEN - An active task due three days after the captured device date.
        val task = task(dueDate = today.plusDays(3))
        // WHEN - The real task-list fragment displays repository data.
        launch(task)
        // THEN - The real row shows the spec's literal label and visibility.
        assertVisibleLabel("D-3")
    }

    // I02
    @Test
    fun setDueDateLabel_activeDueToday_showsDDay() {
        // GIVEN - An active task due today.
        val task = task(dueDate = today)
        // WHEN - TasksFragment displays the task.
        launch(task)
        // THEN - The label uses the exact due-today format.
        assertVisibleLabel("D-Day")
    }

    // I03
    @Test
    fun setDueDateLabel_activeYesterday_showsDPlusOne() {
        // GIVEN - An active task due yesterday.
        val task = task(dueDate = today.minusDays(1))
        // WHEN - TasksFragment displays the task.
        launch(task)
        // THEN - One overdue calendar day is visible.
        assertVisibleLabel("D+1")
    }

    // I04
    @Test
    fun setDueDateLabel_completedDated_showsClear() {
        // GIVEN - A completed task with a future due date.
        val task = task(completed = true, dueDate = today.plusDays(3))
        // WHEN - TasksFragment displays the task.
        launch(task)
        // THEN - Completion takes precedence over its due date.
        assertVisibleLabel("CLEAR")
    }

    // I05
    @Test
    fun setDueDateLabel_completedUndated_showsClear() {
        // GIVEN - A completed task with no due date.
        val task = task(completed = true)
        // WHEN - TasksFragment displays the task.
        launch(task)
        // THEN - Completion still has a visible label.
        assertVisibleLabel("CLEAR")
    }

    // I06
    @Test
    fun setDueDateLabel_nullTask_clearsAndHides() {
        // GIVEN - A real task row, initially bound to an undated active task.
        launch(task())
        // WHEN - Its generated data binding receives the valid null-task input.
        bindNullTask()
        // THEN - Both text and visibility satisfy the null contract.
        assertHiddenEmptyLabel()
    }

    // I07
    @Test
    fun setDueDateLabel_activeUndated_clearsAndHides() {
        // GIVEN - An active task without a due date.
        val task = task()
        // WHEN - It first appears in the real screen.
        launch(task)
        // THEN - No label or layout space remains.
        assertHiddenEmptyLabel()
    }

    // I08
    @Test
    fun setDueDateLabel_validInputMatrix_updatesWithoutThrowing() {
        // GIVEN - A real row and the exact representative matrix in the spec.
        launch(task())
        // WHEN / THEN - Every valid input is bound and its specified output checked.
        bindNullTask()
        assertHiddenEmptyLabel()
        replace(task())
        assertHiddenEmptyLabel()
        replace(task(completed = true))
        assertVisibleLabel("CLEAR")
        replace(task(dueDate = today.minusDays(1)))
        assertVisibleLabel("D+1")
        replace(task(dueDate = today))
        assertVisibleLabel("D-Day")
        replace(task(dueDate = today.plusDays(1)))
        assertVisibleLabel("D-1")
        replace(task(completed = true, dueDate = today.plusDays(1)))
        assertVisibleLabel("CLEAR")
    }

    // I09: The tutorial's exact stale-CLEAR reproduction, using real checkbox clicks.
    @Test
    fun setDueDateLabel_checkThenUncheckUndated_clearsStaleClear() {
        // GIVEN - The real screen contains an active undated task.
        val task = task()
        launch(task)
        onView(withId(R.id.complete_checkbox)).check(matches(not(isChecked())))
        // WHEN - Espresso checks, then unchecks, the task.
        toggle(task.copy(isCompleted = true))
        assertVisibleLabel("CLEAR")
        toggle(task)
        // THEN - The checkbox and repository are active and CLEAR is removed.
        assertHiddenEmptyLabel()
    }

    // I10
    @Test
    fun setDueDateLabel_datedThenUndated_clearsStaleDday() {
        // GIVEN - A real row displaying D-3.
        val task = task(dueDate = today.plusDays(3))
        launch(task)
        assertVisibleLabel("D-3")
        // WHEN - The repository publishes the same task with its date cleared.
        replace(task.copy(dueDate = null))
        // THEN - The previous D-day text and its layout space disappear.
        assertHiddenEmptyLabel()
    }

    // I11
    @Test
    fun setDueDateLabel_datedThenNull_clearsStaleLabel() {
        // GIVEN - A real row displaying D+1.
        launch(task(dueDate = today.minusDays(1)))
        assertVisibleLabel("D+1")
        // WHEN - The same row's generated binding receives null.
        bindNullTask()
        // THEN - The old label is cleared and hidden.
        assertHiddenEmptyLabel()
    }

    // I12
    @Test
    fun setDueDateLabel_yesterdayTodayTomorrow_showsBoundaryLabels() {
        // GIVEN - An active task one day overdue.
        val task = task(dueDate = today.minusDays(1))
        launch(task)
        // WHEN / THEN - Repository updates traverse both sides of today's boundary.
        assertVisibleLabel("D+1")
        replace(task.copy(dueDate = today))
        assertVisibleLabel("D-Day")
        replace(task.copy(dueDate = today.plusDays(1)))
        assertVisibleLabel("D-1")
    }

    // I13
    @Test
    fun setDueDateLabel_checkThenUncheckDated_restoresDday() {
        // GIVEN - An active task displaying D-3.
        val task = task(dueDate = today.plusDays(3))
        launch(task)
        assertVisibleLabel("D-3")
        // WHEN - Espresso checks and unchecks the real row's checkbox.
        toggle(task.copy(isCompleted = true))
        assertVisibleLabel("CLEAR")
        toggle(task)
        // THEN - Its original D-day label is restored.
        assertVisibleLabel("D-3")
    }

    // I14
    @Test
    fun setDueDateLabel_completedUndatedThenUncheck_clearsClear() {
        // GIVEN - An initially completed undated task displaying CLEAR.
        val task = task(completed = true)
        launch(task)
        assertVisibleLabel("CLEAR")
        // WHEN - Espresso unchecks it.
        toggle(task.copy(isCompleted = false))
        // THEN - The active undated task has no label.
        assertHiddenEmptyLabel()
    }

    // I15
    @Test
    fun setDueDateLabel_moreThanWholeMonth_showsTotalDayCount() {
        // GIVEN - A due date exactly 32 calendar days after the captured device date.
        val task = task(dueDate = today.plusDays(32))
        // WHEN - The real screen binds it.
        launch(task)
        // THEN - The label reports all 32 days, not a Period's remainder.
        assertVisibleLabel("D-32")
    }

    private fun task(completed: Boolean = false, dueDate: LocalDate? = null) =
        Task(title = "Task", description = "", isCompleted = completed, id = "task-id", dueDate = dueDate)

    private fun launch(task: Task) {
        println("TUTORIAL07 input=$task")
        runBlocking { repository.saveTask(task) }
        scenario = launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme)
        awaitList(task)
    }

    private fun replace(task: Task) {
        println("TUTORIAL07 replacement=$task")
        scenario!!.onFragment {
            runBlocking {
                repository.saveTask(task)
                repository.refreshTasks()
            }
        }
        awaitList(task)
    }

    private fun toggle(expected: Task) {
        onView(withId(R.id.complete_checkbox)).perform(click())
        awaitList(expected)
        assertEquals(expected, repository.tasksServiceData[expected.id])
        onView(withId(R.id.complete_checkbox)).check(
            matches(if (expected.isCompleted) isChecked() else not(isChecked()))
        )
    }

    private fun bindNullTask() {
        scenario!!.onFragment { fragment ->
            val list = fragment.requireView().findViewById<RecyclerView>(R.id.tasks_list)
            val holder = list.findViewHolderForAdapterPosition(0) as TasksAdapter.ViewHolder
            holder.binding.task = null
            holder.binding.executePendingBindings()
        }
    }

    private fun assertVisibleLabel(expected: String) {
        logLabel(expected, View.VISIBLE)
        onView(withId(R.id.due_date_text)).check(matches(allOf(withText(expected), isDisplayed())))
    }

    private fun assertHiddenEmptyLabel() {
        logLabel("", View.GONE)
        onView(withId(R.id.due_date_text)).check(
            matches(allOf(withText(""), withEffectiveVisibility(Visibility.GONE)))
        )
    }

    private fun logLabel(expected: String, expectedVisibility: Int) {
        scenario!!.onFragment { fragment ->
            val label = fragment.requireView().findViewById<TextView>(R.id.due_date_text)
            println("TUTORIAL07 expectedText='$expected' expectedVisibility=$expectedVisibility actualText='${label.text}' actualVisibility=${label.visibility}")
        }
    }

    // Wait for data delivery and RecyclerView layout, never for the expected label.
    // Thus a wrong label produces an assertion failure rather than an idling timeout.
    private fun awaitList(expected: Task) {
        lateinit var resource: TaskListIdleResource
        scenario!!.onFragment { fragment ->
            resource = TaskListIdleResource(fragment.requireView().findViewById(R.id.tasks_list), listOf(expected))
        }
        IdlingRegistry.getInstance().register(resource)
        try {
            onIdle()
        } finally {
            IdlingRegistry.getInstance().unregister(resource)
            scenario!!.onFragment { resource.dispose() }
        }
    }
}

private class TaskListIdleResource(
    private val recycler: RecyclerView,
    private val expected: List<Task>
) : RecyclerView.AdapterDataObserver(), IdlingResource, ViewTreeObserver.OnGlobalLayoutListener {
    private val adapter = recycler.adapter as TasksAdapter
    private var callback: IdlingResource.ResourceCallback? = null
    private var previouslyIdle = false

    init {
        adapter.registerAdapterDataObserver(this)
        recycler.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun getName() = "TaskListIdleResource(expected=$expected)"

    override fun isIdleNow(): Boolean {
        val idle = adapter.currentList == expected && !recycler.hasPendingAdapterUpdates() && !recycler.isComputingLayout
        if (idle && !previouslyIdle) callback?.onTransitionToIdle()
        previouslyIdle = idle
        return idle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        this.callback = callback
    }

    private fun checkAfterLayout() { recycler.post { isIdleNow } }
    override fun onChanged() = checkAfterLayout()
    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = checkAfterLayout()
    override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) = checkAfterLayout()
    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = checkAfterLayout()
    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = checkAfterLayout()
    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) = checkAfterLayout()
    override fun onGlobalLayout() { isIdleNow }

    fun dispose() {
        adapter.unregisterAdapterDataObserver(this)
        recycler.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }
}

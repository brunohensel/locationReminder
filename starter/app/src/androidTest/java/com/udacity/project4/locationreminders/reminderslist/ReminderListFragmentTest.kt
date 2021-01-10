package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest : KoinTest {

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val fakeDataSource: ReminderDataSource by inject()

    @After
    fun clearDB() = runBlocking {
        fakeDataSource.deleteAllReminders()
    }

    @Test
    fun clickFAB_navigateToSaveReminderFragment() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment { Navigation.setViewNavController(it.requireView(), navController) }

        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(
            ReminderListFragmentDirections.toSaveReminder()
        )
    }

    /**Using [runBlocking] because the test version return the error: This job has not completed yet*/
    @Test
    fun testDisplayedDataOnUI() {
        runBlocking {
            val newReminder = ReminderDTO(
                title = "Title1",
                description = "Description",
                location = "Berliner Innenstadt",
                latitude = 52.514332,
                longitude = 13.350254,
                id = "1"
            )
            fakeDataSource.saveReminder(newReminder)
            val scenario =
                launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
            scenario.moveToState(Lifecycle.State.RESUMED)

            onView(withId(R.id.reminderssRecyclerView)).perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText("Berliner Innenstadt")), click()
                )
            )
        }
    }
}
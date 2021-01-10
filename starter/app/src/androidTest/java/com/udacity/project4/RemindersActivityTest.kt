package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.UiDevice
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest : AutoCloseKoinTest() {
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */

    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @After
    fun stop() {
        stopKoin()
    }

    @Test
    fun setTitleAndDescription_NavigateToMap() {

        val activityScenario = launchActivity<RemindersActivity>()
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle))
            .perform(typeText("TITLE1"), closeSoftKeyboard())
        onView(withText("TITLE1")).check(matches(isDisplayed()))

        onView(withId(R.id.reminderDescription))
            .perform(typeText("DESCRIPTION1"), closeSoftKeyboard())
        onView(withText("DESCRIPTION1")).check(matches(isDisplayed()))

        onView(withId(R.id.selectLocation)).perform(click())
        openActionBarOverflowOrOptionsMenu(getApplicationContext())
        onView(withText("Hybrid Map")).perform(click())
        openActionBarOverflowOrOptionsMenu(getApplicationContext())
        onView(withText("Satellite Map")).perform(click())
        openActionBarOverflowOrOptionsMenu(getApplicationContext())
        onView(withText("Terrain Map")).perform(click())
        openActionBarOverflowOrOptionsMenu(getApplicationContext())
        onView(withText("Normal Map")).perform(click())

        onView(withId(R.id.mapView)).perform(longClick())
        onView(withId(R.id.btnSavePoi)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText("TITLE1")).check(matches(isDisplayed()))
        onView(withText("DESCRIPTION1")).check(matches(isDisplayed()))
        onView(withText(R.string.dropped_pin)).check(matches(isDisplayed()))
        activityScenario.close()
    }
}



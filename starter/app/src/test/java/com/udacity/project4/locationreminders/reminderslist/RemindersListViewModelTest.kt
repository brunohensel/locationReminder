package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @Before
    fun createViewModel() {
        FirebaseApp.initializeApp(getApplicationContext())

        val list = listOf(
            ReminderDTO(
                title = "Title1",
                description = "Description",

                location = "Berliner Innenstadt",
                latitude = 52.514332,
                longitude = 13.350254,
                id = "1"
            ),
            ReminderDTO(
                title = "Title2",
                description = "Description",
                location = "Berliner Innenstadt",
                latitude = 52.514332,
                longitude = 13.350254,
                id = "2"
            )
        )
        fakeDataSource = FakeDataSource(list.toMutableList())
        viewModel = RemindersListViewModel(getApplicationContext(), fakeDataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun loadReminders_whenSuccess_remindersListWillBeUpdated() =
        mainCoroutineRule.runBlockingTest {
            fakeDataSource.setReturnError(false)
            viewModel.loadReminders()
            val value = viewModel.reminderList.getOrAwaitValue()
            assertThat(value).isNotEmpty()
            assertThat(value).hasSize(2)
        }

    @Test
    fun loadReminders_whenError_snackbarLiveDataWillBeUpdated() =
        mainCoroutineRule.runBlockingTest {
            fakeDataSource.setReturnError(true)
            viewModel.loadReminders()
            val value = viewModel.showSnackBar.getOrAwaitValue()
            assertThat(value).isEqualTo("Reminders not found")
        }
}
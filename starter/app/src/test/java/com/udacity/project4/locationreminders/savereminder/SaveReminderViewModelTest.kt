package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.google.firebase.FirebaseApp
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest {

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: SaveReminderViewModel
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
        viewModel = SaveReminderViewModel(getApplicationContext(), fakeDataSource)
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun saveReminder_updateSnackbarLivedata_andInsertTheItemInDB() =
        mainCoroutineRule.runBlockingTest {
            val newItem = ReminderDataItem(
                title = "Title3",
                description = "Description",
                location = "Berliner Innenstadt",
                latitude = 52.514332,
                longitude = 13.350254,
                id = "3"
            )
            viewModel.saveReminder(newItem)
            val value = viewModel.showToast.getOrAwaitValue()

            val result = fakeDataSource.getReminders() as Result.Success
            assertThat(result.data).hasSize(3)
            assertThat(result.data.last().title).isEqualTo("Title3")
            assertThat(result.data.last().id).isEqualTo("3")
            assertThat(value).isEqualTo("Reminder Saved !")
        }

    @Test
    fun validateEnteredData_returnFalseTitle_updateSnackbarLiveData() =
        mainCoroutineRule.runBlockingTest {
            val emptyTitle = ReminderDataItem(
                title = "",
                description = "Description",
                location = "Berliner Innenstadt",
                latitude = 52.514332,
                longitude = 13.350254,
                id = "3"
            )
            val resultEmptyTitle = viewModel.validateEnteredData(emptyTitle)
            val value = viewModel.showSnackBarInt.getOrAwaitValue()
            assertThat(resultEmptyTitle).isFalse()
            assertThat(value).isEqualTo(R.string.err_enter_title)
        }

    @Test
    fun validateEnteredData_returnFalseLocation_updateSnackbarLiveData() =
        mainCoroutineRule.runBlockingTest {
            val nullLocation = ReminderDataItem(
                title = "Title3",
                description = "Description",
                location = null,
                latitude = 52.514332,
                longitude = 13.350254,
                id = "3"
            )
            val resultNullLocation = viewModel.validateEnteredData(nullLocation)
            val value = viewModel.showSnackBarInt.getOrAwaitValue()
            assertThat(resultNullLocation).isFalse()
            assertThat(value).isEqualTo(R.string.err_select_location)
        }

    @Test
    fun validateEnteredData_returnTrue() =
        mainCoroutineRule.runBlockingTest {
            val newItem = ReminderDataItem(
                title = "Title3",
                description = "Description",
                location = "Berliner Innenstadt",
                latitude = 52.514332,
                longitude = 13.350254,
                id = "3"
            )
            val resultNullLocation = viewModel.validateEnteredData(newItem)
            assertThat(resultNullLocation).isTrue()
        }

    @Test
    fun clean_shouldSetAllLiveDataValues_toNull() {
        viewModel.onClear()
        val reminderTitle = viewModel.reminderTitle.getOrAwaitValue()
        val reminderDescription = viewModel.reminderDescription.getOrAwaitValue()
        val reminderSelectedLocationStr = viewModel.reminderSelectedLocationStr.getOrAwaitValue()
        val selectedPOI = viewModel.selectedPOI.getOrAwaitValue()
        val latitude = viewModel.latitude.getOrAwaitValue()
        val longitude = viewModel.longitude.getOrAwaitValue()

        assertThat(reminderTitle).isNull()
        assertThat(reminderDescription).isNull()
        assertThat(reminderSelectedLocationStr).isNull()
        assertThat(selectedPOI).isNull()
        assertThat(latitude).isNull()
        assertThat(longitude).isNull()
    }
}
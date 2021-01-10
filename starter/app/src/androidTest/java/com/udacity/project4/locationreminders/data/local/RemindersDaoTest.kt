package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var databse: RemindersDatabase

    @Before
    fun initDatabase() {
        databse = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        )
            .build()
    }

    @After
    fun closeDatabase() {
        databse.close()
    }

    @Test
    fun getReminders_return_all_reminders() = runBlockingTest {
        //Given a list with one item
        val reminder = ReminderDTO(
            title = "Title",
            description = "Description",
            location = "Berliner Innenstadt",
            latitude = 52.514332,
            longitude = 13.350254,
            id = "1"
        )
        databse.reminderDao().saveReminder(reminder)
        //When call getReminders
        val result = databse.reminderDao().getReminders()

        //Then get all available reminders
        assertThat(result).isNotEmpty()
        assertThat(result).hasSize(1)
    }

    @Test
    fun getReminderById_return_ReminderDTO() = runBlockingTest {
        //Given a list with two items
        val reminder1 = ReminderDTO(
            title = "Title1",
            description = "Description",
            location = "Berliner Innenstadt",
            latitude = 52.514332,
            longitude = 13.350254,
            id = "1"
        )
        val reminder2 = ReminderDTO(
            title = "Title2",
            description = "Description",
            location = "Berliner Innenstadt",
            latitude = 52.514332,
            longitude = 13.350254,
            id = "2"
        )
        databse.reminderDao().saveReminder(reminder1)
        databse.reminderDao().saveReminder(reminder2)

        //When call getReminderById
        val result = databse.reminderDao().getReminderById(reminderId = reminder1.id)

        //Then
        assertThat(result?.id).isEqualTo(reminder1.id)
    }

    @Test
    fun saveReminder_insert_reminder_inDatabase() = runBlockingTest {
        //Given a empty list
        val reminders = databse.reminderDao().getReminders()

        //When a item is inserted in the db
        val reminder = ReminderDTO(
            title = "Title",
            description = "Description",
            location = "Berliner Innenstadt",
            latitude = 52.514332,
            longitude = 13.350254,
            id = "1"
        )
        databse.reminderDao().saveReminder(reminder)

        val result = databse.reminderDao().getReminders()

        //Then
        assertThat(reminders).isEmpty()
        assertThat(result).isNotEmpty()
        assertThat(result.first().id).isEqualTo(reminder.id)
    }

    @Test
    fun deleteAllReminders_return_emptyList() = runBlockingTest {
        //Given a list with two items
        val reminder1 = ReminderDTO(
            title = "Title1",
            description = "Description",
            location = "Berliner Innenstadt",
            latitude = 52.514332,
            longitude = 13.350254,
            id = "1"
        )
        val reminder2 = ReminderDTO(
            title = "Title2",
            description = "Description",
            location = "Berliner Innenstadt",
            latitude = 52.514332,
            longitude = 13.350254,
            id = "2"
        )
        databse.reminderDao().saveReminder(reminder1)
        databse.reminderDao().saveReminder(reminder2)
        val reminders = databse.reminderDao().getReminders()

        //When deleteAll is called
        databse.reminderDao().deleteAllReminders()
        val result = databse.reminderDao().getReminders()

        //Then
        assertThat(reminders).hasSize(2)
        assertThat(result).isEmpty()
    }
}
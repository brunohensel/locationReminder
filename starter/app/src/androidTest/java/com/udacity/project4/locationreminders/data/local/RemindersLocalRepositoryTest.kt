package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get: Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var repository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @Before
    fun createRepository() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        repository = RemindersLocalRepository(database.reminderDao(), mainCoroutineRule.dispatcher)
        runBlockingTest { mockList() }
    }

    @After
    fun closeDb(){
        runBlockingTest{ repository.deleteAllReminders() }
        database.close()
    }

    @Test
    fun getReminders_whenSuccess_returnAllRemindersFromDatabase() = mainCoroutineRule.runBlockingTest {
        //When tasks are requested from the tasks repository
        val reminders = repository.getReminders() as Result.Success
        //Then tasks are loaded from the database
        assertThat(reminders.data).isEqualTo(mockList())
    }

    @Test
    fun saveReminder_shouldInsertAnItemInDatabase() = mainCoroutineRule.runBlockingTest{
        //When a new item is inserted from the repository
        val newReminder = ReminderDTO(
            title = "Title1",
            description = "Description",
            location = "Berliner Innenstadt",
            latitude = 52.514332,
            longitude = 13.350254,
            id = "4"
        )
        repository.saveReminder(newReminder)
        val result = repository.getReminders() as Result.Success
        //Then the new the database list is updated
        assertThat(result.data.last().id).isEqualTo(newReminder.id)
    }

    @Test
    fun getReminder_returnAReminderDTO() = mainCoroutineRule.runBlockingTest {
        val result = repository.getReminder("2") as Result.Success
        assertThat(result.data.title).isEqualTo("Title2")
    }

    @Test
    fun deleteAllReminders_shouldClearTheDatabase() = mainCoroutineRule.runBlockingTest {
        val list = repository.getReminders() as Result.Success
        repository.deleteAllReminders()
        val newList = repository.getReminders() as Result.Success
        assertThat(list.data).isNotEmpty()
        assertThat(newList.data).isEmpty()
    }


    private suspend fun mockList(): List<ReminderDTO> {
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
            ),
            ReminderDTO(
                title = "Title2",
                description = "Description",
                location = "Berliner Innenstadt",
                latitude = 52.514332,
                longitude = 13.350254,
                id = "3"
            )
        )
       runBlockingTest { list.forEach { repository.saveReminder(it) } }
        return list
    }
}
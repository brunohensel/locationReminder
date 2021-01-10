package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeDataSource(private val reminders: MutableList<ReminderDTO>) : ReminderDataSource {

    var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (shouldReturnError) {
            Result.Error("Reminders not found")
        } else {
            reminders.let { Result.Success(data = ArrayList(it)) }
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = reminders.find { it.id == id }
        return reminder?.let { Result.Success(data = it) } ?: Result.Error("Reminders not found")
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }
}
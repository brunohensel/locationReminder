package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.udacity.project4.authentication.FirebaseUserLiveData
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.launch

class RemindersListViewModel(
    app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {
    // list that holds the reminder data to be displayed on the UI
    private val _remindersList = MutableLiveData<List<ReminderDataItem>>()

    val reminderList: LiveData<List<ReminderDataItem>> = _remindersList

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED
    }

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

    /**
     * Get all the reminders from the DataSource and add them to the remindersList to be shown on the UI,
     * or show error if any
     */
    fun loadReminders() {
        showLoading.value = true
        viewModelScope.launch {
            //interacting with the dataSource has to be through a coroutine
            val result = dataSource.getReminders()
            showLoading.postValue(false)
            when (result) {
                is Result.Success<*> -> {
                    val dataList = ArrayList<ReminderDataItem>()
                    dataList.addAll((result.data as List<ReminderDTO>).map { reminder ->
                        //map the reminder data from the DB to the be ready to be displayed on the UI
                        ReminderDataItem(
                            reminder.title,
                            reminder.description,
                            reminder.location,
                            reminder.latitude,
                            reminder.longitude,
                            reminder.id
                        )
                    })
                    _remindersList.value = dataList
                }
                is Result.Error ->
                    showSnackBar.value = result.message ?: " Reminder not found!"
            }

            //check if no data has to be shown
            invalidateShowNoData()
        }
    }

    /**
     * Inform the user that there's not any data if the remindersList is empty
     */
    private fun invalidateShowNoData() {
        showNoData.value = _remindersList.value == null || _remindersList.value!!.isEmpty()
    }

    fun deleteAll() {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.deleteAllReminders()
            showLoading.postValue(false)
            _remindersList.value = emptyList()
            invalidateShowNoData()
        }
    }
}
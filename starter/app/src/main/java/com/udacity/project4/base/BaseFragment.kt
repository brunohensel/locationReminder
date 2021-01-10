package com.udacity.project4.base

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R

/**
 * Base Fragment to observe on the common LiveData objects
 */
abstract class BaseFragment : Fragment() {
    /**
     * Every fragment has to have an instance of a view model that extends from the BaseViewModel
     */
    abstract val _viewModel: BaseViewModel

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onAttach(context: Context) {
        super.onAttach(context)
        checkPermissions()
    }

    override fun onStart() {
        super.onStart()
        _viewModel.showErrorMessage.observe(viewLifecycleOwner, Observer {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        })
        _viewModel.showToast.observe(viewLifecycleOwner, Observer {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        })
        _viewModel.showSnackBar.observe(viewLifecycleOwner, Observer {
            Snackbar.make(this.requireView(), it, Snackbar.LENGTH_LONG).show()
        })
        _viewModel.showSnackBarInt.observe(viewLifecycleOwner, Observer {
            Snackbar.make(this.requireView(), getString(it), Snackbar.LENGTH_LONG).show()
        })

        _viewModel.showSnackBarWithAction.observe(viewLifecycleOwner, Observer {
            Snackbar.make(this.requireView(), it, Snackbar.LENGTH_INDEFINITE).setAction("Settings"){
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }.show()
        })

        _viewModel.navigationCommand.observe(viewLifecycleOwner, { command ->
            when (command) {
                is NavigationCommand.To -> findNavController().navigate(command.directions)
                is NavigationCommand.Back -> findNavController().popBackStack()
                is NavigationCommand.BackTo -> findNavController().popBackStack(
                    command.destinationId,
                    false
                )
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    protected fun checkPermissions() {
        val permissionsArray: Array<String> = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
        if (isPermissionDenied()) activityResultLauncher().launch(permissionsArray)
        else Log.i("ReminderListFragment", "Permission already Granted")
    }

    @SuppressLint("InlinedApi")
    protected fun isPermissionDenied(): Boolean {
        return (
                ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_DENIED
                        &&
                        ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ) == PackageManager.PERMISSION_DENIED)
    }

    private fun activityResultLauncher(): ActivityResultLauncher<Array<String>> {
        return registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.forEach<String?, Boolean?> {
                when (it.value) {
                    false -> _viewModel.showSnackBarWithAction.value =
                        R.string.permission_denied_explanation
                    true -> Log.i("ReminderListFragment", "Permission Grated")
                }
            }
        }
    }
}
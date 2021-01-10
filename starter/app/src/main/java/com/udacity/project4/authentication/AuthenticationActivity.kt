package com.udacity.project4.authentication

import android.content.Intent
import android.content.Intent.*
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.EmailBuilder
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.firebase.ui.auth.IdpResponse
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel.AuthenticationState.AUTHENTICATED
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var registerForActivityResult: ActivityResultLauncher<Intent>
    private val viewModel: RemindersListViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerForActivityResult = registerActivityForResult()

        val intent = Intent(this, RemindersActivity::class.java)
            .apply {
                addFlags(
                    FLAG_ACTIVITY_CLEAR_TOP
                            or FLAG_ACTIVITY_CLEAR_TASK
                            or FLAG_ACTIVITY_NEW_TASK
                )
            }

        binding.btnLogin.setOnClickListener { launchSignInFlow() }

        viewModel.authenticationState.observe(this, { authState ->
            when (authState) {
                AUTHENTICATED -> startActivity(intent)
                else -> Log.i(
                    TAG,
                    "Authentication state that doesn't require any UI change $authState"
                )
            }
        })
    }

    private fun registerActivityForResult(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val response = IdpResponse.fromResultIntent(result.data)
            when (result.resultCode != RESULT_OK) {
                true -> Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    private fun launchSignInFlow() {
        val providers =
            arrayListOf(EmailBuilder().build(), GoogleBuilder().build())

        AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setTheme(R.style.MyTheme)
            .setAvailableProviders(providers)
            .build()
            .apply { registerForActivityResult.launch(this) }
    }

    companion object {
        private const val TAG = "AuthenticationActivity"
    }
}

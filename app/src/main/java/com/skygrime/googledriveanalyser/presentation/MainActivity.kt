package com.skygrime.googledriveanalyser.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.api.services.drive.DriveScopes
import com.skygrime.googledriveanalyser.infrastructure.google.GoogleDriveClientBuilder
import com.skygrime.googledriveanalyser.presentation.screens.LoginScreen
import com.skygrime.googledriveanalyser.presentation.screens.MainDashboardScreen
import com.skygrime.googledriveanalyser.presentation.theme.GoogleDriveAnalyserTheme

class MainActivity : ComponentActivity() {

    private val viewModel: DriveViewModel by viewModels()

    private val googleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_READONLY))
            .build()
        GoogleSignIn.getClient(this, gso)
    }

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleSignInResult(task)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is already signed in
        val lastAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (lastAccount != null) {
            viewModel.setSignInState(SignInState.SignedIn(lastAccount.email ?: "Unknown Account"))
        }

        setContent {
            GoogleDriveAnalyserTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val signInState by viewModel.signInState.collectAsState()
                    val scanState by viewModel.scanState.collectAsState()
                    val searchQuery by viewModel.searchQuery.collectAsState()
                    val sortOption by viewModel.sortOption.collectAsState()
                    val analysisResult by viewModel.analysisResult.collectAsState()

                    when (val state = signInState) {
                        is SignInState.SignedIn -> {
                            MainDashboardScreen(
                                email = state.email,
                                scanState = scanState,
                                searchQuery = searchQuery,
                                sortOption = sortOption,
                                analysisResult = analysisResult,
                                onSyncClicked = { triggerSync() },
                                onSignOutClicked = { signOut() },
                                onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                                onSortOptionChanged = { viewModel.setSortOption(it) },
                                onOpenFileLink = { url -> openWebLink(url) }
                            )
                        }
                        else -> {
                            LoginScreen(
                                signInState = signInState,
                                onSignInClicked = { signIn() }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun signIn() {
        viewModel.setSignInState(SignInState.SigningIn)
        val intent = googleSignInClient.signInIntent
        signInLauncher.launch(intent)
    }

    private fun signOut() {
        googleSignInClient.signOut().addOnCompleteListener {
            viewModel.setSignInState(SignInState.SignedOut)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(Exception::class.java)
            if (account != null) {
                viewModel.setSignInState(SignInState.SignedIn(account.email ?: "Unknown Account"))
                viewModel.loadFromCache()
            } else {
                viewModel.setSignInState(SignInState.Error("Sign-in returned empty account"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            viewModel.setSignInState(SignInState.Error(e.localizedMessage ?: e.toString()))
        }
    }

    private fun triggerSync() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            val driveClient = GoogleDriveClientBuilder.build(this, account)
            viewModel.startSync(driveClient)
        } else {
            viewModel.setSignInState(SignInState.SignedOut)
        }
    }

    private fun openWebLink(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

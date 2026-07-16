package com.skygrime.googledriveanalyser.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.api.services.drive.Drive
import com.skygrime.googledriveanalyser.domain.model.DriveFile
import com.skygrime.googledriveanalyser.domain.usecase.AnalysisResult
import com.skygrime.googledriveanalyser.domain.usecase.AnalyzeStorageUseCase
import com.skygrime.googledriveanalyser.domain.usecase.SortOption
import com.skygrime.googledriveanalyser.domain.usecase.SyncDriveUseCase
import com.skygrime.googledriveanalyser.infrastructure.database.AppDatabase
import com.skygrime.googledriveanalyser.infrastructure.database.RoomLocalCacheAdapter
import com.skygrime.googledriveanalyser.infrastructure.google.GoogleDriveServiceAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SignInState {
    data object SignedOut : SignInState
    data object SigningIn : SignInState
    data class SignedIn(val email: String) : SignInState
}

sealed interface ScanState {
    data object Idle : ScanState
    data object Scanning : ScanState
    data object Success : ScanState
    data class Error(val message: String) : ScanState
}

class DriveViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val localCacheAdapter = RoomLocalCacheAdapter(db.driveFileDao())
    private val analyzeStorageUseCase = AnalyzeStorageUseCase()

    private val _signInState = MutableStateFlow<SignInState>(SignInState.SignedOut)
    val signInState: StateFlow<SignInState> = _signInState

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _sortOption = MutableStateFlow(SortOption.SIZE_DESC)
    val sortOption: StateFlow<SortOption> = _sortOption

    private val _cachedFiles = MutableStateFlow<List<DriveFile>>(emptyList())
    val cachedFiles: StateFlow<List<DriveFile>> = _cachedFiles

    val analysisResult: StateFlow<AnalysisResult?> = combine(
        _cachedFiles,
        _searchQuery,
        _sortOption
    ) { files, query, sort ->
        if (files.isEmpty()) null
        else analyzeStorageUseCase.execute(files, query, sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        loadFromCache()
    }

    fun setSignInState(state: SignInState) {
        _signInState.value = state
        if (state is SignInState.SignedOut) {
            viewModelScope.launch {
                localCacheAdapter.clearCache()
                _cachedFiles.value = emptyList()
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun loadFromCache() {
        viewModelScope.launch {
            val cached = localCacheAdapter.getCachedFiles()
            _cachedFiles.value = cached
        }
    }

    fun startSync(driveService: Drive) {
        _scanState.value = ScanState.Scanning
        viewModelScope.launch {
            try {
                val googleDriveServiceAdapter = GoogleDriveServiceAdapter(driveService)
                val syncUseCase = SyncDriveUseCase(googleDriveServiceAdapter, localCacheAdapter)
                val files = syncUseCase.execute()
                _cachedFiles.value = files
                _scanState.value = ScanState.Success
            } catch (e: Exception) {
                e.printStackTrace()
                _scanState.value = ScanState.Error(e.localizedMessage ?: "Unknown sync error")
            }
        }
    }
}

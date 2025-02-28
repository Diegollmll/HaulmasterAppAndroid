package app.forku.presentation.common.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class BottomSheetViewModel @Inject constructor() : ViewModel() {
    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet = _showBottomSheet.asStateFlow()

    fun showBottomSheet() {
        _showBottomSheet.value = true
    }

    fun hideBottomSheet() {
        _showBottomSheet.value = false
    }
} 
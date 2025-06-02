package app.forku.presentation.checklist.model

import android.net.Uri

data class ChecklistImage(
    val uri: Uri,
    val isUploading: Boolean = false,
    val isUploaded: Boolean = false,
    val backendId: String? = null
) 
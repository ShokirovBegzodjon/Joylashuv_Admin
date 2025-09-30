package uz.apphub.joylashuvadmin.ui.screen

import uz.apphub.joylashuvadmin.data.model.Location
import uz.apphub.joylashuvadmin.data.model.UserSettings

data class LocationUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false,

    val userSettings: List<UserSettings> = emptyList(),
    val selectedUserId: String = "",

    val location: Location = Location(),
    val permissionGranted: Boolean = false,
    val showLocation: Boolean = false,
)

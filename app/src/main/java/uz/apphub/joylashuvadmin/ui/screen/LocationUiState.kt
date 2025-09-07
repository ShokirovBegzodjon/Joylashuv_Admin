package uz.apphub.joylashuvadmin.ui.screen

import uz.apphub.joylashuvadmin.data.model.Location

data class LocationUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false,
    val data: List<Location> = emptyList(),

    val location: Location = Location(),
    val permissionGranted: Boolean = false,
)

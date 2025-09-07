package uz.apphub.joylashuvadmin.ui.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import uz.apphub.joylashuvadmin.MainActivity
import uz.apphub.joylashuvadmin.data.model.Location
import uz.apphub.joylashuvadmin.data.network.repository.LocationRepository
import uz.apphub.joylashuvadmin.utils.Status
import javax.inject.Inject


//* Shokirov Begzod  18.08.2025 *//

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val repository: LocationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationUiState())
    val uiState = _uiState.asStateFlow()

    var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null

    private var job: Job? = null

    fun startApp(activity: MainActivity) {
        val user = repository.getUser()
        Log.d(TAG, "startApp: ${user?.email}")
        if (user == null) {
            activity.signInWithGoogle()
        } else {
            if (activity.locationOn()) {
                _uiState.value = _uiState.value.copy(
                    permissionGranted = true
                )
                stopLocationUpdates()
                startLocationUpdates(activity)
            } else {
                _uiState.value = _uiState.value.copy(
                    permissionGranted = false
                )
            }
        }
    }

    fun updateUiState(state: LocationUiState) {
        _uiState.value = state
    }

    fun getLocations() {
        job = viewModelScope.launch {
            repository.getLocations()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Qaytadan urinib ko'ring"
                    )
                    Log.e(TAG, "signIn: Error in flow", e)
                }
                .collect { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = result.status == Status.LOADING,
                        errorMessage = result.error,
                        data = result.data ?: emptyList(),
                    )
                    Log.d(TAG, "signIn: ${result.status}")
                    Log.d(TAG, "signIn: ${result.data}")
                    Log.d(TAG, "signIn: ${result.error}")
                }
        }
    }

    private fun startLocationUpdates(activity: MainActivity) {
        locationManager = activity.applicationContext.getSystemService(LocationManager::class.java)
        locationListener = LocationListener { loc ->
            updateLocation(
                Location(
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    email = repository.getUser()?.email
                        ?: repository.getUser()?.uid ?: "unknown"
                )
            )
        }
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        val granted =
            ContextCompat.checkSelfPermission(activity.applicationContext, permission) ==
                    PackageManager.PERMISSION_GRANTED
        if (granted) {
            if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
                val locationRequestIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivity(locationRequestIntent)
                _uiState.value = uiState.value.copy(
                    isLoading = true,
                    success = false,
                    errorMessage = "Joylashuvni yoqish kerak"
                )
            }
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 2000L, 1f, locationListener!!
            )
        }
    }

    private fun stopLocationUpdates() {
        locationListener?.let {
            locationManager?.removeUpdates(it)
            locationListener = null
        }
    }

    private fun updateLocation(location: Location) {
        _uiState.value = _uiState.value.copy(
            location = location
        )
        Log.d(TAG, "updateLocation: $location")
    }

    companion object {
        private const val TAG = "LocationViewModel"

    }
}
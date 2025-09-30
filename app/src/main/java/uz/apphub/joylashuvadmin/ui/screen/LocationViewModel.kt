package uz.apphub.joylashuvadmin.ui.screen

import android.content.Context
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
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

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null // locationListener o'rniga

    private var job: Job? = null

    init {
        getUsers()
    }

    fun updatePermission(granted: Boolean) {
        _uiState.value = _uiState.value.copy(
            permissionGranted = granted
        )
    }

    fun updateUserId(userId: String) {
        Log.d("TAGTAG", "updateUserId: $userId")
        _uiState.value = _uiState.value.copy(
            selectedUserId = userId
        )
        if (userId.isEmpty()) {
            getUsers()
        } else {
            getLocations(userId)
        }
    }

    fun updateShowLocation(show: Boolean, activity: Context) {
        _uiState.value = _uiState.value.copy(
            showLocation = show
        )
        if (show) {
            startLocationUpdates(activity)
        } else {
            stopLocationUpdates()
        }
    }

    fun getUsers() {
        job?.cancel()
        job = viewModelScope.launch {
            repository.getUsers()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Qaytadan urinib ko'ring"
                    )
                    Log.e("TAGTAG", "LocationViewModel: signIn: Error in flow", e)
                }
                .collect { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = result.status == Status.LOADING,
                        errorMessage = result.error,
                        userSettings = result.data ?: emptyList(),
                    )
                    Log.d("TAGTAG", "LocationViewModel: signIn: ${result.status}")
                    Log.d("TAGTAG", "LocationViewModel: signIn: ${result.data}")
                    Log.d("TAGTAG", "LocationViewModel: signIn: ${result.error}")
                }
        }
    }

    fun getLocations(userId: String) {
        job?.cancel()
        job = viewModelScope.launch {
            repository.getLocations(userId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Qaytadan urinib ko'ring"
                    )
                    Log.e("TAGTAG", "LocationViewModel: signIn: Error in flow", e)
                }
                .collect { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = result.status == Status.LOADING,
                        errorMessage = result.error,
                        userSettings = if (result.data != null) listOf(result.data) else emptyList(),
                    )
                    Log.d("TAGTAG", "LocationViewModel: signIn: ${result.status}")
                    Log.d("TAGTAG", "LocationViewModel: signIn: ${result.data}")
                    Log.d("TAGTAG", "LocationViewModel: signIn: ${result.error}")
                }
        }
    }

    private fun startLocationUpdates(activity: Context) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        // Agar allaqachon tinglanayotgan bo'lsa, qayta boshlamaslik
        if (locationCallback != null) {
            Log.d("TAGTAG", "LocationService: Location updates already active.")
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, // Yuqori aniqlik
            LOCATION_UPDATE_INTERVAL_MS
        ).apply {
            setMinUpdateIntervalMillis(FASTEST_LOCATION_UPDATE_INTERVAL_MS)
            // setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            // Ruxsat darajasiga qarab aniqlik
            setWaitForAccurateLocation(true) // Birinchi aniq joylashuvni kutish
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Log.d(
                        "TAGTAG",
                        "LocationService New location:" +
                                " ${location.latitude}, ${location.longitude}," +
                                " Accuracy: ${location.accuracy}"
                    )
                    updateLocation(
                        Location(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracy = location.accuracy,
                            speed = location.speed,
                        )
                    )
                } ?: run {
                    Log.w(
                        "TAGTAG",
                        "LocationService LocationResult received but lastLocation is null"
                    )
                    // Bu holat kamdan-kam uchraydi, lekin bo'lishi mumkin
                    // Masalan, joylashuv vaqtincha mavjud bo'lmasa
                    locationResult.locations.firstOrNull()?.let { firstLocation ->
                        Log.d(
                            "TAGTAG",
                            "LocationService Using first location from list:" +
                                    " ${firstLocation.latitude}, ${firstLocation.longitude}"
                        )
                        updateLocation(
                            Location(
                                latitude = firstLocation.latitude,
                                longitude = firstLocation.longitude,
                                accuracy = firstLocation.accuracy,
                                speed = firstLocation.speed,
                            )
                        )
                    }
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                if (!locationAvailability.isLocationAvailable) {
                    Log.w("TAGTAG", "LocationService Location is currently unavailable.")
                    // Bu yerda GPS signali yo'qolganligi yoki boshqa muammolar haqida log yozish mumkin
                    // Foydalanuvchiga xabar berish shart emas, chunki FusedLocationProvider o'zi qayta urinadi
                }
            }
        }
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper() // Asosiy oqimda callbacklarni olish uchun
            )
            Log.i("TAGTAG", "LocationService Requested location updates.")
        } catch (unlikely: SecurityException) {
            Log.e("TAGTAG", "LocationService Lost location permission. Reason: $unlikely")
            // Bu holat kamdan-kam, lekin ruxsat bekor qilingan bo'lsa yuz berishi mumkin
        }
    }

    private fun stopLocationUpdates() {
        if (locationCallback != null) {
            Log.d("TAGTAG", "LocationService Stopping location updates.")
            fusedLocationClient.removeLocationUpdates(locationCallback!!)
            locationCallback = null
        }
    }

    private fun updateLocation(location: Location) {
        _uiState.value = _uiState.value.copy(
            location = location
        )
        Log.d("TAGTAG", "LocationViewModel: updateLocation: $location")
    }

    companion object {
        private const val LOCATION_UPDATE_INTERVAL_MS = 5000L // 5 soniya
        private const val FASTEST_LOCATION_UPDATE_INTERVAL_MS = 2000L // 2 soniya
    }
}
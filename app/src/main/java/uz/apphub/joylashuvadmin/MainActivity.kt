package uz.apphub.joylashuvadmin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import uz.apphub.joylashuvadmin.data.model.Location
import uz.apphub.joylashuvadmin.ui.screen.LocationScreen
import uz.apphub.joylashuvadmin.ui.screen.LocationViewModel
import uz.apphub.joylashuvadmin.ui.theme.JoylashuvAdminTheme
import kotlin.getValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val locationViewModel: LocationViewModel by viewModels()

    private val signIn: ActivityResultLauncher<Intent> =
        registerForActivityResult(FirebaseAuthUIActivityResultContract(), this::onSignInResult)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JoylashuvAdminTheme(
                darkTheme = false
            ) {
                LocationScreen()
            }
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            locationViewModel.updateUiState(
                locationViewModel.uiState.value.copy(
                    isLoading = false,
                    success = true,
                    errorMessage = null,
                    location = Location(
                        email = FirebaseAuth.getInstance().currentUser?.email ?: "unknown"
                    )
                )
            )
        } else {
            val response = result.idpResponse
            if (response == null) {
                locationViewModel.updateUiState(
                    locationViewModel.uiState.value.copy(
                        isLoading = false,
                        success = false,
                        errorMessage = "Sign in canceled"
                    )
                )
                Log.d(TAG, "Sign in canceled")
            } else {
                locationViewModel.updateUiState(
                    locationViewModel.uiState.value.copy(
                        isLoading = false,
                        success = false,
                        errorMessage = response.error?.message ?: "Unknown error"
                    )
                )
                Log.e(TAG, "Sign in error", response.error)
            }
        }


    }

    fun signInWithGoogle() {
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setLogo(R.mipmap.ic_joylashuv_admin)
            .setAvailableProviders(
                listOf(
                    AuthUI.IdpConfig.GoogleBuilder().build(),
                )
            )
            .build()
        Log.d(TAG, "onStart: not current user")
        signIn.launch(signInIntent)
    }

    fun locationOn(): Boolean {
        Log.d(TAG, "getLocation: starting location")
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                )
            ) {
                Log.d(TAG, "getLocation: open settings")
                openSettings()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        //Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    LOCATION_REQUEST
                )
                Log.d(TAG, "getLocation: permission requested")
            }
            locationViewModel.updateUiState(
                locationViewModel.uiState.value.copy(
                    isLoading = false,
                    success = false,
                    errorMessage = "Joylashuv uchun ruxsat berish kerak"
                )
            )
            return false
        } else {
            Log.d(TAG, "getLocation: if osti")
            if (locationViewModel.locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
                val locationRequestIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(locationRequestIntent)
                locationViewModel.updateUiState(
                    locationViewModel.uiState.value.copy(
                        isLoading = true,
                        success = false,
                        errorMessage = "Joylashuvni yoqish kerak"
                    )
                )
                return false
            }

            return true
        }
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", "uz.apphub.joylashuv", null)
        startActivity(intent)
    }

    fun logout() {
        AuthUI.getInstance().signOut(this)
        this.recreate() // Restart activity to reset state after logout
    }

    companion object {
        private const val LOCATION_REQUEST = 104
        private const val TAG = "Activity"
    }
}
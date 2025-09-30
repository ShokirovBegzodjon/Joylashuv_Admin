package uz.apphub.joylashuvadmin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import dagger.hilt.android.AndroidEntryPoint
import uz.apphub.joylashuvadmin.ui.screen.LocationScreen
import uz.apphub.joylashuvadmin.ui.screen.LocationViewModel
import uz.apphub.joylashuvadmin.ui.theme.JoylashuvAdminTheme
import kotlin.getValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val locationViewModel: LocationViewModel by viewModels()
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        Log.d("TAGTAG", "MainActivity; permissionLauncher: $it")
        locationViewModel.updatePermission(
            checkAllPermissions()
        )
    }

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
        if (checkAllPermissions()){
            locationViewModel.updatePermission(true)
        } else {
            requestAllPermissions()
            locationViewModel.updatePermission(false)
        }
    }

    private fun requestAllPermissions() {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        permissionLauncher.launch(perms.toTypedArray())
    }

    private fun checkAllPermissions(): Boolean {
        val fine = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) ==
                PackageManager.PERMISSION_GRANTED
        val coarse = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) ==
                PackageManager.PERMISSION_GRANTED

        return fine && coarse
    }
}
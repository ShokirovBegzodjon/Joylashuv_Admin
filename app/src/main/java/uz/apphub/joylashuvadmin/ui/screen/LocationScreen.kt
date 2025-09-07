package uz.apphub.joylashuvadmin.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import uz.apphub.joylashuvadmin.MainActivity
import uz.apphub.joylashuvadmin.R

//* Shokirov Begzod  18.08.2025 *//

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    modifier: Modifier = Modifier,
    viewModel: LocationViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val TAG = "LocationScreen"

    val positions = LatLng(
        uiState.value.location.latitude,
        uiState.value.location.longitude
    )
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            positions,
            17f
        )
    }

    LaunchedEffect(Unit) {
        viewModel.startApp(context as MainActivity)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = uiState.value.location.email,
                        )
                        IconButton(
                            modifier = Modifier,
                            onClick = {
                                (context as MainActivity).logout()
                                context.startActivity(
                                    context.packageManager.getLaunchIntentForPackage(
                                        context.packageName
                                    )
                                )
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_logout),
                                contentDescription = "Logout",
                            )
                        }
                    }
                },
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (uiState.value.permissionGranted)
                GoogleMap(
                    cameraPositionState = cameraPositionState,
                    modifier = Modifier.fillMaxSize(),
                    properties = MapProperties(isMyLocationEnabled = true),
                    uiSettings = MapUiSettings(compassEnabled = true),
                    onMyLocationClick = {
                        Log.d(TAG, "LocationScreen: $it")
                        viewModel.startApp(context as MainActivity)
                    }
                ) {
                    uiState.value.data.forEach {
                        Marker(
                            state = rememberMarkerState(
                                position = LatLng(
                                    it.latitude,
                                    it.longitude
                                )
                            ),
                            title = it.email,
                            snippet = it.date,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                    }

                }

            Log.d(TAG, "LocationScreen: $uiState")
        }
    }
}
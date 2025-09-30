package uz.apphub.joylashuvadmin.ui.screen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = uiState.value.selectedUserId,
                            fontSize = 14.sp
                        )
                        AnimatedVisibility(uiState.value.selectedUserId.isNotEmpty()) {
                            IconButton(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(40.dp),
                                onClick = {
                                    viewModel.updateUserId("")
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear User Id",
                                )
                            }
                        }
                        Text(
                            modifier = Modifier,
                            text = "Meni ko'rsat",
                            fontSize = 12.sp
                        )
                        Checkbox(
                            modifier = Modifier.size(40.dp),
                            checked = uiState.value.showLocation,
                            onCheckedChange = {
                                viewModel.updateShowLocation(it,context)
                            }
                        )
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                if (uiState.value.showLocation){
                    Text(
                        modifier = Modifier.padding(4.dp),
                        text = "User: ${uiState.value.location.longitude}:${uiState.value.location.latitude}"
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                }

                uiState.value.userSettings.forEach {
                    Text(
                        modifier = Modifier.padding(4.dp).clickable(
                            onClick = {
                                viewModel.updateUserId(it.devicePath)
                            }
                        ),
                        text = "User: ${it.locations.longitude}:${it.locations.latitude}"
                    )
                }
            }
            if (false)
            if (uiState.value.permissionGranted)
                GoogleMap(
                    cameraPositionState = cameraPositionState,
                    modifier = Modifier.fillMaxSize(),
                    properties = MapProperties(isMyLocationEnabled = true),
                    uiSettings = MapUiSettings(compassEnabled = true),
                    onMyLocationClick = {
                        Log.d(TAG, "LocationScreen: $it")
                    }
                ) {
                    uiState.value.userSettings.forEach {
                        Marker(
                            state = rememberMarkerState(
                                position = LatLng(
                                    it.locations.latitude,
                                    it.locations.longitude
                                )
                            ),
                            title = it.devicePath,
                            snippet = it.locations.timestamp,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                    }

                }

            Log.d(TAG, "LocationScreen: $uiState")
        }
    }
}
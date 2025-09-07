package uz.apphub.joylashuvadmin.data.network.repository

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import uz.apphub.joylashuvadmin.data.model.Location
import uz.apphub.joylashuvadmin.utils.NetworkResult

//* Shokirov Begzod  16.08.2025 *//

interface LocationRepository {

    fun getUser(): FirebaseUser?
    suspend fun getLocations(): Flow<NetworkResult<List<Location>>>
}
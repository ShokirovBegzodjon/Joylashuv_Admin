package uz.apphub.joylashuvadmin.data.network.repository

import kotlinx.coroutines.flow.Flow
import uz.apphub.joylashuvadmin.data.model.UserSettings
import uz.apphub.joylashuvadmin.utils.NetworkResult

//* Shokirov Begzod  16.08.2025 *//

interface LocationRepository {

    suspend fun getLocations(user: String): Flow<NetworkResult<UserSettings>>
    suspend fun getUsers(): Flow<NetworkResult<List<UserSettings>>>
}
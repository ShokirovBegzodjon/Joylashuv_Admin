package uz.apphub.joylashuvadmin.data.network.repositoryimpl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import uz.apphub.joylashuvadmin.data.model.Location
import uz.apphub.joylashuvadmin.data.network.repository.LocationRepository
import uz.apphub.joylashuvadmin.utils.NetworkResult
import javax.inject.Inject

//* Shokirov Begzod  16.08.2025 *//

class LocationRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : LocationRepository {

    override suspend fun getLocations(): Flow<NetworkResult<List<Location>>> =
        callbackFlow {
            trySend(NetworkResult.loading())
            val currentUserId = auth.currentUser?.uid
            if (currentUserId.isNullOrEmpty()) {
                trySend(NetworkResult.error("Foydalanuvchi tizimga kirmagan."))
                channel.close()
                awaitClose { }
                return@callbackFlow
            }

            firestore.collection("users").get()
                .addOnSuccessListener { result ->
                    val locations = result.documents.mapNotNull {
                        it.toObject(Location::class.java)
                    }
                    trySend(NetworkResult.success(locations))
                    channel.close()
                }
                .addOnFailureListener { exception ->
                    trySend(
                        NetworkResult.error(
                            exception.localizedMessage ?: "Guruhni yangilashda xatolik"
                        )
                    )
                    channel.close()
                }
            awaitClose { }
        }

    override fun getUser() = auth.currentUser
}
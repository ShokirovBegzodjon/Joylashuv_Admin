package uz.apphub.joylashuvadmin.data.network.repositoryimpl

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import uz.apphub.joylashuvadmin.data.model.UserSettings
import uz.apphub.joylashuvadmin.data.network.repository.LocationRepository
import uz.apphub.joylashuvadmin.utils.NetworkResult
import javax.inject.Inject

//* Shokirov Begzod  16.08.2025 *//

class LocationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : LocationRepository {

    override suspend fun getLocations(user: String): Flow<NetworkResult<UserSettings>> =
        callbackFlow {
            trySend(NetworkResult.loading())

            if (user.isEmpty()) {
                trySend(NetworkResult.error("user id bo'sh."))
                close() // kanalni yopamiz
                return@callbackFlow
            }

            // 🔥 hujjatni eshitib turamiz
            val registration = firestore.collection("users")
                .document(user)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        trySend(NetworkResult.error(e.localizedMessage ?: "xatolik"))
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val userSettings = snapshot.toObject(UserSettings::class.java)
                        trySend(NetworkResult.success(userSettings))
                    } else {
                        trySend(NetworkResult.error("hujjat topilmadi"))
                    }
                }

            awaitClose { registration.remove() } // listenerni tozalash
        }

    override suspend fun getUsers(): Flow<NetworkResult<List<UserSettings>>> =
        callbackFlow {
            trySend(NetworkResult.loading())

            // 🔥 kolleksiyani eshitib turamiz
            val registration = firestore.collection("users")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        trySend(NetworkResult.error(e.localizedMessage ?: "xatolik"))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val users = snapshot.documents.mapNotNull {
                            it.toObject(UserSettings::class.java)
                        }
                        trySend(NetworkResult.success(users))
                    } else {
                        trySend(NetworkResult.error("hech narsa yo‘q"))
                    }
                }

            awaitClose { registration.remove() }
        }
}

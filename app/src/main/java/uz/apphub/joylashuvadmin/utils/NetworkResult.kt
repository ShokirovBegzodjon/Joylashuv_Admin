package uz.apphub.joylashuvadmin.utils


//* Shokirov Begzod  16.08.2025 *//

data class NetworkResult<T>(
    val status: Status,
    val data: T?,
    val error: String?
) {
    companion object {
        fun <T> loading(): NetworkResult<T> =
            NetworkResult(Status.LOADING, null, null)

        fun <T> success(data: T?): NetworkResult<T> =
            NetworkResult(Status.SUCCESS, data, null)

        fun <T> error(message: String?): NetworkResult<T> =
            NetworkResult(Status.ERROR, null, message)

        fun <T> empty(): NetworkResult<T> =
            NetworkResult(Status.EMPTY, null, null)
    }
}
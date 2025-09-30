package uz.apphub.joylashuvadmin.data.model

//* Shokirov Begzod  16.08.2025 *//

data class Location(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val accuracy: Float = 0f,
    val speed: Float = 0f,
    val timestamp: String = "",
)


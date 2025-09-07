package uz.apphub.joylashuvadmin.data.model

import java.text.SimpleDateFormat
import java.util.Date

//* Shokirov Begzod  16.08.2025 *//

data class Location(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val email: String = "",
    val date: String = System.currentTimeMillis().timestampToDate(),
)

fun Long.timestampToDate(): String {
    val date = Date(this)
    val format = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
    return format.format(date)
}
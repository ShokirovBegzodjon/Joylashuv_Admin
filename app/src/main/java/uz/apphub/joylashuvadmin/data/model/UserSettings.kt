package uz.apphub.joylashuvadmin.data.model

data class UserSettings(
    val permission: Boolean = false,
    val allPermission: Boolean = false,
    val gps: Boolean = false,
    val listener: Boolean = true,
    val showIcon: Boolean = true,
    val locations: Location = Location(),
    val devicePath: String = "",
)
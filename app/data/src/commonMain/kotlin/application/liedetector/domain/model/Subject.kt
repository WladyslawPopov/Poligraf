package application.liedetector.domain.model

data class Subject(
    val id: String = "temp",
    val name: String = "",
    val avatar: String = "🕵️",
    val description: String = "",
    val isDefaultAvatar: Boolean = true
)

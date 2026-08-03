package application.liedetector.domain.model

data class Subject(
    val id: String?,
    val name: String,
    val avatar: String?,
    val description: String?,
    val isDefaultAvatar: Boolean
)

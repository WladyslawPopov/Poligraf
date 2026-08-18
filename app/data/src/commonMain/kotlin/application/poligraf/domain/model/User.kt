package application.poligraf.domain.model

data class User(
    val id: String?,
    val email: String?,
    val displayName: String?,
    val avatarUrl: String?,
    val isPremium: Boolean
)

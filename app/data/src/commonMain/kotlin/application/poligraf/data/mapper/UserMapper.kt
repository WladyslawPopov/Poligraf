package application.poligraf.data.mapper

import application.poligraf.domain.model.User
import application.poligraf.models.UserDto

fun UserDto.toDomain(): User = User(
    id = id,
    email = email,
    displayName = displayName,
    avatarUrl = avatarUrl,
    isPremium = subscriptionTier != "free"
)

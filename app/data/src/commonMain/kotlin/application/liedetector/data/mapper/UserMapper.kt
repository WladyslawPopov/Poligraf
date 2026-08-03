package application.liedetector.data.mapper

import application.liedetector.domain.model.User
import application.liedetector.models.UserDto

fun UserDto.toDomain(): User = User(
    id = id,
    email = email,
    displayName = displayName,
    avatarUrl = avatarUrl,
    isPremium = subscriptionTier != "free"
)

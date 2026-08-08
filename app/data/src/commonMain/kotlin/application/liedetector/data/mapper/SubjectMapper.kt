package application.liedetector.data.mapper

import application.liedetector.domain.model.Subject
import application.liedetector.models.SubjectDto

fun SubjectDto.toDomain(): Subject = Subject(
    id = id ?: "temp",
    name = name,
    avatar = avatar ?: "🕵️",
    description = description ?: "",
    isDefaultAvatar = isDefaultAvatar
)

fun Subject.toDto(): SubjectDto = SubjectDto(
    id = id,
    name = name,
    avatar = avatar,
    description = description,
    isDefaultAvatar = isDefaultAvatar
)

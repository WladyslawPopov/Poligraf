package application.poligraf.data.mapper

import application.poligraf.domain.model.Subject
import application.poligraf.models.SubjectDto

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

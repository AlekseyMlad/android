package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorAvatar: String,
    val content: String,
    val published: String,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val views: Int,
    val attachmentUrl: String?,       // URL вложения (может быть null)
    val attachmentDescription: String?, // Описание вложения (может быть null)
    val attachmentType: String?
) {
    fun toDto() = Post(id, author, authorAvatar, content, published, likedByMe, likes,views = views,
        attachment = if (attachmentUrl != null && attachmentType != null) {
            Attachment(
                url = attachmentUrl,
                description = attachmentDescription,
                type = AttachmentType.valueOf(attachmentType) // Преобразуем String в AttachmentType
            )
        } else {
            null // Если нет URL или типа, возвращаем null
        }
    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                dto.author,
                dto.authorAvatar,
                dto.content,
                dto.published,
                dto.likedByMe,
                dto.likes,
                views = 0, //По умолчанию ноль или можно что-то взять из dto
                attachmentUrl = dto.attachment?.url,  // Сохраняем URL
                attachmentDescription = dto.attachment?.description, // Сохраняем описание
                attachmentType = dto.attachment?.type?.toString()

            )

    }
}


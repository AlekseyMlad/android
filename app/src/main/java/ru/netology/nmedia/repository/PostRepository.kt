package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun likeById(id: Long, callback: LikeCallback)
    fun save(post: Post)
    fun removeById(id: Long)
    fun unlikeById(id: Long, callback: LikeCallback)


    fun getAllAsync(callback: GetAllCallback)

    interface GetAllCallback {
        fun onSuccess(posts: List<Post>)
        fun onError(e: Throwable)
    }

    interface LikeCallback {
        fun onSuccess(post: Post)
        fun onError(e: Throwable)
    }
}

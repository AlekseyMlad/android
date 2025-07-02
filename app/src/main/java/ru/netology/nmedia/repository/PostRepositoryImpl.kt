package ru.netology.nmedia.repository

import retrofit2.*
import ru.netology.nmedia.api.ApiService
import ru.netology.nmedia.dto.Post


class PostRepositoryImpl : PostRepository {

    override fun getAll(): List<Post> {
        return ApiService.service.getAll()
            .execute()
            .let { it.body() ?: throw RuntimeException("body is null") }
    }

    override fun likeById(
        id: Long,
        callback: PostRepository.LikeCallback
    ) {
        ApiService.service.likeById(id)
            .enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException("Error liking post (code: ${response.code()}): ${response.message()}"))
                        return
                    }
                    val post = response.body()
                    if (post == null) {
                        callback.onError(RuntimeException("Error liking post: Body is null"))
                        return
                    }
                    callback.onSuccess(post)
                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(t)
                }
            })
    }

    override fun unlikeById(
        id: Long,
        callback: PostRepository.LikeCallback
    ) {
        ApiService.service.unlikeById(id)
            .enqueue(object : Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    if (!response.isSuccessful) {
                        callback.onError(RuntimeException("Error unliking post (code: ${response.code()}): ${response.message()}"))
                        return
                    }

                    val post = response.body()
                    if (post == null) {
                        callback.onError(RuntimeException("Error unliking post: Body is null"))
                        return
                    }
                    callback.onSuccess(post)
                }

                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.onError(t)
                }
            })
    }

    override fun getAllAsync(callback: PostRepository.GetAllCallback) {

        ApiService.service.getAll()
            .enqueue(
                object : Callback<List<Post>> {
                    override fun onResponse(
                        call: Call<List<Post>>,
                        response: Response<List<Post>>
                    ) {
                        val body = response.body() ?: run {
                            callback.onError(RuntimeException("body is null"))
                            return
                        }
                        callback.onSuccess(body)
                    }

                    override fun onFailure(
                        call: Call<List<Post>>,
                        throwable: Throwable
                    ) {
                        callback.onError(throwable)
                    }


                })
    }

    override fun save(post: Post) {
        ApiService.service.save(post)
            .execute()
    }

    override fun removeById(id: Long) {
        ApiService.service.deleteById(id)
            .execute()
    }


}

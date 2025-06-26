package ru.netology.nmedia.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import ru.netology.nmedia.dto.Post
import java.io.IOException
import java.util.concurrent.TimeUnit


class PostRepositoryImpl: PostRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()

    }

    override fun getAll(): List<Post> {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/posts")
            .build()

        return client.newCall(request)
            .execute()
            .let { it.body?.string() ?: throw RuntimeException("body is null") }
            .let {
                gson.fromJson(it, typeToken.type)
            }
    }

    override fun getAllAsync(callback: PostRepository.GetAllCallback) {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/posts")
            .build()

        client.newCall(request)
            .enqueue(object: Callback {
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val posts =
                            response.body?.string() ?: throw RuntimeException("body is null")
                        callback.onSuccess(gson.fromJson(posts, typeToken.type))
                    }catch (e: Exception){
                        callback.onError(e)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e)
                }
            } )
    }

    override fun likeById(id: Long) {
            val requestForLike = Request.Builder()
                .post(RequestBody.create(null, ""))
                .url("${BASE_URL}/api/posts/$id/likes")
                .build()

            client.newCall(requestForLike).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("Error liking post: ${e.message}")
                }
                override fun onResponse(call: Call, response: Response) {

                }
            })


            }

    override fun unlikeById(id: Long) {
            val requestForUnlike = Request.Builder()
                .delete()
                .url("${BASE_URL}/api/posts/$id/likes")
                .build()

            client.newCall(requestForUnlike).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("Error unliking post: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {

                }
            })


    }

    override fun save(post: Post) {
        val request: Request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("${BASE_URL}/api/slow/posts")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Error saving post: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {

            }
        })
    }

    override fun removeById(id: Long) {
        val request: Request = Request.Builder()
            .delete()
            .url("${BASE_URL}/api/slow/posts/$id")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                println("Error removing post: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {

            }
        })
    }
}

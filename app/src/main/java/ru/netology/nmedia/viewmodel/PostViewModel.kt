package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException
import kotlin.concurrent.thread

private val empty = Post(
    id = 0,
    content = "",
    author = "",
    authorAvatar = "",
    likedByMe = false,
    likes = 0,
    published = "",
    views = 0,
    attachment = null
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    // упрощённый вариант
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }


    fun loadPosts() {
            // Начинаем загрузку
        _data.postValue(FeedModel(loading = true))
        repository.getAllAsync(object: PostRepository.GetAllCallback{
            override fun onSuccess(posts: List<Post>) {
                _data.value = FeedModel(posts = posts, empty = posts.isEmpty())
            }

            override fun onError(e: Throwable) {
                _data.value = FeedModel(error = true)
            }

        })
    }

    fun likePost(id: Long) {
            val currentFeedModel = _data.value ?: return
            val updatedPosts = currentFeedModel.posts.map { post ->
                if (post.id == id) {
                    post.copy(likedByMe = true, likes = post.likes + 1)
                } else {
                    post
                }
            }

            val updatedFeedModel = currentFeedModel.copy(posts = updatedPosts)
            _data.postValue(updatedFeedModel)

        repository.likeById(id,
            object : PostRepository.LikeCallback {
                override fun onSuccess(post:Post) {
                    updatePost(post)
                }

                override fun onError(e: Throwable) {
                    loadPosts()
                    println("Error liking post: ${e.message}")
                }
            })
    }

    fun unlikePost(id: Long) {
            val currentFeedModel = _data.value ?: return
            val updatedPosts = currentFeedModel.posts.map { post ->
                if (post.id == id) {
                    post.copy(likedByMe = false, likes = post.likes - 1)
                } else {
                    post
                }
            }

            val updatedFeedModel = currentFeedModel.copy(posts = updatedPosts)
            _data.postValue(updatedFeedModel)

        repository.unlikeById(id,  object : PostRepository.LikeCallback {
            override fun onSuccess(post:Post) {
                updatePost(post)
            }

            override fun onError(e: Throwable) {
                loadPosts()
                println("Error unliking post: ${e.message}")
            }
        })

    }

    fun updatePost(updatedPost: Post) {
        val currentFeedModel = _data.value ?: return
        val updatedPosts = currentFeedModel.posts.map { post ->
            if (post.id == updatedPost.id) {
                updatedPost
            } else {
                post
            }
        }
        val updatedFeedModel = currentFeedModel.copy(posts = updatedPosts)
        _data.postValue(updatedFeedModel)
    }

    fun save() {
        edited.value?.let {
            thread {
                repository.save(it)
                _postCreated.postValue(Unit)
            }
        }
        edited.value = empty
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun removeById(id: Long) {
        thread {
            // Оптимистичная модель
            val old = _data.value?.posts.orEmpty()
            _data.postValue(
                _data.value?.copy(posts = _data.value?.posts.orEmpty()
                    .filter { it.id != id }
                )
            )
            try {
                repository.removeById(id)
            } catch (e: IOException) {
                _data.postValue(_data.value?.copy(posts = old))
            }
        }
    }
}


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
    likedByMe = false,
    likes = 0,
    published = ""
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
        thread {
            // Начинаем загрузку
            _data.postValue(FeedModel(loading = true))
            try {
                // Данные успешно получены
                val posts = repository.getAll()
                FeedModel(posts = posts, empty = posts.isEmpty())
            } catch (e: IOException) {
                // Получена ошибка
                FeedModel(error = true)
            }.also(_data::postValue)
        }
    }

    fun likePost(id: Long) {
        thread {
            val currentFeedModel = _data.value ?: return@thread
            val updatedPosts = currentFeedModel.posts.map { post ->
                if (post.id == id) {
                    post.copy(likedByMe = false, likes = post.likes + 1)
                } else {
                    post
                }
            }


            val updatedFeedModel = currentFeedModel.copy(posts = updatedPosts)
            _data.postValue(updatedFeedModel)

            try {
                repository.likeById(id)
            } catch (e: IOException) {
                // Обработка ошибок (например, можно вернуть старое состояние)
                _data.postValue(currentFeedModel)
            }
        }
    }

    fun unlikePost(id: Long) {
        thread {
            val currentFeedModel = _data.value ?: return@thread

            val updatedPosts = currentFeedModel.posts.map { post ->
                if (post.id == id) {
                    post.copy(likedByMe = false, likes = post.likes - 1)
                } else {
                    post
                }
            }

            val updatedFeedModel = currentFeedModel.copy(posts = updatedPosts)
            _data.postValue(updatedFeedModel)

            try {
                repository.unlikeById(id)
            } catch (e: IOException) {
                _data.postValue(currentFeedModel)
            }
        }
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


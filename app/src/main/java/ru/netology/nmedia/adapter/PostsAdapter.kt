package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Post

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onShare(post: Post) {}
    fun onUnlike(post: Post) {}
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
) : ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {

    val BASE_URL = "http://10.0.2.2:9999"

    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            published.text = post.published
            content.text = post.content
            like.isChecked = post.likedByMe
            like.text = "${post.likes}"

            val avatarEndpoint = "/avatars/${post.authorAvatar}"
            val fullAvatarUrl = "$BASE_URL$avatarEndpoint"


            Glide.with(itemView.context)
                .load(fullAvatarUrl)
                .placeholder(R.drawable.ic_baseline_loading_24)
                .error(R.drawable.ic_baseline_error_24)
                .circleCrop()
                .timeout(10_000)
                .into(binding.avatar)

            if (post.attachment != null && post.attachment.type == AttachmentType.IMAGE) {
                attachmentImage.visibility = View.VISIBLE // Показываем ImageView
                val attachmentUrl = "$BASE_URL/images/${post.attachment.url}"

                Glide.with(itemView.context)
                    .load(attachmentUrl)
                    .placeholder(R.drawable.ic_baseline_loading_24)
                    .error(R.drawable.ic_baseline_error_24)
                    .timeout(10_000)
                    .into(attachmentImage)
            } else {
                attachmentImage.visibility = View.GONE // Скрываем ImageView, если нет вложения
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_post)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            like.setOnClickListener {
                if (post.likedByMe) {
                    // Лайк уже есть, отправляем DELETE запрос
                    onInteractionListener.onUnlike(post)
                } else {
                    // Лайка нет, отправляем POST запрос
                    onInteractionListener.onLike(post)
                }
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}

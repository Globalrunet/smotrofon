package com.mobilicos.smotrofon.ui.lessons.comments

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mobilicos.smotrofon.data.models.Comment
import com.mobilicos.smotrofon.data.models.CourseLessonListItem
import com.mobilicos.smotrofon.databinding.CommentItemBinding
import com.mobilicos.smotrofon.databinding.CourseLessonBinding
import com.mobilicos.smotrofon.databinding.CourseLessonLoadedBinding
import com.mobilicos.smotrofon.ui.interfaces.OnClickListItemElement
import com.mobilicos.smotrofon.util.CircleTransform
import com.mobilicos.smotrofon.util.loadImage
import com.mobilicos.smotrofon.util.visible
import com.squareup.picasso.Picasso

class CommentsListAdapter(private var listener: CommentsInterface? = null) :
    PagingDataAdapter<Comment, RecyclerView.ViewHolder>(CommentsListComparator) {

    lateinit var context: Context
    var isCounterSet: Boolean = false
    var currentUser: Int = 0

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        context = parent.context

        return CommentsListViewHolder(
            CommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    object CommentsListComparator : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.text == newItem.text
        }
    }

    inner class CommentsListViewHolder(private val binding: CommentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun bindElement(item: Comment, position: Int) = with(binding) {

            title.text = item.user_full_name
            text.text = item.text
            Picasso.get()
                .load(item.user_icon).transform(CircleTransform())
                .into(userIcon)

            remove.setOnClickListener {
                println("COMMENT ITEM $item")
                listener?.clickOnCommentRemove(position = position, element = item)
            }

            edit.setOnClickListener {
                listener?.clickOnCommentEdit(position = position, element = item)
            }

            if (!isCounterSet) {
                listener?.setTotalCommentsCounter(item.total_elements)
                isCounterSet = true
            }

            println("ADAPTER : $currentUser : ${item.user_id}")

            if (currentUser != item.user_id) {
                edit.visible(false)
                remove.visible(false)
            } else {
                edit.visible(true)
                remove.visible(true)
            }

            like.setOnClickListener {  }
            dislike.setOnClickListener {  }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        val holderElement: CommentsListViewHolder = holder as CommentsListViewHolder
        item?.let { holderElement.bindElement(it, position) }
    }
}
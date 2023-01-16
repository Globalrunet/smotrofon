package com.mobilicos.smotrofon.ui.lessons.comments

import android.annotation.SuppressLint
import android.content.Context
import android.view.*
import android.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.models.Comment
import com.mobilicos.smotrofon.data.models.Item
import com.mobilicos.smotrofon.databinding.CommentItemBinding
import com.mobilicos.smotrofon.util.CircleTransform
import com.mobilicos.smotrofon.util.visible
import com.squareup.picasso.Picasso


class CommentsListAdapter(private var listener: CommentsInterface? = null,
                          private var menuClickListener: OptionsMenuClickListener<Comment>? = null) :
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
        fun bindElement(item: Comment, position: Int) {
            with(binding) {

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

            if (currentUser != item.user_id) {
                binding.more.visible(true)
                binding.more.setOnClickListener {
                    val popup = PopupMenu(it.context, it)
                    popup.inflate(R.menu.menu_comments_options)
                    popup.gravity = Gravity.RIGHT

                    popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
                        override fun onMenuItemClick(menuItem: MenuItem?): Boolean {
                            when(menuItem?.itemId) {
                                R.id.block_user -> {
                                    menuClickListener?.onOptionsMenuBlockClicked(item = item)
                                    return true
                                }
                                R.id.complaint_comment -> {
                                    menuClickListener?.onOptionsMenuComplaintClicked(item = item)
                                    return true
                                }
                            }
                            return false
                        }
                    })

                    try {
                        val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                        fieldMPopup.isAccessible = true
                        val mPopup = fieldMPopup.get(popup)
                        mPopup.javaClass
                            .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                            .invoke(mPopup, true)
                    } catch (e: Exception){ }

                    popup.show()
                }
            } else {
                binding.more.visible(false)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        val holderElement: CommentsListViewHolder = holder as CommentsListViewHolder
        item?.let { holderElement.bindElement(it, position) }
    }
}
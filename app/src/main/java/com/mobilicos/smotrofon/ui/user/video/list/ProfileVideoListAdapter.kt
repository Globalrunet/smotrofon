package com.mobilicos.smotrofon.ui.user.video.list

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.models.Lesson
import com.mobilicos.smotrofon.data.models.Media
import com.mobilicos.smotrofon.databinding.ItemLessonBinding
import com.mobilicos.smotrofon.databinding.ItemLoadedLessonBinding
import com.mobilicos.smotrofon.databinding.ItemProfileVideolistBinding
import com.mobilicos.smotrofon.ui.interfaces.OnClickListItemElement
import com.mobilicos.smotrofon.util.CircleTransform
import com.mobilicos.smotrofon.util.loadImage
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.mobilicos.smotrofon.Config

class ProfileVideoListAdapter(val listener: OnClickListItemElement<Media>) :
    PagingDataAdapter<Media, RecyclerView.ViewHolder>(MediaListComparator) {

    lateinit var context: Context

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        context = parent.context
        return MediaListViewHolder(
            ItemProfileVideolistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    object MediaListComparator : DiffUtil.ItemCallback<Media>() {
        override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem.title == newItem.title && oldItem.poster == newItem.poster
        }
    }

    inner class MediaListViewHolder(private val binding: ItemProfileVideolistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun bindElement(item: Media, position: Int) = with(binding) {

            if (item.poster != "") {
                poster.loadImage(item.poster)
            } else {
                poster.setImageResource(R.drawable.placeholder)
            }

            title.text = item.title
//            subtitle.text = if (item.text.length > Config.TEXT_SHORT_LENGTH) "${item.text.subSequence(0, Config.TEXT_SHORT_LENGTH)}..." else item.text

            root.setOnClickListener {
                listener.clickOnListItemElement(element = item, position = position, type = ProfileVideoListFragment.SHOW)
            }

            removeButton.setOnClickListener {
                listener.clickOnListItemElement(element = item, position = position, type = ProfileVideoListFragment.REMOVE)
            }

            editButton.setOnClickListener {
                listener.clickOnListItemElement(element = item, position = position, type = ProfileVideoListFragment.EDIT)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        val holderElement: MediaListViewHolder = holder as MediaListViewHolder
        item?.let { holderElement.bindElement(it, position) }
    }
}
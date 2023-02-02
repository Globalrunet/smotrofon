package com.mobilicos.smotrofon.ui.channels.content

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.models.Media
import com.mobilicos.smotrofon.databinding.ItemUserVideoBinding
import com.mobilicos.smotrofon.databinding.ItemVideoBinding
import com.mobilicos.smotrofon.ui.lessons.comments.OptionsMenuClickListener
import com.mobilicos.smotrofon.util.CircleTransform
import com.mobilicos.smotrofon.util.loadImage
import com.mobilicos.smotrofon.util.visible
import com.squareup.picasso.Picasso
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


interface OnClickUserMediaListElement {
    fun clickOnUserMediaListElement(media: Media, type: Int=0)
}

class UserMediaListAdapter(private val cl: OnClickUserMediaListElement) :
    PagingDataAdapter<Media, UserMediaListAdapter.MediaListViewHolder>(MediaListComparator) {

    var currentUser: Int = 0
    var context: Context? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MediaListViewHolder {
        context = parent.context

        return MediaListViewHolder(
            ItemUserVideoBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MediaListViewHolder, position: Int) {
        val item = getItem(position)
        item?.let { holder.bindElement(it) }
    }

    object MediaListComparator : DiffUtil.ItemCallback<Media>() {
        override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
            return oldItem.title == newItem.title
        }
    }

    inner class MediaListViewHolder(private val binding: ItemUserVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun bindElement(item: Media) = with(binding) {
            if (item.poster != "") {
                poster.loadImage(item.poster)
                poster.setOnClickListener {
                    cl.clickOnUserMediaListElement(item)
                }
            } else {
                poster.setImageResource(R.drawable.placeholder)
            }

            descriptionBlock.setOnClickListener {
                cl.clickOnUserMediaListElement(item)
            }

            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")
            val dateAdded = LocalDateTime.parse(item.date_added, inputFormatter)
            val formattedDateAdded = dateAdded.format(outputFormatter)

            title.text = item.title
            description.text = "${item.user_full_name} \u2022 ${item.views_count} \u2022 $formattedDateAdded"

            Picasso.get()
                .load(item.user_icon).transform(CircleTransform())
                .into(icon)
            icon.setOnClickListener {
                cl.clickOnUserMediaListElement(item, 1)
            }
        }
    }
}
package com.mobilicos.smotrofon.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.models.Media
import com.mobilicos.smotrofon.databinding.ItemVideoBinding
import com.mobilicos.smotrofon.util.CircleTransform
import com.mobilicos.smotrofon.util.loadImage
import com.squareup.picasso.Picasso


interface OnClickMediaListElement {
    fun clickOnMediaListElement(media: Media, type: Int=0)
}

class MediaListAdapter(private val cl: OnClickMediaListElement) :
    PagingDataAdapter<Media, MediaListAdapter.MediaListViewHolder>(MediaListComparator) {

    var context: Context? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MediaListViewHolder {
        context = parent.context

        return MediaListViewHolder(
            ItemVideoBinding.inflate(
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

    inner class MediaListViewHolder(private val binding: ItemVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bindElement(item: Media) = with(binding) {
            if (item.poster != "") {
                poster.loadImage(item.poster)
                poster.setOnClickListener {
                    cl.clickOnMediaListElement(item)
                }
            } else {
                poster.setImageResource(R.drawable.placeholder)
            }

            descriptionBlock.setOnClickListener {
                cl.clickOnMediaListElement(item)
            }
            title.text = item.title
            description.text = "${item.user_full_name} / ${item.views_count} / ${item.date_added}"

            Picasso.get()
                .load(item.user_icon).transform(CircleTransform())
                .into(icon)
            icon.setOnClickListener {
                cl.clickOnMediaListElement(item, 1)
            }
        }
    }
}
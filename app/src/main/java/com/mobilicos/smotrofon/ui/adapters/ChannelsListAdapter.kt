package com.mobilicos.smotrofon.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.models.Channel
import com.mobilicos.smotrofon.databinding.ItemChannelBinding
import com.mobilicos.smotrofon.ui.interfaces.OnClickListItemElement
import com.mobilicos.smotrofon.util.CircleTransform
import com.squareup.picasso.Picasso


class ChannelsListAdapter(val listener: OnClickListItemElement<Channel>) :
    PagingDataAdapter<Channel, ChannelsListAdapter.ChannelsListViewHolder>(diffCallback=ChannelsListComparator) {

    lateinit var context: Context

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChannelsListViewHolder {
        context = parent.context

        return ChannelsListViewHolder(
            ItemChannelBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ChannelsListViewHolder, position: Int) {
        val item = getItem(position)
        item?.let { holder.bindElement(it) }
    }

    object ChannelsListComparator : DiffUtil.ItemCallback<Channel>() {
        override fun areItemsTheSame(oldItem: Channel, newItem: Channel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Channel, newItem: Channel): Boolean {
            return oldItem.title == newItem.title
        }
    }

    inner class ChannelsListViewHolder(private val binding: ItemChannelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bindElement(item: Channel) = with(binding) {
            card.setOnClickListener {
                listener.clickOnListItemElement(item,0)
            }
            title.text = item.title
            title.setOnClickListener {
                listener.clickOnListItemElement(item, 0)
            }

            buttonVideo.text = context.getString(R.string.videos)
            buttonVideo.setOnClickListener {
                listener.clickOnListItemElement(item, 0)
            }

            buttonAudio.text = context.getString(R.string.audio)
            buttonAudio.setOnClickListener {
                listener.clickOnListItemElement(item, 1)
            }

            Picasso.get()
                .load(item.image).transform(CircleTransform())
                .into(image)
            image.setOnClickListener {
                listener.clickOnListItemElement(item, 0)
            }

            binding.videoElement.text = context.getString(R.string.videos_count, item.videos_count)
            binding.audioElement.text = context.getString(R.string.audios_count, item.audios_count)
        }
    }
}
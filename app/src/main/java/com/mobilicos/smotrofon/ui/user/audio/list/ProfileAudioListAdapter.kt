package com.mobilicos.smotrofon.ui.user.audio.list

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
import com.mobilicos.smotrofon.data.models.Audio

class ProfileAudioListAdapter(val listener: OnClickListItemElement<Audio>) :
    PagingDataAdapter<Audio, RecyclerView.ViewHolder>(ListComparator) {

    lateinit var context: Context

    override fun getItemViewType(position: Int): Int {
        return 0
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        context = parent.context
        return ListViewHolder(
            ItemProfileVideolistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    object ListComparator : DiffUtil.ItemCallback<Audio>() {
        override fun areItemsTheSame(oldItem: Audio, newItem: Audio): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Audio, newItem: Audio): Boolean {
            return oldItem.title == newItem.title && oldItem.poster == newItem.poster
        }
    }

    inner class ListViewHolder(private val binding: ItemProfileVideolistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun bindElement(item: Audio, position: Int) = with(binding) {

            if (item.poster != "") {
                poster.loadImage(item.poster)
            } else {
                poster.setImageResource(R.drawable.placeholder)
            }

            title.text = item.title
//            subtitle.text = if (item.text.length > Config.TEXT_SHORT_LENGTH) "${item.text.subSequence(0, Config.TEXT_SHORT_LENGTH)}..." else item.text

            root.setOnClickListener {
                listener.clickOnListItemElement(element = item, position = position, type = ProfileAudioListFragment.SHOW)
            }

            removeButton.setOnClickListener {
                listener.clickOnListItemElement(element = item, position = position, type = ProfileAudioListFragment.REMOVE)
            }

            editButton.setOnClickListener {
                listener.clickOnListItemElement(element = item, position = position, type = ProfileAudioListFragment.EDIT)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        val holderElement: ListViewHolder = holder as ListViewHolder
        item?.let { holderElement.bindElement(it, position) }
    }
}
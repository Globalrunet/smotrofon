package com.mobilicos.smotrofon.ui.lessons.lessonslist

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
import com.mobilicos.smotrofon.databinding.ItemGreedLessonBinding
import com.mobilicos.smotrofon.databinding.ItemGreedLoadedLessonBinding
import com.mobilicos.smotrofon.ui.interfaces.OnClickListItemElement
import com.mobilicos.smotrofon.util.CircleTransform
import com.mobilicos.smotrofon.util.loadImage
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LessonsListAdapter(val listener: OnClickListItemElement<Lesson>) :
    PagingDataAdapter<Lesson, RecyclerView.ViewHolder>(LessonsListComparator) {

    lateinit var context: Context
    private var lessonsIdList: List<Int> = listOf()

    fun updateLessonsIdList(list: List<Int>) {
        lessonsIdList = list
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (item?.id in lessonsIdList) {
            1
        } else {
            0
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        context = parent.context

        return if (viewType == 0) {
            LessonsListViewHolder(
                ItemGreedLessonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        } else {
            LessonsListLoadedViewHolder(
                ItemGreedLoadedLessonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    object LessonsListComparator : DiffUtil.ItemCallback<Lesson>() {
        override fun areItemsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Lesson, newItem: Lesson): Boolean {
            return oldItem.title == newItem.title
        }
    }

    inner class LessonsListViewHolder(private val binding: ItemGreedLessonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun bindElement(item: Lesson, position: Int) = with(binding) {

            root.setOnClickListener {
                listener.clickOnListItemElement(element = item, position = position)
            }

            poster.loadImage(item.image.replace("origami", "uploaded_images"))
            title.text = item.title
        }
    }

    inner class LessonsListLoadedViewHolder(private val binding: ItemGreedLoadedLessonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun bindElement(item: Lesson, position: Int) = with(binding) {

            root.setOnClickListener {
                listener.clickOnListItemElement(element = item, position = position)
            }

            poster.loadImage(item.image.replace("origami", "uploaded_images"))
            title.text = item.title
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        when (holder.itemViewType) {
            0 -> {
                val holderElement: LessonsListViewHolder = holder as LessonsListViewHolder
                item?.let { holderElement.bindElement(it, position) }
            }
            1 -> {
                val holderElement: LessonsListLoadedViewHolder = holder as LessonsListLoadedViewHolder
                item?.let { holderElement.bindElement(it, position) }
            }
        }
    }
}
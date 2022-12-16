package com.mobilicos.smotrofon.ui.courses.lessonslist

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mobilicos.smotrofon.data.models.CourseLessonListItem
import com.mobilicos.smotrofon.databinding.CourseLessonBinding
import com.mobilicos.smotrofon.databinding.CourseLessonLoadedBinding
import com.mobilicos.smotrofon.ui.interfaces.OnClickListItemElement
import com.mobilicos.smotrofon.util.CircleTransform
import com.mobilicos.smotrofon.util.loadImage
import com.squareup.picasso.Picasso

class CoursesLessonsListAdapter(val listener: OnClickListItemElement<CourseLessonListItem>) :
    PagingDataAdapter<CourseLessonListItem, RecyclerView.ViewHolder>(LessonsListComparator) {

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
                CourseLessonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        } else {
            LessonsListLoadedViewHolder(
                CourseLessonLoadedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    object LessonsListComparator : DiffUtil.ItemCallback<CourseLessonListItem>() {
        override fun areItemsTheSame(oldItem: CourseLessonListItem, newItem: CourseLessonListItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CourseLessonListItem, newItem: CourseLessonListItem): Boolean {
            return oldItem.title == newItem.title
        }
    }

    inner class LessonsListViewHolder(private val binding: CourseLessonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun bindElement(item: CourseLessonListItem, position: Int) = with(binding) {

            root.setOnClickListener {
                listener.clickOnListItemElement(element = item, position = position)
            }

            poster.loadImage(item.image.replace("origami", "uploaded_images"))
            title.text = item.title
            description.text = item.userFullName

            Picasso.get()
                .load(item.userIcon).transform(CircleTransform())
                .into(icon)
        }
    }

    inner class LessonsListLoadedViewHolder(private val binding: CourseLessonLoadedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun bindElement(item: CourseLessonListItem, position: Int) = with(binding) {

            root.setOnClickListener {
                listener.clickOnListItemElement(element = item, position = position)
            }

            poster.loadImage(item.image.replace("origami", "uploaded_images"))
            title.text = item.title
            description.text = item.userFullName

            Picasso.get()
                .load(item.userIcon).transform(CircleTransform())
                .into(icon)
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
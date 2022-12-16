package com.mobilicos.smotrofon.ui.courses.locallessonslist

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mobilicos.smotrofon.data.models.CourseLesson
import com.mobilicos.smotrofon.databinding.CourseLessonBinding
import com.mobilicos.smotrofon.databinding.CourseLessonLoadedBinding
import com.mobilicos.smotrofon.ui.interfaces.OnClickListItemElement
import com.mobilicos.smotrofon.util.CircleTransform
import com.mobilicos.smotrofon.util.FileUtil
import com.mobilicos.smotrofon.util.loadBitmap
import com.mobilicos.smotrofon.util.loadImage
import com.squareup.picasso.Picasso
import java.io.File

class LocalCoursesLessonsListAdapter(val listener: OnClickListItemElement<CourseLesson>) :
    PagingDataAdapter<CourseLesson, RecyclerView.ViewHolder>(LessonsListComparator) {

    lateinit var context: Context
    private var lessonsIdList: List<Int> = listOf()

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

    object LessonsListComparator : DiffUtil.ItemCallback<CourseLesson>() {
        override fun areItemsTheSame(oldItem: CourseLesson, newItem: CourseLesson): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CourseLesson, newItem: CourseLesson): Boolean {
            return oldItem.title == newItem.title
        }
    }

    inner class LessonsListViewHolder(private val binding: CourseLessonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun bindElement(item: CourseLesson, position: Int) = with(binding) {

            root.setOnClickListener {
                listener.clickOnListItemElement(element = item, position = position)
            }

            poster.loadBitmap(getImageBitmap(item))
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
        fun bindElement(item: CourseLesson, position: Int) = with(binding) {

            root.setOnClickListener {
                listener.clickOnListItemElement(element = item, position = position)
            }

            poster.loadBitmap(getImageBitmap(item))
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

    private fun getImageBitmap(item: CourseLesson): Bitmap? {
        var imgFile = File(
            FileUtil.getStorageFile(context),
            "ci_" + item.ident.toString() + "_icon${item.imageExtension}"
        )
        if (!imgFile.exists()) {
            when (item.imageExtension) {
                ".png" ->  imgFile = File(
                    FileUtil.getStorageFile(context),
                    "ci_" + item.ident.toString() + "_icon.jpg"
                )
                ".jpg" -> imgFile = File(
                    FileUtil.getStorageFile(context),
                    "ci_" + item.ident.toString() + "_icon.png"
                )
            }
        }

        var bm: Bitmap? = null
        if (imgFile.exists()) {
            bm = BitmapFactory.decodeFile(imgFile.absolutePath)
        }

        return bm
    }
}
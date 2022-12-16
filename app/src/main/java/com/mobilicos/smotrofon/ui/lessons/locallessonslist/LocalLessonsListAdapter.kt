package com.mobilicos.smotrofon.ui.lessons.locallessonslist

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mobilicos.smotrofon.data.models.Item
import com.mobilicos.smotrofon.databinding.ItemGreedLessonBinding
import com.mobilicos.smotrofon.databinding.ItemGreedLoadedLessonBinding
import com.mobilicos.smotrofon.ui.interfaces.OnClickListItemElement
import com.mobilicos.smotrofon.util.FileUtil
import com.mobilicos.smotrofon.util.loadBitmap
import java.io.File

class LocalLessonsListAdapter(val listener: OnClickListItemElement<Item>) :
    PagingDataAdapter<Item, RecyclerView.ViewHolder>(LessonsListComparator) {

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
                ItemGreedLessonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        } else {
            LessonsListLoadedViewHolder(
                ItemGreedLoadedLessonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    object LessonsListComparator : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.title == newItem.title
        }
    }

    inner class LessonsListViewHolder(private val binding: ItemGreedLessonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun bindElement(item: Item, position: Int) = with(binding) {

            root.setOnClickListener {
                listener.clickOnListItemElement(element = item, position = position)
            }

            poster.loadBitmap(getImageBitmap(item))
            title.text = item.title
        }
    }

    inner class LessonsListLoadedViewHolder(private val binding: ItemGreedLoadedLessonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun bindElement(item: Item, position: Int) = with(binding) {

            root.setOnClickListener {
                listener.clickOnListItemElement(element = item, position = position)
            }

            poster.loadBitmap(getImageBitmap(item))
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

    private fun getImageBitmap(item: Item): Bitmap? {
        var imgFile = File(
            FileUtil.getStorageFile(context),
            "i_" + item.ident.toString() + "_icon${item.iconExtension}"
        )
        if (!imgFile.exists()) {
            when (item.iconExtension) {
                ".png" ->  imgFile = File(
                    FileUtil.getStorageFile(context),
                    "i_" + item.ident.toString() + "_icon.jpg"
                )
                ".jpg" -> imgFile = File(
                    FileUtil.getStorageFile(context),
                    "i_" + item.ident.toString() + "_icon.png"
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
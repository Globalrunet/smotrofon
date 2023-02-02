package com.mobilicos.smotrofon.ui.media.list

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
import com.mobilicos.smotrofon.databinding.ItemVideoBinding
import com.mobilicos.smotrofon.ui.lessons.comments.OptionsMenuClickListener
import com.mobilicos.smotrofon.util.CircleTransform
import com.mobilicos.smotrofon.util.loadImage
import com.mobilicos.smotrofon.util.visible
import com.squareup.picasso.Picasso
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


interface OnClickMediaListElement {
    fun clickOnMediaListElement(media: Media, type: Int=0)
}

class MediaListAdapter(private val cl: OnClickMediaListElement,
                       private var menuClickListener: OptionsMenuClickListener<Media>? = null) :
    PagingDataAdapter<Media, MediaListAdapter.MediaListViewHolder>(MediaListComparator) {

    var currentUser: Int = 0
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

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
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
                cl.clickOnMediaListElement(item, 1)
            }


            if (currentUser != item.user_id) {
                binding.more.visible(true)
                binding.more.setOnClickListener {

//                    val wrapper: Context = ContextThemeWrapper(it.context, R.style.SettingsPopupMenu)

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
                                    menuClickListener?.onOptionsMenuReportClicked(item = item, view = binding.root)
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
}
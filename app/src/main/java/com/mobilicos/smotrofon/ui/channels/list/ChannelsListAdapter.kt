package com.mobilicos.smotrofon.ui.channels.list

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
import com.mobilicos.smotrofon.data.models.Channel
import com.mobilicos.smotrofon.databinding.ItemChannelBinding
import com.mobilicos.smotrofon.ui.interfaces.OnClickListItemElement
import com.mobilicos.smotrofon.ui.lessons.comments.OptionsMenuClickListener
import com.mobilicos.smotrofon.util.CircleTransform
import com.mobilicos.smotrofon.util.visible
import com.squareup.picasso.Picasso


class ChannelsListAdapter(val listener: OnClickListItemElement<Channel>,
                          private var menuClickListener: OptionsMenuClickListener<Channel>? = null) :
    PagingDataAdapter<Channel, ChannelsListAdapter.ChannelsListViewHolder>(diffCallback= ChannelsListComparator) {

    var currentUser: Int = 0
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

//            videoElement.text = context.getString(R.string.videos)
            videoElement.setOnClickListener {
                listener.clickOnListItemElement(item, 0)
            }

//            audioElement.text = context.getString(R.string.audio)
            audioElement.setOnClickListener {
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
            binding.userInfo.text = context.getString(R.string.subscribers_count, item.user_subscribers_count.toString())

            if (currentUser != item.id) {
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
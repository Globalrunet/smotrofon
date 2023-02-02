package com.mobilicos.smotrofon.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobilicos.smotrofon.data.models.Media
import com.mobilicos.smotrofon.databinding.RelatedVideoItemBinding
import com.mobilicos.smotrofon.ui.media.list.OnClickMediaListElement
import com.mobilicos.smotrofon.util.CircleTransform
import com.mobilicos.smotrofon.util.loadImage
import com.squareup.picasso.Picasso

interface OnVideoListDataLoaded {
    fun onVideoListDataLoaded()
}

class RelatedMediaListRecyclerAdapter(
    elements: List<Media>,
    cl: OnClickMediaListElement
)
    : RecyclerView.Adapter<RelatedMediaListRecyclerAdapter.RelatedVideoListViewHolder>() {

    var el: List<Media> = elements
    var pcl: OnClickMediaListElement = cl
    var context: Context? = null

    class RelatedVideoListViewHolder(private val binding: RelatedVideoItemBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bindVideoItem(item: Media, pcl: OnClickMediaListElement) = with (binding) {
            poster.loadImage(item.poster)
            poster.setOnClickListener {
                pcl.clickOnMediaListElement(item)
            }
            descriptionBlock.setOnClickListener {
                pcl.clickOnMediaListElement(item)
            }
            title.text = item.title
            description.text = "${item.user_full_name} / ${item.views_count} / ${item.date_added}"

            Picasso.get()
                .load(item.user_icon).transform(CircleTransform())
                .into(icon)
            icon.setOnClickListener {
                println("RESULT PRINT PICASSO");
                pcl.clickOnMediaListElement(media=item, type=1)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelatedVideoListViewHolder {
        return RelatedVideoListViewHolder(
            RelatedVideoItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RelatedVideoListViewHolder, position: Int) {
        val item = el[position]
        item.let {
            holder.bindVideoItem(it, pcl)
        }
    }

    override fun getItemCount(): Int {
        return el.size
    }
}
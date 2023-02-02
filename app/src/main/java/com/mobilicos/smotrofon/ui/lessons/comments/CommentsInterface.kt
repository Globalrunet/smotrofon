package com.mobilicos.smotrofon.ui.lessons.comments

import android.view.View
import com.mobilicos.smotrofon.data.models.Comment

interface CommentsInterface {
    fun clickOnCommentRemove(position: Int, element: Comment)

    fun clickOnCommentEdit(position: Int, element: Comment)

    fun clickOnLikeButton(position: Int, element: Comment)

    fun clickOnDislikeButton(position: Int, element: Comment)

    fun setTotalCommentsCounter(counter: Int)
}

interface OptionsMenuClickListener<T> {
    fun onOptionsMenuBlockClicked(item: T, view: View? = null)

    fun onOptionsMenuReportClicked(item: T, view: View? = null)
}
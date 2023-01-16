package com.mobilicos.smotrofon.ui.lessons.comments

import com.mobilicos.smotrofon.data.models.Comment

interface CommentsInterface {
    fun clickOnCommentRemove(position: Int, element: Comment)

    fun clickOnCommentEdit(position: Int, element: Comment)

    fun clickOnLikeButton(position: Int, element: Comment)

    fun clickOnDislikeButton(position: Int, element: Comment)

    fun setTotalCommentsCounter(counter: Int)
}

interface OptionsMenuClickListener<T> {
    fun onOptionsMenuBlockClicked(item: T)

    fun onOptionsMenuComplaintClicked(item: T)
}
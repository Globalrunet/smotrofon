package com.mobilicos.smotrofon.ui.lessons.comments

import com.mobilicos.smotrofon.data.models.Comment

interface CommentsInterface {
    fun clickOnCommentRemove(position: Int, element: Comment)

    fun clickOnCommentEdit(position: Int, element: Comment)
}
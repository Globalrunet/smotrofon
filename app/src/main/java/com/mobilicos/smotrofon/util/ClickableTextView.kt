package com.mobilicos.smotrofon.util

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener


class ClickableTextView : androidx.appcompat.widget.AppCompatTextView, OnTouchListener {
    constructor(
        context: Context?, attrs: AttributeSet?,
        defStyle: Int
    ) : super(context!!, attrs, defStyle) {
        setup()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        setup()
    }

    constructor(context: Context?, checkableId: Int) : super(context!!) {
        setup()
    }

    constructor(context: Context?) : super(context!!) {
        setup()
    }

    private fun setup() {
        setOnTouchListener(this)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (hasOnClickListeners()) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> isSelected = true
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> isSelected = false
            }
        }

        // allow target view to handle click
        return false
    }
}
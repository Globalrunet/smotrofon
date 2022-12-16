package com.mobilicos.smotrofon.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.google.android.material.chip.Chip
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.ui.interfaces.OnClickBottomSheetListElement
import com.mobilicos.smotrofon.util.visible


class BottomSheetSimpleListBaseAdapter(private val context: Context,
                                       private val items: List<String>,
                                       private var currentPosition: Int = 0) : BaseAdapter() {

    private var listener: OnClickBottomSheetListElement? = null

    fun setCurrentPosition(position: Int) {
        currentPosition = position
    }

    fun setListener(l: OnClickBottomSheetListElement) {
        listener = l
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val cv: View = convertView
            ?: LayoutInflater.from(context).inflate(R.layout.simple_list_element_with_radio, parent, false)

        val titleView = cv.findViewById<TextView>(R.id.title)
        val checkbox = cv.findViewById<CheckBox>(R.id.checkbox)

        checkbox.isChecked = (currentPosition == position)
        checkbox.visible(currentPosition == position)

        titleView.text = items[position]

        cv.setOnClickListener {
            if (!(position == 0 && currentPosition ==0)) {
                checkbox.isChecked = !checkbox.isChecked
                checkbox.visible(checkbox.isChecked)

                listener?.clickOnBottomSheetListElement(position, checkbox.isChecked)
            }

        }

        return cv
    }
}
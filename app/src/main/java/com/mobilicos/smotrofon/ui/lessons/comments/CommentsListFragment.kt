package com.mobilicos.smotrofon.ui.lessons.comments

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.ViewModelProvider
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.databinding.BottomSheetLayoutBinding
import com.mobilicos.smotrofon.ui.viewmodels.MediaViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class CommentsListFragment : BottomSheetDialogFragment() {

    lateinit var binding: BottomSheetLayoutBinding
    override fun getTheme() = R.style.AppBottomSheetDialogTheme



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = BottomSheetLayoutBinding.inflate(layoutInflater, container, false)

        val video = ViewModelProvider(requireActivity())[MediaViewModel::class.java].getSelected().value!!

        binding.title.text = video.title
        binding.description.text = video.text
        binding.iconClearDown.setOnClickListener {
            dialog?.hide()
            dialog?.dismiss()
        }

        dialog?.setOnShowListener {
            val height: Int = Resources.getSystem().displayMetrics.heightPixels
            val dialog = it as BottomSheetDialog
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.maxHeight = height
            behavior.peekHeight = height*50/100
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behavior = BottomSheetBehavior.from(bottomSheet)

            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        dismiss()
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    binding.iconClearDown.rotation = slideOffset * 180
                }
            })
        }
    }
}
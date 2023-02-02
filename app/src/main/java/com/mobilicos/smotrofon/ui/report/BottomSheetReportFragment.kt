package com.mobilicos.smotrofon.ui.report

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mobilicos.smotrofon.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.mobilicos.smotrofon.databinding.BottomSheetReportFragmentBinding
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.ui.interfaces.OnClickBottomSheetListElement
import com.mobilicos.smotrofon.util.visible


class BottomSheetReportFragment : BottomSheetDialogFragment() {

    private var sharedPref: SharedPreferences? = null
    private val viewModel: ReportViewModel by activityViewModels()
    lateinit var binding: BottomSheetReportFragmentBinding

    override fun getTheme() = R.style.AppBottomSheetDialogTheme

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomSheetReportFragmentBinding.inflate(layoutInflater, container, false)

        sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)

        binding.iconClearDown.setOnClickListener {
            dialog?.hide()
            dismiss()
        }

        dialog?.setOnShowListener {
            val dialog = it as BottomSheetDialog
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        val listView = binding.bottomSheetListMain
        val reasons = resources.getStringArray(R.array.report_reasons)

        val adapter = BottomSheetReportSimpleListBaseAdapter(context = this.requireContext(),
            items = reasons.asList(), viewModel.currentReportReasonPosition)

        adapter.setListener(l = object: OnClickBottomSheetListElement {
            override fun clickOnBottomSheetListElement(position: Int, checked: Boolean) {
                if (checked) {
                    adapter.setCurrentPosition(position)
                } else {
                    adapter.setCurrentPosition(-1)
                }

                adapter.notifyDataSetChanged()

                viewModel.currentReportReasonPosition = position
                viewModel.addReport()
            }
        })

        listView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addReportCollect()
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

    private fun addReportCollect() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reportResult.collect {
                    when (it.status) {
                        Result.Status.LOADING -> {
                            binding.prependProgress.visible(true)
                        }
                        Result.Status.ERROR -> {
                            binding.prependProgress.visible(false)
                            showMessage(activity?.getString(R.string.report_add_error))
                            viewModel.setReportAddResult(false)
                            viewModel.clearReportResult()
                        }
                        Result.Status.SUCCESS -> {
                            if (it.data != null && it.data.result) {
                                binding.prependProgress.visible(false)
                                viewModel.setReportAddResult(true)
                                viewModel.clearReportResult()
                                dismiss()
                            } else {
                                showMessage(activity?.getString(R.string.report_add_error))
                                binding.prependProgress.visible(false)
                                viewModel.clearReportResult()
                            }
                        }
                        else -> {
                            binding.prependProgress.visible(false)
                        }
                    }
                }
            }
        }
    }

    private fun showMessage(msg: String?) {
        if (msg != null) {
            Snackbar.make(binding.root, msg, Snackbar.ANIMATION_MODE_SLIDE).setAction("OK!") {
            }.show()
        }
    }

    override fun dismiss() {
        super.dismiss()

        viewModel.appLabel = ""
        viewModel.model = ""
        viewModel.objectId = 0
        viewModel.currentReportReasonPosition = -1
    }
}
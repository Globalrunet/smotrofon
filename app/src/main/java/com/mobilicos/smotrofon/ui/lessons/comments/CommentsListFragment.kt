package com.mobilicos.smotrofon.ui.lessons.comments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface.OnShowListener
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.DisplayMetrics
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.databinding.BottomSheetFragmentListBinding
import com.mobilicos.smotrofon.databinding.LessonsListBinding
import com.mobilicos.smotrofon.ui.courses.contenttab.CoursesContentViewModel
import com.mobilicos.smotrofon.ui.courses.lessonslist.CoursesLessonsListAdapter
import com.mobilicos.smotrofon.ui.courses.lessonslist.CoursesLessonsListViewModel
import com.mobilicos.smotrofon.util.CircleTransform
import com.mobilicos.smotrofon.util.visible
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class CommentsListFragment : BottomSheetDialogFragment() {

    private var commentsListAdapter: CommentsListAdapter? = null
    lateinit var binding: BottomSheetFragmentListBinding
    override fun getTheme() = R.style.AppBottomSheetDialogTheme
    private val commentsListViewModel: CommentsListViewMode by viewModels()
    private var sharedPref: SharedPreferences? = null
    private var userId: Int = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)

        binding = BottomSheetFragmentListBinding.inflate(layoutInflater, container, false)
        binding.header.text = requireContext().getString(R.string.fragment_layout_comments_header, 123)

        binding.addCommentTextLabel.setOnClickListener {
            val bottomSheetDialog =  BottomSheetDialog(requireContext())
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_add_comment)
            val behavior = bottomSheetDialog.behavior
            val icon = bottomSheetDialog.findViewById<ImageView>(R.id.addCommentUserIcon)
            val send = bottomSheetDialog.findViewById<ImageButton>(R.id.send)
            val comment = bottomSheetDialog.findViewById<TextInputEditText>(R.id.addComment)

            if (userId > 0) {
                Picasso.get()
                    .load(sharedPref!!.getString("user_icon", "")).transform(CircleTransform())
                    .into(icon)
            }

            comment?.setOnTouchListener { v, event ->
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                }

                false
            }

            behavior.isDraggable = false

            bottomSheetDialog.show()
        }

        sharedPref?.let {
            userId = it.getInt("user_id", 0)
        }

        if (userId > 0) {
            Picasso.get()
                .load(sharedPref!!.getString("user_icon", "")).transform(CircleTransform())
                .into(binding.userIcon)
        }

        binding.iconClearDown.setOnClickListener {
            dialog?.hide()
            dialog?.dismiss()
        }

        setQuery()
        setSwipeRefreshAdapter()
        lessonsDataSet()


        return binding.root
    }

    fun setQuery() {
        commentsListViewModel.setQuery(app_label = "lesson", model = "item", object_id = 340, key = "sdfjljljljlsf")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {

            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { pl ->
                val behaviour = BottomSheetBehavior.from(pl)
                setupFullHeight(pl)
                behaviour.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }
        return dialog
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

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    private fun lessonsDataSet() {
        if (binding.recyclerView.adapter == null) {
            binding.swipeRefreshLayout.isRefreshing = true

            binding.recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = commentsListAdapter
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    commentsListViewModel.commentsByQueryData.collectLatest {
                        println("INSIDE COMMENTS BY QUERY DATA $it // adapter $commentsListAdapter")
                        commentsListAdapter?.submitData(it)
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    commentsListAdapter?.loadStateFlow?.collect {
                        binding.prependProgress.isVisible = it.source.prepend is LoadState.Loading
                        binding.appendProgress.isVisible = it.source.append is LoadState.Loading
                        binding.swipeRefreshLayout.isRefreshing = it.refresh is LoadState.Loading

                        if (it.source.refresh is LoadState.NotLoading
                            && it.append.endOfPaginationReached
                            && commentsListAdapter?.itemCount ?: 0 < 1
                        ) {
                            binding.recyclerView.visible(false)
                            binding.emptyView.visible(true)
                        } else {
                            binding.recyclerView.visible(true)
                            binding.emptyView.visible(false)
                        }
                    }
                }
            }
        }
    }

    private fun setSwipeRefreshAdapter() {
        if (commentsListAdapter == null) {
            commentsListAdapter = CommentsListAdapter()
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = true
            commentsListAdapter?.refresh()
        }
    }
}
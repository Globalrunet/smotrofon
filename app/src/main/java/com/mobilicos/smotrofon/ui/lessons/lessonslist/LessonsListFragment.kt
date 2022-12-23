package com.mobilicos.smotrofon.ui.lessons.lessonslist

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.ConfigurationCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.models.Lesson
import com.mobilicos.smotrofon.databinding.BestLessonsListBinding
import com.mobilicos.smotrofon.ui.adapters.BottomSheetSimpleListBaseAdapter
import com.mobilicos.smotrofon.ui.interfaces.OnClickBottomSheetListElement
import com.mobilicos.smotrofon.ui.interfaces.OnClickListItemElement
import com.mobilicos.smotrofon.ui.lessons.contenttab.LessonsContentFragmentDirections
import com.mobilicos.smotrofon.ui.lessons.contenttab.LessonsContentViewModel
import com.mobilicos.smotrofon.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlin.random.Random

@AndroidEntryPoint
class LessonsListFragment : Fragment(), MenuProvider, OnClickListItemElement<Lesson> {
    private var lessonsListAdapter: LessonsListAdapter? = null
    private var binding: BestLessonsListBinding? = null
    private val lessonsListViewModel: LessonsListViewModel by viewModels()
    private val lessonsContentViewModel: LessonsContentViewModel by activityViewModels()
    private lateinit var searchView: SearchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BestLessonsListBinding.inflate(layoutInflater, container, false)

        if (lessonsListViewModel.changedPosition > -1) {
            lessonsListViewModel.getLessonsIdList()
        }

        setCurrentProject()
        setLanguage()
        setSwipeRefreshAdapter()
        lessonsDataSet()
        setUIListeners()
        showPromoBottomSheet()

        return binding!!.root
    }

    private fun showPromoBottomSheet() {

        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val sharedStatus = sharedPref.getBoolean("promo_share_app_status", false)
        val rand =  Random(System.nanoTime()).nextInt(0, 3)

        if (sharedStatus || rand != 0) return

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val behavior = bottomSheetDialog.behavior
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_promo)
        bottomSheetDialog.behavior

        val closeButton = bottomSheetDialog.findViewById<ImageButton>(R.id.icon_clear_down)
        val shareButton = bottomSheetDialog.findViewById<MaterialButton>(R.id.share_button)

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetDialog.dismiss()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                closeButton?.let {
                    it.rotation = slideOffset * 180
                }
            }
        })

        closeButton?.let {
            it.setOnClickListener {
                if (behavior.state != BottomSheetBehavior.STATE_HIDDEN) {
                    behavior.state = BottomSheetBehavior.STATE_HIDDEN
                }

                bottomSheetDialog.dismiss()
            }
        }

        shareButton?.let {
            it.setOnClickListener {
                shareApp()

                val editor = sharedPref.edit()
                editor.putBoolean("promo_share_app_status", true)
                editor.apply()
            }
        }

        bottomSheetDialog.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setCurrentProject() {
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        lessonsListViewModel.setCurrentProjectIdent(
            project = sharedPref.getInt("best_lessons_projects_position", 0))
    }

    private fun lessonsDataSet() {
        if (binding!!.recyclerView.adapter == null) {
            binding!!.swipeRefreshLayout.isRefreshing = true

            binding!!.recyclerView.apply {
                layoutManager = GridLayoutManager(context, 2)
                adapter = lessonsListAdapter
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    lessonsListViewModel.lessonsIdList.collectLatest {
                        println("LESSONS IDS : ?? $it")
                        lessonsListAdapter?.updateLessonsIdList(it)
                        if (lessonsListViewModel.changedPosition > -1) {
                            lessonsListAdapter?.notifyItemChanged(lessonsListViewModel.changedPosition)
                            lessonsListViewModel.changedPosition = -1
                        }
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    lessonsListViewModel.lessonsByQueryData.collectLatest {
                        lessonsListAdapter?.submitData(it)
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    lessonsListAdapter?.loadStateFlow?.collect {
                        binding!!.prependProgress.isVisible = it.source.prepend is LoadState.Loading
                        binding!!.appendProgress.isVisible = it.source.append is LoadState.Loading
                        binding!!.swipeRefreshLayout.isRefreshing = it.refresh is LoadState.Loading

                        if (it.source.refresh is LoadState.NotLoading
                            && it.append.endOfPaginationReached
                            && lessonsListAdapter?.itemCount ?: 0 < 1
                        ) {
                            binding!!.recyclerView.visible(false)
                            binding!!.emptyView.visible(true)
                        } else {
                            binding!!.recyclerView.visible(true)
                            binding!!.emptyView.visible(false)
                        }
                    }
                }
            }
        }
    }

    private fun setLanguage() {
        val currentLanguageCode: String = ConfigurationCompat.getLocales(resources.configuration)[0]?.language?.lowercase()?: Config.DEFAULT_LANGUAGE
        lessonsListViewModel.setCurrentLanguage(language = currentLanguageCode.lowercase())
    }

    override fun clickOnListItemElement(element: Lesson, type: Int, position: Int) {
        lessonsListViewModel.changedPosition = position
        val action = LessonsContentFragmentDirections.actionLessonsToLessonInfo(commentsCounter = element.commentsCount, ident = element.ident, objectId = element.id)
        lessonsContentViewModel.currentTab = 0
        lessonsListViewModel.searchString = searchView.query.toString()
        println("RESULT ONCLICK ${lessonsListViewModel.searchString}")
        findNavController().navigate(action)
    }

    private fun setSwipeRefreshAdapter() {
        if (lessonsListAdapter == null) {
            lessonsListAdapter = LessonsListAdapter(this)
        }
        binding!!.swipeRefreshLayout.setOnRefreshListener {
            binding!!.swipeRefreshLayout.isRefreshing = true
            lessonsListAdapter?.refresh()
        }
    }

    private fun setUIListeners() {
        binding!!.share.setOnClickListener {
            shareApp()
        }

        binding!!.rate.setOnClickListener {
            rateApp()
        }
    }

    private fun shareApp() {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type="text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.mobilicos.smotrofon")
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app_title)))
    }

    private fun rateApp() {
        try {
            val url = "market://details?id=" + Config.APP_DESCRIPTOR
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            val url = "http://play.google.com/store/apps/details?id=" + Config.APP_DESCRIPTOR
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.best_lessons_menu, menu)

        val search = menu.findItem(R.id.search)
        searchView = search.actionView as SearchView
        searchView.maxWidth = Integer.MAX_VALUE
        searchView.queryHint = getString(R.string.query_hint_lesson_search)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                lessonsListViewModel.setQuery(query ?: "")

                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText == null || newText == "") {
                    lessonsListViewModel.searchString.isEmpty().let {
                        lessonsListViewModel.setQuery("")
                    }
                }

                return true
            }
        })

        if (lessonsListViewModel.searchString.isNotEmpty()) {
            searchView.setQuery(lessonsListViewModel.searchString, true)
            searchView.clearFocus()
            search.expandActionView()
        }
        println("ONCREATEMENU")
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

        when (menuItem.itemId) {
            R.id.tune -> {
                val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
                val currentPosition = sharedPref.getInt("best_lessons_projects_position", 0)

                val closeButton = requireActivity().findViewById<ImageButton>(R.id.icon_clear_down)
                val bottomSheet = requireActivity().findViewById<FrameLayout>(R.id.bottom_sheet)
                val sheetBehavior = BottomSheetBehavior.from(bottomSheet)
                val listView = requireActivity().findViewById<ListView>(R.id.bottom_sheet_list_main)
                val projects = resources.getStringArray(R.array.projects)

                sheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {}

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        closeButton.rotation = slideOffset * 180
                    }
                })

                val adapter = BottomSheetSimpleListBaseAdapter(context = this.requireContext(),
                    items = projects.asList(),
                    currentPosition = currentPosition)

                adapter.setListener(l = object: OnClickBottomSheetListElement {
                    override fun clickOnBottomSheetListElement(position: Int, checked: Boolean) {
                        sharedPref.edit().apply {
                            if (checked) {
                                putInt("best_lessons_projects_position", position)
                                adapter.setCurrentPosition(position)
                            } else {
                                putInt("best_lessons_projects_position", 0)
                                adapter.setCurrentPosition(0)
                            }
                            apply()
                            adapter.notifyDataSetChanged()
                            lessonsListViewModel.setCurrentProjectIdent(
                                project = sharedPref.getInt("best_lessons_projects_position", 0))
                        }
                        if (sheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
                            sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                        }
                    }
                })

                listView.adapter = adapter
                bottomSheet.visible(true)

                if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                    sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }

                closeButton.setOnClickListener(View.OnClickListener {
                    if (sheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
                        sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    }
                })
            }
            R.id.share -> {
                shareApp()
            }
            R.id.rate -> {
                rateApp()
            }
        }

        return true
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }
}
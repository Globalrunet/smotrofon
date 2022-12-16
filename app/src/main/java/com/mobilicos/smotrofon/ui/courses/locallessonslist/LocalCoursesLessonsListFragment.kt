package com.mobilicos.smotrofon.ui.courses.locallessonslist

import android.os.Bundle
import android.view.*
import android.widget.SearchView
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.models.CourseLesson
import com.mobilicos.smotrofon.data.models.CourseLessonListItem
import com.mobilicos.smotrofon.databinding.LessonsListBinding
import com.mobilicos.smotrofon.ui.courses.contenttab.CoursesContentFragmentDirections
import com.mobilicos.smotrofon.ui.courses.contenttab.CoursesContentViewModel
import com.mobilicos.smotrofon.ui.interfaces.OnClickListItemElement
import com.mobilicos.smotrofon.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class LocalCoursesLessonsListFragment : Fragment(), MenuProvider, OnClickListItemElement<CourseLesson> {

    private var lessonsCoursesLessonsListAdapter: LocalCoursesLessonsListAdapter? = null
    private var binding: LessonsListBinding? = null
    private val coursesContentViewModel: CoursesContentViewModel by activityViewModels()
    private val lessonsListViewModel: LocalCoursesLessonsListViewModel by viewModels()
    private lateinit var searchView: SearchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = LessonsListBinding.inflate(layoutInflater, container, false)

        println("LESSONS START")
        setLanguage()
        setSwipeRefreshAdapter()
        lessonsDataSet()

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun lessonsDataSet() {
        if (binding!!.recyclerView.adapter == null) {
            binding!!.swipeRefreshLayout.isRefreshing = true

            binding!!.recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = lessonsCoursesLessonsListAdapter
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    lessonsListViewModel.lessonsByQueryData.collectLatest {
                        println("LESSONS ${it}")
                        lessonsCoursesLessonsListAdapter?.submitData(it)
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    lessonsCoursesLessonsListAdapter?.loadStateFlow?.collect {
                        binding!!.prependProgress.isVisible = it.source.prepend is LoadState.Loading
                        binding!!.appendProgress.isVisible = it.source.append is LoadState.Loading
                        binding!!.swipeRefreshLayout.isRefreshing = it.refresh is LoadState.Loading

                        if (it.source.refresh is LoadState.NotLoading
                            && it.append.endOfPaginationReached
                            && lessonsCoursesLessonsListAdapter?.itemCount ?: 0 < 1
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

    private fun setSwipeRefreshAdapter() {
        if (lessonsCoursesLessonsListAdapter == null) {
            lessonsCoursesLessonsListAdapter = LocalCoursesLessonsListAdapter(this)
        }
        binding!!.swipeRefreshLayout.setOnRefreshListener {
            binding!!.swipeRefreshLayout.isRefreshing = true
            lessonsCoursesLessonsListAdapter?.refresh()
        }
    }

    override fun clickOnListItemElement(element: CourseLesson, type:Int, position:Int) {
        lessonsListViewModel.changedPosition = position
        coursesContentViewModel.currentTab = 1
        val action = CoursesContentFragmentDirections.actionLocalCoursesLessonsToCoursesLessonInfo(element.ident)
        lessonsListViewModel.searchString = searchView.query.toString()
        findNavController().navigate(action)
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.lessons_menu, menu)

        val search = menu.findItem(R.id.search)
        searchView = search.actionView as SearchView
        searchView.maxWidth = Integer.MAX_VALUE
        searchView.queryHint = getString(R.string.query_hint_lesson_search)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                lessonsListViewModel.setQuery(query ?: "")
                lessonsListViewModel.searchString = query ?: ""

                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText == null || newText == "") {
                    lessonsListViewModel.searchString.isEmpty().let {
                        lessonsListViewModel.setQuery("")
                        lessonsListViewModel.searchString = ""
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
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return true
    }

    private fun showError(msg: String) {
        Snackbar.make(binding!!.root, msg, Snackbar.LENGTH_INDEFINITE).setAction("DISMISS") {}.show()
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()

        lessonsListViewModel.emitData()
    }
}
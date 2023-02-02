package com.mobilicos.smotrofon.ui.media.list

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.models.Media
import com.mobilicos.smotrofon.data.models.Suggestion
import com.mobilicos.smotrofon.databinding.MediaListBinding
import com.mobilicos.smotrofon.ui.viewmodels.ListingViewModel
import kotlinx.coroutines.flow.*
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.util.StringUtils
import com.google.android.material.snackbar.Snackbar
import com.mobilicos.smotrofon.ui.lessons.comments.OptionsMenuClickListener
import com.mobilicos.smotrofon.ui.media.viewer.MediaViewerFragment
import com.mobilicos.smotrofon.ui.report.BottomSheetReportFragment
import com.mobilicos.smotrofon.ui.report.ReportViewModel
import com.mobilicos.smotrofon.util.hideKeyboard
import com.mobilicos.smotrofon.util.showKeyboard
import com.mobilicos.smotrofon.ui.media.viewer.MediaViewModel
import com.mobilicos.smotrofon.ui.channels.content.UserContentViewModel
import com.mobilicos.smotrofon.util.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MediaListFragment : Fragment(), OnClickMediaListElement,
    SearchView.OnQueryTextListener, OptionsMenuClickListener<Media> {

    private lateinit var mediaListAdapter: MediaListAdapter
    lateinit var list: ArrayList<String>
    lateinit var listAdapter: ArrayAdapter<String>
    private lateinit var binding: MediaListBinding
    private val viewModel: ListingViewModel by viewModels()
    private val mediaListViewModel: MediaListViewModel by viewModels()
    private val mediaViewModel: MediaViewModel by activityViewModels()
    private val reportViewModel: ReportViewModel by activityViewModels()
    private var sharedPref: SharedPreferences? = null
    private var userId: Int = 0
    private var userKey: String = ""
    private val appLabel: String = "video"
    private var model: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = MediaListBinding.inflate(layoutInflater, container, false)

        sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        sharedPref?.let {
            userKey = it.getString("key", "").toString()
            userId = it.getInt("user_id", 0)
        }

        model = when(mediaListViewModel.getContentType()) {
            Config.TYPE_VIDEO -> "video"
            Config.TYPE_AUDIO -> "audio"
            else -> ""
        }

        mediaSelectButtonsUI()
        mediaSelectButtonSetListeners()
        setSearchUIListeners()
        setSwipeRefreshAdapter()
        addReportCollect()
        mediaDataSet()
        subscribeUi()

        mediaListAdapter.currentUser = userId

        return binding.root
    }

    private fun mediaDataSet() {
        if (binding.recyclerView.adapter == null) {
            binding.swipeRefreshLayout.isRefreshing = true

            binding.recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = mediaListAdapter
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    mediaListViewModel.mediaByQueryData.collectLatest {
                        mediaListAdapter.submitData(it)
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    mediaListAdapter.loadStateFlow.collect {
                        binding.prependProgress.isVisible = it.source.prepend is LoadState.Loading
                        binding.appendProgress.isVisible = it.source.append is LoadState.Loading
                        binding.swipeRefreshLayout.isRefreshing = it.refresh is LoadState.Loading

                        if (it.source.refresh is LoadState.NotLoading
                            && it.append.endOfPaginationReached
                            && mediaListAdapter.itemCount < 1
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

    override fun clickOnMediaListElement(media: Media, type: Int) {
        if (type == 0) {
            mediaViewModel.select(media, mediaListViewModel.getContentType())
            mediaViewModel.setRelatedVideoListEmpty()
            mediaViewModel.currentGraph = "media"

            val descriptionFragment = MediaViewerFragment()
            descriptionFragment.show(requireActivity().supportFragmentManager, descriptionFragment.tag)
        } else {
            val userContentViewModel: UserContentViewModel by activityViewModels()

            userContentViewModel.currentUser = media.user_id
            userContentViewModel.currentTab = when(mediaListViewModel.getContentType()) {
                Config.TYPE_VIDEO -> 0
                Config.TYPE_AUDIO -> 1
                else -> 0
            }
            userContentViewModel.currentType = mediaListViewModel.getContentType()
            userContentViewModel.userFullName = media.user_full_name
            userContentViewModel.userImageUrl = media.user_icon
            userContentViewModel.userSubscribersCount = media.user_subscribers_count

            findNavController().navigate(R.id.action_media_to_userContent)
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        mediaListViewModel.setQuery(newText ?: "")

        return true
    }

    private fun populateListAdapter(newText: String?) {
        val filteredList: ArrayList<String> = ArrayList()

        if (newText != null && newText.isNotEmpty()) {
            if (mediaListViewModel.getContentType() == Config.TYPE_AUDIO) {
                viewModel.fetchAudioSuggestions(newText)
            } else {
                viewModel.fetchVideoSuggestions(newText)
            }
        } else {
            listAdapter.clear()
            listAdapter.addAll(filteredList)
            listAdapter.notifyDataSetChanged()
        }
    }

    private fun subscribeUi() {
        viewModel.videoSuggestionsList.observe(viewLifecycleOwner, Observer {

            val filteredList: ArrayList<String> = ArrayList()

            when (it.status) {
                Result.Status.SUCCESS -> {
                    it.data?.elements?.let {element ->

                        for (e: Suggestion in element) {
                            filteredList.add(StringUtils.trimTitle(str = e.title,
                                length = Config.SUGGESTION_TITLE_MAX_LENGTH))
                        }

                        listAdapter.clear()
                        listAdapter.addAll(filteredList)
                        listAdapter.notifyDataSetChanged()
                    }
                    binding.loadingSuggestions.visibility = View.GONE
                }
                Result.Status.ERROR -> {
                    it.message?.let {
                        showError(it)
                    }
                    binding.loadingSuggestions.visibility = View.GONE
                }
                Result.Status.LOADING -> {
                    binding.loadingSuggestions.visibility = View.VISIBLE
                }
                else -> {}
            }
        })
    }

    private fun mediaSelectButtonsUI() {
        if (mediaListViewModel.getContentType() == Config.TYPE_VIDEO) {
            binding.audioButton.setBackgroundResource(R.drawable.layout_rounded_bg_inactive)
            binding.audioButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.videoButton.setBackgroundResource(R.drawable.layout_rounded_bg_active)
            binding.videoButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        if (mediaListViewModel.getContentType() == Config.TYPE_AUDIO) {
            binding.audioButton.setBackgroundResource(R.drawable.layout_rounded_bg_active)
            binding.audioButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.videoButton.setBackgroundResource(R.drawable.layout_rounded_bg_inactive)
            binding.videoButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }

    private fun mediaSelectButtonSetListeners() {
        binding.audioButton.setOnClickListener { it as TextView
            mediaListViewModel.setContentType(Config.TYPE_AUDIO)

            it.setBackgroundResource(R.drawable.layout_rounded_bg_active)
            it.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.videoButton.setBackgroundResource(R.drawable.layout_rounded_bg_inactive)
            binding.videoButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        binding.videoButton.setOnClickListener { it as TextView
            mediaListViewModel.setContentType(Config.TYPE_VIDEO)

            it.setBackgroundResource(R.drawable.layout_rounded_bg_active)
            it.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding.audioButton.setBackgroundResource(R.drawable.layout_rounded_bg_inactive)
            binding.audioButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }

    private fun setSearchUIListeners() {
        binding.searchButton.setOnClickListener {
            if (binding.search.visibility == View.GONE) {
                binding.search.visibility = View.VISIBLE
                binding.search.requestFocus()
                binding.search.showKeyboard()
            } else {
                binding.search.visibility = View.GONE
                binding.search.setQuery("", true)
                binding.search.hideKeyboard()
            }
        }

        binding.search.setOnQueryTextListener(this)

        list = ArrayList()

        listAdapter = ArrayAdapter<String>(
            requireActivity(),
            android.R.layout.simple_list_item_1,
            list
        )

        binding.suggestions.adapter = listAdapter
        binding.suggestions.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as String
        }
    }

    private fun setSwipeRefreshAdapter() {
        mediaListAdapter = MediaListAdapter(cl = this, menuClickListener = this)
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = true
            mediaListAdapter.refresh()
        }
    }

    private fun addReportCollect() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                reportViewModel.reportAddResult.collect {
                    it?.let {
                        if (it) {
                            showMessage(activity?.getString(R.string.report_add_success))
                        } else {
                            showMessage(activity?.getString(R.string.report_add_error))
                        }
                        val v = view?.findViewById<View>(reportViewModel.reportViewId)
                        v?.visible(false)
                        reportViewModel.setReportAddResult(null)
                        reportViewModel.reportViewId = -1
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

    override fun onOptionsMenuBlockClicked(item: Media, view: View?) {
        showMessage(activity?.getString(R.string.user_blocked_success_message))
    }

    override fun onOptionsMenuReportClicked(item: Media, view: View?) {
        context?.let {

            val fragment = BottomSheetReportFragment()

            reportViewModel.appLabel = appLabel
            reportViewModel.model = model
            reportViewModel.objectId = item.id
            reportViewModel.key = userKey
            reportViewModel.reportViewId = view?.id ?: -1
            fragment.show(requireActivity().supportFragmentManager, fragment.tag)
        }
    }

    private fun showError(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_INDEFINITE).setAction("DISMISS") {
        }.show()
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
package com.mobilicos.smotrofon.ui.fragments

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
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.util.StringUtils
import com.google.android.material.snackbar.Snackbar
import com.mobilicos.smotrofon.ui.adapters.MediaListAdapter
import com.mobilicos.smotrofon.ui.adapters.OnClickMediaListElement
import com.mobilicos.smotrofon.util.hideKeyboard
import com.mobilicos.smotrofon.util.showKeyboard
import com.mobilicos.smotrofon.ui.viewmodels.MediaListViewModel
import com.mobilicos.smotrofon.ui.viewmodels.MediaViewModel
import com.mobilicos.smotrofon.ui.viewmodels.UserContentViewModel
import com.mobilicos.smotrofon.util.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MediaListFragment : Fragment(), OnClickMediaListElement, SearchView.OnQueryTextListener {

    lateinit var mediaListAdapter: MediaListAdapter
    lateinit var list: ArrayList<String>
    lateinit var listAdapter: ArrayAdapter<String>
    private var binding: MediaListBinding? = null
    private val viewModel: ListingViewModel by viewModels()
    private val mediaListViewModel: MediaListViewModel by viewModels()
    private val mediaViewModel: MediaViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = MediaListBinding.inflate(layoutInflater, container, false)

        mediaSelectButtonsUI()
        mediaSelectButtonSetListeners()
        setSearchUIListeners()
        setSwipeRefreshAdapter()

        mediaDataSet()
        subscribeUi()

        return binding!!.root
    }

    private fun mediaDataSet() {
        if (binding!!.recyclerView.adapter == null) {
            binding!!.swipeRefreshLayout.isRefreshing = true

            binding!!.recyclerView.apply {
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
                        binding!!.prependProgress.isVisible = it.source.prepend is LoadState.Loading
                        binding!!.appendProgress.isVisible = it.source.append is LoadState.Loading
                        binding!!.swipeRefreshLayout.isRefreshing = it.refresh is LoadState.Loading

                        if (it.source.refresh is LoadState.NotLoading
                            && it.append.endOfPaginationReached
                            && mediaListAdapter.itemCount < 1
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

    override fun clickOnMediaListElement(media: Media, type: Int) {
        if (type==0) {
            mediaViewModel.select(media, mediaListViewModel.getContentType())
            mediaViewModel.setRelatedVideoListEmpty()

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
                            filteredList.add(StringUtils.trimTitle(str=e.title, length = Config.SUGGESTION_TITLE_MAX_LENGTH))
                        }

                        listAdapter.clear()
                        listAdapter.addAll(filteredList)
                        listAdapter.notifyDataSetChanged()
                    }
                    binding!!.loadingSuggestions.visibility = View.GONE
                }

                Result.Status.ERROR -> {
                    it.message?.let {
                        showError(it)
                    }
                    binding!!.loadingSuggestions.visibility = View.GONE
                }

                Result.Status.LOADING -> {
                    binding!!.loadingSuggestions.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun mediaSelectButtonsUI() {
        if (mediaListViewModel.getContentType() == Config.TYPE_VIDEO) {
            binding!!.audioButton.setBackgroundResource(R.drawable.layout_rounded_bg_inactive)
            binding!!.audioButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding!!.videoButton.setBackgroundResource(R.drawable.layout_rounded_bg_active)
            binding!!.videoButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        if (mediaListViewModel.getContentType() == Config.TYPE_AUDIO) {
            binding!!.audioButton.setBackgroundResource(R.drawable.layout_rounded_bg_active)
            binding!!.audioButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding!!.videoButton.setBackgroundResource(R.drawable.layout_rounded_bg_inactive)
            binding!!.videoButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }

    private fun mediaSelectButtonSetListeners() {
        binding!!.audioButton.setOnClickListener { it as TextView
            mediaListViewModel.setContentType(Config.TYPE_AUDIO)

            it.setBackgroundResource(R.drawable.layout_rounded_bg_active)
            it.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding!!.videoButton.setBackgroundResource(R.drawable.layout_rounded_bg_inactive)
            binding!!.videoButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        binding!!.videoButton.setOnClickListener { it as TextView
            mediaListViewModel.setContentType(Config.TYPE_VIDEO)

            it.setBackgroundResource(R.drawable.layout_rounded_bg_active)
            it.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            binding!!.audioButton.setBackgroundResource(R.drawable.layout_rounded_bg_inactive)
            binding!!.audioButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }
    }

    private fun setSearchUIListeners() {
        binding!!.searchButton.setOnClickListener {
            if (binding!!.search.visibility == View.GONE) {
                binding!!.search.visibility = View.VISIBLE
                binding!!.search.requestFocus()
                binding!!.search.showKeyboard()
            } else {
                binding!!.search.visibility = View.GONE
                binding!!.search.setQuery("", true)
                binding!!.search.hideKeyboard()
            }
        }

        binding!!.search.setOnQueryTextListener(this)

        list = ArrayList()

        listAdapter = ArrayAdapter<String>(
            requireActivity(),
            android.R.layout.simple_list_item_1,
            list
        )

        binding!!.suggestions.adapter = listAdapter
        binding!!.suggestions.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as String
        }
    }

    private fun setSwipeRefreshAdapter() {
        mediaListAdapter = MediaListAdapter(this)
        binding!!.swipeRefreshLayout.setOnRefreshListener {
            binding!!.swipeRefreshLayout.isRefreshing = true
            mediaListAdapter.refresh()
        }
    }

    private fun showError(msg: String) {
        Snackbar.make(binding!!.root, msg, Snackbar.LENGTH_INDEFINITE).setAction("DISMISS") {
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
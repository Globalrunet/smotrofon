package com.mobilicos.smotrofon.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.data.models.Media
import com.mobilicos.smotrofon.databinding.UserVideoListBinding
import com.mobilicos.smotrofon.ui.adapters.MediaListAdapter
import com.mobilicos.smotrofon.ui.viewmodels.MediaViewModel
import com.mobilicos.smotrofon.ui.adapters.OnClickMediaListElement
import com.mobilicos.smotrofon.ui.viewmodels.UserContentViewModel
import com.mobilicos.smotrofon.util.visible
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class UserVideoListFragment : Fragment(), OnClickMediaListElement {

    private var binding: UserVideoListBinding? = null
    lateinit var videoViewModel: MediaViewModel
    lateinit var videoListAdapter: MediaListAdapter
    private val userContentViewModel: UserContentViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = UserVideoListBinding.inflate(layoutInflater, container, false)

        println("RESULT IN UVLF ${userContentViewModel.currentUser}")

        videoListAdapter = MediaListAdapter(this)

        binding!!.swipeRefreshLayout.setOnRefreshListener {
            binding!!.swipeRefreshLayout.isRefreshing = true
            videoListAdapter.refresh()
        }

        videoDataSet()

        return binding!!.root
    }

    private fun videoDataSet() {
        if (binding!!.recyclerView.adapter == null) {

            binding!!.swipeRefreshLayout.isRefreshing = true

            binding!!.recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = videoListAdapter
                setHasFixedSize(true)
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    userContentViewModel.getUserMediaListData(
                        user = userContentViewModel.currentUser,
                        type = Config.TYPE_VIDEO
                    )
                        .collectLatest {
//                        binding!!.recyclerView.smoothScrollToPosition(0)
                            videoListAdapter.submitData(it)
                            if (videoListAdapter.snapshot().items.isEmpty()) {
                                binding!!.emptyView.isVisible = true
                                binding!!.recyclerView.isVisible = false
                            } else {
                                binding!!.emptyView.isVisible = false
                                binding!!.recyclerView.isVisible = true
                            }
                        }
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    videoListAdapter.loadStateFlow.collect {
                        binding!!.prependProgress.isVisible = it.source.prepend is LoadState.Loading
                        binding!!.appendProgress.isVisible = it.source.append is LoadState.Loading
                        binding!!.swipeRefreshLayout.isRefreshing = it.refresh is LoadState.Loading

                        if (it.source.refresh is LoadState.NotLoading
                            && it.append.endOfPaginationReached
                            && videoListAdapter.itemCount < 1
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
        videoViewModel = ViewModelProvider(requireActivity())[MediaViewModel::class.java]
        videoViewModel.select(media=media, type= Config.TYPE_VIDEO)

        val descriptionFragment = MediaViewerFragment()
        descriptionFragment.show(requireActivity().supportFragmentManager, descriptionFragment.tag)
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
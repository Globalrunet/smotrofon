package com.mobilicos.smotrofon.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobilicos.smotrofon.R
import kotlinx.coroutines.flow.*
import com.google.android.material.snackbar.Snackbar
import com.mobilicos.smotrofon.data.models.Channel
import com.mobilicos.smotrofon.databinding.ChannelsListBinding
import com.mobilicos.smotrofon.ui.adapters.ChannelsListAdapter
import com.mobilicos.smotrofon.ui.interfaces.OnClickListItemElement
import com.mobilicos.smotrofon.ui.viewmodels.*
import com.mobilicos.smotrofon.util.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChannelsListFragment : Fragment(), OnClickListItemElement<Channel> {

    lateinit var channelsListAdapter: ChannelsListAdapter
    private var binding: ChannelsListBinding? = null
    private val channelsListViewModel: ChannelsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ChannelsListBinding.inflate(layoutInflater, container, false)

        setSwipeRefreshAdapter()
        channelsDataSet()

        return binding!!.root
    }

    private fun channelsDataSet() {
        if (binding!!.recyclerView.adapter == null) {
            binding!!.swipeRefreshLayout.isRefreshing = true

            binding!!.recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = channelsListAdapter
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    channelsListViewModel.channelsByQueryData.collectLatest {
                        channelsListAdapter.submitData(it)
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    channelsListAdapter.loadStateFlow.collect {
                        binding!!.prependProgress.isVisible = it.source.prepend is LoadState.Loading
                        binding!!.appendProgress.isVisible = it.source.append is LoadState.Loading
                        binding!!.swipeRefreshLayout.isRefreshing = it.refresh is LoadState.Loading

                        if (it.source.refresh is LoadState.NotLoading
                            && it.append.endOfPaginationReached
                            && channelsListAdapter.itemCount < 1
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

    override fun clickOnListItemElement(element: Channel, type:Int, position: Int) {
        val userContentViewModel: UserContentViewModel by activityViewModels()

        println("RESULT ${element.id}")
        userContentViewModel.currentUser = element.id
        userContentViewModel.currentTab = type
        userContentViewModel.currentType = type

        findNavController().navigate(R.id.action_channels_to_channelUserContent)
    }

    private fun setSwipeRefreshAdapter() {
        channelsListAdapter = ChannelsListAdapter(this)
        binding!!.swipeRefreshLayout.setOnRefreshListener {
            binding!!.swipeRefreshLayout.isRefreshing = true
            channelsListAdapter.refresh()
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
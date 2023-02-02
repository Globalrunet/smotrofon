package com.mobilicos.smotrofon.ui.channels.list

import android.content.Context
import android.content.SharedPreferences
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
import com.mobilicos.smotrofon.ui.channels.content.UserContentViewModel
import com.mobilicos.smotrofon.ui.interfaces.OnClickListItemElement
import com.mobilicos.smotrofon.ui.lessons.comments.OptionsMenuClickListener
import com.mobilicos.smotrofon.ui.report.BottomSheetReportFragment
import com.mobilicos.smotrofon.ui.report.ReportViewModel
import com.mobilicos.smotrofon.util.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChannelsListFragment : Fragment(), OnClickListItemElement<Channel>, OptionsMenuClickListener<Channel> {

    lateinit var channelsListAdapter: ChannelsListAdapter
    private lateinit var binding: ChannelsListBinding
    private val channelsListViewModel: ChannelsViewModel by viewModels()
    private val reportViewModel: ReportViewModel by activityViewModels()
    private var sharedPref: SharedPreferences? = null
    private var userId: Int = 0
    private var userKey: String = ""
    private val appLabel: String = "user"
    private var model: String = "user"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ChannelsListBinding.inflate(layoutInflater, container, false)

        sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        sharedPref?.let {
            userKey = it.getString("key", "").toString()
            userId = it.getInt("user_id", 0)
        }

        setSwipeRefreshAdapter()
        channelsDataSet()
        addReportCollect()

        return binding.root
    }

    private fun channelsDataSet() {
        if (binding.recyclerView.adapter == null) {
            binding.swipeRefreshLayout.isRefreshing = true

            binding.recyclerView.apply {
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
                        binding.prependProgress.isVisible = it.source.prepend is LoadState.Loading
                        binding.appendProgress.isVisible = it.source.append is LoadState.Loading
                        binding.swipeRefreshLayout.isRefreshing = it.refresh is LoadState.Loading

                        if (it.source.refresh is LoadState.NotLoading
                            && it.append.endOfPaginationReached
                            && channelsListAdapter.itemCount < 1
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

    override fun clickOnListItemElement(element: Channel, type:Int, position: Int) {
        val userContentViewModel: UserContentViewModel by activityViewModels()

        println("RESULT ${element.id}")
        userContentViewModel.currentUser = element.id
        userContentViewModel.currentTab = type
        userContentViewModel.currentType = type

        userContentViewModel.userFullName = element.title
        userContentViewModel.userImageUrl = element.image
        userContentViewModel.userSubscribersCount = element.user_subscribers_count

        findNavController().navigate(R.id.action_channels_to_channelUserContent)
    }

    private fun setSwipeRefreshAdapter() {
        channelsListAdapter = ChannelsListAdapter(listener = this, menuClickListener = this)
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = true
            channelsListAdapter.refresh()
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

    override fun onOptionsMenuBlockClicked(item: Channel, view: View?) {
        showMessage(activity?.getString(R.string.user_blocked_success_message))
    }

    override fun onOptionsMenuReportClicked(item: Channel, view: View?) {
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
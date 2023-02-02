package com.mobilicos.smotrofon.ui.user.audio.list

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.models.Audio
import com.mobilicos.smotrofon.databinding.ProfileAudiolistBinding
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.ui.interfaces.OnClickListItemElement
import com.mobilicos.smotrofon.ui.user.audio.edit.EditAudioViewModel
import com.mobilicos.smotrofon.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest


@AndroidEntryPoint
class ProfileAudioListFragment : Fragment(), MenuProvider, OnClickListItemElement<Audio> {
    private var mediaListAdapter: ProfileAudioListAdapter? = null
    private var binding: ProfileAudiolistBinding? = null
    private val mediaListViewModel: ProfileAudioListViewModel by viewModels()
    private val editVideoViewModel: EditAudioViewModel by activityViewModels()
    private lateinit var searchView: SearchView
    private var removeAlertDialog: AlertDialog? = null

    companion object {
        const val SHOW = 0
        const val REMOVE = 1
        const val EDIT = 2
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = ProfileAudiolistBinding.inflate(layoutInflater, container, false)

        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        mediaListViewModel.key = sharedPref?.getString("key", "") ?: ""
        mediaListViewModel.setQuery(query = "")

        setSwipeRefreshAdapter()
        lessonsDataSet()
        removeCollect()

        if (mediaListViewModel.isRemoveDialogShown) {
            mediaListViewModel.currentElement?.let {
                showRemoveDialog(element = it, position = mediaListViewModel.currentPosition)
            }
        }

        binding!!.addVideo.setOnClickListener {
            val action = ProfileAudioListFragmentDirections.actionAudioListToAddAudio()
            findNavController().navigate(action)
        }

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
                adapter = mediaListAdapter
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    mediaListViewModel.lessonsByQueryData.collectLatest {
                        mediaListAdapter?.submitData(it)
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    mediaListAdapter?.loadStateFlow?.collect {
                        binding!!.prependProgress.isVisible = it.source.prepend is LoadState.Loading
                        binding!!.appendProgress.isVisible = it.source.append is LoadState.Loading
                        binding!!.swipeRefreshLayout.isRefreshing = it.refresh is LoadState.Loading

                        if (it.source.refresh is LoadState.NotLoading
                            && it.append.endOfPaginationReached
                            && mediaListAdapter?.itemCount ?: 0 < 1
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

    private fun removeCollect() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaListViewModel.removeVideoResult.collect {
                    when (it.status) {
                        Result.Status.LOADING -> {
                            println("RESULT LOADING")
                            binding!!.prependProgress.visible(true)
                            binding!!.appendProgress.visible(true)
                        }
                        Result.Status.ERROR -> {
                            binding!!.prependProgress.visible(false)
                            binding!!.appendProgress.visible(false)
                            showMessage(requireActivity().getString(R.string.edit_audio_save_error))
                            mediaListViewModel.clearRemoveVideoResult()
                        }
                        Result.Status.SUCCESS -> {
                            if (it.data != null && it.data.result) {
                                showMessage(activity?.getString(R.string.remove_audio_success))
                            } else {
                                showMessage(requireActivity().getString(R.string.remove_audio_error))
                            }
                            binding!!.prependProgress.visible(false)
                            binding!!.appendProgress.visible(false)
                            mediaListViewModel.clearRemoveVideoResult()
                            mediaListAdapter?.refresh()
                        }
                        else -> {
                            binding!!.prependProgress.visible(false)
                            binding!!.appendProgress.visible(false)
                            mediaListViewModel.clearRemoveVideoResult()
                        }
                    }
                }
            }
        }
    }

    override fun clickOnListItemElement(element: Audio, type: Int, position: Int) {
        if (type == REMOVE) {
            showRemoveDialog(element = element, position = position)
        } else if (type == EDIT) {
            editVideoViewModel.currentVideoItem = element
            val action = ProfileAudioListFragmentDirections.actionAudioListToAudioEdit(element.id)
            findNavController().navigate(action)
        }
    }

    private fun showRemoveDialog(element: Audio, position: Int) {
        context?.let {

            mediaListViewModel.isRemoveDialogShown = true
            mediaListViewModel.currentElement = element
            mediaListViewModel.currentPosition = position
            val id = element.id

            removeAlertDialog = MaterialAlertDialogBuilder(it)
                .setTitle(resources.getString(R.string.dialog_remove_audio_title))
                .setMessage(element.title)
                .setNegativeButton(resources.getString(R.string.dialog_remove_negative_button_title)) { dialog, which ->
                    mediaListViewModel.isRemoveDialogShown = false
                    mediaListViewModel.currentElement = null
                    mediaListViewModel.currentPosition = -1
                }
                .setPositiveButton(resources.getString(R.string.dialog_remove_positive_button_title)) { dialog, which ->
                    mediaListViewModel.removeVideo(id = id)
                    mediaListViewModel.isRemoveDialogShown = false
                    mediaListViewModel.currentElement = null
                    mediaListViewModel.currentPosition = -1
                }
                .setCancelable(true)
                .setOnDismissListener {
                    mediaListViewModel.isRemoveDialogShown = false
                    mediaListViewModel.currentElement = null
                    mediaListViewModel.currentPosition = -1
                }
                .show()
        }
    }

    private fun setSwipeRefreshAdapter() {
        if (mediaListAdapter == null) {
            mediaListAdapter = ProfileAudioListAdapter(this)
        }
        binding!!.swipeRefreshLayout.setOnRefreshListener {
            binding!!.swipeRefreshLayout.isRefreshing = true
            mediaListAdapter?.refresh()
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.lessons_menu, menu)

        val search = menu.findItem(R.id.search)
        searchView = search.actionView as SearchView
        searchView.maxWidth = Integer.MAX_VALUE
        searchView.queryHint = getString(R.string.query_hint_lesson_search)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                mediaListViewModel.setQuery(query ?: "")
                mediaListViewModel.searchString = query ?: ""

                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText == null || newText == "") {
                    mediaListViewModel.searchString.isEmpty().let {
                        mediaListViewModel.setQuery("")
                    }
                }

                return true
            }
        })

        if (mediaListViewModel.searchString.isNotEmpty()) {
            searchView.setQuery(mediaListViewModel.searchString, true)
            searchView.clearFocus()
            search.expandActionView()
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }

    private fun showError(msg: String) {
        Snackbar.make(binding!!.root, msg, Snackbar.LENGTH_INDEFINITE).setAction("DISMISS") {
        }.show()
    }

    override fun onResume() {
        super.onResume()

        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        if (editVideoViewModel.needToRefreshData) {
            mediaListAdapter?.refresh()
            editVideoViewModel.needToRefreshData = false
        }
    }

    private fun showMessage(msg: String?) {
        if (msg != null) {
            Snackbar.make(binding!!.root, msg, Snackbar.ANIMATION_MODE_SLIDE).setAction("OK!") {
            }.show()
        }
    }
}
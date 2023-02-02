package com.mobilicos.smotrofon.ui.channels.content

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.databinding.UserChannelContentFragmentBinding
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.ui.media.viewer.MediaViewModel
import com.mobilicos.smotrofon.ui.report.BottomSheetReportFragment
import com.mobilicos.smotrofon.ui.report.ReportViewModel
import com.mobilicos.smotrofon.util.CircleTransform
import com.mobilicos.smotrofon.util.visible
import com.squareup.picasso.Picasso
import kotlinx.coroutines.delay


class UserContentFragment : Fragment(), MenuProvider {

    private lateinit var binding: UserChannelContentFragmentBinding
    private var sharedPref: SharedPreferences? = null
    private val userContentViewModel: UserContentViewModel by activityViewModels()
    private val reportViewModel: ReportViewModel by activityViewModels()
    private var userKey: String = ""
    private val appLabel: String = "auth"
    private var model: String = "user"
    private val mediaViewModel: MediaViewModel by activityViewModels()
    private var userId: Int = 0
    private var subscribedString: String = ""
    private var subscribedList: List<Int> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = UserChannelContentFragmentBinding.inflate(layoutInflater, container, false)

        sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        sharedPref?.let { it ->
            userKey = it.getString("key", "").toString()
            userId = it.getInt("user_id", 0)
            subscribedString = it.getString("subscribed_str", "").toString()
            subscribedList = subscribedString.split(";").filter { e -> e.isNotEmpty() }
                .map { e -> e.toInt() }
        }

        if (userContentViewModel.currentUser == userId) {
            binding.userSubscribe.visible(false)
        } else {
            binding.userSubscribe.visible(true)
            binding.userSubscribe.setOnClickListener {
                mediaViewModel.subscribeUser(key = userKey, otherUserId = userContentViewModel.currentUser)
            }

            if (userContentViewModel.currentUser in subscribedList) {
                binding.userSubscribe.text = getString(R.string.subscribed_title)
            } else {
                binding.userSubscribe.text = getString(R.string.subscribe_title)
            }
        }

        binding.subscribersCounter.text = getString(R.string.subscribers_count, userContentViewModel.userSubscribersCount.toString())

        makeUserUI()
        addReportCollect()
        subscribeUserCollect()

        return binding.root
    }

    private fun subscribeUserCollect() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaViewModel.subscribeUserResponseData.collect {
                    when (it.status) {
                        Result.Status.LOADING -> {
                            binding.prependProgress.visible(true)
                        }
                        Result.Status.ERROR -> {
                            binding.prependProgress.visible(false)
                            showMessage(activity?.getString(R.string.subscribe_user_error))
                            mediaViewModel.clearSubscribeUserResponseData()
                        }
                        Result.Status.SUCCESS -> {
                            if (it.data != null && it.data.result) {
                                if (it.data.subscribed) {
                                    binding.userSubscribe.text = getString(R.string.subscribed_title)
                                    showMessage(activity?.getString(R.string.subscribe_user_success))
                                } else {
                                    binding.userSubscribe.text = getString(R.string.subscribe_title)
                                    showMessage(activity?.getString(R.string.unsubscribe_user_success))
                                }
                                sharedPref?.edit()?.putString("subscribed_str", it.data.subscribed_str)
                                    ?.apply()
                                subscribedList = it.data.subscribed_str.split(";").filter { e -> e.isNotEmpty() }
                                    .map { e -> e.toInt() }
                            } else {
                                showMessage(activity?.getString(R.string.subscribe_user_error))
                            }
                            binding.prependProgress.visible(false)
                            delay(100)
                            mediaViewModel.clearSubscribeUserResponseData()
                        }
                        else -> {
                            binding.prependProgress.visible(false)
                            mediaViewModel.clearSubscribeUserResponseData()
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tabLayout: TabLayout = binding.tabLayout
        val demoCollectionAdapter = DemoCollectionAdapter(this)
        val viewPager: ViewPager2 = binding.pager
        viewPager.adapter = demoCollectionAdapter

        val tabTitles = ArrayList<String>()
        tabTitles.add(getString(R.string.videos))
        tabTitles.add(getString(R.string.audio))
        TabLayoutMediator(
            tabLayout, viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = tabTitles[position]
        }.attach()

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onResume() {
        super.onResume()

        val userContentViewModel: UserContentViewModel by activityViewModels()

        val tabLayout: TabLayout = binding.tabLayout
        val viewPager: ViewPager2 = binding.pager
        val tab = tabLayout.getTabAt(userContentViewModel.currentTab)
        tab!!.select()
        val smoothScroll = false

        viewPager.setCurrentItem(userContentViewModel.currentTab, smoothScroll)
        if (!smoothScroll) {
            viewPager.post { viewPager.requestTransform() }
        }
    }

    private fun makeUserUI() {
        if (userContentViewModel.currentUser > 0) {
            with (userContentViewModel) {
                Picasso.get()
                        .load(userImageUrl).transform(CircleTransform())
                    .into(binding.avatar)
                binding.username.text = userFullName
            }
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

    class DemoCollectionAdapter internal constructor(fragment: Fragment?) :
        FragmentStateAdapter(fragment!!) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> UserVideoListFragment()
                else -> UserAudioListFragment()
            }
        }

        override fun getItemCount(): Int {
            return 2
        }
    }

    private fun showMessage(msg: String?) {
        if (msg != null) {
            Snackbar.make(binding.root, msg, Snackbar.ANIMATION_MODE_SLIDE).setAction("OK!") {
            }.show()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        menuInflater.inflate(R.menu.menu_comments_options, menu)

//        NEED TO TEST THIS SOLUTION
//        menu.add(0, 1, 1, menuIconWithText(getResources().getDrawable(R.mipmap.user_2), getResources().getString(R.string.action_profile)));
//        menu.add(0, 2, 2, menuIconWithText(getResources().getDrawable(R.mipmap.add_user), getResources().getString(R.string.action_add_user)));
//        menu.add(0, 3, 3, menuIconWithText(getResources().getDrawable(R.mipmap.switch_profile), getResources().getString(R.string.action_switch_profile)));
//        menu.add(0, 4, 4, menuIconWithText(getResources().getDrawable(R.mipmap.logout), getResources().getString(R.string.action_sign_out)));
//        return true;

//        NOT WORKING HERE
//        try {
//            val fieldMPopup = Menu::class.java.getDeclaredField("mPopup")
//            fieldMPopup.isAccessible = true
//            val mPopup = fieldMPopup.get(menu)
//            mPopup.javaClass
//                .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
//                .invoke(mPopup, true)
//        } catch (e: Exception){ }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.complaint_comment -> {
                context?.let {
                    val fragment = BottomSheetReportFragment()

                    reportViewModel.appLabel = appLabel
                    reportViewModel.model = model
                    reportViewModel.objectId = userContentViewModel.currentUser
                    reportViewModel.key = userKey
                    reportViewModel.reportViewId = view?.id ?: -1
                    fragment.show(requireActivity().supportFragmentManager, fragment.tag)
                }
            }
            R.id.block_user -> {
                showMessage(activity?.getString(R.string.user_blocked_success_message))
            }
        }

        return false
    }
}
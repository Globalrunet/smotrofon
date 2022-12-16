package com.mobilicos.smotrofon.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.mobilicos.smotrofon.databinding.UserContentFragmentBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mobilicos.smotrofon.ui.viewmodels.UserContentViewModel

class UserContentFragment : Fragment() {

    private var binding: UserContentFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = UserContentFragmentBinding.inflate(layoutInflater, container, false)

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tabLayout: TabLayout = binding!!.tabLayout
        val demoCollectionAdapter = DemoCollectionAdapter(this)
        val viewPager: ViewPager2 = binding!!.pager
        viewPager.adapter = demoCollectionAdapter

        val tabTitles = ArrayList<String>()
        tabTitles.add("Видео")
        tabTitles.add("Аудио")
        TabLayoutMediator(
            tabLayout, viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun onResume() {
        super.onResume()

        val userContentViewModel: UserContentViewModel by activityViewModels()

        val tabLayout: TabLayout = binding!!.tabLayout
        val viewPager: ViewPager2 = binding!!.pager
        val tab = tabLayout.getTabAt(userContentViewModel.currentTab)
        tab!!.select()
        val smoothScroll = false

        println("RESULT FROM UCT ?? ${userContentViewModel.currentTab}")

        viewPager.setCurrentItem(userContentViewModel.currentTab, smoothScroll)
        if (!smoothScroll) {
            viewPager.post { viewPager.requestTransform() }
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
}
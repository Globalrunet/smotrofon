package com.mobilicos.smotrofon.ui.lessons.contenttab

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
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.ui.lessons.lessonslist.LessonsListFragment
import com.mobilicos.smotrofon.ui.lessons.locallessonslist.LocalLessonsListFragment

class LessonsContentFragment : Fragment() {

    private val lessonsContentViewModel: LessonsContentViewModel by activityViewModels()
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
        tabTitles.add(getString(R.string.courses_tab_title_all_items))
        tabTitles.add(getString(R.string.courses_tab_title_local_items))
        TabLayoutMediator(
            tabLayout, viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun onResume() {
        super.onResume()

        val tabLayout: TabLayout = binding!!.tabLayout
        val viewPager: ViewPager2 = binding!!.pager
        val tab = tabLayout.getTabAt(lessonsContentViewModel.currentTab)
        tab!!.select()
        val smoothScroll = false

        viewPager.setCurrentItem(lessonsContentViewModel.currentTab, smoothScroll)
        if (!smoothScroll) {
            viewPager.post { viewPager.requestTransform() }
        }
    }

    class DemoCollectionAdapter internal constructor(fragment: Fragment?) :
        FragmentStateAdapter(fragment!!) {
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> LessonsListFragment()
                else -> LocalLessonsListFragment()
            }
        }

        override fun getItemCount(): Int {
            return 2
        }
    }
}
package com.mobilicos.smotrofon.ui.mainnavhost

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.google.android.material.snackbar.Snackbar
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.databinding.FragmentMainBottomAppBarBinding
import com.mobilicos.smotrofon.ui.fragments.ChannelsListFragment
import com.mobilicos.smotrofon.ui.fragments.MediaListFragment
import com.mobilicos.smotrofon.ui.fragments.ui.MessengerFragment

class MainBottomAppBarFragment : Fragment() {

    private var binding: FragmentMainBottomAppBarBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBottomAppBarBinding.inflate(layoutInflater, container, false)

        if (savedInstanceState == null) {
            val fragment = MediaListFragment()
            val manager: FragmentManager = requireActivity().supportFragmentManager
            manager.beginTransaction().replace(R.id.bottom_app_bar_host, fragment).commit()
        }

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        binding!!.fab.setOnClickListener {
//            showMessage(requireActivity().getString(R.string.fragment_under_construction))
//        }

        binding!!.bottomNavigationView.setOnItemSelectedListener { item ->
            println("RESULT setOnItemSelectedListener $item")

            val manager: FragmentManager = requireActivity().supportFragmentManager

//            val fragment: Fragment = when(item.itemId) {
//                R.id.media -> {
//                    MediaListFragment()
//                }
//                R.id.channels -> {
//                    ChannelsListFragment()
//                }
//                R.id.four -> {
//                    MessengerFragment()
//                }
//                R.id.five -> {
//                    MessengerFragment()
//                }
//                else -> MessengerFragment()
//            }

            manager.commit {
                setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.slide_out
                )
//                replace(R.id.bottom_app_bar_host, fragment)
                addToBackStack("one")
            }

            true
        }

        binding!!.bottomNavigationView.background = null
    }

    private fun showMessage(msg: String) {
        Snackbar.make(binding!!.root, msg, Snackbar.LENGTH_INDEFINITE).setAction("OK!") {
        }.show()
    }
}
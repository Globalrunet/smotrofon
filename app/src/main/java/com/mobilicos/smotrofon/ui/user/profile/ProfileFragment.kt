package com.mobilicos.smotrofon.ui.user.profile

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomappbar.BottomAppBar
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.models.UserLogin
import com.mobilicos.smotrofon.databinding.FragmentProfileBinding
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.ui.media.viewer.MediaViewModel
import com.mobilicos.smotrofon.ui.user.login.LoginUserFragment
import com.mobilicos.smotrofon.util.CircleTransform
import com.mobilicos.smotrofon.util.visible
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay


@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var navController: NavController
    private lateinit var binding: FragmentProfileBinding
    private var sharedPref: SharedPreferences? = null
    private var userId: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        navController = findNavController()
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)

        sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        userId = sharedPref?.getInt("user_id", 0)

        val currentBackStackEntry = navController.currentBackStackEntry!!
        val savedStateHandle = currentBackStackEntry.savedStateHandle
        var isNavigated = false

        savedStateHandle.getLiveData<Boolean>(LoginUserFragment.LOGIN_SUCCESSFUL)
            .observe(currentBackStackEntry, Observer { success ->
                isNavigated = !success
            })

        if (isNavigated) {
            navigateToMain()
        } else if (userId != null && userId!! > 0) {
            makeUserUI()
        } else {
            navigateToLogin()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<BottomAppBar>(R.id.bottomNavigationView)?.visible(false)

        getUserDataCollect()
    }

    private fun navigateToMain() {
        navController.graph.setStartDestination(R.id.media)
        val navOptions = NavOptions.Builder()
            .setPopUpTo(navController.graph.startDestinationId, true)
            .build()
        navController.navigate(navController.graph.startDestinationId, null, navOptions)
    }

    private fun getUserDataCollect() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.getUserDataResponseData.collect {
                    when (it.status) {
                        Result.Status.SUCCESS -> {
                            if (it.data != null && it.data.result) {
                                sharedPref?.edit()?.putInt("subscribers_count",
                                    it.data.subscribers_count)
                                    ?.apply()
                                binding.subscribersCount.text = getString(R.string.subscribers_count,
                                    it.data.subscribers_count.toString())
                                binding.subscribersCount.visible(true)
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun makeUserUI() {
        sharedPref?.let {
            userId?.let { id ->
                profileViewModel.getUserData(id = id)
            }

            Picasso.get()
                .load(sharedPref!!.getString("user_icon", "")).transform(CircleTransform())
                .into(binding.avatar)
            binding.username.text = sharedPref!!.getString("user_full_name", "")
            binding.videoElement.text = getString(R.string.videos_count, sharedPref!!.getInt("videos_count", 0))
            binding.audioElement.text = getString(R.string.audios_count, sharedPref!!.getInt("audios_count", 0))
            binding.coursesElement.text = getString(R.string.courses_lessons_count, 0, 0)
        }

        with (binding) {
            videolist.setOnClickListener {
                val action = ProfileFragmentDirections.actionProfileToVideolist()
                findNavController().navigate(action)
            }

            audiolist.setOnClickListener {
                val action = ProfileFragmentDirections.actionProfileToAudiolist()
                findNavController().navigate(action)
            }

            settings.setOnClickListener {
                val action = ProfileFragmentDirections.actionProfileToSettings()
                findNavController().navigate(action)
            }

            logout.setOnClickListener {
                logout()
            }
        }
    }

    private fun navigateToLogin() {
        navController.navigate(R.id.loginScreen)
    }

    private fun logout() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        sharedPref?.edit()?.let {
            it.remove("key")
            it.remove("first_name")
            it.remove("last_name")
            it.remove("username")
            it.remove("email")
            it.remove("description")
            it.remove("phone")
            it.remove("site")
            it.remove("user_id")
            it.remove("user_icon")
            it.remove("videos_count")
            it.remove("audios_count")
            it.remove("user_full_name")
            it.apply()
        }

        navigateToLogin()
    }
}
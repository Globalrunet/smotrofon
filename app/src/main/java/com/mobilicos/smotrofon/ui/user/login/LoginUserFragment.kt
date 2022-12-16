package com.mobilicos.smotrofon.ui.user.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.models.UserLogin
import com.mobilicos.smotrofon.databinding.FragmentLoginBinding
import com.mobilicos.smotrofon.util.afterTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.ui.user.registration.RegistrationUserFragment
import com.mobilicos.smotrofon.util.visible

@AndroidEntryPoint
class LoginUserFragment : Fragment() {
    companion object {
        const val LOGIN_SUCCESSFUL: String = "LOGIN_SUCCESSFUL"
    }

    private lateinit var binding: FragmentLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var savedStateHandle: SavedStateHandle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(layoutInflater, container, false)

        savedStateHandle = findNavController().previousBackStackEntry!!.savedStateHandle
        savedStateHandle[LOGIN_SUCCESSFUL] = false

        val cbe = findNavController().currentBackStackEntry!!
        val ssh = findNavController().currentBackStackEntry!!.savedStateHandle

        ssh.getLiveData<Boolean>(RegistrationUserFragment.REGISTRATION_SUCCESSFUL)
            .observe(cbe, Observer {
                if (it) {
                    savedStateHandle[LOGIN_SUCCESSFUL] = true
                    findNavController().popBackStack()
                }
            })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        subscribeUI()
        loginFormStateCollect()
        loginCollect()
    }

    private fun subscribeUI() {
        binding.username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username = binding.username.text.toString(),
                password = binding.password.text.toString()
            )
        }

        binding.password.apply {
            this.afterTextChanged {
                loginViewModel.loginDataChanged(
                    username = binding.username.text.toString(),
                    password = binding.password.text.toString()
                )
            }

            this.setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        if (binding.login.isEnabled) {
                            loginViewModel.login(
                                binding.username.text.toString(),  binding.password.text.toString())
                        }
                }
                false
            }
        }

        binding.login.setOnClickListener {
            binding.loading.visibility = View.VISIBLE
            loginViewModel.login(binding.username.text.toString(), binding.password.text.toString())
        }

        binding.registration.setOnClickListener {
            val action = LoginUserFragmentDirections.actionLoginToRegistration()
            findNavController().navigate(action)
        }
    }

    private fun loginFormStateCollect() {
        lifecycleScope.launch {
            loginViewModel.loginFormState.collect {

                binding.login.isEnabled = it.isDataValid
                if (it.usernameError != null) {
                    binding.usernameLayout.error = requireActivity().getString(it.usernameError)
                } else {
                    binding.usernameLayout.error = null
                }

                if (it.passwordError != null) {
                    binding.passwordLayout.error = requireActivity().getString(it.passwordError)
                } else {
                    binding.passwordLayout.error = null
                }
            }
        }
    }

    private fun loginCollect() {
        lifecycleScope.launch {
            loginViewModel.loginResult.collect() {
                println("RESULT ${it.data}")
                when (it.status) {
                    Result.Status.LOADING -> binding.loading.visible(true)
                    Result.Status.ERROR -> {
                        showMessage(requireActivity().getString(R.string.login_error_other))
                        binding.loading.visible(false)
                    }
                    Result.Status.SUCCESS -> {
                        if (it.data != null && it.data.result) {
                            showMessage(activity?.getString(R.string.login_success))
                            saveUserData(it.data)
                            savedStateHandle[LOGIN_SUCCESSFUL] = true
                            findNavController().popBackStack()
                        } else {
                            showMessage(requireActivity().getString(R.string.login_error))
                        }
                        binding.loading.visible(false)
                    }
                    else -> binding.loading.visible(false)
                }
            }
        }
    }

    private fun saveUserData(data: UserLogin?) {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        with (sharedPref?.edit()) {
            if (this != null && data != null) {
                putString("key", data.key)
                putString("username", data.username)
                putString("firstname", data.firstname)
                putString("lastname", data.lastname)
                putString("email", data.email)
                putString("description", data.description)
                putString("phone", data.phone)
                putString("site", data.site)
                putInt("user_id", data.user_id)
                putString("user_icon", data.user_icon)
                putInt("videos_count", data.videos_count)
                putInt("audios_count", data.audios_count)
                putString("user_full_name", data.user_full_name)
                apply()
            }
        }
    }

    private fun showMessage(msg: String?) {
        if (msg != null) {
            Snackbar.make(binding.root, msg, Snackbar.LENGTH_INDEFINITE).setAction("OK!") {
            }.show()
        }
    }
}
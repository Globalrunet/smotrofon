package com.mobilicos.smotrofon.ui.user.registration

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.models.UserLogin
import com.mobilicos.smotrofon.databinding.FragmentLoginBinding
import com.mobilicos.smotrofon.databinding.FragmentRegistrationBinding
import com.mobilicos.smotrofon.util.afterTextChanged
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.ui.user.license.LicenseFragmentDirections
import com.mobilicos.smotrofon.ui.user.login.LoginUserFragment
import com.mobilicos.smotrofon.ui.user.login.LoginUserFragmentDirections
import com.mobilicos.smotrofon.util.visible

@AndroidEntryPoint
class RegistrationUserFragment : Fragment() {
    companion object {
        const val REGISTRATION_SUCCESSFUL: String = "REGISTRATION_SUCCESSFUL"
    }

    private lateinit var binding: FragmentRegistrationBinding
    private val registrationViewModel: RegistrationViewModel by viewModels()
    private lateinit var savedStateHandle: SavedStateHandle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRegistrationBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        savedStateHandle = findNavController().previousBackStackEntry!!.savedStateHandle
        savedStateHandle[REGISTRATION_SUCCESSFUL] = false


        binding.license?.text = Html.fromHtml(String.format(getString(R.string.license_link)))
        binding.license?.setOnClickListener {
            val action = RegistrationUserFragmentDirections.actionRegistrationToLicense()
            findNavController().navigate(action)
        }

        subscribeUI()
        registrationFormStateCollect()
        registrationCollect()
    }

    private fun subscribeUI() {
        binding.firstname.afterTextChanged {
            formDataChanged()
        }

        binding.lastname.afterTextChanged {
            formDataChanged()
        }

        binding.email.afterTextChanged {
            formDataChanged()
        }

        binding.password.apply {
            this.afterTextChanged {
                formDataChanged()
            }

            this.setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        if (binding.registration.isEnabled) {
                            startRegistration()
                        }
                }
                false
            }
        }

        binding.registration.setOnClickListener {
            startRegistration()
        }
    }

    private fun startRegistration() {
        binding.loading.visibility = View.VISIBLE
        registrationViewModel.registration(
            binding.firstname.text.toString(),
            binding.lastname.text.toString(),
            binding.email.text.toString(),
            binding.password.text.toString()
        )
    }

    private fun formDataChanged() {
        registrationViewModel.registrationDataChanged(
            firstname = binding.firstname.text.toString(),
            lastname = binding.lastname.text.toString(),
            email = binding.email.text.toString(),
            password = binding.password.text.toString()
        )
    }

    private fun registrationFormStateCollect() {
        lifecycleScope.launch {
            registrationViewModel.registrationFormState.collect {

                binding.registration.isEnabled = it.isDataValid
                if (it.firstnameError != null) {
                    binding.firstnameLayout.error = requireActivity().getString(it.firstnameError)
                } else {
                    binding.firstnameLayout.error = null
                }

                if (it.lastnameError != null) {
                    binding.lastnameLayout.error = requireActivity().getString(it.lastnameError)
                } else {
                    binding.lastnameLayout.error = null
                }

                if (it.emailError != null) {
                    binding.emailLayout.error = requireActivity().getString(it.emailError)
                } else {
                    binding.emailLayout.error = null
                }

                if (it.passwordError != null) {
                    binding.passwordLayout.error = requireActivity().getString(it.passwordError)
                } else {
                    binding.passwordLayout.error = null
                }
            }
        }
    }

    private fun registrationCollect() {
        lifecycleScope.launch {
            registrationViewModel.registrationResult.collect() {
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

                            savedStateHandle[REGISTRATION_SUCCESSFUL] = true
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
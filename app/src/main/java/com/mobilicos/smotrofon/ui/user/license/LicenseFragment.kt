package com.mobilicos.smotrofon.ui.user.license

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mobilicos.smotrofon.databinding.FragmentLicenseBinding
import com.mobilicos.smotrofon.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LicenseFragment : Fragment() {

    private lateinit var binding: FragmentLicenseBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentLicenseBinding.inflate(layoutInflater, container, false)

        val data = readFileText("license.html")
        binding.license.loadData(data, "text/html", "en_US");

        return binding.root
    }

    fun readFileText(fileName: String): String {
        return requireActivity().assets.open(fileName).bufferedReader().use { it.readText() }
    }
}
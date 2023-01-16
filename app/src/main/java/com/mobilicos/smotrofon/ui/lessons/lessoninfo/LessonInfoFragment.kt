package com.mobilicos.smotrofon.ui.lessons.lessoninfo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.appodeal.ads.Appodeal
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.LessonRepository.DownloadState
import com.mobilicos.smotrofon.databinding.LessonInfoFragmentBinding
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.ui.lessons.comments.CommentsListFragment
import com.mobilicos.smotrofon.util.FileUtil
import com.mobilicos.smotrofon.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random


@AndroidEntryPoint
class LessonInfoFragment : Fragment() {

    private lateinit var binding: LessonInfoFragmentBinding
    private val lessonInfoViewModel: LessonInfoViewModel by viewModels()
    private val args: LessonInfoFragmentArgs by navArgs()
    private var ident: Int = 0
    private var elementId: Int = 0
    private var initialCommentsCount: Int = 0
    private var interstitial: InterstitialAd? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = LessonInfoFragmentBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialCommentsCount = args.commentsCounter
        ident = args.ident
        elementId = args.objectId

        val currentLanguageCode: String = ConfigurationCompat.getLocales(resources.configuration)[0]?.language?:"EN"
        val language = if (currentLanguageCode.lowercase() == "ru") "ru" else "en"
        lessonInfoViewModel.setCurrentLanguage(language)

        lessonInfoViewModel.setFileToSaveFiles(FileUtil.getStorageFile(requireContext()))

        val isJsonFileExists = File(FileUtil.getStorageFile(requireContext()),
            ident.toString() + "_$language.json").exists()

        if (!isJsonFileExists) {
            binding.begin.isEnabled = false
            lessonInfoViewModel.setFileToSaveFiles(FileUtil.getStorageFile(requireContext()))
//            downloadLessonInfoCollect()
            downloadLessonInfoStreamCollect()
            binding.loading.visible(true)
            binding.loadingProgress.visible(true)
            lessonInfoViewModel.downloadLessonByIdentStream(ident)
        } else {
            updateUI()
        }

        binding.begin.setOnClickListener {
            val action = LessonInfoFragmentDirections.actionLessonInfoToStepsInfo(ident = ident, objectId = elementId)
            findNavController().navigate(action)
            showAdmobInterstitial()
        }


        binding.comments.count = 0
        binding.comments.setOnClickListener {
            val commentsFragment = CommentsListFragment()

            val args = Bundle()
            args.putString("current_app_label", "lesson")
            args.putString("current_model", "item")
            args.putInt("current_object_id", elementId)
            commentsFragment.arguments = args

            commentsFragment.show(requireActivity().supportFragmentManager, commentsFragment.tag)
        }

        binding.comments.count = initialCommentsCount

        binding.like.count = 3
        binding.like.setOnClickListener {
            binding.like.increase()
        }

        initAdmobInterstitialAd()
    }

    private fun downloadLessonInfoCollect() {
        lifecycleScope.launch {
            lessonInfoViewModel.downloadLessonResult.collect() {
                println("RESULT ${it.status} // $ident")
                when (it.status) {
                    Result.Status.LOADING -> binding.loading.visible(true)
                    Result.Status.ERROR -> {
                        binding.loading.visible(false)
                    }
                    Result.Status.SUCCESS -> {
                        updateUI()
                        binding.loading.visible(false)
                    }
                    else -> binding.loading.visible(false)
                }
            }
        }
    }

    private fun downloadLessonInfoStreamCollect() {
        lifecycleScope.launch {
            lessonInfoViewModel.downloadLessonStreamResult.collect() {
                when (it) {
                    is DownloadState.Downloading -> {
                        binding.loading.visible(true)
                        binding.loadingProgress.visible(true)
                        binding.loadingProgress.text = getString(R.string.loading_progress_title, it.progress)
                        binding.loading.progress = it.progress
                    }
                    is DownloadState.Failed -> {
                        binding.loadingProgress.visible(false)
                        binding.loading.progress = 0
                        binding.loading.visible(false)
                    }
                    DownloadState.Finished -> {
                        updateUI()
                        binding.loadingProgress.visible(false)
                        binding.loading.progress = 0
                        binding.loading.visible(false)
                    }
                    else -> {
                        binding.loadingProgress.visible(false)
                        binding.loading.progress = 0
                        binding.loading.visible(false)
                    }
                }
            }
        }
    }

    private fun updateUI() {
        val lessonItem = lessonInfoViewModel.getItem(itemIdent = ident)

        var imgFile = File(
            FileUtil.getStorageFile(requireContext()),
            "i_" + ident.toString() + "_image${lessonItem?.item?.iconExtension}"
        )
        if (!imgFile.exists()) {
            when (lessonItem?.item?.iconExtension) {
                ".png" ->  imgFile = File(
                FileUtil.getStorageFile(requireContext()),
                "i_" + ident.toString() + "_image.jpg"
                )
                ".jpg" -> imgFile = File(
                FileUtil.getStorageFile(requireContext()),
                "i_" + ident.toString() + "_image.png"
                )
            }
        }

        var bm: Bitmap? = null
        if (imgFile.exists()) {
            bm = BitmapFactory.decodeFile(imgFile.absolutePath)
        }

        with (binding) {
            img.setImageBitmap(bm)
            begin.isEnabled = true
            description.text = lessonItem?.item?.text
        }

        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = lessonItem?.item?.title
    }

    private fun showAppodealInterstitial() {
        val rand =  Random(System.nanoTime()).nextInt(0, 2)

        if (rand != 0) return
        if(Appodeal.isLoaded(Appodeal.INTERSTITIAL)) {
            Appodeal.show(requireActivity(), Appodeal.INTERSTITIAL)
        }
    }

    private fun showAdmobInterstitial() {
        val rand =  Random(System.nanoTime()).nextInt(0, 2)

        if (rand != 0) return
        if (interstitial != null) interstitial?.show(requireActivity())
    }

    private fun initAdmobInterstitialAd() {
        try {
            val admobInterstitialId = Config.ADMOB_INTERSTITIAL_ID
            if (admobInterstitialId.isNotEmpty()) {
                val adRequest = AdRequest.Builder().build()
                InterstitialAd.load(requireActivity(), admobInterstitialId, adRequest,
                    object : InterstitialAdLoadCallback() {
                        override fun onAdLoaded(interstitialAd: InterstitialAd) {
                            Log.e("AD LOADED", "LOADED")
                            interstitial = interstitialAd
                        }

                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            Log.e("AD LOADED ERROR", loadAdError.message)
                            interstitial = null
                        }
                    })
            }
        } catch (e: Exception) {
            e.localizedMessage?.let { Log.e("Error:", it) }
        }
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
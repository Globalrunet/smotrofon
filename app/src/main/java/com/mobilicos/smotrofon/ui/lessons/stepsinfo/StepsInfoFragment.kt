package com.mobilicos.smotrofon.ui.lessons.stepsinfo

import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.appodeal.ads.Appodeal
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.models.LessonItem
import com.mobilicos.smotrofon.databinding.FragmentStepsInfoBinding
import com.mobilicos.smotrofon.ui.lessons.comments.CommentsListFragment
import com.mobilicos.smotrofon.util.FileUtil
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import dagger.hilt.android.AndroidEntryPoint
import java.io.BufferedReader
import java.io.File
import java.util.*
import kotlin.random.Random

@AndroidEntryPoint
class StepsInfoFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentStepsInfoBinding
    private val stepsInfoViewModel: StepsInfoViewModel by viewModels()
    private val args: StepsInfoFragmentArgs by navArgs()
    private var ident: Int = 0
    private var elementId: Int = 0
    private var initialCommentsCount: Int = 0
    private var animationInterval = 500L
    private var lessonItem: LessonItem? = null
    private var timer: Timer? = null
    private var descriptionContainerHeight: Int = 0
    private var interstitial: InterstitialAd? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initialCommentsCount = args.commentsCounter
        ident = args.ident
        elementId = args.objectId

        println("COMMENTS ARGS $initialCommentsCount // $ident // $elementId")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentStepsInfoBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.preview.setOnClickListener(this)
        binding.next.setOnClickListener(this)
        binding.replay.setOnClickListener(this)

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

        descriptionContainerHeight = binding.descriptionContainer.layoutParams.height

        val currentLanguageCode: String = ConfigurationCompat.getLocales(resources.configuration)[0]?.language?:"EN"

        var language = "en"
        if (currentLanguageCode.lowercase() == "ru") language = "ru"

        lessonItem = getItem(itemIdent = ident, language = language)
        stepsInfoViewModel.item = lessonItem?.item
        stepsInfoViewModel.currentFrame = 0
        updateUI()
        animateStepContent()
        initAdmobInterstitialAd()
    }

    private fun updateUI() {
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = lessonItem?.item?.title
    }

    private fun getItem(itemIdent: Int, language: String): LessonItem? {
        val bufferedReader: BufferedReader = File(
            FileUtil.getStorageFile(requireContext()),
            itemIdent.toString() + "_$language.json"
        ).bufferedReader()
        val inputString = bufferedReader.use { it.readText() }

        val moshi = Moshi.Builder().build()
        val jsonAdapter: JsonAdapter<LessonItem> = moshi.adapter(
            LessonItem::class.java
        )

        return jsonAdapter.fromJson(inputString)
    }

    override fun onClick(v: View) {
        timer?.cancel()

        if (v === binding.next) {
            stepsInfoViewModel.increaseStep()
        }
        if (v === binding.preview) {
            if (stepsInfoViewModel.currentStep > 0) {
                stepsInfoViewModel.decreaseStep()
            } else {
                findNavController().popBackStack()
            }
        }

        if (!stepsInfoViewModel.hasNextStep()) {
            showAdmobInterstitial()
        }

        stepsInfoViewModel.currentFrame = 0
        animateStepContent()
    }

    private fun animateStepContent() {
        val counterString =  requireContext().resources.getString(
            R.string.lesson_steo_of_steps,
            stepsInfoViewModel.currentStep + 1,
            stepsInfoViewModel.stepsCount()
        )
        binding.stepsCounter.text = counterString

        setDescription()

        println("RESULT ANIM ${stepsInfoViewModel.item?.animationInterval}")

        if (stepsInfoViewModel.item?.animationInterval ?: 0 > 0) {
            animationInterval = stepsInfoViewModel.item?.animationInterval!!
        }

        if (stepsInfoViewModel.framesCount() > 0) {
            timer = Timer()
            timer!!.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    activity?.runOnUiThread {
                        try {
                            if (stepsInfoViewModel.hasCurrentFrame()) {
                                var bm: Bitmap? = null
                                val file = File(
                                    FileUtil.getStorageFile(activity!!), stepsInfoViewModel.getCurrentFrameFileName()
                                )
                                println("RESULT // ${stepsInfoViewModel.getCurrentFrameFileName()}")
                                if (file.exists()) {
                                    bm = FileUtil.getResizedFileBitmap(file.absolutePath)
                                    if (bm != null) binding.img.setImageBitmap(bm)
                                }

                                // Image size
                                val displayMetrics = activity!!.resources.displayMetrics
                                val dpWidth = displayMetrics.widthPixels.toFloat()
                                val params: ViewGroup.LayoutParams = binding.img.layoutParams
                                params.width = dpWidth.toInt()
                                params.height = (dpWidth / bm!!.width * bm.height).toInt()
                                binding.img.layoutParams = params
                                // Image size

                                stepsInfoViewModel.increaseFrame()
                            } else {
                                timer?.cancel()
                                timer = null
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }, 0, animationInterval)
        }
    }

    private fun setDescription() {
        val textLength: Int = stepsInfoViewModel.getStepDescription().length

        if (textLength > 3) {
            binding.description.text = stepsInfoViewModel.getStepDescription()
            binding.descriptionContainer.visibility = View.VISIBLE
            val viewHeight: Int = when {
                textLength > 400 -> {
                    descriptionContainerHeight * 2
                }
                textLength > 200 -> {
                    descriptionContainerHeight
                }
                else -> {
                    descriptionContainerHeight / 2
                }
            }
            val anim = ValueAnimator.ofInt(0, viewHeight)
            anim.addUpdateListener { valueAnimator ->
                val value = valueAnimator.animatedValue as Int
                val layoutParams: ViewGroup.LayoutParams =
                    binding.descriptionContainer.layoutParams
                layoutParams.height = value
                binding.descriptionContainer.layoutParams = layoutParams
            }
            anim.duration = 500
            anim.start()
        } else {
            binding.description.text = ""
            binding.descriptionContainer.visibility = View.GONE
        }
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

    override fun onDestroyView() {
        super.onDestroyView()

        timer?.cancel()
        timer = null
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
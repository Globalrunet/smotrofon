package com.mobilicos.smotrofon.ui.courses.lessoninfo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.models.CourseLessonItem
import com.mobilicos.smotrofon.databinding.CoursesMultistepsLessonBinding
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.util.FileUtil
import com.mobilicos.smotrofon.util.loadBitmap
import com.mobilicos.smotrofon.util.visible
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File


@AndroidEntryPoint
class CourseLessonInfoFragment : Fragment() {

    private lateinit var binding: CoursesMultistepsLessonBinding
    private val lessonInfoViewModel: CourseLessonInfoViewModel by viewModels()
    private val args: CourseLessonInfoFragmentArgs by navArgs()
    private var ident: Int = 4000265

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = CoursesMultistepsLessonBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ident = args.ident

        val currentLanguageCode: String = ConfigurationCompat.getLocales(resources.configuration)[0]?.language?:"EN"
        val language = if (currentLanguageCode.lowercase() == "ru") "ru" else "en"
        lessonInfoViewModel.setCurrentLanguage(language)

        lessonInfoViewModel.setFileToSaveFiles(FileUtil.getStorageFile(requireContext()))

        val isJsonFileExists = File(FileUtil.getStorageFile(requireContext()),
            "c_" + ident.toString() + "_$language.json").exists()

        if (!isJsonFileExists) {
            downloadLessonInfoCollect()
            lessonInfoViewModel.downloadLessonByIdent(ident)
        } else {
            updateUI()
        }
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

    private fun updateUI() {

        val courseLessonItem = lessonInfoViewModel.getItem(itemIdent = ident)

        var imgFile = File(
            FileUtil.getStorageFile(requireContext()),
            "ci_" + ident.toString() + "_image${courseLessonItem?.item?.imageExtension}"
        )
        if (!imgFile.exists()) {
            when (courseLessonItem?.item?.imageExtension) {
                ".png" ->  imgFile = File(
                FileUtil.getStorageFile(requireContext()),
                "ci_" + ident.toString() + "_image.jpg"
                )
                ".jpg" -> imgFile = File(
                FileUtil.getStorageFile(requireContext()),
                "ci_" + ident.toString() + "_image.png"
                )
            }
        }

        var bm: Bitmap? = null
        if (imgFile.exists()) {
            bm = BitmapFactory.decodeFile(imgFile.absolutePath)
            if (bm != null) binding.lessonImage.loadBitmap(bm)
        }

        with (binding) {
            lessonImage.setImageBitmap(bm)
            name.text = courseLessonItem?.item?.title
            description.text = courseLessonItem?.item?.text
            stepsContainer.removeAllViews()

            for (i in 0 until courseLessonItem?.item?.steps?.size!!) {
                val inflater = LayoutInflater.from(context)
                val stepView = inflater.inflate(R.layout.lesson_step_info, null) as LinearLayout
                val step = courseLessonItem.item.steps[i]
                val name = stepView.findViewById<TextView>(R.id.name)
                val description = stepView.findViewById<TextView>(R.id.description)
                val tips = stepView.findViewById<TextView>(R.id.tips)
                val image = stepView.findViewById<ImageView>(R.id.lesson_step_image)

                name.text = step.title
                if (step.tips.isNotEmpty()) {
                    tips.text = step.tips
                    tips.visibility = View.VISIBLE
                } else {
                    tips.visibility = View.GONE
                }

                description.text = step.text

                imgFile = File(
                    FileUtil.getStorageFile(requireContext()),
                    "ci_" + ident.toString() + "_" + step.stepNumber + "_image${courseLessonItem.item.imageExtension}"
                )
                if (!imgFile.exists()) {
                    when (courseLessonItem.item.imageExtension) {
                        ".png" ->  imgFile = File(
                            FileUtil.getStorageFile(requireContext()),
                            "ci_" + ident.toString() + "_" + step.stepNumber + "_image.jpg"
                        )
                        ".jpg" -> imgFile = File(
                            FileUtil.getStorageFile(requireContext()),
                            "ci_" + ident.toString() + "_" + step.stepNumber + "_image.png"
                        )
                    }
                }

                if (imgFile.exists()) {
                    bm = BitmapFactory.decodeFile(imgFile.absolutePath)
                    if (bm != null) image.loadBitmap(bm!!)
                }

                stepsContainer.addView(stepView)
            }
        }

        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = courseLessonItem?.item?.title
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
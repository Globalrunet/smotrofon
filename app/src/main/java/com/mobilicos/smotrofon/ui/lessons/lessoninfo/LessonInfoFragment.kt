package com.mobilicos.smotrofon.ui.lessons.lessoninfo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.ConfigurationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.mobilicos.smotrofon.databinding.LessonInfoFragmentBinding
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.ui.lessons.comments.CommentsListFragment
import com.mobilicos.smotrofon.util.FileUtil
import com.mobilicos.smotrofon.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File


@AndroidEntryPoint
class LessonInfoFragment : Fragment() {

    private lateinit var binding: LessonInfoFragmentBinding
    private val lessonInfoViewModel: LessonInfoViewModel by viewModels()
    private val args: LessonInfoFragmentArgs by navArgs()
    private var ident: Int = 0
    private var elementId: Int = 0
    private var initialCommentsCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
            downloadLessonInfoCollect()
            lessonInfoViewModel.downloadLessonByIdent(ident)
        } else {
            updateUI()
        }

        binding.begin.setOnClickListener {
            val action = LessonInfoFragmentDirections.actionLessonInfoToStepsInfo(ident = ident, objectId = elementId)
            findNavController().navigate(action)
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

    override fun onStart() {
        super.onStart()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }
}
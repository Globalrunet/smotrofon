package com.mobilicos.smotrofon.ui.user.audio.edit

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.databinding.FragmentUserAudioEditBinding
import com.mobilicos.smotrofon.databinding.FragmentUserVideoEditBinding
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.util.RoundedRectTransform
import com.mobilicos.smotrofon.util.afterTextChanged
import com.mobilicos.smotrofon.util.calculateInSampleSize
import com.mobilicos.smotrofon.util.visible
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@AndroidEntryPoint
class EditAudioFragment : Fragment() {
    private lateinit var binding: FragmentUserAudioEditBinding
    private val viewModel: EditAudioViewModel by activityViewModels()
    private val args: EditAudioFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUserAudioEditBinding.inflate(layoutInflater, container, false)

        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        viewModel.key = sharedPref?.getString("key", "") ?: ""

        with (binding) {
            title.setText(viewModel.currentVideoItem?.title ?: "")

            with(text) {
                setText(viewModel.currentVideoItem?.text ?: "")

                setOnTouchListener { v, event ->
                    v.parent.requestDisallowInterceptTouchEvent(true)

                    if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_UP) {
                        v.performClick()
                        v.parent.requestDisallowInterceptTouchEvent(false)
                    }
                    false
                }
            }

            if (viewModel.currentVideoItem?.poster_large != "") {
                Picasso.get()
                    .load(viewModel.currentVideoItem?.poster_large)
                    .placeholder(R.drawable.placeholder)
                    .transform(RoundedRectTransform())
                    .into(poster)
            } else {
                Picasso.get()
                    .load(R.drawable.placeholder)
                    .transform(RoundedRectTransform())
                    .into(poster)
            }

            editButton.setOnClickListener {
                isAllPermissionsGranted()
                openSelectImageForResult()
            }
        }

        viewModel.formDataChanged(
            title = binding.title.text.toString(),
            text = binding.text.text.toString()
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        subscribeUI()
        formStateCollect()
        updateCollect()
        uploadPosterCollect()
    }

    private fun subscribeUI() {
        binding.title.afterTextChanged {
            viewModel.formDataChanged(
                title = binding.title.text.toString(),
                text = binding.text.text.toString()
            )
        }

        binding.text.afterTextChanged {
            viewModel.formDataChanged(
                title = binding.title.text.toString(),
                text = binding.text.text.toString()
            )
        }

        binding.save.setOnClickListener {
            viewModel.updateVideoData()
        }
    }

    private fun formStateCollect() {
        lifecycleScope.launch {
            viewModel.updateVideoFormState.collect {

                binding.save.isEnabled = it.isDataValid
                if (it.titleError != null) {
                    binding.titleLayout.error = requireActivity().getString(it.titleError)
                } else {
                    binding.titleLayout.error = null
                }

                if (it.textError != null) {
                    binding.textLayout.error = requireActivity().getString(it.textError)
                } else {
                    binding.textLayout.error = null
                }
            }
        }
    }

    private fun updateCollect() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateVideoResult.collect {
                    when (it.status) {
                        Result.Status.LOADING -> {
                            println("RESULT LOADING")
                            binding.prependProgress.visible(true)
                            binding.appendProgress.visible(true)
                        }
                        Result.Status.ERROR -> {
                            showMessage(requireActivity().getString(R.string.edit_video_save_error))
                            binding.prependProgress.visible(false)
                            binding.appendProgress.visible(false)
                            viewModel.clearVideoResult()
                        }
                        Result.Status.SUCCESS -> {
                            if (it.data != null && it.data.result) {
                                showMessage(activity?.getString(R.string.edit_video_save_success))
                            } else {
                                showMessage(requireActivity().getString(R.string.edit_video_save_error))
                            }
                            binding.prependProgress.visible(false)
                            binding.appendProgress.visible(false)
                            viewModel.clearVideoResult()
                            viewModel.needToRefreshData = true
                        }
                        else -> {
                            binding.prependProgress.visible(false)
                            binding.appendProgress.visible(false)
                            viewModel.clearVideoResult()
                        }
                    }

                }
            }
        }
    }

    private fun uploadPosterCollect() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uploadMediaPosterResult.collect {
                    when (it.status) {
                        Result.Status.LOADING -> {
                            println("RESULT LOADING")
                            binding.prependProgress.visible(true)
                            binding.appendProgress.visible(true)
                        }
                        Result.Status.ERROR -> {
                            showMessage(requireActivity().getString(R.string.edit_video_save_error))
                            binding.prependProgress.visible(false)
                            binding.appendProgress.visible(false)
                            viewModel.clearUploadPosterResult()
                        }
                        Result.Status.SUCCESS -> {
                            if (it.data != null && it.data.result) {
                                Picasso.get()
                                    .load(it.data.image)
                                    .placeholder(R.drawable.placeholder)
                                    .transform(RoundedRectTransform())
                                    .into(binding.poster)
                                showMessage(activity?.getString(R.string.edit_video_save_success))
                            } else {
                                showMessage(requireActivity().getString(R.string.edit_video_save_error))
                            }
                            binding.prependProgress.visible(false)
                            binding.appendProgress.visible(false)
                            viewModel.clearUploadPosterResult()
                            viewModel.needToRefreshData = true
                        }
                        else -> {
                            binding.prependProgress.visible(false)
                            binding.appendProgress.visible(false)
                            viewModel.clearUploadPosterResult()
                        }
                    }

                }
            }
        }
    }

    fun openSelectImageForResult() {
        Log.e("START SELECTING", "IMG")

        if (isAllPermissionsGranted()) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)

            resultLauncher.launch(intent)
        }
    }

    private var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result -> selectImage(result) }

    private fun generateFileUri(): Uri {
        val f = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "cache_pac_" + System.currentTimeMillis() + ".jpeg"
        )
        Log.e(ContentValues.TAG, "Generated fileName = $f")
        return Uri.fromFile(f)
    }

    private fun isAllPermissionsGranted(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.v(ContentValues.TAG, "Permission is granted")
            return true
        } else {
            Log.v(ContentValues.TAG, "Permission is revoked")
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 1
            )
            return false
        }
    }

    private fun selectImage(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            var fresult = false
            val photoUri: Uri = generateFileUri()

            if (data != null) {
                val selectedImage = data.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

                val cursor = requireContext().contentResolver.query(
                    selectedImage!!,
                    filePathColumn,
                    null,
                    null,
                    null
                )
                cursor!!.moveToFirst()

                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                val picturePath = cursor.getString(columnIndex)

                println("DATA PICTURE PATH $picturePath")
                cursor.close()

                if (picturePath != null) {
                    var angle = 0
                    try {
                        val exif = ExifInterface(picturePath)
                        val orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                        )
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                            angle = 270
                        }
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                            angle = 180
                        }
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                            angle = 90
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    val matrix = Matrix()
                    matrix.postRotate(angle.toFloat())
                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(picturePath, options)
                    options.inSampleSize = calculateInSampleSize(
                        options,
                        Config.POSTER_MAX_WIDTH,
                        Config.POSTER_MAX_HEIGHT
                    )
                    options.inJustDecodeBounds = false
                    Log.e("PATH", picturePath)
                    var bm = BitmapFactory.decodeFile(picturePath, options)
                    bm = Bitmap.createBitmap(bm, 0, 0, bm.width, bm.height, matrix, true)

                    var out: FileOutputStream? = null
                    try {
                        out = FileOutputStream(photoUri.path)
                        bm.compress(Bitmap.CompressFormat.JPEG, 70, out)
                        fresult = true
                    } catch (e: Exception) {
                        e.localizedMessage?.let { Log.e("ERROR", it) }
                        e.printStackTrace()
                    } finally {
                        try {
                            out?.close()
                        } catch (e: IOException) {
                            e.localizedMessage?.let { Log.e("ERROR", it) }
                            e.printStackTrace()
                        }
                    }
                }
            }

            if (fresult) {
                photoUri.path?.let {
                    viewModel.image = it
                    viewModel.uploadMediaPoster()
                }
            }

            println("RESULT $result")
            println("RESULT ${photoUri.path}")
            println("RESULT: $data")
        }
    }

    private fun showMessage(msg: String?) {
        if (msg != null) {
            Snackbar.make(binding.root, msg, Snackbar.ANIMATION_MODE_SLIDE).setAction("OK!") {
            }.show()
        }
    }
}
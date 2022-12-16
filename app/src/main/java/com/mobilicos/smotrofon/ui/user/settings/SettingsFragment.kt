package com.mobilicos.smotrofon.ui.user.settings

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
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.models.UserLogin
import com.mobilicos.smotrofon.data.responses.UpdateUsernamesResponse
import com.mobilicos.smotrofon.data.responses.UploadUserImageResponse
import com.mobilicos.smotrofon.databinding.FragmentProfileSettingsBinding
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.util.RoundedRectTransform
import com.mobilicos.smotrofon.util.afterTextChanged
import com.mobilicos.smotrofon.util.calculateInSampleSize
import com.mobilicos.smotrofon.util.visible
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@AndroidEntryPoint
class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentProfileSettingsBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileSettingsBinding.inflate(layoutInflater, container, false)

        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        viewModel.key = sharedPref?.getString("key", "") ?: ""

        if (!viewModel.updateUsernamesFormState.value.isInited) {
            viewModel.usernamesFormDataChanged(
                isInited = true,
                firstName = sharedPref?.getString("firstname", "") ?: "",
                lastName = sharedPref?.getString("lastname", "") ?: ""
            )
        }

        with (binding) {
            firstname.setText(viewModel.updateUsernamesFormState.value.firstName)
            lastname.setText(viewModel.updateUsernamesFormState.value.lastName)
            println("PASSWORD BEFORE")
            password.setText(viewModel.updatePasswordFormState.value.password)
            println("PASSWORD AFTER")

            if (sharedPref?.getString("user_icon", "") != "") {
                Picasso.get()
                    .load(sharedPref?.getString("user_icon", ""))
                    .placeholder(R.drawable.placeholder)
                    .transform(RoundedRectTransform())
                    .into(image)
            } else {
                Picasso.get()
                    .load(R.drawable.placeholder)
                    .transform(RoundedRectTransform())
                    .into(image)
            }

            editImageButton.setOnClickListener {
                isAllPermissionsGranted()
                openSelectImageForResult()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        subscribeUI()
        usernamesFormStateCollect()
        passwordFormStateCollect()
        updateUsernamesCollect()
        updateUserPasswordCollect()
        uploadUserImageCollect()
    }

    private fun subscribeUI() {
        binding.firstname.afterTextChanged {
            viewModel.usernamesFormDataChanged(
                isInited = true,
                firstName = binding.firstname.text.toString(),
                lastName = binding.lastname.text.toString()
            )
        }

        binding.lastname.afterTextChanged {
            viewModel.usernamesFormDataChanged(
                isInited = true,
                firstName = binding.firstname.text.toString(),
                lastName = binding.lastname.text.toString()
            )
        }

        binding.password.afterTextChanged {
            println("PASSWORD CHANGED")
            viewModel.passwordFormDataChanged(
                password = binding.password.text.toString()
            )
        }

        binding.saveNamesButton.setOnClickListener {
            viewModel.updateUsernames()
        }

        binding.savePasswordButton.setOnClickListener {
            viewModel.updateUserPassword()
        }
    }

    private fun usernamesFormStateCollect() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateUsernamesFormState.collect {
                    binding.saveNamesButton.isEnabled = it.isDataValid
                    if (it.firstNameError != null) {
                        binding.firstnameLayout.error = requireActivity().getString(it.firstNameError)
                    } else {
                        binding.firstnameLayout.error = null
                    }

                    if (it.lastNameError != null) {
                        binding.lastnameLayout.error = requireActivity().getString(it.lastNameError)
                    } else {
                        binding.lastnameLayout.error = null
                    }
                }
            }
        }
    }

    private fun passwordFormStateCollect() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updatePasswordFormState.collect {
                    binding.savePasswordButton.isEnabled = it.isDataValid
                    if (it.passwordError != null) {
                        binding.passwordLayout.error = requireActivity().getString(it.passwordError)
                    } else {
                        binding.passwordLayout.error = null
                    }
                }
            }
        }
    }

    private fun updateUsernamesCollect() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updateUsernamesResult.collect {
                    when (it.status) {
                        Result.Status.LOADING -> {
                            println("RESULT LOADING")
                            binding.saveNamesProgress.visible(true)
                        }
                        Result.Status.ERROR -> {
                            showMessage(requireActivity().getString(R.string.edit_video_save_error))
                            binding.saveNamesProgress.visible(false)
                            viewModel.clearUpdateUsernamesResult()
                        }
                        Result.Status.SUCCESS -> {
                            if (it.data != null && it.data.result) {
                                showMessage(activity?.getString(R.string.edit_video_save_success))
                                saveUsernames(data = it.data)
                            } else {
                                showMessage(requireActivity().getString(R.string.edit_video_save_error))
                            }
                            binding.saveNamesProgress.visible(false)
                            viewModel.clearUpdateUsernamesResult()
                        }
                        else -> {
                            binding.saveNamesProgress.visible(false)
                            viewModel.clearUpdateUsernamesResult()
                        }
                    }
                }
            }
        }
    }

    private fun updateUserPasswordCollect() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.updatePasswordResult.collect {
                    when (it.status) {
                        Result.Status.LOADING -> {
                            println("RESULT LOADING")
                            binding.savePasswordProgress.visible(true)
                        }
                        Result.Status.ERROR -> {
                            showMessage(requireActivity().getString(R.string.edit_video_save_error))
                            binding.savePasswordProgress.visible(false)
                            viewModel.clearUpdateUserPasswordResult()
                        }
                        Result.Status.SUCCESS -> {
                            if (it.data != null && it.data.result) {
                                showMessage(activity?.getString(R.string.edit_video_save_success))
                            } else {
                                showMessage(requireActivity().getString(R.string.edit_video_save_error))
                            }
                            binding.savePasswordProgress.visible(false)
                            viewModel.clearUpdateUserPasswordResult()
                        }
                        else -> {
                            binding.savePasswordProgress.visible(false)
                            viewModel.clearUpdateUserPasswordResult()
                        }
                    }
                }
            }
        }
    }

    private fun uploadUserImageCollect() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uploadUserImageResult.collect {
                    when (it.status) {
                        Result.Status.LOADING -> {
                            println("RESULT LOADING")
                            binding.imageProgress.visible(true)
                        }
                        Result.Status.ERROR -> {
                            showMessage(requireActivity().getString(R.string.edit_video_save_error))
                            binding.imageProgress.visible(false)
                            viewModel.clearUploadUserImageResult()
                        }
                        Result.Status.SUCCESS -> {
                            if (it.data != null && it.data.result) {
                                Picasso.get()
                                    .load(it.data.image)
                                    .placeholder(R.drawable.placeholder)
                                    .transform(RoundedRectTransform())
                                    .into(binding.image)

                                saveUserImage(data = it.data)
                                showMessage(activity?.getString(R.string.edit_video_save_success))
                            } else {
                                showMessage(requireActivity().getString(R.string.edit_video_save_error))
                            }
                            binding.imageProgress.visible(false)
                            viewModel.clearUploadUserImageResult()
                        }
                        else -> {
                            binding.imageProgress.visible(false)
                            viewModel.clearUploadUserImageResult()
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
                    viewModel.uploadUserImage()
                }
            }

            println("RESULT $result")
            println("RESULT ${photoUri.path}")
            println("RESULT: $data")
        }
    }

    private fun saveUsernames(data: UpdateUsernamesResponse?) {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        with (sharedPref?.edit()) {
            if (this != null && data != null) {
                putString("firstname", data.firstName)
                putString("lastname", data.lastName)
                putString("user_full_name", "${data.firstName} ${data.lastName}")
                apply()
            }
        }
    }

    private fun saveUserImage(data: UploadUserImageResponse?) {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        with (sharedPref?.edit()) {
            if (this != null && data != null) {
                putString("user_icon", data.image)
                apply()
            }
        }
    }

    private fun showMessage(msg: String?) {
        if (msg != null) {
            Snackbar.make(binding.root, msg, Snackbar.ANIMATION_MODE_SLIDE).setAction("OK!") {
            }.show()
        }
    }
}
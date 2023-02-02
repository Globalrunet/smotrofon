package com.mobilicos.smotrofon.ui.lessons.comments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.models.Comment
import com.mobilicos.smotrofon.databinding.BottomSheetFragmentListBinding
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.ui.report.BottomSheetReportFragment
import com.mobilicos.smotrofon.ui.report.ReportViewModel
import com.mobilicos.smotrofon.util.*
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


@AndroidEntryPoint
class CommentsListFragment : BottomSheetDialogFragment(), CommentsInterface, OptionsMenuClickListener<Comment> {

    private var commentsListAdapter: CommentsListAdapter? = null
    lateinit var binding: BottomSheetFragmentListBinding
    override fun getTheme() = R.style.AppBottomSheetDialogTheme
    private val commentsListViewModel: CommentsListViewMode by viewModels()
    private val reportViewModel: ReportViewModel by activityViewModels()
    private var sharedPref: SharedPreferences? = null
    private var userId: Int = 0
    private var userKey: String = ""
    private var addCommentDialog: BottomSheetDialog? = null
    private var editCommentDialog: BottomSheetDialog? = null
    private var removeAlertDialog: AlertDialog? = null
    private val appLabel: String = "comment"
    private val model: String = "comment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        sharedPref?.let {
            userKey = it.getString("key", "").toString()
            userId = it.getInt("user_id", 0)
        }

        arguments?.let { args ->
            val currentAppLabel = args.getString("current_app_label", "")
            val currentModel = args.getString("current_model", "")
            val currentObjectId = args.getInt("current_object_id", 0)

            commentsListViewModel.setObjectData(app_label = currentAppLabel,
                model = currentModel,
                object_id = currentObjectId)
        }

        binding = BottomSheetFragmentListBinding.inflate(layoutInflater, container, false)

        with (binding) {
            header.text = requireContext().getString(R.string.fragment_layout_comments_header, 0)
            addCommentTextLabel.setOnClickListener {
                if (userId > 0) {
                    setAddComment()
                } else {
                    showMessage(requireActivity().getString(R.string.need_account))
                }
            }

            if (userId > 0) {
                Picasso.get()
                    .load(sharedPref!!.getString("user_icon", "")).transform(CircleTransform())
                    .into(userIcon)
            }

            iconClearDown.setOnClickListener {
                dialog?.hide()
                dialog?.dismiss()
            }

            setQuery()
            setSwipeRefreshAdapter()
            lessonsDataSet()
            addCommentCollect()
            editCommentCollect()
            removeCommentCollect()
            addReportCollect()

            commentsListAdapter?.currentUser = userId
            println("ADAPTER SET USERID $userId")

            return root
        }
    }

    override fun onResume() {
        super.onResume()


    }

    private var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result -> selectImage(result) }

    @SuppressLint("IntentReset")
    private fun openSelectImageForResult() {
        Log.e("START SELECTING", "IMG")

        if (isAllPermissionsGranted()) {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"

            resultLauncher.launch(intent)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setAddComment() {
        addCommentDialog =  BottomSheetDialog(requireContext())
        addCommentDialog?.let {
            it.setContentView(R.layout.bottom_sheet_add_comment)
            val behavior = it.behavior
            val icon = it.findViewById<ImageView>(R.id.addCommentUserIcon)
            val addPhoto = it.findViewById<ImageView>(R.id.photo)
            val send = it.findViewById<ImageButton>(R.id.send)
            val comment = it.findViewById<TextInputEditText>(R.id.addComment)

            if (userId > 0) {
                Picasso.get()
                    .load(sharedPref!!.getString("user_icon", "")).transform(CircleTransform())
                    .into(icon)
            }

            send?.setOnClickListener {
                val images = commentsListViewModel.getImagesList()
                commentsListViewModel.addComment(key = userKey, text = comment?.text.toString(), images = images)
            }

            addPhoto?.setOnClickListener {
                isAllPermissionsGranted()
                openSelectImageForResult()
            }

            send?.isEnabled = false
            comment?.apply {
                this.afterTextChanged { text ->
                    send?.isEnabled = text.isNotEmpty()
                }
            }

            addCommentDialog!!.setOnDismissListener {
                commentsListViewModel.removeAllImage()
            }

            comment?.requestFocus()
            comment?.showKeyboard()
            behavior.isDraggable = false
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            it.show()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setEditComment(element: Comment) {
        editCommentDialog =  BottomSheetDialog(requireContext())
        editCommentDialog?.let {
            it.setContentView(R.layout.bottom_sheet_edit_comment)
            val behavior = it.behavior
            val icon = it.findViewById<ImageView>(R.id.commentUserIcon)
            val send = it.findViewById<ImageButton>(R.id.send)
            val comment = it.findViewById<TextInputEditText>(R.id.comment)

            if (userId > 0) {
                Picasso.get()
                    .load(sharedPref!!.getString("user_icon", "")).transform(CircleTransform())
                    .into(icon)
            }

            send?.setOnClickListener {
                commentsListViewModel.editComment(key = userKey,
                    comment_id = element.id,
                    text = comment?.text.toString())
            }

            comment?.setText(element.text)
            send?.isEnabled = comment?.text.toString().isNotEmpty()
            comment?.apply {
                this.afterTextChanged { text ->
                    send?.isEnabled = text.isNotEmpty()
                }
            }

            comment?.requestFocus()
            comment?.showKeyboard()
            behavior.isDraggable = false
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            it.show()
        }
    }

    private fun addCommentCollect() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                commentsListViewModel.addCommentResponseData.collect {
                    addCommentDialog?.let { dialog ->

                        val send = dialog.findViewById<ImageButton>(R.id.send)
                        val comment = dialog.findViewById<TextInputEditText>(R.id.addComment)
                        val addCommentProgress = dialog.findViewById<LinearProgressIndicator>(R.id.addCommentProgress)

                        when (it.status) {
                            Result.Status.LOADING -> {
                                send?.isEnabled = false
                                addCommentProgress?.visible(true)
                            }
                            Result.Status.ERROR -> {
                                send?.isEnabled = true
                                addCommentProgress?.visible(false)
                                commentsListViewModel.clearAddCommentResult()
                                showMessage(activity?.getString(R.string.comments_add_comment_error))
                            }
                            Result.Status.SUCCESS -> {
                                if (it.data != null && it.data.result) {
                                    comment?.setText("")
                                    comment?.hideKeyboard()
                                    showMessage(activity?.getString(R.string.comments_add_comment_success))
                                    dialog.dismiss()
                                    addCommentDialog = null
                                    commentsListAdapter?.isCounterSet = false
                                    commentsListAdapter?.refresh()
                                } else {
                                    showMessage(activity?.getString(R.string.comments_add_comment_error))
                                }
                                send?.isEnabled = true
                                addCommentProgress?.visible(false)
                                commentsListViewModel.clearAddCommentResult()
                            }
                            else -> {
                                send?.isEnabled = comment?.text?.isNotEmpty() ?: false
                                addCommentProgress?.visible(false)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun editCommentCollect() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                commentsListViewModel.editCommentResponseData.collect {
                    editCommentDialog?.let { dialog ->

                        val send = dialog.findViewById<ImageButton>(R.id.send)
                        val comment = dialog.findViewById<TextInputEditText>(R.id.comment)
                        val progress = dialog.findViewById<LinearProgressIndicator>(R.id.progress)

                        when (it.status) {
                            Result.Status.LOADING -> {
                                send?.isEnabled = false
                                progress?.visible(true)
                            }
                            Result.Status.ERROR -> {
                                send?.isEnabled = true
                                progress?.visible(false)
                                commentsListViewModel.clearEditCommentResult()
                                showMessage(activity?.getString(R.string.comments_edit_comment_error))
                            }
                            Result.Status.SUCCESS -> {
                                if (it.data != null && it.data.result) {
                                    comment?.setText("")
                                    comment?.hideKeyboard()
                                    showMessage(activity?.getString(R.string.comments_edit_comment_success))
                                    dialog.dismiss()
                                    editCommentDialog = null
                                    commentsListAdapter?.isCounterSet = false
                                    commentsListAdapter?.refresh()
                                } else {
                                    showMessage(activity?.getString(R.string.comments_add_comment_error))
                                }
                                send?.isEnabled = true
                                progress?.visible(false)
                                commentsListViewModel.clearEditCommentResult()
                            }
                            else -> {
                                send?.isEnabled = comment?.text?.isNotEmpty() ?: false
                                progress?.visible(false)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun removeCommentCollect() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                commentsListViewModel.removeCommentResponseData.collect {
                    when (it.status) {
                        Result.Status.LOADING -> {
                            binding.prependProgress.visible(true)
                        }
                        Result.Status.ERROR -> {
                            binding.prependProgress.visible(false)
                            commentsListViewModel.clearRemoveCommentResult()
                            showMessage(activity?.getString(R.string.comments_remove_comment_error))
                        }
                        Result.Status.SUCCESS -> {
                            if (it.data != null && it.data.result) {
                                showMessage(activity?.getString(R.string.comments_remove_comment_success))
                                addCommentDialog = null
                                commentsListAdapter?.isCounterSet = false
                                commentsListAdapter?.refresh()
                            } else {
                                showMessage(activity?.getString(R.string.comments_remove_comment_error))
                            }
                            binding.prependProgress.visible(false)
                            commentsListViewModel.clearRemoveCommentResult()
                        }
                        else -> {
                            binding.prependProgress.visible(false)
                        }
                    }
                }
            }
        }
    }

    fun setQuery() {
        commentsListViewModel.setQuery(key = userKey)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {

            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let { pl ->
                val behaviour = BottomSheetBehavior.from(pl)
                setupFullHeight(pl)
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                behaviour.isDraggable = false
            }
        }
        return dialog
    }

    override fun onStart() {
        super.onStart()

        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behavior = BottomSheetBehavior.from(bottomSheet)

            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        dismiss()
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    binding.iconClearDown.rotation = slideOffset * 180
                }
            })
        }

        if (commentsListViewModel.isRemoveDialogShown) {
            commentsListViewModel.currentElement?.let {
                showRemoveDialog(element = it, position = commentsListViewModel.currentPosition)
            }
        }
    }

    private fun showRemoveDialog(element: Comment, position: Int = 0) {
        context?.let {

            commentsListViewModel.isRemoveDialogShown = true
            commentsListViewModel.currentElement = element
            commentsListViewModel.currentPosition = position
            val id = element.id

            removeAlertDialog = MaterialAlertDialogBuilder(it)
                .setTitle(resources.getString(R.string.dialog_remove_comment_title))
                .setMessage(element.text.take(120))
                .setNegativeButton(resources.getString(R.string.dialog_remove_negative_button_title)) { dialog, which ->
                    commentsListViewModel.isRemoveDialogShown = false
                    commentsListViewModel.currentElement = null
                    commentsListViewModel.currentPosition = -1
                }
                .setPositiveButton(resources.getString(R.string.dialog_remove_positive_button_title)) { dialog, which ->
                    commentsListViewModel.removeComment(key = userKey, comment_id = element.id)
                    commentsListViewModel.isRemoveDialogShown = false
                    commentsListViewModel.currentElement = null
                    commentsListViewModel.currentPosition = -1
                }
                .setCancelable(true)
                .setOnDismissListener {
                    commentsListViewModel.isRemoveDialogShown = false
                    commentsListViewModel.currentElement = null
                    commentsListViewModel.currentPosition = -1
                }
                .show()
        }
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }

    private fun lessonsDataSet() {
        if (binding.recyclerView.adapter == null) {

            binding.recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = commentsListAdapter
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    commentsListViewModel.commentsByQueryData.collectLatest {
                        commentsListAdapter?.submitData(it)
                    }
                }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    commentsListAdapter?.loadStateFlow?.collect {
                        println("COMMENT LOAD STATE ${it.source.prepend} /// ${it.source.append}")
                        binding.prependProgress.isVisible = it.source.prepend is LoadState.Loading
                        binding.appendProgress.isVisible = it.source.append is LoadState.Loading
                        binding.swipeRefreshLayout.isRefreshing = it.refresh is LoadState.Loading

                        if (it.source.refresh is LoadState.NotLoading
                            && it.append.endOfPaginationReached
                            && (commentsListAdapter?.itemCount ?: 0) < 1
                        ) {
                            binding.recyclerView.visible(false)
                            binding.emptyView.visible(true)
                        } else {
                            binding.recyclerView.visible(true)
                            binding.emptyView.visible(false)
                        }
                    }
                }
            }
        }
    }

    private fun addReportCollect() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                reportViewModel.reportAddResult.collect {
                    it?.let {
                        if (it) {
                            showMessage(activity?.getString(R.string.report_add_success))
                        } else {
                            showMessage(activity?.getString(R.string.report_add_error))
                        }
                        val v = view?.findViewById<View>(reportViewModel.reportViewId)
                        v?.visible(false)
                        reportViewModel.setReportAddResult(null)
                        reportViewModel.reportViewId = -1
                    }
                }
            }
        }
    }

    private fun setSwipeRefreshAdapter() {
        if (commentsListAdapter == null) {
            commentsListAdapter = CommentsListAdapter(listener = this, menuClickListener = this)
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            commentsListAdapter?.refresh()
        }
    }

    override fun clickOnCommentRemove(position: Int, element: Comment) {
        showRemoveDialog(element = element, position = position)
    }

    override fun clickOnCommentEdit(position: Int, element: Comment) {
        setEditComment(element = element)
    }

    override fun clickOnLikeButton(position: Int, element: Comment) {

    }

    override fun clickOnDislikeButton(position: Int, element: Comment) {

    }

    override fun setTotalCommentsCounter(counter: Int) {
        binding.header.text =
            requireContext().getString(R.string.comments_list_header, counter)
    }

    private fun showMessage(msg: String?) {
        if (msg != null) {
            Snackbar.make(binding.root, msg, Snackbar.ANIMATION_MODE_SLIDE).setAction("OK!") {
            }.show()
        }
    }

    override fun onOptionsMenuBlockClicked(item: Comment, view: View?) {
        showMessage(activity?.getString(R.string.user_blocked_success_message))
    }

    override fun onOptionsMenuReportClicked(item: Comment, view: View?) {
        context?.let {

            val fragment = BottomSheetReportFragment()

            reportViewModel.appLabel = appLabel
            reportViewModel.model = model
            reportViewModel.objectId = item.id
            reportViewModel.key = userKey
            reportViewModel.reportViewId = view?.id ?: -1
            fragment.show(requireActivity().supportFragmentManager, fragment.tag)
        }
    }

    private fun selectImage(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            var fresult = false
            val photoUri: Uri = generateFileUri()
            val iconUri: Uri = generateFileUri()

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
                        bm.compress(Bitmap.CompressFormat.JPEG, 90, out)
                        println("BITMAP IMAGE ${bm.width} // ${bm.height}")

                        out = FileOutputStream(iconUri.path)
                        val iconBm = Bitmap.createScaledBitmap(bm, bm.width/5, bm.height/5, false)
                        println("BITMAP ICON ${iconBm.width} // ${iconBm.height}")
                        iconBm.compress(Bitmap.CompressFormat.JPEG, 90, out)

                        println("BITMAP IMG AND ICON PATH: ${photoUri.path} // ${iconUri.path}")

                        bm.recycle()
                        iconBm.recycle()

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
                commentsListViewModel.removeAllImage()
                commentsListViewModel.addImage(key = photoUri.path!!, image = photoUri.path!!, icon = iconUri.path!!)
                addIconToImagesContainer(key = photoUri.path!!, iconPath = iconUri.path!!)
            }
        }
    }

    private fun addIconToImagesContainer(key: String, iconPath: String) {
        val li = LayoutInflater.from(requireContext())
        val view = li.inflate(R.layout.icon_image_view, null)
        val iconView = view.findViewById<ImageView>(R.id.image)
        val removeIconView = view.findViewById<ImageButton>(R.id.remove)
        val bm = BitmapFactory.decodeFile(iconPath)

        val squareBitmap: Bitmap = if (bm.width >= bm.height) {
            Bitmap.createBitmap(bm, bm.width / 2 - bm.height / 2, 0, bm.height, bm.height)
        } else {
            Bitmap.createBitmap(bm, 0, bm.height / 2 - bm.width / 2, bm.width, bm.width)
        }

        Glide.with(requireContext()).load(squareBitmap).into(iconView)

        addCommentDialog?.let {
            val container =  it.findViewById<LinearLayout>(R.id.imagesContainerLinearLayout)
            view.tag = key
            container?.removeAllViews()
            container?.addView(view)

            removeIconView.setOnClickListener {
                container?.removeView(view)
                commentsListViewModel.removeImage(key = key)
            }
        }
    }

    private fun generateFileUri(): Uri {
        val randomInt = (1000000..10000000).random()
        val f = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "cache_pac_" + System.currentTimeMillis() + "_" + randomInt +".jpeg"
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
}
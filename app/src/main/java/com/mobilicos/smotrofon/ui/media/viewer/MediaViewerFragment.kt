package com.mobilicos.smotrofon.ui.media.viewer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.models.Media
import com.mobilicos.smotrofon.data.models.MediaResponse
import com.mobilicos.smotrofon.databinding.VideoViewerBinding
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.ui.adapters.RelatedMediaListRecyclerAdapter
import com.mobilicos.smotrofon.ui.channels.content.UserContentViewModel
import com.mobilicos.smotrofon.ui.fragments.BottomFragment
import com.mobilicos.smotrofon.ui.lessons.comments.CommentsListFragment
import com.mobilicos.smotrofon.ui.media.list.OnClickMediaListElement
import com.mobilicos.smotrofon.util.CircleTransform
import com.mobilicos.smotrofon.util.visible
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@AndroidEntryPoint
class MediaViewerFragment : BottomSheetDialogFragment(), Player.Listener, OnClickMediaListElement {

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        VideoViewerBinding.inflate(layoutInflater)
    }
    private lateinit var media: Media
    private var player: ExoPlayer? = null
    private var currentItem = 0
    private var playWhenReady = true
    private var isConfigurationChanged = false
    private val mediaViewModel: MediaViewModel by activityViewModels()
    private var sharedPref: SharedPreferences? = null
    private var userId: Int = 0
    private var userKey: String = ""
    private var subscribedString: String = ""
    private var subscribedList: List<Int> = listOf()


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        sharedPref?.let { it ->
            userKey = it.getString("key", "").toString()
            userId = it.getInt("user_id", 0)
            subscribedString = it.getString("subscribed_str", "").toString()
            subscribedList = subscribedString.split(";").filter { e -> e.isNotEmpty() }
                .map { e -> e.toInt() }
        }

        isConfigurationChanged = savedInstanceState?.getBoolean("isConfigurationChange") ?: false

        val mediaListObserver = Observer<Result<MediaResponse>> {
            if (it.status == Result.Status.SUCCESS && it.data != null) {

                binding.recyclerView.isNestedScrollingEnabled = false
                binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.recyclerView.adapter = RelatedMediaListRecyclerAdapter(it.data.elements, this)
                binding.recyclerView.visible(true)
//                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        mediaViewModel.getObservableProduct().observe(this, mediaListObserver)

        media = mediaViewModel.getSelected().value!!

        configureView()

        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")
        val dateAdded = LocalDateTime.parse(media.date_added, inputFormatter)
        val formattedDateAdded = dateAdded.format(outputFormatter)

        binding.title.text = media.title
        val videoDescription =  resources.getString(
            R.string.videoviewer_video_description,
            media.views_count,
            formattedDateAdded
        )

        binding.videoDescription.text = videoDescription

        Picasso.get().load(media.user_icon).transform(CircleTransform()).into(binding.userIcon)
        binding.userContainer.setOnClickListener { goToUserAccount(media = media) }
        binding.userFullName.text = media.user_full_name
        binding.userInfo.text = getString(R.string.subscribers_count, media.user_subscribers_count.toString())

        binding.titleBlock.setOnClickListener {
            val descriptionFragment = BottomFragment()
            descriptionFragment.show(requireActivity().supportFragmentManager, descriptionFragment.tag)
        }

        binding.blockLike.setOnClickListener {
            showNeedUserAccountToast()
        }
        binding.blockDislike.setOnClickListener {
            showNeedUserAccountToast()
        }
        binding.blockShare.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type="text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, "http://smotrofon.ru/video/viewer/${media.id}/")
            startActivity(Intent.createChooser(shareIntent, "Отправить ссылку на видео"))
        }
        binding.blockDownload.setOnClickListener {
            showNeedUserAccountToast()
        }
        binding.userSubscribe.setOnClickListener {
            mediaViewModel.subscribeUser(key = userKey, otherUserId = media.user_id)
        }

        if (media.user_id in subscribedList) {
            binding.userSubscribe.text = getString(R.string.subscribed_title)
        } else {
            binding.userSubscribe.text = getString(R.string.subscribe_title)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                refreshRelatedMediaList()
            }
        }

//        binding.swipeRefreshLayout.setOnRefreshListener {
//            refreshRelatedMediaList()
//        }

        binding.addCommentTextLabel.setOnClickListener {
            val commentsFragment = CommentsListFragment()

            val args = Bundle()
            args.putString("current_app_label", "video")

            if (mediaViewModel.getContentType() == Config.TYPE_VIDEO) {
                args.putString("current_model", "video")
            } else {
                args.putString("current_model", "audio")
            }

            args.putInt("current_object_id", media.id)
            commentsFragment.arguments = args

            commentsFragment.show(requireActivity().supportFragmentManager, commentsFragment.tag)
        }

        subscribeUserCollect()

        return binding.root
    }

    private fun subscribeUserCollect() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mediaViewModel.subscribeUserResponseData.collect {
                    println("SUBSCRIBE SUCCESS START ${it.status}")
                    when (it.status) {
                        Result.Status.LOADING -> {
                            binding.prependProgress.visible(true)
                        }
                        Result.Status.ERROR -> {
                            binding.prependProgress.visible(false)
                            showMessage(activity?.getString(R.string.subscribe_user_error))
                            mediaViewModel.clearSubscribeUserResponseData()
                        }
                        Result.Status.SUCCESS -> {
                            if (it.data != null && it.data.result) {

                                println("SUBSCRIBE SUCCESS ${it.data}")

                                if (it.data.subscribed) {
                                    binding.userSubscribe.text = getString(R.string.subscribed_title)
                                    showMessage(activity?.getString(R.string.subscribe_user_success))
                                } else {
                                    binding.userSubscribe.text = getString(R.string.subscribe_title)
                                    showMessage(activity?.getString(R.string.unsubscribe_user_success))
                                }
                                sharedPref?.edit()?.putString("subscribed_str", it.data.subscribed_str)
                                    ?.apply()
                                subscribedList = it.data.subscribed_str.split(";").filter { e -> e.isNotEmpty() }
                                    .map { e -> e.toInt() }
                            } else {
                                showMessage(activity?.getString(R.string.subscribe_user_error))
                            }
                            binding.prependProgress.visible(false)
                            delay(100)
                            mediaViewModel.clearSubscribeUserResponseData()
                        }
                        else -> {
                            binding.prependProgress.visible(false)
                            mediaViewModel.clearSubscribeUserResponseData()
                        }
                    }
                }
            }
        }
    }

    private fun refreshRelatedMediaList() {
//        binding.swipeRefreshLayout.isRefreshing = true
        mediaViewModel.fetchRelatedMedia()
    }

    override fun clickOnMediaListElement(media: Media, type: Int) {
        if (type == 0) {
            mediaViewModel.select(media, mediaViewModel.getContentType())
            mediaViewModel.setCurrentPosition(0)
            mediaViewModel.setRelatedVideoListEmpty()

            this.dismiss()
            val descriptionFragment = MediaViewerFragment()

            descriptionFragment.show(requireActivity().supportFragmentManager, descriptionFragment.tag)
        } else {
            goToUserAccount(media = media)
        }
    }

    private fun goToUserAccount(media: Media) {
        println("ACCOUNT : $media")
        val userContentViewModel: UserContentViewModel by activityViewModels()

        userContentViewModel.currentUser = media.user_id
        userContentViewModel.currentTab = 0
        userContentViewModel.currentType = mediaViewModel.getContentType()
        userContentViewModel.userFullName = media.user_full_name
        userContentViewModel.userImageUrl = media.user_icon
        userContentViewModel.userSubscribersCount = media.user_subscribers_count


        println("NAVCONTROLLER CURRENT: ${findNavController().graph}  ${findNavController().currentDestination}")
        findNavController().popBackStack(R.id.channels, true)
        findNavController().navigate(R.id.channels)
        findNavController().navigate(R.id.action_channels_to_channelUserContent)
        this.dismiss()
    }

    private fun preparePlayer() {

        if (mediaViewModel.getPlayer() != null) {
            player = mediaViewModel.getPlayer()
        } else {
            player = ExoPlayer.Builder(requireContext()).setSeekBackIncrementMs(5000)
                    .setSeekForwardIncrementMs(5000)
                    .build()

            val mediaItem = MediaItem.fromUri(media.url)
            player!!.setMediaItem(mediaItem)
            player!!.seekTo(mediaViewModel.getCurrentPosition())
            player!!.playWhenReady = playWhenReady
            player!!.prepare()

            mediaViewModel.setIsPlaying(playWhenReady)
            mediaViewModel.setPlayer(player!!)
        }

        val image = ImageView(requireContext())
        image.layoutParams = ViewGroup.LayoutParams(Config.DEFAULT_ARTWORK_WIDTH, Config.DEFAULT_ARTWORK_HEIGHT)

        Glide.with(this).load(media.poster_large)
            .listener(object : RequestListener<Drawable> {

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.player.defaultArtwork = resource
                    return true
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
            .into(image)

        binding.player.player = player

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val width: Int = Resources.getSystem().displayMetrics.widthPixels
            val height: Int = Resources.getSystem().displayMetrics.heightPixels

            binding.playerContainer.layoutParams = LinearLayout.LayoutParams(width, height)
            binding.player.layoutParams = LinearLayout.LayoutParams(width, height)
        }

        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun showMessage(msg: String?) {
        if (msg != null) {
            Snackbar.make(binding.root, msg, Snackbar.ANIMATION_MODE_SLIDE).setAction("OK!") {
            }.show()
        }
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            mediaViewModel.setCurrentPosition(exoPlayer.currentPosition)
            currentItem = exoPlayer.currentMediaItemIndex
            playWhenReady = exoPlayer.playWhenReady
            exoPlayer.release()
            player = null
        }

        mediaViewModel.setPlayer(null)
        mediaViewModel.setIsPlaying(false)

        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun showNeedUserAccountToast() {
        val toast: Toast = Toast.makeText(context, "Войдите в аккаунт для действия", Toast.LENGTH_LONG)
        toast.show()
    }

    private fun configureView() {
        val width: Int = Resources.getSystem().displayMetrics.widthPixels
        val height: Int = Resources.getSystem().displayMetrics.heightPixels
        binding.playerContainer.layoutParams = LinearLayout.LayoutParams(width, height*33/100)

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val paramsvideoContent = binding.videoContent.layoutParams
            paramsvideoContent.height = height * 67 / 100
            paramsvideoContent.width = width
            binding.videoContent.layoutParams = paramsvideoContent

            dialog?.setOnShowListener {
                val dialog = it as BottomSheetDialog
                val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.maxHeight = height
                behavior.peekHeight = height*33/100
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        } else {
            val paramsvideoContent = binding.videoContent.layoutParams
            paramsvideoContent.height = height
            paramsvideoContent.width = width
            binding.videoContent.layoutParams = paramsvideoContent

            dialog?.setOnShowListener {
                val dialog = it as BottomSheetDialog
                val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.maxHeight = height
                behavior.peekHeight = height
                behavior.state = BottomSheetBehavior.STATE_EXPANDED

                bottomSheet.layoutParams = CoordinatorLayout.LayoutParams(width, height)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behavior = BottomSheetBehavior.from(bottomSheet)

            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == STATE_HIDDEN) {
                        dismiss()
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
        }

        preparePlayer()

        if (mediaViewModel.getIsPlaying()) player?.play()
    }

    override fun onResume() {
        super.onResume()
        if (player == null) {
            preparePlayer()
        }

        if (mediaViewModel.getIsPlaying()) player?.play()
    }

    override fun onPause() {
        super.onPause()

        player?.let { mediaViewModel.setIsPlaying(it.isPlaying) }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (!requireActivity().isChangingConfigurations) {
            releasePlayer()
            mediaViewModel.setCurrentPosition(0)
            mediaViewModel.setRelatedVideoListEmpty()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("isConfigurationChange", requireActivity().isChangingConfigurations);
    }
}

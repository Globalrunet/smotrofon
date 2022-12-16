package com.mobilicos.smotrofon.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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
import com.mobilicos.smotrofon.Config
import com.mobilicos.smotrofon.R
import com.mobilicos.smotrofon.data.models.Media
import com.mobilicos.smotrofon.data.models.MediaResponse
import com.mobilicos.smotrofon.databinding.VideoViewerBinding
import com.mobilicos.smotrofon.model.Result
import com.mobilicos.smotrofon.ui.adapters.OnClickMediaListElement
import com.mobilicos.smotrofon.ui.adapters.RelatedMediaListRecyclerAdapter
import com.mobilicos.smotrofon.ui.viewmodels.MediaViewModel
import com.mobilicos.smotrofon.ui.viewmodels.UserContentViewModel
import com.mobilicos.smotrofon.util.CircleTransform
import com.mobilicos.smotrofon.util.visible
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


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

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        isConfigurationChanged = savedInstanceState?.getBoolean("isConfigurationChange") ?: false

        val mediaListObserver = Observer<Result<MediaResponse>> {
            if (it.status == Result.Status.SUCCESS && it.data != null) {

                binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.recyclerView.adapter = RelatedMediaListRecyclerAdapter(it.data.elements, this)
                binding.recyclerView.visible(true)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }

        mediaViewModel.getObservableProduct().observe(this, mediaListObserver)

        media = mediaViewModel.getSelected().value!!

        configureView()

        binding.title.text = media.title
        val videoDescription =  resources.getString(
            R.string.videoviewer_video_description,
            media.views_count,
            media.date_added
        )

        binding.videoDescription.text = videoDescription

        Picasso.get().load(media.user_icon).transform(CircleTransform()).into(binding.userIcon)
        binding.userIcon.setOnClickListener { goToUserAccount(media=media) }
        binding.userFullName.text = media.user_full_name
        binding.userInfo.text = media.user_full_name

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
            showNeedUserAccountToast()
        }

        lifecycleScope.launch {
            // We repeat on the STARTED lifecycle because an Activity may be PAUSED
            // but still visible on the screen, for example in a multi window app
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                refreshRelatedMediaList()
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshRelatedMediaList()
        }

        return binding.root
    }

    private fun refreshRelatedMediaList() {
        binding.swipeRefreshLayout.isRefreshing = true
        mediaViewModel.fetchRelatedMedia()
    }

    override fun clickOnMediaListElement(media: Media, type: Int) {
        if (type == 0) {
            println("RESULT TYPE 0")
            mediaViewModel.select(media, mediaViewModel.getContentType())
            mediaViewModel.setCurrentPosition(0)
            mediaViewModel.setRelatedVideoListEmpty()

            this.dismiss()
            val descriptionFragment = MediaViewerFragment()

            descriptionFragment.show(requireActivity().supportFragmentManager, descriptionFragment.tag)
        } else {
            goToUserAccount(media=media)
        }
    }

    private fun goToUserAccount(media: Media) {
        val userContentViewModel: UserContentViewModel by activityViewModels()

        println("RESULT TYPE 1")
        println("RESULT OPEN NEW FRAGMENT ${media.user_id}")
        userContentViewModel.currentUser = media.user_id
        userContentViewModel.currentTab = 0
        userContentViewModel.currentType = mediaViewModel.getContentType()

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
                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    println("GLIDER ${resource}")
                    binding.player.defaultArtwork = resource
                    return true
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    TODO("Not yet implemented")
                }
            })
            .into(image)

        binding.player.player = player

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val width: Int = Resources.getSystem().displayMetrics.widthPixels
            val height: Int = Resources.getSystem().displayMetrics.heightPixels

            binding.playerContainer.layoutParams = LinearLayout.LayoutParams(width, height)
            binding.player.layoutParams = LinearLayout.LayoutParams(width, height-getStatusBarHeight())
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

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
//        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    private fun showSystemUi() {
//        (requireActivity() as AppCompatActivity).supportActionBar?.show()
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
            // Находим сам bottomSheet и достаём из него Behaviour
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behavior = BottomSheetBehavior.from(bottomSheet)

            behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == STATE_HIDDEN) {
                        dismiss()
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {

                }
            })
        }

        preparePlayer()

        println("RESULT on start is playing ${mediaViewModel.getIsPlaying()}")

        if (mediaViewModel.getIsPlaying()) player?.play()

        println("RESULT ONSTART")
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (player == null) {
            preparePlayer()
        }

        println("RESULT on resume is playing ${mediaViewModel.getIsPlaying()}")
        if (mediaViewModel.getIsPlaying()) player?.play()
        println("RESULT ONRESUME")
    }

    override fun onPause() {
        super.onPause()

        showSystemUi()

        println("RESULT on pause is playing ${player!!.isPlaying}")

        player?.let { mediaViewModel.setIsPlaying(it.isPlaying) }

//        if (!requireActivity().isChangingConfigurations) {
//            player?.pause()
//        }

        println("RESULT ONPAUSE")
    }

    override fun onStop() {
        super.onStop()

        println("RESULT onStop is playing ${player!!.isPlaying}")
        println("RESULT ONSTOP")
    }

    override fun onDestroy() {
        super.onDestroy()
        println("RESULT ONDESTROY")

        if (!requireActivity().isChangingConfigurations) {
            releasePlayer()
            mediaViewModel.setCurrentPosition(0)
            mediaViewModel.setRelatedVideoListEmpty()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        println("RESULT onSaveInstanceState ${requireActivity().isChangingConfigurations}")

        outState.putBoolean("isConfigurationChange", requireActivity().isChangingConfigurations);
    }
}

package com.devmcry.imageprocessor.ui.opengl

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.devmcry.imageprocessor.R
import com.devmcry.imageprocessor.ui.opengl.recorder.OnRecordListener
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 *  @author : DevMcryYu
 *  @date : 2020/12/3 15:55
 *  @description :
 */
class OpenGLFragment : Fragment() {
    private lateinit var root: View
    private lateinit var fab: FloatingActionButton

    private lateinit var eplayerView: EPlayerView
    private lateinit var recordImageView: ImageView
    private val player by lazy {
        SimpleExoPlayer.Builder(requireContext()).build().apply {
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ALL
        }
    }

    companion object {
        const val REQ_CHOOSE_PICS = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_opengl, container, false)
        eplayerView = root.findViewById(R.id.eplayerView)
        fab = root.findViewById(R.id.fab)
        recordImageView = root.findViewById(R.id.recordImageView)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initOpenGL()
    }

    private fun initView() {
        fab.setOnClickListener { view ->
            val index = listOf("1", "3", "4", "5", "8").shuffled()[0]
            val videoSource =
                ProgressiveMediaSource.Factory(DefaultDataSourceFactory(requireContext()))
                    .createMediaSource(MediaItem.fromUri(Uri.parse("asset:///cover${index}.mp4")))
            player.setMediaSource(videoSource)
            player.prepare()
            Intent().run {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(this, getString(R.string.choose_pics)),
                    REQ_CHOOSE_PICS
                )
            }
        }
    }

    private fun initOpenGL() {
        val videoSource = ProgressiveMediaSource.Factory(DefaultDataSourceFactory(requireContext()))
            .createMediaSource(MediaItem.fromUri(Uri.parse("asset:///cover8.mp4")))

        // Prepare the player with the source.
        player.setMediaSource(videoSource)
        player.prepare()

        // NO.1.1 player init render
        eplayerView.initRenderer(context, OnRecordListener {
            recordImageView.setImageBitmap(it)
        })
        eplayerView.setSimpleExoPlayer(player)
        // NO.1.2 player set filter

        if (context != null) {
            var bitmap: Bitmap = BitmapFactory.decodeResource(requireContext().resources, R.drawable.test1)
            var ratio = bitmap.width.toFloat() / bitmap.height
            var height = (requireContext().resources.displayMetrics.widthPixels / ratio).toInt()
            eplayerView.buildContentFilter(bitmap)

            eplayerView.layoutParams.height = height
            eplayerView.requestLayout()
        }
        eplayerView.buildAlphaFrameFilter()
        eplayerView.onResume()
    }


    // 选完图片之后重新绘制
    private fun updateGLSurfaceView(bitmap: Bitmap) {
        val ratio = bitmap.width.toFloat() / bitmap.height
        val height = (requireContext().resources.displayMetrics.widthPixels / ratio).toInt()
        eplayerView.buildContentFilter(bitmap)
        eplayerView.layoutParams.height = height
        eplayerView.requestLayout()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CHOOSE_PICS -> {
                when (resultCode) {
                    RESULT_OK -> {
                        data?.data?.run {
                            context?.contentResolver?.openInputStream(this)?.run {
                                BitmapFactory.decodeStream(this).also {
                                    updateGLSurfaceView(it)
                                    close()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
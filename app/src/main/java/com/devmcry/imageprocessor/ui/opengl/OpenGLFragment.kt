package com.devmcry.imageprocessor.ui.opengl

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.devmcry.imageprocessor.R
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
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

    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var eplayerView: EPlayerView
    private lateinit var glRender: OpenGLRender

    companion object {
        const val REQ_CHOOSE_PICS = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_opengl, container, false)
        glSurfaceView = root.findViewById(R.id.surfaceView)
        eplayerView = root.findViewById(R.id.eplayerView)
        fab = root.findViewById(R.id.fab)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initOpenGL()
    }

    private fun initView() {
        fab.setOnClickListener { view ->
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
//        val videoRenderer = MovieRenderer()
//        val afd  = context?.assets?.openFd("cover.mp4") ?: return
//
//        videoRenderer.mediaPlayer = MediaPlayer().apply {
//            setDataSource(afd.fileDescriptor,afd.startOffset,afd.length)
//            setOnCompletionListener {
//                it.start()
//            }
//        }
//        glSurfaceView.runCatching {
//            setEGLContextClientVersion(3)
//            setRenderer(videoRenderer)
//            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
//        }.onFailure {
//            it.printStackTrace()
//        }
//
        glSurfaceView.runCatching {
            glRender = OpenGLRender(requireContext())
            setEGLContextClientVersion(3)
            setRenderer(glRender)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
//            updateGLSurfaceView(BitmapFactory.decodeFile("/storage/emulated/0/Download/wallhaven-lmkk2y.jpg"))
        }.onFailure {
            it.printStackTrace()
        }
        val videoSource = ProgressiveMediaSource.Factory(DefaultDataSourceFactory(requireContext()))
            .createMediaSource(Uri.parse("asset:///cover1.mp4"))

        // SimpleExoPlayer
        val player = ExoPlayerFactory.newSimpleInstance(requireContext())
        // Prepare the player with the source.
        player.prepare(videoSource)
        player.playWhenReady = true
        player.repeatMode = Player.REPEAT_MODE_ALL
        eplayerView.setSimpleExoPlayer(player)
        eplayerView.setPlayerScaleType(PlayerScaleType.RESIZE_NONE)
        eplayerView.setGlFilter(AlphaFrameFilter())
        eplayerView.onResume()
    }

    private fun updateGLSurfaceView(bitmap: Bitmap) {
        glSurfaceView.runCatching {
            postDelayed({
                val ratio = bitmap.width.toFloat() / bitmap.height
                val height = (glSurfaceView.measuredWidth / ratio).toInt()
                glSurfaceView.layoutParams.height = height
                eplayerView.layoutParams.height = height
                requestLayout()
                queueEvent {
                    glRender.mBitmap = bitmap
                }
            }, 10)
        }
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
package com.devmcry.imageprocessor.ui.dynamic

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.devmcry.imageprocessor.R
import com.devmcry.imageprocessor.ui.opengl.AlphaFrameFilter
import com.devmcry.imageprocessor.ui.opengl.EPlayerView
import com.devmcry.imageprocessor.ui.opengl.OpenGLRender
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory

/**
 *  @author : DevMcryYu
 *  @date : 2020/12/9 11:21
 *  @description :
 */
class DynamicFilterContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private val playerView: EPlayerView
    private val glSurfaceView: GLSurfaceView
    private lateinit var glRender: OpenGLRender

    init {
        val root = LayoutInflater.from(context).inflate(R.layout.container_dynamic_filter, this)
        playerView = root.findViewById(R.id.playerView)
        glSurfaceView = root.findViewById(R.id.contentView)
        glSurfaceView.runCatching {
            glRender = OpenGLRender(context)
            setEGLContextClientVersion(3)
            setRenderer(glRender)
            renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        }.onFailure {
            it.printStackTrace()
        }

        val videoSource = ProgressiveMediaSource.Factory(DefaultDataSourceFactory(context))
            .createMediaSource(Uri.parse("asset:///cover1.mp4"))

        // SimpleExoPlayer
        val player = ExoPlayerFactory.newSimpleInstance(context)
        // Prepare the player with the source.
        player.prepare(videoSource)
        player.playWhenReady = true
        player.repeatMode = Player.REPEAT_MODE_ALL
        playerView.setSimpleExoPlayer(player)
        playerView.setGlFilter(AlphaFrameFilter())
        playerView.onResume()
    }

    fun onResume() {
        playerView.onResume()
    }

    fun onPause() {
        playerView.onPause()
    }

    fun updateGLSurfaceView(bitmap: Bitmap) {
        glSurfaceView.runCatching {
            postDelayed({
                val ratio = bitmap.width.toFloat() / bitmap.height
                val height = (context.resources.displayMetrics.widthPixels / ratio).toInt()
                glSurfaceView.layoutParams.height = height
                requestLayout()
                queueEvent {
                    glRender.mBitmap = bitmap
                    requestRender()
                }
            }, 100)
        }
    }
}
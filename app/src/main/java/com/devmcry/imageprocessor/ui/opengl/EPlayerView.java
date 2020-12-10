package com.devmcry.imageprocessor.ui.opengl;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.devmcry.imageprocessor.ui.opengl.filter.GlFilter;
import com.devmcry.imageprocessor.ui.opengl.renderer.EPlayerRenderer;
import com.devmcry.imageprocessor.ui.opengl.util.EConfigChooser;
import com.devmcry.imageprocessor.ui.opengl.util.EContextFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.video.VideoListener;

/**
 * Created by sudamasayuki on 2017/05/16.
 */
public class EPlayerView extends GLSurfaceView implements VideoListener {

    private final static String TAG = EPlayerView.class.getSimpleName();

    private EPlayerRenderer renderer;
    private SimpleExoPlayer player;

    private float videoAspect = 1f;

    public EPlayerView(Context context) {
        this(context, null);
    }

    public EPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setEGLContextFactory(new EContextFactory());
        setEGLConfigChooser(new EConfigChooser());

        setZOrderOnTop(true);
        setZOrderMediaOverlay(true);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.RGBA_8888);
    }

    public void initRenderer() {
        if (renderer == null) {
            renderer = new EPlayerRenderer(this);
        }
        setRenderer(renderer);
    }


    public EPlayerView setSimpleExoPlayer(SimpleExoPlayer player) {
        if (this.player != null) {
            this.player.release();
            this.player = null;
        }
        this.player = player;
        this.player.addVideoListener(this);
        this.renderer.setSimpleExoPlayer(player);
        return this;
    }

    public void setGlFilter(GlFilter glFilter) {
        renderer.setGlFilter(glFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        renderer.release();
    }

    //////////////////////////////////////////////////////////////////////////
    // SimpleExoPlayer.VideoListener

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        // Log.d(TAG, "width = " + width + " height = " + height + " unappliedRotationDegrees = " + unappliedRotationDegrees + " pixelWidthHeightRatio = " + pixelWidthHeightRatio);
        videoAspect = ((float) width / height) * pixelWidthHeightRatio;
        // Log.d(TAG, "videoAspect = " + videoAspect);
        requestLayout();
    }

    @Override
    public void onRenderedFirstFrame() {
        // do nothing
    }
}

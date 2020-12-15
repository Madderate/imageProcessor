package com.devmcry.imageprocessor.ui.opengl.renderer;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES30;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import com.devmcry.imageprocessor.ui.opengl.EPlayerView;
import com.devmcry.imageprocessor.ui.opengl.filter.AlphaFrameFilter;
import com.devmcry.imageprocessor.ui.opengl.filter.ContentFilter;
import com.devmcry.imageprocessor.ui.opengl.filter.GlPreviewFilter;
import com.devmcry.imageprocessor.ui.opengl.recorder.PixelRecorder;
import com.devmcry.imageprocessor.ui.opengl.util.EFramebufferObject;
import com.devmcry.imageprocessor.ui.opengl.util.ESurfaceTexture;
import com.devmcry.imageprocessor.ui.opengl.util.EglUtil;
import com.google.android.exoplayer2.SimpleExoPlayer;

import javax.microedition.khronos.egl.EGLConfig;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
import static android.opengl.GLES30.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES30.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES30.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES30.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES30.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES30.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES30.GL_LINEAR;
import static android.opengl.GLES30.GL_NEAREST;
import static android.opengl.GLES30.GL_TEXTURE_2D;

/**
 * Created by sudamasayuki on 2017/05/16.
 */

public class EPlayerRenderer extends EFrameBufferObjectRenderer implements SurfaceTexture.OnFrameAvailableListener {

    final private static  String TAG = EPlayerRenderer.class.getSimpleName();

    private boolean updatePreviewSurface = false;


    private ESurfaceTexture previewTexture;
    private GlPreviewFilter previewFilter;
    private AlphaFrameFilter alphaFrameFilter;
    private ContentFilter contentFilter;

    private float coverSourceWHRatio = 1080 / 1920f;

    public void setPixelRecorder(PixelRecorder pixelRecorder) {
        this.pixelRecorder = pixelRecorder;
    }

    // 录制视频最后输出
    private PixelRecorder pixelRecorder;


    private final EPlayerView glPreview;

    private SimpleExoPlayer simpleExoPlayer;

    public EPlayerRenderer(EPlayerView glPreview) {
        super();
        this.glPreview = glPreview;
    }

    public void buildAlphaFrameFilter() {
        glPreview.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (alphaFrameFilter != null) {
                    alphaFrameFilter.release();
                }
                alphaFrameFilter = new AlphaFrameFilter();
                glPreview.requestRender();
            }
        });
    }

    public void buildContentFilter(Bitmap bitmap) {
        glPreview.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (contentFilter != null) {
                    contentFilter.release();
                }
                contentFilter = new ContentFilter(bitmap);
                glPreview.requestRender();
            }
        });
    }


    @Override
    public void onSurfaceCreated(final EGLConfig config) {
        // NO.2 初始化render
        // NO.2.1 清屏
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES30.glClear(GL_COLOR_BUFFER_BIT);

        final int[] args = new int[1];

        previewTexture = EglUtil.INSTANCE.createSurfaceTexture(args, GL_TEXTURE_EXTERNAL_OES);
        previewTexture.setOnFrameAvailableListener(EPlayerRenderer.this);

        // GL_TEXTURE_EXTERNAL_OES
        previewFilter = new GlPreviewFilter(GL_TEXTURE_EXTERNAL_OES, coverSourceWHRatio);


        if (pixelRecorder != null) {
            pixelRecorder.startRecord();
        }

        Surface surface = new Surface(previewTexture.getSurfaceTexture());
        this.simpleExoPlayer.setVideoSurface(surface);

        synchronized (this) {
            updatePreviewSurface = false;
        }

//        GLES30.glGetIntegerv(GL_MAX_TEXTURE_SIZE, args, 0);
    }



    @Override
    public void onSurfaceChanged(final int width, final int height) {
        Log.d(TAG, "onSurfaceChanged width = " + width + "  height = " + height);

        previewFilter.setupAfterSizeChange(width, height);
        if (alphaFrameFilter != null) {
            alphaFrameFilter.setupAfterSizeChange(width, height);
        }

        if (contentFilter != null) {
            contentFilter.setupAfterSizeChange(width, height);
        }

        if (pixelRecorder != null) {
            pixelRecorder.initPixelBuffer(width, height);
        }
    }

    @Override
    public void onDrawFrame(final EFramebufferObject bufferObject) {
        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glClear(GL_COLOR_BUFFER_BIT);
        // 关键：修正视频颜色显示
        GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ZERO);

        synchronized (this) {
            if (updatePreviewSurface) {
                previewTexture.updateTexImage();
                previewTexture.getTransformMatrix(previewFilter.getSTMatrix());
                updatePreviewSurface = false;
            }
        }

        EFramebufferObject nextBuffer1 = EglUtil.INSTANCE.bufferPipe(previewFilter, bufferObject, previewTexture.getTextureId());
        EFramebufferObject nextBuffer2 = EglUtil.INSTANCE.bufferPipe(alphaFrameFilter, nextBuffer1);

        contentFilter.setBufferObject(bufferObject);
        EglUtil.INSTANCE.bufferPipe(contentFilter, nextBuffer2);

        // 录制每一帧
        if (pixelRecorder != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            pixelRecorder.bindPixelBuffer();
        }

        GLES30.glDisable(GLES30.GL_BLEND);
    }

    @Override
    public synchronized void onFrameAvailable(final SurfaceTexture previewTexture) {
        updatePreviewSurface = true;
        glPreview.requestRender();
    }


    public void setSimpleExoPlayer(SimpleExoPlayer simpleExoPlayer) {
        this.simpleExoPlayer = simpleExoPlayer;
    }

    public void release() {
        if (alphaFrameFilter != null) {
            alphaFrameFilter.release();
        }
        if (contentFilter != null) {
            contentFilter.release();
        }
        if (previewFilter != null) {
            previewFilter.release();
        }
    }

}

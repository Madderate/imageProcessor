package com.devmcry.imageprocessor.ui.opengl.renderer;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.devmcry.imageprocessor.ui.opengl.ContentFilter;
import com.devmcry.imageprocessor.ui.opengl.EPlayerView;
import com.devmcry.imageprocessor.ui.opengl.filter.AlphaFrameFilter;
import com.devmcry.imageprocessor.ui.opengl.filter.GlPreviewFilter;
import com.devmcry.imageprocessor.ui.opengl.util.EFramebufferObject;
import com.devmcry.imageprocessor.ui.opengl.util.EglUtil;
import com.google.android.exoplayer2.SimpleExoPlayer;

import org.jetbrains.annotations.NotNull;

import javax.microedition.khronos.egl.EGLConfig;

import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES30.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES30.GL_LINEAR;
import static android.opengl.GLES30.GL_MAX_TEXTURE_SIZE;
import static android.opengl.GLES30.GL_NEAREST;
import static android.opengl.GLES30.GL_TEXTURE_2D;
import static android.opengl.GLES30.glViewport;
import static com.devmcry.imageprocessor.ui.opengl.util.EglUtil.NO_TEXTURE;

/**
 * Created by sudamasayuki on 2017/05/16.
 */

public class EPlayerRenderer extends EFrameBufferObjectRenderer implements SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG =EPlayerRenderer.class.getSimpleName();


    private ESurfaceTexture previewTexture;
    private boolean updateSurface = false;


    private float[] MVPMatrix = new float[16];
    private float[] ProjMatrix = new float[16];
    private float[] MMatrix = new float[16];
    private float[] VMatrix = new float[16];
    private float[] STMatrix = new float[16];


    private int previewTextureId;
    private GlPreviewFilter previewFilter;

    private ContentFilter contentFilter;
    private EFramebufferObject contentBufferObject;
    private Bitmap contentBitmap;

    private AlphaFrameFilter alphaFrameFilter;
    private EFramebufferObject alphaFrameBufferObject;


    private final EPlayerView glPreview;

    private float textureRatio = 1080/1920f;

    private SimpleExoPlayer simpleExoPlayer;

    public EPlayerRenderer(EPlayerView glPreview) {
        super();
        Matrix.setIdentityM(STMatrix, 0);
        this.glPreview = glPreview;
    }

    public void setAlphaFrameFilter(final AlphaFrameFilter filter) {
        glPreview.queueEvent(new Runnable() {
            @Override
            public void run() {

                if (alphaFrameFilter != null) {
                    alphaFrameFilter.release();
                    alphaFrameFilter = null;
                }

                if (alphaFrameBufferObject != null) {
                    alphaFrameBufferObject.release();
                    alphaFrameBufferObject = null;
                }

                alphaFrameFilter = filter;
                alphaFrameBufferObject = new EFramebufferObject();
                glPreview.requestRender();
            }
        });
    }


    public void setContentFilter(@NotNull final ContentFilter filter, Bitmap bitmap) {
        glPreview.queueEvent(new Runnable() {
            @Override
            public void run() {

                if (contentFilter != null) {
                    contentFilter.release();
                    contentFilter = null;
                }

                if (contentBufferObject != null) {
                    contentBufferObject.release();
                    contentBufferObject = null;
                }

                contentFilter = filter;
                contentBufferObject = new EFramebufferObject();
                contentBitmap = bitmap;

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

        // NO.2.2 生成纹理，记录纹理ID
        GLES30.glGenTextures(args.length, args, 0);
        previewTextureId = args[0];

        previewTexture = new ESurfaceTexture(previewTextureId);
        previewTexture.setOnFrameAvailableListener(EPlayerRenderer.this::onFrameAvailable);

        // external  target GL_TEXTURE_EXTERNAL_OES 为纹理单元目标类型
        // NO.2.3 绑定 external 纹理
        GLES30.glBindTexture(previewTexture.getTextureTarget(), previewTextureId);
        // NO.2.4 配置 external 纹理过滤模式和环绕方式
        GLES30.glTexParameterf(previewTexture.getTextureTarget(), GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GLES30.glTexParameterf(previewTexture.getTextureTarget(), GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        GLES30.glTexParameteri(previewTexture.getTextureTarget(), GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(previewTexture.getTextureTarget(), GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);


        // NO.2.5 绑定 normal 纹理
        GLES30.glBindTexture(GL_TEXTURE_2D, 0);

        // GL_TEXTURE_EXTERNAL_OES
        previewFilter = new GlPreviewFilter(previewTexture.getTextureTarget());
        previewFilter.setup();

        Surface surface = new Surface(previewTexture.getSurfaceTexture());
        this.simpleExoPlayer.setVideoSurface(surface);

        Matrix.setLookAtM(VMatrix, 0,
                0.0f, 0.0f, 5.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f
        );

        synchronized (this) {
            updateSurface = false;
        }



        if (contentFilter != null && contentBufferObject != null && contentBitmap != null) {
            int textureId = EglUtil.INSTANCE.loadTexture(contentBitmap, NO_TEXTURE, GLES30.GL_TEXTURE_2D, false);
            contentFilter.setContentTextureId(textureId);
            contentFilter.setup();
        }

        if (alphaFrameFilter != null && alphaFrameBufferObject != null) {
            alphaFrameFilter.setup();
        }

        GLES30.glGetIntegerv(GL_MAX_TEXTURE_SIZE, args, 0);

    }

    @Override
    public void onSurfaceChanged(final int width, final int height) {
        Log.d(TAG, "onSurfaceChanged width = " + width + "  height = " + height);

        if (contentFilter != null && contentBufferObject != null) {
            contentBufferObject.setup(width, height);
        }

        if (alphaFrameFilter != null && alphaFrameBufferObject != null) {
            alphaFrameBufferObject.setup(width, (int)(width/textureRatio) );
        }



        float bottom = -textureRatio * height / width;
        float top = 2 + bottom;

        Matrix.orthoM(ProjMatrix, 0, -textureRatio, textureRatio, bottom,  top, 5, 7);
        Matrix.setIdentityM(MMatrix, 0);
    }

    @Override
    public void onDrawFrame(final EFramebufferObject bufferObject) {
        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA);


        synchronized (this) {
            if (updateSurface) {
                previewTexture.updateTexImage();
                previewTexture.getTransformMatrix(STMatrix);
                updateSurface = false;
            }
        }

//        if (isNewFilter) {
//            if (glFilter != null) {
//                glFilter.setup();
////                glFilter.setFrameSize(fbo.getWidth(), fbo.getHeight());
//            }
//            isNewFilter = false;
//        }

        if (alphaFrameFilter != null && alphaFrameBufferObject != null) {
            alphaFrameBufferObject.enable();
            glViewport(0, 0, alphaFrameBufferObject.getWidth(), alphaFrameBufferObject.getHeight());
        }

        GLES30.glClear(GL_COLOR_BUFFER_BIT);

        Matrix.multiplyMM(MVPMatrix, 0, VMatrix, 0, MMatrix, 0);
        Matrix.multiplyMM(MVPMatrix, 0, ProjMatrix, 0, MVPMatrix, 0);

        previewFilter.draw(previewTextureId, MVPMatrix, STMatrix, textureRatio);




//        if (contentFilter != null && contentBufferObject != null) {
//            bufferObject.enable();
//            GLES30.glClear(GL_COLOR_BUFFER_BIT);
//            contentFilter.draw(contentFilter.getContentTextureId(), alphaFrameBufferObject);
//        }

        if (alphaFrameFilter != null && alphaFrameBufferObject != null) {
            bufferObject.enable();
            GLES30.glClear(GL_COLOR_BUFFER_BIT);
            alphaFrameFilter.draw(alphaFrameBufferObject.getTexName(), alphaFrameBufferObject);
        }

    }

    @Override
    public synchronized void onFrameAvailable(final SurfaceTexture previewTexture) {
        updateSurface = true;
        glPreview.requestRender();
    }

    public void setSimpleExoPlayer(SimpleExoPlayer simpleExoPlayer) {
        this.simpleExoPlayer = simpleExoPlayer;
    }

    public void release() {
        if (alphaFrameFilter != null) {
            alphaFrameFilter.release();
        }
        if (alphaFrameBufferObject != null) {
            alphaFrameBufferObject.release();
        }
        if (previewTexture != null) {
            previewTexture.release();
        }
    }

}

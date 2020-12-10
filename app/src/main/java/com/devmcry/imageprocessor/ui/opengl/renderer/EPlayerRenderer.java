package com.devmcry.imageprocessor.ui.opengl.renderer;

import android.graphics.SurfaceTexture;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.devmcry.imageprocessor.ui.opengl.util.EFramebufferObject;
import com.devmcry.imageprocessor.ui.opengl.EPlayerView;
import com.devmcry.imageprocessor.ui.opengl.filter.GlFilter;
import com.devmcry.imageprocessor.ui.opengl.filter.GlPreviewFilter;
import com.google.android.exoplayer2.SimpleExoPlayer;

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

/**
 * Created by sudamasayuki on 2017/05/16.
 */

public class EPlayerRenderer extends EFrameBufferObjectRenderer implements SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG =EPlayerRenderer.class.getSimpleName();


    private ESurfaceTexture previewTexture;
    private boolean updateSurface = false;

    private int texName;

    private float[] MVPMatrix = new float[16];
    private float[] ProjMatrix = new float[16];
    private float[] MMatrix = new float[16];
    private float[] VMatrix = new float[16];
    private float[] STMatrix = new float[16];


    private EFramebufferObject filterFramebufferObject;
    private GlPreviewFilter previewFilter;

    private GlFilter glFilter;
    private final EPlayerView glPreview;

    private float textureRatio = 1080/1920f;

    private SimpleExoPlayer simpleExoPlayer;

    public EPlayerRenderer(EPlayerView glPreview) {
        super();
        Matrix.setIdentityM(STMatrix, 0);
        this.glPreview = glPreview;
    }

    public void setGlFilter(final GlFilter filter) {
        glPreview.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (glFilter != null) {
                    glFilter.release();

                    glFilter = null;
                }
                glFilter = filter;
//                isNewFilter = true;
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
        texName = args[0];


        // previewTexture 仅仅提供了 onFrameAvailable 回调，不纳入主流程
        previewTexture = new ESurfaceTexture(texName);
        previewTexture.setOnFrameAvailableListener(this);


        // external  target GL_TEXTURE_EXTERNAL_OES 为纹理单元目标类型
        // NO.2.3 绑定 external 纹理
        GLES30.glBindTexture(previewTexture.getTextureTarget(), texName);
        // NO.2.4 配置 external 纹理过滤模式和环绕方式
        GLES30.glTexParameterf(previewTexture.getTextureTarget(), GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GLES30.glTexParameterf(previewTexture.getTextureTarget(), GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        GLES30.glTexParameteri(previewTexture.getTextureTarget(), GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(previewTexture.getTextureTarget(), GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);


        // NO.2.5 绑定 normal 纹理
        GLES30.glBindTexture(GL_TEXTURE_2D, 0);

        filterFramebufferObject = new EFramebufferObject();
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

        if (glFilter != null) {
//            isNewFilter = true;
            glFilter.setup();
        }

        GLES30.glGetIntegerv(GL_MAX_TEXTURE_SIZE, args, 0);

    }

    @Override
    public void onSurfaceChanged(final int width, final int height) {
        Log.d(TAG, "onSurfaceChanged width = " + width + "  height = " + height);
        filterFramebufferObject.setup(width, (int)(width/textureRatio) );
//        previewFilter.setFrameSize(1080, 1920);
//        if (glFilter != null) {
//            glFilter.setFrameSize(1080, 1920);
//        }

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

        if (glFilter != null) {
            filterFramebufferObject.enable();
            glViewport(0, 0, filterFramebufferObject.getWidth(), filterFramebufferObject.getHeight());
        }

        GLES30.glClear(GL_COLOR_BUFFER_BIT);

        Matrix.multiplyMM(MVPMatrix, 0, VMatrix, 0, MMatrix, 0);
        Matrix.multiplyMM(MVPMatrix, 0, ProjMatrix, 0, MVPMatrix, 0);

        previewFilter.draw(texName, MVPMatrix, STMatrix, textureRatio);

        if (glFilter != null) {
            bufferObject.enable();
            GLES30.glClear(GL_COLOR_BUFFER_BIT);
            glFilter.draw(filterFramebufferObject.getTexName(), bufferObject);
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
        if (glFilter != null) {
            glFilter.release();
        }
        if (previewTexture != null) {
            previewTexture.release();
        }
    }

}

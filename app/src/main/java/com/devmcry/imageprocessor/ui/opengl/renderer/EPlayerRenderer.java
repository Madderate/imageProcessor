package com.devmcry.imageprocessor.ui.opengl.renderer;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;
import android.view.Surface;

import com.devmcry.imageprocessor.ui.opengl.EPlayerView;
import com.devmcry.imageprocessor.ui.opengl.filter.AlphaFrameFilter;
import com.devmcry.imageprocessor.ui.opengl.filter.ContentFilter;
import com.devmcry.imageprocessor.ui.opengl.filter.GlPreviewFilter;
import com.devmcry.imageprocessor.ui.opengl.recorder.PixelRecorder;
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

    final private static  String TAG = EPlayerRenderer.class.getSimpleName();

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
    private Bitmap contentBitmap;

    private AlphaFrameFilter alphaFrameFilter;
    private EFramebufferObject videoFrameBufferObject;
    private EFramebufferObject alphaFrameBufferObject;
    private float alphaFrameSourceRatio = 1080 / 1920f;

    // 录制视频最后输出
    private PixelRecorder pixelRecorder;

    private final EPlayerView glPreview;

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

                if (videoFrameBufferObject != null) {
                    videoFrameBufferObject.release();
                    videoFrameBufferObject = null;
                }
                if (alphaFrameBufferObject != null) {
                    alphaFrameBufferObject.release();
                    alphaFrameBufferObject = null;
                }

                alphaFrameFilter = filter;
                videoFrameBufferObject = new EFramebufferObject();
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

                contentFilter = filter;
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
        previewTexture.setOnFrameAvailableListener(EPlayerRenderer.this);

        // external  target GL_TEXTURE_EXTERNAL_OES 为纹理单元目标类型
        // NO.2.3 绑定 external 纹理
        GLES30.glBindTexture(previewTexture.getTextureTarget(), previewTextureId);
        // NO.2.4 配置 external 纹理过滤模式和环绕方式
        GLES30.glTexParameterf(previewTexture.getTextureTarget(), GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        GLES30.glTexParameterf(previewTexture.getTextureTarget(), GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        GLES30.glTexParameteri(previewTexture.getTextureTarget(), GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(previewTexture.getTextureTarget(), GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        // 解绑 texture
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


        pixelRecorder = new PixelRecorder();
        pixelRecorder.startRecord();

        GLES30.glGetIntegerv(GL_MAX_TEXTURE_SIZE, args, 0);
    }

    @Override
    public void onSurfaceChanged(final int width, final int height) {
        Log.d(TAG, "onSurfaceChanged width = " + width + "  height = " + height);

        if (contentFilter != null && contentBitmap != null) {
            int textureId = EglUtil.INSTANCE.loadTexture(contentBitmap, NO_TEXTURE, GLES30.GL_TEXTURE_2D, false);

            contentFilter.setContentTextureId(textureId);
            contentFilter.setup();
        }

        if (alphaFrameFilter != null) {
            alphaFrameFilter.setup();
            videoFrameBufferObject.setup(width, (int) (width / alphaFrameSourceRatio));
            alphaFrameBufferObject.setup(width, height);
        }

        pixelRecorder.initPixelBuffer(width, height);

        // 关键：视频居中
        // 由于视频和图片是顶点对齐，为了将视频居中需要对视频渲染的正交矩阵进行重新运算
        // bottom = ratioVideo / ratioImage
        // top = 2 + bottom
        // left right bottom top 取值 -1 ～ 1
        float bottom = -alphaFrameSourceRatio * height / width;
        float top = 2 + bottom;

        Matrix.orthoM(ProjMatrix, 0, -alphaFrameSourceRatio, alphaFrameSourceRatio, bottom, top, 5, 7);
        Matrix.setIdentityM(MMatrix, 0);
    }

    @Override
    public void onDrawFrame(final EFramebufferObject bufferObject) {
        GLES30.glEnable(GLES30.GL_BLEND);

        synchronized (this) {
            if (updateSurface) {
                previewTexture.updateTexImage();
                previewTexture.getTransformMatrix(STMatrix);
                updateSurface = false;
            }
        }

        GLES30.glClear(GL_COLOR_BUFFER_BIT);

        Matrix.multiplyMM(MVPMatrix, 0, VMatrix, 0, MMatrix, 0);
        Matrix.multiplyMM(MVPMatrix, 0, ProjMatrix, 0, MVPMatrix, 0);

        if (previewFilter != null) {
            // 开启 videoFrameBuffer 接收原始视频的帧数据
            videoFrameBufferObject.enable();
            glViewport(0, 0, videoFrameBufferObject.getWidth(), videoFrameBufferObject.getHeight());
            // 将视频帧绘制到 fbo 上
            previewFilter.draw(previewTextureId, MVPMatrix, STMatrix, alphaFrameSourceRatio);
        }

        if (alphaFrameFilter != null) {
            // 开启 alphaFrameBufferObject 接收视频帧合并后的产物
            alphaFrameBufferObject.enable();
            glViewport(0, 0, videoFrameBufferObject.getWidth(), videoFrameBufferObject.getHeight());
            // 将合并视频帧绘制到 fbo 上
            alphaFrameFilter.draw(videoFrameBufferObject.getTexName(), null);
        }

        if (contentFilter != null) {
            // 关键：修正视频颜色显示
            GLES30.glBlendFunc(GLES30.GL_ONE, GLES30.GL_ZERO);
            // 开启 bufferObject 接收最终产物
            bufferObject.enable();
            glViewport(0, 0, bufferObject.getWidth(), bufferObject.getHeight());
            // 将最终视频帧绘制到 fbo 上
            contentFilter.draw(alphaFrameBufferObject.getTexName(), null);
        }


        // 录制每一帧
        pixelRecorder.bindPixelBuffer();

        GLES30.glDisable(GLES30.GL_BLEND);
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
        if (videoFrameBufferObject != null) {
            videoFrameBufferObject.release();
        }
        if (alphaFrameBufferObject != null) {
            alphaFrameBufferObject.release();
        }
        if (previewTexture != null) {
            previewTexture.release();
        }
    }

}

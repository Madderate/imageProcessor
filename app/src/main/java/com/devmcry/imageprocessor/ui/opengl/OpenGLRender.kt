package com.devmcry.imageprocessor.ui.opengl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.devmcry.imageprocessor.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.min
import kotlin.math.round

/**
 *  @author : DevMcryYu
 *  @date : 2020/12/3 17:39
 *  @description :
 */
// 绘制图片的原理：定义一组矩形区域的顶点，然后根据纹理坐标把图片作为纹理贴在该矩形区域内。

// 原始的矩形区域的顶点坐标，因为后面使用了顶点法绘制顶点，所以不用定义绘制顶点的索引。无论窗口的大小为多少，在OpenGL二维坐标系中都是为下面表示的矩形区域
private val CUBE = floatArrayOf(
    -1.0f, -1.0f, // v1
    1.0f, -1.0f,  // v2
    -1.0f, 1.0f,  // v3
    1.0f, 1.0f,   // v4
)

// 纹理也有坐标系，称 UV 坐标，或者 ST 坐标。UV 坐标定义为左上角（0，0），右下角（1，1），一张图片无论大小为多少，在 UV 坐标系中都是图片左上角为（0，0），右下角（1，1）
// 纹理坐标，每个坐标的纹理采样对应上面顶点坐标。
private val TEXTURE_NO_ROTATION = floatArrayOf(
    0.0f, 1.0f, // v1
    1.0f, 1.0f, // v2
    0.0f, 0.0f, // v3
    1.0f, 0.0f, // v4
)

class OpenGLRender(private val mContext: Context) : GLSurfaceView.Renderer,
    OnFrameAvailableListener {
    var mBitmap: Bitmap? = null
        set(value) {
            if (value != null) {
                mImageWidth = value.width
                mImageHeight = value.height
                adjustImageScaling()
                // 把图片数据加载进GPU，生成对应的纹理id
                mGLTextureId = value.loadTexture(mGLTextureId)
            }
            field = value
        }
    private var mGLTextureId: Int = 0 // 纹理 id
    private var mImageWidth: Int = 0
    private var mImageHeight: Int = 0
    private var mOutputWidth: Int = 0
    private var mOutputHeight: Int = 0
    private val mGLImageHandler by lazy { OpenGLImageShader() }

    private var mGLFilterTextureId: Int = 0 // 滤镜纹理 id
    private val mGLDynamicFilterHandler by lazy { OpenGLDynamicFilterShader() }

    // 顶点数组缓冲器
    private val mGLCubeBuffer by lazy {
        ByteBuffer.allocateDirect(CUBE.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
    }

    // 纹理数组缓冲器
    private val mGLTextureBuffer by lazy {
        ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
    }


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)

        // 加载图片
        mBitmap = ResourcesCompat.getDrawable(mContext.resources, R.color.black, mContext.theme)
            ?.toBitmap(1, 1)

        mGLCubeBuffer.put(CUBE).position(0)
        mGLTextureBuffer.put(TEXTURE_NO_ROTATION).position(0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mOutputWidth = width
        mOutputHeight = height
        GLES30.glViewport(0, 0, width, height) // 调整窗口大小
        adjustImageScaling() // 调整图片显示大小。如果不调用该方法，则会导致图片整个拉伸到填充窗口显示区域
    }

    override fun onDrawFrame(gl: GL10?) {
//        GLES30.glEnable(GLES30.GL_BLEND)
//        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        // 根据纹理id，顶点和纹理坐标数据绘制图片
        if (mGLTextureId != NO_TEXTURE) {
            mGLImageHandler.onDraw(mGLTextureId, mGLCubeBuffer, mGLTextureBuffer)
        }
    }

    private fun adjustImageScaling() {
        val outputWidth = mOutputWidth.toFloat()
        val outputHeight = mOutputHeight.toFloat()

        val ratioMax = min(outputWidth / mImageWidth, outputHeight / mImageHeight)
        // 居中后图片显示的大小
        val imageWidthNew = round(mImageWidth * ratioMax)
        val imageHeightNew = round(mImageHeight * ratioMax)

        // 图片被拉伸的比例
        val ratioWidth = outputWidth / imageWidthNew
        val ratioHeight = outputHeight / imageHeightNew
        // 根据拉伸比例还原顶点
        val cube = floatArrayOf(
            CUBE[0] / ratioWidth, CUBE[1] / ratioHeight,
            CUBE[2] / ratioWidth, CUBE[3] / ratioHeight,
            CUBE[4] / ratioWidth, CUBE[5] / ratioHeight,
            CUBE[6] / ratioWidth, CUBE[7] / ratioHeight
        )

        mGLCubeBuffer.clear()
        mGLCubeBuffer.put(cube).position(0)
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        TODO("Not yet implemented")
    }
}
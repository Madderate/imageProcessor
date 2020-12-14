package com.devmcry.imageprocessor.ui.dynamic

import android.opengl.GLES30
import com.devmcry.imageprocessor.ui.opengl.util.EglUtil
import java.nio.FloatBuffer

/**
 *  @author : DevMcryYu
 *  @date : 2020/12/3 18:54
 *  @description :
 */

// 数据中有多少个顶点，管线就调用多少次顶点着色器
// position 顶点着色器的顶点坐标,由外部程序传入
// inputTextureCoordinate 传入的纹理坐标
// textureCoordinate 最终顶点位置
const val NO_FILTER_VERTEX_SHADER = """
    attribute vec4 position;
    attribute vec4 inputTextureCoordinate;
        
    varying vec2 textureCoordinate;
        
    void main()
    {
        gl_Position = position;
        textureCoordinate = inputTextureCoordinate.xy;
    }
    """

// 光栅化后产生了多少个片段，就会插值计算出多少个varying变量，同时渲染管线就会调用多少次片段着色器
// textureCoordinate 最终顶点位置，上面顶点着色器的varying变量会传递到这里
// inputImageTexture 外部传入的图片纹理 即代表整张图片的数据
// texture2D(inputImageTexture, textureCoordinate) 调用函数 进行纹理贴图
const val NO_FILTER_FRAGMENT_SHADER = """
    varying highp vec2 textureCoordinate;
    
    uniform sampler2D inputImageTexture;
    
    void main()
    {
         gl_FragColor = texture2D(inputImageTexture, textureCoordinate);
    }
    """

abstract class IOpenGLShader @JvmOverloads constructor(
    private val vertexShader: String = NO_FILTER_VERTEX_SHADER,
    private val fragmentShader: String = NO_FILTER_FRAGMENT_SHADER
) {
    protected val mRunOnDraw = mutableListOf<Runnable>()
    protected val mGLProgramId: Int by lazy { loadProgram(vertexShader, fragmentShader) }
    protected val mGLAttrPosition: Int
    protected val mGLUniformTexture: Int
    protected val mGLAttrTextureCoordinate: Int

    init {
        // 编译链接着色器，创建着色器程序
        mGLAttrPosition = GLES30.glGetAttribLocation(mGLProgramId, "position") // 顶点着色器的顶点坐标
        mGLUniformTexture =
            GLES30.glGetUniformLocation(mGLProgramId, "inputImageTexture") // 传入的图片纹理
        mGLAttrTextureCoordinate =
            GLES30.glGetAttribLocation(mGLProgramId, "inputTextureCoordinate") // 顶点着色器的纹理坐标
    }

    open fun draw(textureId: Int, cubeBuffer: FloatBuffer, textureBuffer: FloatBuffer) {}

    fun onDraw(textureId: Int, cubeBuffer: FloatBuffer, textureBuffer: FloatBuffer) {
        GLES30.glUseProgram(mGLProgramId)
        // 顶点着色器的顶点坐标
        cubeBuffer.position(0)
        GLES30.glVertexAttribPointer(mGLAttrPosition, 2, GLES30.GL_FLOAT, false, 0, cubeBuffer)
        GLES30.glEnableVertexAttribArray(mGLAttrPosition)
        // 顶点着色器的纹理坐标
        textureBuffer.position(0)
        GLES30.glVertexAttribPointer(
            mGLAttrTextureCoordinate,
            2,
            GLES30.GL_FLOAT,
            false,
            0,
            textureBuffer
        )
        GLES30.glEnableVertexAttribArray(mGLAttrTextureCoordinate)
        // 传入的图片纹理
        if (textureId != NO_TEXTURE) {
            EglUtil.activateTexture(GLES30.GL_TEXTURE_2D, textureId, 0, mGLUniformTexture)
        }

        draw(textureId, cubeBuffer, textureBuffer)

        // 绘制顶点 ，方式有顶点法和索引法
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4) // 顶点法，按照传入渲染管线的顶点顺序及采用的绘制方式将顶点组成图元进行绘制

        GLES30.glDisableVertexAttribArray(mGLAttrPosition)
        GLES30.glDisableVertexAttribArray(mGLAttrTextureCoordinate)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
    }
}
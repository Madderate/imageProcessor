package com.devmcry.imageprocessor.ui.slideshow

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.devmcry.imageprocessor.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SlideshowFragment : Fragment() {

    private lateinit var slideshowViewModel: SlideshowViewModel
    private lateinit var surfaceView: GLSurfaceView
    private val render by lazy { PlayRender(mDrawer) }
    private val mDrawer by lazy { TriangleDrawer() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        slideshowViewModel =
            ViewModelProvider(this).get(SlideshowViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_slideshow, container, false)
        surfaceView = root.findViewById(R.id.surfaceView)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRender()
    }

    override fun onDestroyView() {
        mDrawer.release()
        super.onDestroyView()
    }

    private fun initRender() {
        surfaceView.run {
            setEGLContextClientVersion(2)
            setRenderer(render)
        }
    }
}

class PlayRender(private val mDrawer: IDrawer) : GLSurfaceView.Renderer {
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0f, 0f, 0f, 0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        mDrawer.setTextureID(1)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        mDrawer.draw()
    }

}

class TriangleDrawer : IDrawer {
    //顶点坐标
    private val mVertexCoors = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        0f, 1f
    )

    //纹理坐标
    private val mTextureCoors = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0.5f, 0f
    )

    //纹理ID
    private var mTextureId: Int = -1

    //OpenGL程序ID
    private var mProgram: Int = -1

    // 顶点坐标接收者
    private var mVertexPosHandler: Int = -1

    // 纹理坐标接收者
    private var mTexturePosHandler: Int = -1

    private lateinit var mVertexBuffer: FloatBuffer
    private lateinit var mTextureBuffer: FloatBuffer

    init {
        //【步骤1: 初始化顶点坐标】
        initPos()
    }

    private fun initPos() {
        val bb = ByteBuffer.allocateDirect(mVertexCoors.size * 4)
        bb.order(ByteOrder.nativeOrder())
        //将坐标数据转换为FloatBuffer，用以传入给OpenGL ES程序
        mVertexBuffer = bb.asFloatBuffer()
        mVertexBuffer.put(mVertexCoors)
        mVertexBuffer.position(0)

        val cc = ByteBuffer.allocateDirect(mTextureCoors.size * 4)
        cc.order(ByteOrder.nativeOrder())
        mTextureBuffer = cc.asFloatBuffer()
        mTextureBuffer.put(mTextureCoors)
        mTextureBuffer.position(0)
    }

    override fun setTextureID(id: Int) {
        mTextureId = id
    }

    override fun draw() {
        if (mTextureId != -1) {
            //【步骤2: 创建、编译并启动OpenGL着色器】
            createGLPrg()
            //【步骤3: 开始渲染绘制】
            doDraw()
        }
    }

    private fun createGLPrg() {
        if (mProgram == -1) {
            val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, getVertexShader())
            val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, getFragmentShader())

            //创建OpenGL ES程序，注意：需要在OpenGL渲染线程中创建，否则无法渲染
            mProgram = GLES30.glCreateProgram()
            //将顶点着色器加入到程序
            GLES30.glAttachShader(mProgram, vertexShader)
            //将片元着色器加入到程序中
            GLES30.glAttachShader(mProgram, fragmentShader)
            //连接到着色器程序
            GLES30.glLinkProgram(mProgram)

            mVertexPosHandler = GLES30.glGetAttribLocation(mProgram, "aPosition")
            mTexturePosHandler = GLES30.glGetAttribLocation(mProgram, "aCoordinate")
        }
        //使用OpenGL程序
        GLES30.glUseProgram(mProgram)
    }

    private fun doDraw() {
        //启用顶点的句柄
        GLES30.glEnableVertexAttribArray(mVertexPosHandler)
        GLES30.glEnableVertexAttribArray(mTexturePosHandler)
        //设置着色器参数
        GLES30.glVertexAttribPointer(mVertexPosHandler, 2, GLES30.GL_FLOAT, false, 0, mVertexBuffer)
        GLES30.glVertexAttribPointer(
            mTexturePosHandler,
            2,
            GLES30.GL_FLOAT,
            false,
            0,
            mTextureBuffer
        )
        //开始绘制
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
    }

    override fun release() {
        GLES30.glDisableVertexAttribArray(mVertexPosHandler)
        GLES30.glDisableVertexAttribArray(mTexturePosHandler)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        GLES30.glDeleteTextures(1, intArrayOf(mTextureId), 0)
        GLES30.glDeleteProgram(mProgram)
    }

    private fun getVertexShader(): String {
        return "attribute vec4 aPosition;" +
                "void main() {" +
                "  gl_Position = aPosition;" +
                "}"
    }

    private fun getFragmentShader(): String {
        return "precision mediump float;" +
                "void main() {" +
                "  gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);" +
                "}"
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        //根据type创建顶点着色器或者片元着色器
        val shader = GLES30.glCreateShader(type)
        //将资源加入到着色器中，并编译
        GLES30.glShaderSource(shader, shaderCode)
        GLES30.glCompileShader(shader)

        return shader
    }
}

interface IDrawer {
    fun draw()
    fun setTextureID(id: Int)
    fun release()
}
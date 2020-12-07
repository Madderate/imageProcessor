package com.devmcry.imageprocessor.ui.opengl

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import android.view.Surface
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 *  @author : DevMcryYu
 *  @date : 2020/12/4 16:22
 *  @description :
 */
internal inline fun <T> glRun(message: String = "", block: (() -> T)): T {
    return block().also {
        var error: Int = GLES30.glGetError()
        while (error != GLES30.GL_NO_ERROR) {
            error = GLES30.glGetError()
            Log.d("MOVIE_GL_ERROR", "$message: $error")
            throw RuntimeException("GL Error: $message")
        }
    }
}

class MovieRenderer: GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private var program = 0
    private var textureId = 0

    // Handles
    private var mvpMatrixHandle = 0
    private var stMatrixHandle = 0
    private var positionHandle = 0
    private var textureHandle = 0

    // Surface Texture
    private var updateSurface = false
    private lateinit var surfaceTexture: SurfaceTexture

    // Matrices
    private var mvpMatrix = FloatArray(16)
    private var stMatrix = FloatArray(16)

    // float buffer
    private val vertices: FloatBuffer = ByteBuffer.allocateDirect(VERTICES_DATA.size * FLOAT_SIZE_BYTES)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer().also {
            it.put(VERTICES_DATA).position(0)
        }

    var mediaPlayer: MediaPlayer? = null

    @Synchronized
    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        updateSurface = true
    }

    override fun onDrawFrame(gl: GL10?) {
        synchronized(this) {
            if (updateSurface) {
                surfaceTexture.updateTexImage()
                surfaceTexture.getTransformMatrix(stMatrix)
                updateSurface = false
            }
        }

        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT or GLES30.GL_COLOR_BUFFER_BIT)

        glRun("glUseProgram: $program") {
            GLES30.glUseProgram(program)
        }

        vertices.position(VERTICES_POS_OFFSET);

        glRun("glVertexAttribPointer: Stride bytes") {
            GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false,
                VERTICES_STRIDE_BYTES, vertices)
        }

        glRun("glEnableVertexAttribArray") {
            GLES30.glEnableVertexAttribArray(positionHandle)
        }

        vertices.position(VERTICES_UV_OFFSET)

        glRun("glVertexAttribPointer: texture handle") {
            GLES30.glVertexAttribPointer(textureHandle, 3, GLES30.GL_FLOAT, false,
                VERTICES_STRIDE_BYTES, vertices)
        }

        glRun("glEnableVertexAttribArray") {
            GLES30.glEnableVertexAttribArray(textureHandle)
        }

        Matrix.setIdentityM(mvpMatrix, 0)

        glRun("glUniformMatrix4fv: mvpMatrix") {
            GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        }

        glRun("glUniformMatrix4fv: stMatrix") {
            GLES30.glUniformMatrix4fv(stMatrixHandle, 1, false, stMatrix, 0)
        }

        glRun("glDrawArrays: GL_TRIANGLE_STRIP") {
            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        }

        GLES30.glFinish()
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        program = createProgram()
        positionHandle = "aPosition".attr()
        textureHandle = "aTextureCoord".attr()
        mvpMatrixHandle = "uMVPMatrix".uniform()
        stMatrixHandle = "uSTMatrix".uniform()
        createTexture()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    private fun createTexture() {
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        textureId = textures.first()
        glRun("glBindTexture textureId") { GLES30.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureId) }

        GLES30.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST)
        GLES30.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)

        surfaceTexture = SurfaceTexture(textureId)
        surfaceTexture.setOnFrameAvailableListener(this)

        val surface = Surface(surfaceTexture)
        mediaPlayer?.setSurface(surface)
        surface.release()

        try {
            mediaPlayer?.prepare()
        } catch (error: IOException) {
            Log.e("MovieRenderer", "media player prepare failed");
            throw error
        }

        synchronized(this) {
            updateSurface = false
        }

        mediaPlayer?.start()
    }



    private fun String.attr(): Int {
        return glRun("Get attribute location: $this") {
            GLES30.glGetAttribLocation(program, this).also {
                if (it == -1) fail("Error Attribute: $this not found!")
            }
        }
    }

    private fun String.uniform(): Int {
        return glRun("Get uniform location: $this") {
            GLES30.glGetUniformLocation(program, this).also {
                if (it == -1) fail("Error Uniform: $this not found!")
            }
        }
    }

    companion object {
        private const val GL_TEXTURE_EXTERNAL_OES = 0x8D65

        private const val FLOAT_SIZE_BYTES = 4
        private const val VERTICES_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES
        private const val VERTICES_POS_OFFSET = 0
        private const val VERTICES_UV_OFFSET = 3

        private val VERTICES_DATA = floatArrayOf(
            -1.0f, -1.0f, 0f, 0.0f, 0.0f,
            1.0f, -1.0f, 0f, 1.0f, 0.0f,
            -1.0f,  1.0f, 0f, 0.0f, 1.0f,
            1.0f,  1.0f, 0f, 1.0f, 1.0f
        )

        private const val VERTEX_SHADER = """
            uniform mat4 uMVPMatrix;
            uniform mat4 uSTMatrix;
            attribute vec4 aPosition;
            attribute vec4 aTextureCoord;
            varying vec2 vTextureCoord;
            void main() {
                gl_Position = uMVPMatrix * aPosition;
                vTextureCoord = (uSTMatrix * aTextureCoord).xy;
            }
        """

        private const val FRAGMENT_SHADER = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            varying vec2 vTextureCoord;
            uniform samplerExternalOES sTexture;
            void main() {
              gl_FragColor = texture2D(sTexture, vTextureCoord);
            }
        """

        private fun createShader(type: Int, source: String): Int {
            val shader = GLES30.glCreateShader(type)
            if (shader == 0) throw RuntimeException("Cannot create shader $type\n$source")
            GLES30.glShaderSource(shader, source)
            GLES30.glCompileShader(shader)

            val args = IntArray(1)
            GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, args, 0)
            if (args.first() == 0) {
                Log.e("MOVIE_SHADER", "Failed to compile shader source")
                Log.e("MOVIE_SHADER", GLES30.glGetShaderInfoLog(shader))
                GLES30.glDeleteShader(shader)
                throw RuntimeException("Could not compile shader $source\n$type")
            }

            return shader
        }

        private fun createProgram(vertexShaderSource: String = VERTEX_SHADER,
                                  fragmentShaderSource: String = FRAGMENT_SHADER): Int {

            val vertexShader = createShader(GLES30.GL_VERTEX_SHADER, vertexShaderSource)
            val fragmentShader = createShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderSource)

            val program = GLES30.glCreateProgram()
            if (program == 0) throw RuntimeException("Cannot create program")

            glRun("Attach vertex shader to program") {
                GLES30.glAttachShader(program, vertexShader)
            }

            glRun("Attach fragment shader to program") {
                GLES30.glAttachShader(program, fragmentShader)
            }

            GLES30.glLinkProgram(program)
            val args = IntArray(1)
            GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, args, 0)

            if (args.first() != GLES30.GL_TRUE) {
                val info = GLES30.glGetProgramInfoLog(program)
                GLES30.glDeleteProgram(program)
                throw RuntimeException("Cannot link program $program, Info: $info")
            }

            return program
        }


        private fun fail(message: String): Nothing {
            throw RuntimeException(message)
        }

    }
}
package com.devmcry.imageprocessor.ui.opengl.util

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLException
import android.opengl.GLUtils
import android.util.Log
import com.devmcry.imageprocessor.BuildConfig
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Created by sudamasayuki on 2017/05/16.
 */
object EglUtil {
    const val NO_TEXTURE = -1
    private const val FLOAT_SIZE_BYTES = 4

    fun loadShader(strSource: String?, iType: Int): Int {
        val compiled = IntArray(1)
        val iShader = GLES30.glCreateShader(iType)
        GLES30.glShaderSource(iShader, strSource)
        GLES30.glCompileShader(iShader)
        GLES30.glGetShaderiv(iShader, GLES30.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            Log.d("Load Shader Failed", "Compilation " + GLES30.glGetShaderInfoLog(iShader))
            return 0
        }
        return iShader
    }

    @Throws(GLException::class)
    fun createProgram(vertexShader: Int, pixelShader: Int): Int {
        val program = GLES20.glCreateProgram()
        if (program == 0) {
            throw RuntimeException("Could not create program")
        }
        GLES30.glAttachShader(program, vertexShader)
        GLES30.glAttachShader(program, pixelShader)
        GLES30.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES20.GL_TRUE) {
            GLES30.glDeleteProgram(program)
            throw RuntimeException("Could not link program")
        }
        return program
    }

    fun checkEglError(operation: String) {
        if (!BuildConfig.DEBUG) return
        var error: Int
        while (GLES30.glGetError().also { error = it } != GLES30.GL_NO_ERROR) {
            throw RuntimeException("$operation: glError $error")
        }
    }

    fun setupSampler(target: Int, mag: Int, min: Int) {
        GLES30.glTexParameterf(target, GLES20.GL_TEXTURE_MAG_FILTER, mag.toFloat())
        GLES30.glTexParameterf(target, GLES20.GL_TEXTURE_MIN_FILTER, min.toFloat())
        GLES30.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(target, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
    }

    fun createBuffer(data: FloatArray): Int {
        return createBuffer(toFloatBuffer(data))
    }

    fun createBuffer(data: FloatBuffer): Int {
        val buffers = IntArray(1)
        GLES30.glGenBuffers(buffers.size, buffers, 0)
        updateBufferData(buffers[0], data)
        return buffers[0]
    }

    fun toFloatBuffer(data: FloatArray): FloatBuffer {
        val buffer = ByteBuffer
                .allocateDirect(data.size * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        buffer.put(data).position(0)
        return buffer
    }

    fun updateBufferData(bufferName: Int, data: FloatBuffer) {
        GLES30.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferName)
        GLES30.glBufferData(GLES20.GL_ARRAY_BUFFER, data.capacity() * FLOAT_SIZE_BYTES, data, GLES20.GL_STATIC_DRAW)
        GLES30.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    fun loadTexture(img: Bitmap, usedTexId: Int, recycle: Boolean): Int {
        val textures = IntArray(1)
        if (usedTexId == NO_TEXTURE) {
            GLES30.glGenTextures(1, textures, 0)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0])
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat())
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR.toFloat())
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat())
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D,
                    GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat())
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, img, 0)
        } else {
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, usedTexId)
            GLUtils.texSubImage2D(GLES30.GL_TEXTURE_2D, 0, 0, 0, img)
            textures[0] = usedTexId
        }
        if (recycle) {
            img.recycle()
        }
        return textures[0]
    }


    fun activateTexture(type: Int, textureId: Int, index: Int, textureHandler: Int) {
        // 激活指定纹理单元
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + index)
        // 绑定纹理ID到纹理单元
        GLES30.glBindTexture(type, textureId)
        // 将激活的纹理单元传递到着色器里面
        GLES30.glUniform1i(textureHandler, 0)
        // 配置边缘过渡参数
        GLES30.glTexParameterf(type, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR.toFloat())
        GLES30.glTexParameterf(type, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat())
        GLES30.glTexParameteri(type, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(type, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
    }
}
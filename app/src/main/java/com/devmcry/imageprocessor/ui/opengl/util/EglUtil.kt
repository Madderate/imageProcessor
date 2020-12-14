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
import kotlin.jvm.Throws

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
        val program = GLES30.glCreateProgram()
        if (program == 0) {
            throw RuntimeException("Could not create program")
        }
        GLES30.glAttachShader(program, vertexShader)
        GLES30.glAttachShader(program, pixelShader)
        GLES30.glLinkProgram(program)
        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] != GLES30.GL_TRUE) {
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
        GLES30.glTexParameterf(target, GLES30.GL_TEXTURE_MAG_FILTER, mag.toFloat())
        GLES30.glTexParameterf(target, GLES30.GL_TEXTURE_MIN_FILTER, min.toFloat())
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
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
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, bufferName)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, data.capacity() * FLOAT_SIZE_BYTES, data, GLES30.GL_STATIC_DRAW)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0)
    }


    fun loadTexture(img: Bitmap, usedTextureId: Int, textureType: Int=GLES30.GL_TEXTURE_2D, recycle: Boolean=false): Int {
        val textures = intArrayOf(NO_TEXTURE)
        if (usedTextureId == NO_TEXTURE) {
            GLES30.glGenTextures(1, textures, 0)
            GLES30.glBindTexture(textureType, textures[0])
            GLES30.glTexParameterf(
                    textureType,
                    GLES30.GL_TEXTURE_MAG_FILTER,
                    GLES30.GL_LINEAR.toFloat()
            )
            GLES30.glTexParameterf(
                    textureType,
                    GLES30.GL_TEXTURE_MIN_FILTER,
                    GLES30.GL_LINEAR.toFloat()
            )
            //纹理也有坐标系，称UV坐标，或者ST坐标
            GLES30.glTexParameterf(
                    textureType,
                    GLES30.GL_TEXTURE_WRAP_S,
                    GLES30.GL_REPEAT.toFloat()
            ) // S轴的拉伸方式为重复，决定采样值的坐标超出图片范围时的采样方式
            GLES30.glTexParameterf(
                    textureType,
                    GLES30.GL_TEXTURE_WRAP_T,
                    GLES30.GL_REPEAT.toFloat()
            ) // T轴的拉伸方式为重复

            GLUtils.texImage2D(textureType, 0, img, 0)
        } else {
            GLES30.glBindTexture(textureType, usedTextureId)
            GLUtils.texSubImage2D(textureType, 0, 0, 0, img)
            textures[0] = usedTextureId
        }
        if (recycle) {
            img.recycle()
        }
        return textures[0]
    }

    // 激活指定纹理，draw 的时候调用
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


    fun createTexture(): Int {
        var args = IntArray(1)
        // 生成纹理
        GLES30.glGenTextures(args.size, args, 0)
        return args[0]
    }

    fun createFBOTexture(args: IntArray, width: Int, height: Int): Int {
        GLES30.glGenTextures(args.size, args, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, args[0])
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height,
                0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null)
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST.toFloat())
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR.toFloat())
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE.toFloat())
        GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE.toFloat())
        GLES30.glBindTexture(GLES20.GL_TEXTURE_2D,0)
        return args[0]
    }

    // 解绑 frameBuffer
    fun unbindFrameBuffer() {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }

    fun createFrameBuffer(args: IntArray): Int {
        GLES30.glGenFramebuffers(args.size, args, 0)
        return args[0]
    }

    fun createRenderBuffer(args: IntArray): Int {
        GLES30.glGenRenderbuffers(args.size, args, 0)
        return args[0]
    }


    // frameBuffer texture renderBuffer bind together
    fun bindFBO(fb: Int, textureId: Int) {
        bindFBO(fb, textureId, GLES30.GL_NONE)
    }
    fun bindFBO(fb: Int, textureId: Int, rb: Int, width: Int=0, height: Int=0) {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fb)
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0,
                GLES30.GL_TEXTURE_2D, textureId, 0)

        if (rb != GLES30.GL_NONE) {
            GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, rb)
            GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT16, width, height)
            GLES30.glFramebufferRenderbuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_RENDERBUFFER, rb)
        }
    }

    fun unbindFBO() {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_NONE)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, GLES30.GL_NONE)
    }

    fun deleteFBO(frame: IntArray, textures:IntArray) {
//        GLES30.glDeleteRenderbuffers(1, fRender, 0)
        // 删除Frame Buffer
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, GLES30.GL_NONE)
        GLES30.glDeleteFramebuffers(1, frame, 0)
        // 删除纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
        GLES30.glDeleteTextures(1, textures, 0)

        // 删除Render Buffer
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, GLES30.GL_NONE)
    }
}
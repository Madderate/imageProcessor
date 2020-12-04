package com.devmcry.imageprocessor.ui.opengl

import android.graphics.Bitmap
import android.hardware.Camera
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLUtils
import android.util.Log
import java.nio.IntBuffer


/**
 *  @author : DevMcryYu
 *  @date : 2020/12/3 18:04
 *  @description :
 */

const val NO_TEXTURE = -1

fun Bitmap.loadTexture(usedTextureId: Int, recycle: Boolean = false): Int {
    val textures = intArrayOf(NO_TEXTURE)
    if (usedTextureId != NO_TEXTURE) {
        GLES30.glGenTextures(1, textures, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0])
        GLES30.glTexParameterf(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MAG_FILTER,
            GLES30.GL_LINEAR.toFloat()
        )
        GLES30.glTexParameterf(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_LINEAR.toFloat()
        )
        //纹理也有坐标系，称UV坐标，或者ST坐标
        GLES30.glTexParameterf(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_WRAP_S,
            GLES30.GL_REPEAT.toFloat()
        ) // S轴的拉伸方式为重复，决定采样值的坐标超出图片范围时的采样方式
        GLES30.glTexParameterf(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_WRAP_T,
            GLES30.GL_REPEAT.toFloat()
        ) // T轴的拉伸方式为重复

        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, this, 0)
    } else {
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, usedTextureId)
        GLUtils.texSubImage2D(GLES30.GL_TEXTURE_2D, 0, 0, 0, this)
    }
    if (recycle) {
        recycle()
    }
    return textures[0]
}

fun IntBuffer.loadTexture(size: Camera.Size, usedTextureId: Int) {

}

/**
 * 加载着色器
 * @param iType
 * @return
 */
fun String.loadShader(iType: Int): Int {
    val compiled = IntArray(1)
    val iShader = GLES20.glCreateShader(iType)
    GLES20.glShaderSource(iShader, this)
    GLES20.glCompileShader(iShader)
    GLES20.glGetShaderiv(iShader, GLES20.GL_COMPILE_STATUS, compiled, 0)
    if (compiled[0] == 0) {
        Log.d("Load Shader Failed", "Compilation${GLES20.glGetShaderInfoLog(iShader)}")
        return 0
    }
    return iShader
}

/**
 * 加载着色器程序
 * @param strVSource
 * @param strFSource
 * @return
 */
fun loadProgram(strVSource: String, strFSource: String): Int {
    val iVShader: Int = strVSource.loadShader(GLES30.GL_VERTEX_SHADER) // 顶点着色器
    if (iVShader == 0) {
        Log.d("Load Program", "Vertex Shader Failed")
        return 0
    }
    val iFShader: Int = strFSource.loadShader(GLES30.GL_FRAGMENT_SHADER) // 片元着色器
    if (iFShader == 0) {
        Log.d("Load Program", "Fragment Shader Failed")
        return 0
    }
    val iProgramId: Int = GLES30.glCreateProgram()
    GLES30.glAttachShader(iProgramId, iVShader)
    GLES30.glAttachShader(iProgramId, iFShader)
    GLES30.glLinkProgram(iProgramId)
    // 获取program的链接情况
    val link = IntArray(1)
    GLES30.glGetProgramiv(iProgramId, GLES30.GL_LINK_STATUS, link, 0)
    if (link[0] <= 0) {
        Log.d("Load Program", "Linking Failed")
        return 0
    }
    GLES30.glDeleteShader(iVShader)
    GLES30.glDeleteShader(iFShader)
    return iProgramId
}
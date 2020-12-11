package com.devmcry.imageprocessor.ui.opengl.filter

import android.opengl.GLES30
import com.devmcry.imageprocessor.ui.opengl.util.EglUtil.NO_TEXTURE


/**
 *  @author : DevMcryYu
 *  @date : 2020/12/9 18:50
 *  @description :
 */
class ContentFilter : GlFilter(DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER) {
    companion object {
       val CUBE_DATA = floatArrayOf(
                -1.0f, -1.0f, 0.0f, 0.0f, 1.0f,
                1.0f, -1.0f, 0.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, 0.0f, 0.0f, 0.0f,
                1.0f, 1.0f, 0.0f, 1.0f, 0.0f
        )

        const val FRAGMENT_SHADER = """
    varying highp vec2 vTextureCoord;
    uniform sampler2D sTexture;
    uniform sampler2D sTexture2;
    void main(){
         lowp vec4 coverTexture = texture2D(sTexture2, vTextureCoord);
         lowp vec4 contentTexture = texture2D(sTexture, vTextureCoord);
         gl_FragColor = mix(contentTexture, coverTexture, coverTexture.a);
    }
    """
    }

    var contentTextureId = NO_TEXTURE


    fun setup(cubeData: FloatArray) {
        verticeFragmentData = cubeData
        setup()
    }

    override fun onDraw() {
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, contentTextureId)
        GLES30.glUniform1i(getHandle("sTexture2"), 0)
    }
}
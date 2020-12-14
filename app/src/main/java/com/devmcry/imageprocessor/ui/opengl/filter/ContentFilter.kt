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
                uniform sampler2D sTextureContent;
                void main(){
                    lowp vec4 coverTexture = texture2D(sTexture, vTextureCoord);
                    lowp vec4 contentTexture = texture2D(sTextureContent, vec2(vTextureCoord.x,1.0-vTextureCoord.y));
                    gl_FragColor = mix(contentTexture, coverTexture, coverTexture.a);
                }
                """
    }

    var contentTextureId = NO_TEXTURE

    fun setup(cubeData: FloatArray, textureId: Int) {
//        verticeFragmentData = cubeData
        contentTextureId = textureId
        setup()
    }


    override fun onDraw() {
        val idx = 1
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + idx)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, contentTextureId)
        GLES30.glUniform1i(getHandle("sTextureContent"), idx)
    }
}
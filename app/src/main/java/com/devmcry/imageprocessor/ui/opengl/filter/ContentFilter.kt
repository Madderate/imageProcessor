package com.devmcry.imageprocessor.ui.opengl.filter

import android.graphics.Bitmap
import android.opengl.GLES30
import com.devmcry.imageprocessor.ui.opengl.util.EglUtil.NO_TEXTURE
import com.devmcry.imageprocessor.ui.opengl.util.EglUtil.loadTexture


/**
 *  @author : DevMcryYu
 *  @date : 2020/12/9 18:50
 *  @description :
 */
class ContentFilter(private val contentBitmap: Bitmap): GlFilter(DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER), FilterInterface {

    companion object {

        const val FRAGMENT_SHADER = """
                varying highp vec2 vTextureCoord;
                uniform sampler2D sTexture;
                uniform sampler2D sTextureContent;
                void main(){
                    lowp vec4 coverTexture = texture2D(sTexture, vTextureCoord);
                    lowp vec4 contentTexture = texture2D(sTextureContent, vec2(vTextureCoord.x, 1.0-vTextureCoord.y));
                    gl_FragColor = mix(contentTexture, coverTexture, coverTexture.a);
                }
                """
    }

    private var contentTextureId = NO_TEXTURE


    override fun onDraw() {
        val idx = 1
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + idx)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, contentTextureId)
        GLES30.glUniform1i(getHandle("sTextureContent"), idx)
    }

    override fun setupAfterSizeChange(width: Int, height: Int) {
        super.setup()
        val textureId = loadTexture(contentBitmap, NO_TEXTURE, GLES30.GL_TEXTURE_2D, false)
        contentTextureId = textureId
    }
}
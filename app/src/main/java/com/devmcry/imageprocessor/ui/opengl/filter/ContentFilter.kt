package com.devmcry.imageprocessor.ui.opengl

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLES30
import com.devmcry.imageprocessor.ui.opengl.filter.GlFilter
import com.devmcry.imageprocessor.ui.opengl.util.EFramebufferObject

/**
 *  @author : DevMcryYu
 *  @date : 2020/12/9 18:50
 *  @description :
 */
class ContentFilter : GlFilter(DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER) {
    companion object {
        const val FRAGMENT_SHADER = """
    varying highp vec2 vTextureCoord;
    uniform sampler2D sTexture;
    void main(){
         gl_FragColor = texture2D(sTexture, vTextureCoord);
    }
    """
    }

    var contentTextureId = NO_TEXTURE

}
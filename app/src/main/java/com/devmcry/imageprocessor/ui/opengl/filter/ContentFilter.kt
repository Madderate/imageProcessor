package com.devmcry.imageprocessor.ui.opengl

import com.devmcry.imageprocessor.ui.opengl.filter.GlFilter

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
    uniform sampler2D sTexture2;
    void main(){
         gl_FragColor = texture2D(sTexture, -vTextureCoord);
    }
    """
    }

    var contentTextureId = NO_TEXTURE


    private val CUBE_DATA = floatArrayOf(
        -1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
        1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
        -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
        1.0f, -1.0f, 0.0f, 1.0f, 0.0f
    )


    override fun setup(): Unit {
        verticeFragmentData = CUBE_DATA
        super.setup()
    }
}
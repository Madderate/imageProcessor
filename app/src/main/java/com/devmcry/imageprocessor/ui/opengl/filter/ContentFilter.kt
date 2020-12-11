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
    uniform sampler2D sTexture1;
    uniform sampler2D sTexture2;
    void main(){
         gl_FragColor = texture2D(sTexture1, -vTextureCoord);
    }
    """
    }

    var contentTextureId = NO_TEXTURE

}
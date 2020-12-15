package com.devmcry.imageprocessor.ui.opengl.filter

import com.devmcry.imageprocessor.ui.opengl.util.EFramebufferObject

class AlphaFrameFilter : GlFilter(VERTEX_SHADER, FRAGMENT_SHADER), FilterInterface {
    companion object {
        const val VERTEX_SHADER = """attribute vec4 aPosition;
                attribute vec4 aTextureCoord;
                varying highp vec2 vTextureCoord;
                varying highp vec2 vTextureCoord2;
                void main() {
                    gl_Position = aPosition;
                    vTextureCoord = vec2(aTextureCoord.x*0.5, aTextureCoord.y);
                    vTextureCoord2 = vec2(aTextureCoord.x*0.5+0.5, aTextureCoord.y);
                }"""
        const val FRAGMENT_SHADER = """precision mediump float;
                varying highp vec2 vTextureCoord;
                varying highp vec2 vTextureCoord2;
                uniform lowp sampler2D sTexture;
                void main() {
                    vec4 color1 = texture2D(sTexture, vTextureCoord);
                    vec4 color2 = texture2D(sTexture, vTextureCoord2);
                    gl_FragColor = vec4(color1.rgb, color2.r);
                }"""
    }


    override fun setupAfterSizeChange(width: Int, height: Int) {
        super.setup()
        // buffer setup when size is changed
        mBufferObject = EFramebufferObject()
        mBufferObject!!.setup(width, height)
    }
}
package com.devmcry.imageprocessor.ui.opengl

import java.nio.FloatBuffer

/**
 *  @author : DevMcryYu
 *  @date : 2020/12/4 14:27
 *  @description :
 */
private val VERTEX_SHADER = """
    attribute vec4 aPosition;
    attribute vec4 aTextureCoord;
    varying highp vec2 vTextureCoord;
    varying highp vec2 vTextureCoord2;
    void main() {
    gl_Position = aPosition;
    vTextureCoord = vec2(aTextureCoord.x*0.5, aTextureCoord.y);
    vTextureCoord2 = vec2(aTextureCoord.x*0.5+0.5, aTextureCoord.y);
    }
    
    """.trimIndent()
private const val FRAGMENT_SHADER = "precision mediump float;\n" +
        "varying highp vec2 vTextureCoord;\n" +
        "varying highp vec2 vTextureCoord2;\n" +
        "uniform lowp sampler2D sTexture;\n" +
        "void main() {\n" +
        "vec4 color1 = texture2D(sTexture, vTextureCoord);\n" +
        "vec4 color2 = texture2D(sTexture, vTextureCoord2);\n" +
        "gl_FragColor = vec4(color1.rgb, color2.r);\n" +
        "}\n"

class OpenGLDynamicFilterShader : IOpenGLShader(VERTEX_SHADER, FRAGMENT_SHADER)
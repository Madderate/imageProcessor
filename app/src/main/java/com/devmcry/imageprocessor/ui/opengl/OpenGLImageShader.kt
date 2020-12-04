package com.devmcry.imageprocessor.ui.opengl

import android.opengl.GLES30
import java.nio.FloatBuffer

/**
 *  @author : DevMcryYu
 *  @date : 2020/12/3 18:47
 *  @description :
 */
class OpenGLImageShader : IOpenGLShader() {
    override fun onDraw(textureId: Int, cubeBuffer: FloatBuffer, textureBuffer: FloatBuffer) {
        GLES30.glUseProgram(mGLProgramId);
        // 顶点着色器的顶点坐标
        cubeBuffer.position(0);
        GLES30.glVertexAttribPointer(mGLAttrPosition, 2, GLES30.GL_FLOAT, false, 0, cubeBuffer);
        GLES30.glEnableVertexAttribArray(mGLAttrPosition);
        // 顶点着色器的纹理坐标
        textureBuffer.position(0);
        GLES30.glVertexAttribPointer(mGLAttrTextureCoordinate, 2, GLES30.GL_FLOAT, false, 0, textureBuffer);
        GLES30.glEnableVertexAttribArray(mGLAttrTextureCoordinate);
        // 传入的图片纹理
        if (textureId != NO_TEXTURE) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId);
            GLES30.glUniform1i(mGLUniformTexture, 0);
        }

        // 绘制顶点 ，方式有顶点法和索引法
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4); // 顶点法，按照传入渲染管线的顶点顺序及采用的绘制方式将顶点组成图元进行绘制

        GLES30.glDisableVertexAttribArray(mGLAttrPosition);
        GLES30.glDisableVertexAttribArray(mGLAttrTextureCoordinate);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
    }
}